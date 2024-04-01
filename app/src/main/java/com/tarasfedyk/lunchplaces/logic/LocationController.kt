package com.tarasfedyk.lunchplaces.logic

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.tarasfedyk.lunchplaces.logic.model.LocationSnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface LocationController {
    suspend fun determineCurrentLocation(): LocationSnapshot?
}

class LocationControllerImpl @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationController {

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    override suspend fun determineCurrentLocation(): LocationSnapshot? {
        val cancellationTokenSource = CancellationTokenSource()
        val currentLocationTask = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token
        )
        return currentLocationTask.await(cancellationTokenSource)?.toLocationSnapshot()
    }

    private fun Location.toLocationSnapshot() =
        LocationSnapshot(LatLng(latitude, longitude), accuracy)
}