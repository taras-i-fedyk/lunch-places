package com.tarasfedyk.lunchplaces.biz

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.tarasfedyk.lunchplaces.biz.data.LocationState
import com.tarasfedyk.lunchplaces.biz.util.ReplaceableLaunchCoroutine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {

    private val currentLocationDeterminer: ReplaceableLaunchCoroutine =
        ReplaceableLaunchCoroutine(viewModelScope) { determineCurrentLocationInternal() }

    private val _locationStateFlow: MutableStateFlow<LocationState> =
        MutableStateFlow(LocationState())
    val locationStateFlow: StateFlow<LocationState> = _locationStateFlow.asStateFlow()

    fun determineCurrentLocation() {
        currentLocationDeterminer.replaceableLaunch()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private suspend fun determineCurrentLocationInternal() {
        try {
            val cancellationTokenSource = CancellationTokenSource()
            val currentLocationTask = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token
            )
            val currentLocation = currentLocationTask.await(cancellationTokenSource)
            _locationStateFlow.value = LocationState(currentLocation = currentLocation)
        } catch (e: Exception) {
            if (e !is CancellationException) {
                _locationStateFlow.value = LocationState(currentLocationError = e)
            }
        }
    }
}