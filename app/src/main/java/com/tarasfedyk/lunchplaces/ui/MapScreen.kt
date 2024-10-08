package com.tarasfedyk.lunchplaces.ui

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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraMoveStartedReason
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
import kotlinx.coroutines.launch
import kotlin.math.log2

// the higher the maximum zoom level, the larger the value of this constant as the degree of magnification
private const val MAX_ZOOM_LEVEL: Float = 21f
// the higher the maximum location accuracy, the smaller the value of this constant as a radius in meters
private const val MAX_LOCATION_ACCURACY: Float = 4f

private val MAP_VIEWPORT_PADDING: Dp = 48.dp

@Composable
fun MapScreen(
    mapConfig: MapConfig,
    isNoLocationPermissionGranted: Boolean,
    currentLocationStatus: Status<Unit, LocationSnapshot>?,
    onDetermineCurrentLocation: () -> Unit
) {
    val density = LocalDensity.current

    val mapAlpha = if (mapConfig.isMapVisible) 1f else 0f
    val mapTopPadding = with (density) { mapConfig.mapTopPadding.toDp() }

    val cameraPositionState = rememberCameraPositionState()

    val coroutineScope = rememberCoroutineScope()
    val mapViewportPadding = with (density) { MAP_VIEWPORT_PADDING.roundToPx() }
    var isMapViewportFocused by remember { mutableStateOf(false) }
    val onSetMapViewportFocused: (Boolean) -> Unit = remember { { isMapViewportFocused = it } }
    val onExploreProximity: () -> Unit = remember(mapConfig.mapViewport, mapViewportPadding) {
        {
            coroutineScope.launch {
                cameraPositionState.animateToMapViewport(
                    mapConfig.mapViewport!!.bounds,
                    mapViewportPadding,
                    onSetMapViewportFocused
                )
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier
            .alpha(mapAlpha)
            .padding(top = mapTopPadding),
        floatingActionButton = {
            if (mapConfig.mapViewport != null) {
                if (!isMapViewportFocused) {
                    ProximityButton(onExploreProximity)
                }
            } else {
                CurrentLocationButton(onDetermineCurrentLocation)
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        DynamicMap(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(paddingValues)
                .padding(paddingValues),
            cameraPositionState = cameraPositionState,
            mapViewport = mapConfig.mapViewport,
            mapViewportPadding = mapViewportPadding,
            onSetMapViewportFocused = onSetMapViewportFocused,
            isNoLocationPermissionGranted = isNoLocationPermissionGranted,
            currentLocationStatus = currentLocationStatus
        )

        if (currentLocationStatus is Status.Failure) {
            CurrentLocationError(
                snackbarHostState = snackbarHostState,
                errorId = currentLocationStatus.id,
                errorType = currentLocationStatus.errorType,
                onDetermineCurrentLocation = onDetermineCurrentLocation
            )
        }
    }

    LaunchedEffect(mapConfig.mapViewport) {
        isMapViewportFocused = false
    }
}

@Composable
private fun DynamicMap(
    cameraPositionState: CameraPositionState,
    mapViewport: MapViewport?,
    mapViewportPadding: Int,
    onSetMapViewportFocused: (Boolean) -> Unit,
    isNoLocationPermissionGranted: Boolean,
    currentLocationStatus: Status<Unit, LocationSnapshot>?,
    modifier: Modifier = Modifier
) {
    var isMapLoaded by remember { mutableStateOf(false) }
    val onMapLoaded = remember { { isMapLoaded = true } }

    GoogleMap(
        modifier = modifier,
        onMapLoaded = onMapLoaded,
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false
        ),
        properties = MapProperties(
            maxZoomPreference = MAX_ZOOM_LEVEL,
            isMyLocationEnabled = !isNoLocationPermissionGranted
        ),
        cameraPositionState = cameraPositionState,
        contentDescription = stringResource(R.string.map_description)
    ) {
        if (mapViewport != null) {
            Marker(
                state = MarkerState(position = mapViewport.destinationPoint)
            )
        }
    }

    DynamicCameraPosition(
        isMapLoaded = isMapLoaded,
        cameraPositionState = cameraPositionState,
        mapViewport = mapViewport,
        mapViewportPadding = mapViewportPadding,
        onSetMapViewportFocused = onSetMapViewportFocused,
        currentLocationStatus = currentLocationStatus
    )
}

@Composable
private fun DynamicCameraPosition(
    isMapLoaded: Boolean,
    cameraPositionState: CameraPositionState,
    mapViewport: MapViewport?,
    mapViewportPadding: Int,
    onSetMapViewportFocused: (Boolean) -> Unit,
    currentLocationStatus: Status<Unit, LocationSnapshot>?
) {
    val wasCameraMovedNotByDeveloper by remember {
        derivedStateOf {
            cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.API_ANIMATION ||
            cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE
        }
    }

    LaunchedEffect(wasCameraMovedNotByDeveloper) {
        if (wasCameraMovedNotByDeveloper) {
            onSetMapViewportFocused(false)
        }
    }

    LaunchedEffect(
        isMapLoaded,
        cameraPositionState,
        mapViewport,
        mapViewportPadding,
        onSetMapViewportFocused,
        currentLocationStatus
    ) {
        if (!isMapLoaded) return@LaunchedEffect

        if (mapViewport != null) {
            cameraPositionState.animateToMapViewport(
                mapViewport.bounds, mapViewportPadding, onSetMapViewportFocused
            )
        } else if (currentLocationStatus is Status.Success<*, LocationSnapshot>) {
            cameraPositionState.animateToCurrentLocation(
                currentLocation = currentLocationStatus.result,
                onSetMapViewportFocused
            )
        }
    }
}

private suspend fun CameraPositionState.animateToMapViewport(
    mapViewportBounds: LatLngBounds,
    mapViewportPadding: Int,
    onSetMapViewportFocused: (Boolean) -> Unit
) {
    val cameraUpdate = CameraUpdateFactory.newLatLngBounds(mapViewportBounds, mapViewportPadding)
    animate(cameraUpdate)

    onSetMapViewportFocused(true)
}

private suspend fun CameraPositionState.animateToCurrentLocation(
    currentLocation: LocationSnapshot,
    onSetMapViewportFocused: (Boolean) -> Unit
) {
    onSetMapViewportFocused(false)

    val currentPoint = currentLocation.point
    val currentZoomLevel = calculateZoomLevel(currentLocation.accuracy)
    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentPoint, currentZoomLevel)
    animate(cameraUpdate)
}

private fun calculateZoomLevel(locationAccuracy: Float): Float =
    // based on how close the given location accuracy is to the maximum location accuracy
    // and assuming the maximum location accuracy should be accompanied by the maximum zoom level
    MAX_ZOOM_LEVEL - (log2(locationAccuracy) - log2(MAX_LOCATION_ACCURACY))

@Composable
private fun ProximityButton(onExploreProximity: () -> Unit) {
    FloatingActionButton(onClick = onExploreProximity) {
        Icon(
            painter = painterResource(R.drawable.ic_proximity),
            contentDescription = stringResource(R.string.proximity_button_description)
        )
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
    errorId: Int,
    errorType: ErrorType,
    onDetermineCurrentLocation: () -> Unit
) {
    val isAppSettingsError = errorType == ErrorType.LOCATION_PERMISSION
    val errorMessage = when (errorType) {
        ErrorType.LOCATION_SERVICES -> stringResource(R.string.map_services_error_message)
        ErrorType.LOCATION_PERMISSION -> stringResource(R.string.map_permission_error_message)
        else -> stringResource(R.string.map_location_error_message)
    }

    PermanentErrorSnackbar(
        snackbarHostState = snackbarHostState,
        isAppSettingsError = isAppSettingsError,
        errorId = errorId,
        errorMessage = errorMessage,
        onRetry = onDetermineCurrentLocation
    )
}

@Preview(showBackground = true)
@Composable
private fun MapScreenPreview() {
    AppTheme {
        MapScreen(
            mapConfig = MapConfig(),
            isNoLocationPermissionGranted = false,
            currentLocationStatus = null,
            onDetermineCurrentLocation = {}
        )
    }
}