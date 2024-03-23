package com.tarasfedyk.lunchplaces.biz

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.tarasfedyk.lunchplaces.biz.data.GeoState
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.biz.util.ReplaceableLaunchCoroutine
import com.tarasfedyk.lunchplaces.biz.util.asLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class GeoViewModel @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {

    private val currentLocationDeterminer: ReplaceableLaunchCoroutine =
        ReplaceableLaunchCoroutine(viewModelScope) { determineCurrentLocationImpl() }
    private val lunchPlacesSearcher: ReplaceableLaunchCoroutine =
        ReplaceableLaunchCoroutine(viewModelScope) { searchLunchPlacesImpl() }

    private val _geoStateFlow: MutableStateFlow<GeoState> =
        MutableStateFlow(GeoState())
    val geoStateFlow: StateFlow<GeoState> = _geoStateFlow.asStateFlow()

    fun determineCurrentLocation() {
        currentLocationDeterminer.replaceableLaunch()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private suspend fun determineCurrentLocationImpl() {
        try {
            _geoStateFlow.value = GeoState(
                currentLocationStatus = Status.Pending
            )
            val cancellationTokenSource = CancellationTokenSource()
            val currentLocationTask = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token
            )
            val mutableCurrentLocation = currentLocationTask.await(cancellationTokenSource)
            if (mutableCurrentLocation != null) {
                val currentLocation = mutableCurrentLocation.asLocation()
                _geoStateFlow.value = GeoState(
                    currentLocationStatus = Status.Success(currentLocation)
                )
            } else {
                _geoStateFlow.value = GeoState(
                    currentLocationStatus = Status.Failure()
                )
            }
        } catch (e: Exception) {
            if (e !is CancellationException) {
                _geoStateFlow.value = GeoState(
                    currentLocationStatus = Status.Failure(e)
                )
            }
        }
    }

    fun searchLunchPlaces() {
        lunchPlacesSearcher.replaceableLaunch()
    }

    private suspend fun searchLunchPlacesImpl() {}
}