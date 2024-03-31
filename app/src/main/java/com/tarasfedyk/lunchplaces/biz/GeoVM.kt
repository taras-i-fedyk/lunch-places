package com.tarasfedyk.lunchplaces.biz

import androidx.annotation.MainThread
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tarasfedyk.lunchplaces.biz.model.ErrorType
import com.tarasfedyk.lunchplaces.biz.model.GeoState
import com.tarasfedyk.lunchplaces.biz.model.Status
import com.tarasfedyk.lunchplaces.biz.util.ReplaceableLauncher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    private val _geoStateFlow: MutableStateFlow<GeoState> = MutableStateFlow(GeoState())
    val geoStateFlow: StateFlow<GeoState> = _geoStateFlow.asStateFlow()

    init {
        savedStateHandle.get<GeoState>(GEO_STATE_KEY)?.let { savedGeoState ->
            _geoStateFlow.value = savedGeoState
        }
    }

    fun determineCurrentLocation() {
        currentLocationLauncher.launch {
            determineCurrentLocationImpl()
        }
    }

    private suspend fun determineCurrentLocationImpl() {
        try {
            updateGeoState { it.copy(currentLocationStatus = Status.Pending(Unit)) }

            val currentLocation = locationController.determineCurrentLocation()

            val currentLocationStatus = if (currentLocation != null) {
                Status.Success(Unit, currentLocation)
            } else {
                Status.Failure(Unit, ErrorType.NULL_LOCATION)
            }
            updateGeoState { it.copy(currentLocationStatus = currentLocationStatus) }
        } catch (e: Exception) {
            if (e !is RuntimeException || e is SecurityException) {
                val errorType = if (e is SecurityException) {
                    ErrorType.LOCATION_ACCESS
                } else {
                    ErrorType.UNKNOWN
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

    fun refreshLunchPlaces() {
        lunchPlacesLauncher.launch {
            val lunchPlacesStatus = geoStateFlow.value.lunchPlacesStatus
            if (lunchPlacesStatus != null) {
                searchLunchPlacesImpl(lunchPlacesStatus.arg)
            }
        }
    }

    fun discardLunchPlaces() {
        lunchPlacesLauncher.launch {
            updateGeoState { it.copy(lunchPlacesStatus = null) }
        }
    }

    private suspend fun searchLunchPlacesImpl(query: String) {
        try {
            updateGeoState { it.copy(lunchPlacesStatus = Status.Pending(query)) }

            determineCurrentLocation()

            val currentLocationTerminalStatus = geoStateFlow
                .first { it.currentLocationStatus is Status.Terminal }
                .currentLocationStatus
            if (currentLocationTerminalStatus is Status.Success) {
                val currentLatLng = currentLocationTerminalStatus.result.latLng
                val lunchPlaces = repo.searchLunchPlaces(query, currentLatLng)

                updateGeoState { it.copy(lunchPlacesStatus = Status.Success(query, lunchPlaces)) }
            } else {
                val errorType = (currentLocationTerminalStatus as Status.Failure).errorType
                updateGeoState { it.copy(lunchPlacesStatus = Status.Failure(query, errorType)) }
            }
        } catch (e: Exception) {
            if (e !is RuntimeException) {
                updateGeoState { it.copy( lunchPlacesStatus = Status.Failure(query, ErrorType.UNKNOWN)) }
            } else {
                throw e
            }
        }
    }

    @MainThread
    private fun updateGeoState(function: (GeoState) -> GeoState) {
        val currentGeoState = _geoStateFlow.value
        val newGeoState = function(currentGeoState)
        _geoStateFlow.value = newGeoState
        savedStateHandle[GEO_STATE_KEY] = newGeoState
    }

    companion object {
        private const val GEO_STATE_KEY: String = "geo_state"
    }
}