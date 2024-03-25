package com.tarasfedyk.lunchplaces.biz

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.tarasfedyk.lunchplaces.biz.data.GeoState
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.biz.exc.NullLocationException
import com.tarasfedyk.lunchplaces.biz.util.ReplaceableLauncher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.lang.RuntimeException
import javax.inject.Inject

@HiltViewModel
class GeoViewModel @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {

    private val currentLocationLauncher: ReplaceableLauncher = ReplaceableLauncher(viewModelScope)
    private val lunchPlacesLauncher: ReplaceableLauncher = ReplaceableLauncher(viewModelScope)

    private val _geoStateFlow: MutableStateFlow<GeoState> = MutableStateFlow(GeoState())
    val geoStateFlow: StateFlow<GeoState> = _geoStateFlow.asStateFlow()

    fun determineCurrentLocation() {
        currentLocationLauncher.launch {
            determineCurrentLocationImpl()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private suspend fun determineCurrentLocationImpl() {
        try {
            _geoStateFlow.value = _geoStateFlow.value.copy(
                currentLocationStatus = Status.Pending(Unit)
            )

            val cancellationTokenSource = CancellationTokenSource()
            val currentLocationTask = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token
            )
            val currentLocation = currentLocationTask.await(cancellationTokenSource)

            _geoStateFlow.value = _geoStateFlow.value.copy(
                currentLocationStatus =
                    if (currentLocation != null) {
                        Status.Success(Unit, currentLocation)
                    } else {
                        Status.Failure(Unit, NullLocationException())
                    }
            )
        } catch (e: Exception) {
            if (e !is RuntimeException || e is SecurityException) {
                _geoStateFlow.value = _geoStateFlow.value.copy(
                    currentLocationStatus = Status.Failure(Unit, e)
                )
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

    private suspend fun searchLunchPlacesImpl(query: String) {
        try {
            _geoStateFlow.value = _geoStateFlow.value.copy(
                lunchPlacesStatus = Status.Pending(query)
            )

            determineCurrentLocation()
            val currentLocationTerminalStatus = geoStateFlow
                .first { it.currentLocationStatus is Status.Terminal }
                .currentLocationStatus

            if (currentLocationTerminalStatus is Status.Success) {
                val lunchPlaces = List(size = 100) { i ->
                    LunchPlace(id = (i + 1).toString())
                }
                _geoStateFlow.value = _geoStateFlow.value.copy(
                    lunchPlacesStatus = Status.Success(query, lunchPlaces)
                )
            } else {
                val currentLocationError =
                    (currentLocationTerminalStatus as Status.Failure).error
                _geoStateFlow.value = _geoStateFlow.value.copy(
                    lunchPlacesStatus = Status.Failure(query, currentLocationError)
                )
            }
        } catch (e: Exception) {
            if (e !is RuntimeException || e is SecurityException) {
                _geoStateFlow.value = _geoStateFlow.value.copy(
                    lunchPlacesStatus = Status.Failure(query, e)
                )
            } else {
                throw e
            }
        }
    }
}