package com.tarasfedyk.lunchplaces.biz

import androidx.annotation.MainThread
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.libraries.places.api.net.PlacesStatusCodes
import com.tarasfedyk.lunchplaces.biz.data.ErrorType
import com.tarasfedyk.lunchplaces.biz.data.GeoState
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.biz.util.ReplaceableLauncher
import com.tarasfedyk.lunchplaces.biz.data.LocationPermissionLevel
import com.tarasfedyk.lunchplaces.biz.data.LocationSnapshot
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.MediaLimits
import com.tarasfedyk.lunchplaces.biz.data.SearchSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.lang.RuntimeException
import javax.inject.Inject

@HiltViewModel
class GeoVM @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val locationController: LocationController,
    private val mediaLimits: MediaLimits,
    private val settingsRepo: SettingsRepo,
    private val placesRepo: PlacesRepo
) : ViewModel() {

    private val _locationPermissionLevelFlow: MutableStateFlow<LocationPermissionLevel?> =
        MutableStateFlow(value = null)
    val locationPermissionLevelFlow: StateFlow<LocationPermissionLevel?> =
        _locationPermissionLevelFlow.asStateFlow()

    val searchSettingsFlow: StateFlow<SearchSettings?> = settingsRepo.searchSettingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    private val currentLocationLauncher: ReplaceableLauncher = ReplaceableLauncher(viewModelScope)
    private val lunchPlacesLauncher: ReplaceableLauncher = ReplaceableLauncher(viewModelScope)
    private var currentLocationStatusId: Int = 0
    private var lunchPlacesStatusId: Int = 0
    val geoStateFlow: StateFlow<GeoState> = savedStateHandle.getStateFlow(
        key = GEO_STATE_KEY,
        initialValue = GeoState()
    )

    init {
        refreshLunchPlaces(RefreshFilter.PENDING_DATA)
    }

    fun setLocationPermissionLevel(locationPermissionLevel: LocationPermissionLevel) {
        _locationPermissionLevelFlow.value = locationPermissionLevel
        onLocationPermissionLevelChanged()
    }

    private fun onLocationPermissionLevelChanged() {
        determineCurrentLocation()
        refreshLunchPlaces(RefreshFilter.FAILED_DATA)
    }

    fun setSearchSettings(searchSettings: SearchSettings) {
        viewModelScope.launch {
            settingsRepo.setSearchSettings(searchSettings)
            onSearchSettingsChanged()
        }
    }

    private fun onSearchSettingsChanged() {
        refreshLunchPlaces(RefreshFilter.NONE)
    }

    fun determineCurrentLocation() {
        currentLocationLauncher.launch {
            determineCurrentLocationImpl()
        }
    }

    private suspend fun determineCurrentLocationImpl() {
        updateGeoState { it.copy(currentLocationStatus = currentLocationStatusPending()) }

        try {
            val currentLocation = locationController.determineCurrentLocation()

            val currentLocationStatus = if (currentLocation != null) {
                currentLocationStatusSuccess(currentLocation)
            } else {
                currentLocationStatusFailure(ErrorType.CURRENT_LOCATION)
            }
            updateGeoState { it.copy(currentLocationStatus = currentLocationStatus) }
        } catch (e: Exception) {
            if (e !is RuntimeException || e is SecurityException) {
                val errorType = when {
                    e is ApiException && e.statusCode == CommonStatusCodes.API_NOT_CONNECTED -> {
                        ErrorType.LOCATION_SERVICES
                    }
                    e is SecurityException -> ErrorType.LOCATION_PERMISSION
                    else -> ErrorType.CURRENT_LOCATION
                }
                updateGeoState {
                    it.copy(currentLocationStatus = currentLocationStatusFailure(errorType))
                }
            } else {
                throw e
            }
        }
    }

    fun searchForLunchPlaces(query: String) {
        lunchPlacesLauncher.launch {
            searchForLunchPlacesImpl(query)
        }
    }

    private fun refreshLunchPlaces(refreshFilter: RefreshFilter) {
        lunchPlacesLauncher.launch {
            val lunchPlacesStatus = geoStateFlow.first().lunchPlacesStatus ?: return@launch

            val shouldPerformRefresh = when (refreshFilter) {
                RefreshFilter.PENDING_DATA -> lunchPlacesStatus is Status.Pending
                RefreshFilter.FAILED_DATA -> lunchPlacesStatus is Status.Failure
                RefreshFilter.NONE -> true
            }

            if (shouldPerformRefresh) {
                searchForLunchPlacesImpl(lunchPlacesStatus.arg.query)
            }
        }
    }

    fun discardLunchPlaces() {
        lunchPlacesLauncher.launch {
            updateGeoState { it.copy(lunchPlacesStatus = null) }
        }
    }

    private suspend fun searchForLunchPlacesImpl(query: String) {
        val searchSettings = searchSettingsFlow.filterNotNull().first()
        var searchFilter = SearchFilter(query, mediaLimits, searchSettings)
        updateGeoState {
            it.copy(lunchPlacesStatus = lunchPlacesStatusPending(searchFilter))
        }

        determineCurrentLocation()
        yield()
        val currentLocationTerminalStatus = geoStateFlow
            .first { it.currentLocationStatus is Status.Terminal }
            .currentLocationStatus

        try {
            if (currentLocationTerminalStatus is Status.Success) {
                val currentPoint = currentLocationTerminalStatus.result.point
                searchFilter = searchFilter.copy(originPoint = currentPoint)
                val lunchPlaces = placesRepo.searchForLunchPlaces(searchFilter)

                updateGeoState {
                    it.copy(lunchPlacesStatus = lunchPlacesStatusSuccess(searchFilter, lunchPlaces))
                }
            } else {
                val errorType = (currentLocationTerminalStatus as Status.Failure).errorType
                updateGeoState {
                    it.copy(lunchPlacesStatus = lunchPlacesStatusFailure(searchFilter, errorType))
                }
            }
        } catch (e: Exception) {
            if (e !is RuntimeException) {
                val errorType = when {
                    e is ApiException && e.statusCode == PlacesStatusCodes.INVALID_REQUEST -> {
                        ErrorType.INVALID_CONFIG
                    }
                    e is ApiException && e.statusCode == CommonStatusCodes.NETWORK_ERROR -> {
                        ErrorType.INTERNET_CONNECTION
                    }
                    e is ApiException && e.statusCode == PlacesStatusCodes.OVER_QUERY_LIMIT -> {
                        ErrorType.QUERY_LIMITS
                    }
                    else -> ErrorType.UNKNOWN
                }
                updateGeoState {
                    it.copy(lunchPlacesStatus = lunchPlacesStatusFailure(searchFilter, errorType))
                }
            } else {
                throw e
            }
        }
    }

    @MainThread
    private suspend fun updateGeoState(function: (GeoState) -> GeoState) {
        val geoState = geoStateFlow.first()
        val newGeoState = function(geoState)
        savedStateHandle[GEO_STATE_KEY] = newGeoState
    }

    private fun currentLocationStatusPending(): Status<Unit, Nothing> {
        ++currentLocationStatusId
        return Status.Pending(id = currentLocationStatusId, arg = Unit)
    }

    private fun currentLocationStatusSuccess(
        currentLocation: LocationSnapshot
    ): Status<Unit, LocationSnapshot> {
        ++currentLocationStatusId
        return Status.Success(id = currentLocationStatusId, arg = Unit, result = currentLocation)
    }

    private fun currentLocationStatusFailure(errorType: ErrorType): Status<Unit, Nothing> {
        ++currentLocationStatusId
        return Status.Failure(id = currentLocationStatusId, arg = Unit, errorType = errorType)
    }

    private fun lunchPlacesStatusPending(
        searchFilter: SearchFilter
    ): Status<SearchFilter, Nothing> {
        ++lunchPlacesStatusId
        return Status.Pending(id = lunchPlacesStatusId, arg = searchFilter)
    }

    private fun lunchPlacesStatusSuccess(
        searchFilter: SearchFilter,
        lunchPlaces: List<LunchPlace>
    ): Status<SearchFilter, List<LunchPlace>> {
        ++lunchPlacesStatusId
        return Status.Success(id = lunchPlacesStatusId, arg = searchFilter, result = lunchPlaces)
    }

    private fun lunchPlacesStatusFailure(
        searchFilter: SearchFilter,
        errorType: ErrorType
    ): Status<SearchFilter, Nothing> {
        ++lunchPlacesStatusId
        return Status.Failure(id = lunchPlacesStatusId, arg = searchFilter, errorType = errorType)
    }

    private enum class RefreshFilter {
        NONE,
        PENDING_DATA,
        FAILED_DATA,
    }

    companion object {
        private const val GEO_STATE_KEY: String = "geo_state"
    }
}