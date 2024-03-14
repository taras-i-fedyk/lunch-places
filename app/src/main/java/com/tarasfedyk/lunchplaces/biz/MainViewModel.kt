package com.tarasfedyk.lunchplaces.biz

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.tarasfedyk.lunchplaces.biz.util.ReplaceableLaunchCoroutine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {

    private val currentLocationDeterminer: ReplaceableLaunchCoroutine =
        ReplaceableLaunchCoroutine(viewModelScope) { determineCurrentLocationInternal() }
    private val _currentLocationFlow: MutableSharedFlow<Location?> = MutableSharedFlow(replay = 1)
    val currentLocationFlow: SharedFlow<Location?> = _currentLocationFlow.asSharedFlow()

    fun determineCurrentLocation() {
        currentLocationDeterminer.replaceableLaunch()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private suspend fun determineCurrentLocationInternal() {
        val cancellationTokenSource = CancellationTokenSource()
        val currentLocationTask = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token
        )
        try {
            val currentLocation = currentLocationTask.await(cancellationTokenSource)
            _currentLocationFlow.emit(currentLocation)
        } catch (e: Exception) {
            if (e !is CancellationException) {
                _currentLocationFlow.emit(null)
            }
        }
    }
}