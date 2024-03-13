package com.tarasfedyk.lunchplaces.biz

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {
    @Volatile
    private var currentLocationJob: Job? = null
    private val _currentLocationFlow: MutableSharedFlow<Location?> = MutableSharedFlow()
    val currentLocationFlow: SharedFlow<Location?> = _currentLocationFlow.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    fun determineCurrentLocation() {
        currentLocationJob?.cancel()
        currentLocationJob = viewModelScope.launch {
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
}