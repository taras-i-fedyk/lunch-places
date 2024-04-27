package com.tarasfedyk.lunchplaces.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.tarasfedyk.lunchplaces.R
import com.tarasfedyk.lunchplaces.biz.data.ErrorType
import com.tarasfedyk.lunchplaces.biz.data.LocationPermissionsLevel
import com.tarasfedyk.lunchplaces.biz.data.LocationSnapshot
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.biz.data.isCoarseOrFine
import com.tarasfedyk.lunchplaces.ui.util.PermanentErrorSnackbar
import kotlin.math.log2

// the higher the zoom level, the larger the value of this constant as the degree of magnification
private const val MAX_ZOOM_LEVEL: Float = 21f
// the higher the location accuracy, the smaller the value of this constant as a radius in meters
private const val MAX_LOCATION_ACCURACY: Float = 4f

@Composable
fun MapScreen(
    locationPermissionsLevel: LocationPermissionsLevel?,
    onDetermineCurrentLocation: () -> Unit,
    currentLocationStatus: Status<Unit, LocationSnapshot>?
) {
    val isCurrentLocationEnabled = locationPermissionsLevel.isCoarseOrFine

    val cameraPositionState = rememberCameraPositionState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            if (isCurrentLocationEnabled) {
                CurrentLocationButton(onDetermineCurrentLocation)
            }
        }
    ) { paddingValues ->
        GoogleMap(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false
            ),
            properties = MapProperties(
                maxZoomPreference = MAX_ZOOM_LEVEL,
                isMyLocationEnabled = isCurrentLocationEnabled
            ),
            cameraPositionState = cameraPositionState
        )

        CameraPositionAnimation(
            cameraPositionState, isCurrentLocationEnabled, currentLocationStatus
        )

        if (currentLocationStatus is Status.Failure) {
            MapError(
                snackbarHostState, currentLocationStatus.errorType, onDetermineCurrentLocation
            )
        }
    }
}

@Composable
private fun CameraPositionAnimation(
    cameraPositionState: CameraPositionState,
    isCurrentLocationEnabled: Boolean,
    currentLocationStatus: Status<Unit, LocationSnapshot>?
) {
    val currentCameraPositionState by rememberUpdatedState(cameraPositionState)

    LaunchedEffect(isCurrentLocationEnabled, currentLocationStatus) {
        if (!isCurrentLocationEnabled) {
            val defaultCameraPosition = CameraPositionState().position
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(defaultCameraPosition)
            currentCameraPositionState.animate(cameraUpdate)
        } else if (currentLocationStatus is Status.Success<*, LocationSnapshot>) {
            val currentLocation = currentLocationStatus.result
            val currentLatLng = currentLocation.latLng
            val currentZoomLevel = recommendZoomLevel(currentLocation.accuracy)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, currentZoomLevel)
            currentCameraPositionState.animate(cameraUpdate)
        }
    }
}

@Composable
private fun CurrentLocationButton(onDetermineCurrentLocation: () -> Unit) {
    FloatingActionButton(onClick = onDetermineCurrentLocation) {
        Icon(
            painter = painterResource(R.drawable.ic_current_location),
            contentDescription = stringResource(R.string.current_location_button_description)
        )
    }
}

@Composable
private fun MapError(
    snackbarHostState: SnackbarHostState,
    errorType: ErrorType,
    onDetermineCurrentLocation: () -> Unit
) {
    val isAppSettingsError = errorType == ErrorType.LOCATION_PERMISSIONS
    val errorMessage = if (errorType == ErrorType.LOCATION_PERMISSIONS) {
        stringResource(R.string.map_permissions_error_message)
    } else {
        stringResource(R.string.map_error_message)
    }

    PermanentErrorSnackbar(
        snackbarHostState = snackbarHostState,
        isAppSettingsError = isAppSettingsError,
        errorMessage = errorMessage,
        onRetry = onDetermineCurrentLocation
    )
}

private fun recommendZoomLevel(locationAccuracy: Float): Float =
    // based on how close the given location accuracy is to the maximum location accuracy
    // and assuming the maximum location accuracy should be accompanied by the maximum zoom level
    MAX_ZOOM_LEVEL - (log2(locationAccuracy) - log2(MAX_LOCATION_ACCURACY))

@Preview(showBackground = true)
@Composable
private fun MapPreview() {
    MapScreen(
        locationPermissionsLevel = null,
        onDetermineCurrentLocation = {},
        currentLocationStatus = null
    )
}