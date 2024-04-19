package com.tarasfedyk.lunchplaces.biz

import androidx.annotation.MainThread
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tarasfedyk.lunchplaces.biz.data.ErrorType
import com.tarasfedyk.lunchplaces.biz.data.GeoState
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.biz.util.ReplaceableLauncher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.yield
import java.lang.RuntimeException
import javax.inject.Inject

@HiltViewModel
class GeoVM @Inject constructor(
    private val locationController: LocationController,
    private val repo: Repo,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val currentLocationLauncher: ReplaceableLauncher = ReplaceableLauncher(viewModelScope)
    private val lunchPlacesLauncher: ReplaceableLauncher = ReplaceableLauncher(viewModelScope)

    val geoStateFlow: StateFlow<GeoState> = savedStateHandle.getStateFlow(
        key = Keys.GEO_STATE,
        initialValue = GeoState()
    )

    fun determineCurrentLocation() {
        currentLocationLauncher.launch {
            determineCurrentLocationImpl()
        }
    }

    fun discardCurrentLocation() {
        currentLocationLauncher.launch {
            updateGeoState { it.copy(currentLocationStatus = null) }
        }
    }

    private suspend fun determineCurrentLocationImpl() {
        try {
            updateGeoState { it.copy(currentLocationStatus = Status.Pending(Unit)) }

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
                    ErrorType.LOCATION_ACCESS
                } else {
                    ErrorType.CURRENT_LOCATION
                }
                updateGeoState { it.copy(currentLocationStatus = Status.Failure(Unit, errorType)) }
            } else {
                throw e
            }
        }
    }

    fun searchLunchPlaces(searchFilter: SearchFilter) {
        lunchPlacesLauncher.launch {
            searchLunchPlacesImpl(searchFilter)
        }
    }

    fun discardLunchPlaces() {
        lunchPlacesLauncher.launch {
            updateGeoState { it.copy(lunchPlacesStatus = null) }
        }
    }

    private suspend fun searchLunchPlacesImpl(searchFilter: SearchFilter) {
        try {
            updateGeoState { it.copy(lunchPlacesStatus = Status.Pending(searchFilter)) }

            determineCurrentLocation()
            val currentLocationTerminalStatus = geoStateFlow
                .first { it.currentLocationStatus is Status.Terminal }
                .currentLocationStatus

            if (currentLocationTerminalStatus is Status.Success) {
                val currentLatLng = currentLocationTerminalStatus.result.latLng
                val lunchPlaces = repo.searchLunchPlaces(searchFilter, currentLatLng)

                updateGeoState { it.copy(lunchPlacesStatus = Status.Success(searchFilter, lunchPlaces)) }
            } else {
                val errorType = (currentLocationTerminalStatus as Status.Failure).errorType
                updateGeoState { it.copy(lunchPlacesStatus = Status.Failure(searchFilter, errorType)) }
            }
        } catch (e: Exception) {
            if (e !is RuntimeException) {
                updateGeoState { it.copy(lunchPlacesStatus = Status.Failure(searchFilter, ErrorType.LUNCH_PLACES)) }
            } else {
                throw e
            }
        }
    }

    @MainThread
    private suspend fun updateGeoState(function: (GeoState) -> GeoState) {
        val currentGeoState = geoStateFlow.value
        val newGeoState = function(currentGeoState)
        savedStateHandle[Keys.GEO_STATE] = newGeoState

        // we're doing this to ensure that the observers can react to each update
        // without an old update being silently overwritten by a new one in certain edge cases
        yield()
    }

    private object Keys {
        const val GEO_STATE: String = "geo_state"
    }
}