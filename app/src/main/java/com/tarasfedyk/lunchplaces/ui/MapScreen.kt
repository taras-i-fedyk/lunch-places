package com.tarasfedyk.lunchplaces.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.tarasfedyk.lunchplaces.R
import com.tarasfedyk.lunchplaces.biz.data.ErrorType
import com.tarasfedyk.lunchplaces.biz.data.LocationSnapshot
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.ui.data.MapConfig
import com.tarasfedyk.lunchplaces.ui.data.MapViewport
import com.tarasfedyk.lunchplaces.ui.theme.AppTheme
import com.tarasfedyk.lunchplaces.ui.util.PermanentErrorSnackbar
import kotlin.math.log2

// the higher the zoom level, the larger the value of this constant as the degree of magnification
private const val MAX_ZOOM_LEVEL: Float = 21f
// the higher the location accuracy, the smaller the value of this constant as a radius in meters
private const val MAX_LOCATION_ACCURACY: Float = 4f

@Composable
fun MapScreen(
    mapConfig: MapConfig,
    areAllLocationPermissionsDenied: Boolean,
    onDetermineCurrentLocation: () -> Unit,
    currentLocationStatus: Status<Unit, LocationSnapshot>?
) {
    val density = LocalDensity.current
    val mapAlpha = if (mapConfig.isMapVisible) 1f else 0f
    val mapTopPadding = with (density) { mapConfig.mapTopPadding.toDp() }

    var isMapLaidOut by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier
            .alpha(mapAlpha)
            .padding(top = mapTopPadding),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            if (mapConfig.mapViewport == null && !areAllLocationPermissionsDenied) {
                CurrentLocationButton(onDetermineCurrentLocation)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(paddingValues)
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .onPlaced { isMapLaidOut = true },
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false
                ),
                properties = MapProperties(
                    maxZoomPreference = MAX_ZOOM_LEVEL,
                    isMyLocationEnabled = !areAllLocationPermissionsDenied
                ),
                cameraPositionState = cameraPositionState,
                contentDescription = stringResource(R.string.map_description)
            ) {
                if (mapConfig.mapViewport != null) {
                    Marker(
                        state = MarkerState(position = mapConfig.mapViewport.destinationPoint)
                    )
                }
            }

            AnimatedCameraPosition(
                isMapLaidOut,
                cameraPositionState,
                areAllLocationPermissionsDenied,
                currentLocationStatus,
                mapConfig.mapViewport
            )

            if (currentLocationStatus is Status.Failure) {
                CurrentLocationError(
                    snackbarHostState,
                    currentLocationStatus.errorType,
                    onDetermineCurrentLocation
                )
            }
        }
    }
}

@Composable
private fun AnimatedCameraPosition(
    isMapLaidOut: Boolean,
    cameraPositionState: CameraPositionState,
    areAllLocationPermissionsDenied: Boolean,
    currentLocationStatus: Status<Unit, LocationSnapshot>?,
    mapViewport: MapViewport?
) {
    val density = LocalDensity.current
    val mapViewportPadding = with (density) { MapViewport.Padding.roundToPx() }

    LaunchedEffect(
        isMapLaidOut,
        cameraPositionState,
        mapViewport,
        mapViewportPadding,
        areAllLocationPermissionsDenied,
        currentLocationStatus
    ) {
        if (!isMapLaidOut) return@LaunchedEffect

        val cameraUpdate = if (mapViewport != null) {
            CameraUpdateFactory.newLatLngBounds(mapViewport.bounds, mapViewportPadding)
        } else {
            if (areAllLocationPermissionsDenied) {
                val defaultCameraPosition = CameraPositionState().position
                CameraUpdateFactory.newCameraPosition(defaultCameraPosition)
            } else if (currentLocationStatus is Status.Success<*, LocationSnapshot>) {
                val currentLocation = currentLocationStatus.result
                val currentPoint = currentLocation.point
                val currentZoomLevel = calculateZoomLevel(currentLocation.accuracy)
                CameraUpdateFactory.newLatLngZoom(currentPoint, currentZoomLevel)
            } else {
                null
            }
        }

        if (cameraUpdate != null) {
            cameraPositionState.animate(cameraUpdate)
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
private fun CurrentLocationError(
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

private fun calculateZoomLevel(locationAccuracy: Float): Float =
    // based on how close the given location accuracy is to the maximum location accuracy
    // and assuming the maximum location accuracy should be accompanied by the maximum zoom level
    MAX_ZOOM_LEVEL - (log2(locationAccuracy) - log2(MAX_LOCATION_ACCURACY))

@Preview(showBackground = true)
@Composable
private fun MapScreenPreview() {
    AppTheme {
        MapScreen(
            mapConfig = MapConfig(),
            areAllLocationPermissionsDenied = false,
            onDetermineCurrentLocation = {},
            currentLocationStatus = null
        )
    }
}