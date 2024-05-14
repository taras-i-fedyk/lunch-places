package com.tarasfedyk.lunchplaces.biz

import androidx.annotation.MainThread
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.tarasfedyk.lunchplaces.biz.data.ErrorType
import com.tarasfedyk.lunchplaces.biz.data.GeoState
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.biz.util.ReplaceableLauncher
import com.tarasfedyk.lunchplaces.biz.data.LocationPermissionsLevel
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
    private val mediaLimits: MediaLimits,
    private val locationController: LocationController,
    private val settingsRepo: SettingsRepo,
    private val geoRepo: GeoRepo
) : ViewModel() {

    private val _locationPermissionsLevelFlow: MutableStateFlow<LocationPermissionsLevel?> =
        MutableStateFlow(value = null)
    val locationPermissionsLevelFlow: StateFlow<LocationPermissionsLevel?> =
        _locationPermissionsLevelFlow.asStateFlow()

    val searchSettingsFlow: StateFlow<SearchSettings?> = settingsRepo.searchSettingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    private val currentLocationLauncher: ReplaceableLauncher = ReplaceableLauncher(viewModelScope)
    private val lunchPlacesLauncher: ReplaceableLauncher = ReplaceableLauncher(viewModelScope)
    val geoStateFlow: StateFlow<GeoState> = savedStateHandle.getStateFlow(
        key = GEO_STATE_KEY,
        initialValue = GeoState()
    )

    init {
        refreshLunchPlaces(RefreshApplicability.PENDING_ITEMS)
    }

    fun setLocationPermissionsLevel(locationPermissionsLevel: LocationPermissionsLevel) {
        _locationPermissionsLevelFlow.value = locationPermissionsLevel
        onLocationPermissionsLevelChanged()
    }

    private fun onLocationPermissionsLevelChanged() {
        determineCurrentLocation()
        refreshLunchPlaces(RefreshApplicability.FAILED_ITEMS)
    }

    fun setSearchSettings(searchSettings: SearchSettings) {
        viewModelScope.launch {
            settingsRepo.setSearchSettings(searchSettings)
            onSearchSettingsChanged()
        }
    }

    private fun onSearchSettingsChanged() {
        refreshLunchPlaces(RefreshApplicability.ANY_ITEMS)
    }

    fun determineCurrentLocation() {
        currentLocationLauncher.launch {
            determineCurrentLocationImpl()
        }
    }

    private suspend fun determineCurrentLocationImpl() {
        updateGeoState { it.copy(currentLocationStatus = Status.Pending(Unit)) }

        try {
            val currentLocation = locationController.determineCurrentLocation()

            val currentLocationStatus = if (currentLocation != null) {
                Status.Success(Unit, currentLocation)
            } else {
                Status.Failure(Unit, ErrorType.CURRENT_LOCATION)
            }
            updateGeoState { it.copy(currentLocationStatus = currentLocationStatus) }
        } catch (e: Exception) {
            if (e !is RuntimeException || e is SecurityException) {
                val errorType = if (e is SecurityException) {
                    ErrorType.LOCATION_PERMISSIONS
                } else {
                    ErrorType.CURRENT_LOCATION
                }
                updateGeoState { it.copy(currentLocationStatus = Status.Failure(Unit, errorType)) }
            } else {
                throw e
            }
        }
    }

    fun searchLunchPlaces(query: String) {
        lunchPlacesLauncher.launch {
            searchLunchPlacesImpl(query)
        }
    }

    private fun refreshLunchPlaces(refreshApplicability: RefreshApplicability) {
        lunchPlacesLauncher.launch {
            val lunchPlacesStatus = geoStateFlow.first().lunchPlacesStatus ?: return@launch

            val shouldPerformRefresh = when (refreshApplicability) {
                RefreshApplicability.PENDING_ITEMS -> lunchPlacesStatus is Status.Pending
                RefreshApplicability.FAILED_ITEMS -> lunchPlacesStatus is Status.Failure
                RefreshApplicability.ANY_ITEMS -> true
            }

            if (shouldPerformRefresh) {
                searchLunchPlacesImpl(lunchPlacesStatus.arg.query)
            }
        }
    }

    fun discardLunchPlaces() {
        lunchPlacesLauncher.launch {
            updateGeoState { it.copy(lunchPlacesStatus = null) }
        }
    }

    private suspend fun searchLunchPlacesImpl(query: String) {
        val searchSettings = searchSettingsFlow.filterNotNull().first()
        var searchFilter = SearchFilter(query, mediaLimits, searchSettings)
        updateGeoState { it.copy(lunchPlacesStatus = Status.Pending(searchFilter)) }

        determineCurrentLocation()
        yield()
        val currentLocationTerminalStatus = geoStateFlow
            .first { it.currentLocationStatus is Status.Terminal }
            .currentLocationStatus

        try {
            if (currentLocationTerminalStatus is Status.Success) {
                val currentPoint = currentLocationTerminalStatus.result.point
                searchFilter = searchFilter.copy(originPoint = currentPoint)
                val lunchPlaces = geoRepo.searchLunchPlaces(searchFilter)

                updateGeoState { it.copy(lunchPlacesStatus = Status.Success(searchFilter, lunchPlaces)) }
            } else {
                val errorType = (currentLocationTerminalStatus as Status.Failure).errorType
                updateGeoState { it.copy(lunchPlacesStatus = Status.Failure(searchFilter, errorType)) }
            }
        } catch (e: Exception) {
            if (e !is RuntimeException) {
                val errorType = if (e is ApiException && e.statusCode == CommonStatusCodes.NETWORK_ERROR) {
                    ErrorType.INTERNET_CONNECTION
                } else {
                    ErrorType.UNKNOWN
                }
                updateGeoState { it.copy(lunchPlacesStatus = Status.Failure(searchFilter, errorType)) }
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

    private enum class RefreshApplicability {
        PENDING_ITEMS,
        FAILED_ITEMS,
        ANY_ITEMS
    }

    companion object {
        private const val GEO_STATE_KEY: String = "geo_state"
    }
}