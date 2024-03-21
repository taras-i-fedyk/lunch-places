package com.tarasfedyk.lunchplaces.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.location.Location
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.tarasfedyk.lunchplaces.biz.data.LocationState
import com.tarasfedyk.lunchplaces.biz.util.areAllValuesFalse
import com.tarasfedyk.lunchplaces.biz.util.isPermissionGranted
import com.tarasfedyk.lunchplaces.biz.util.rememberMultiplePermissionsStateWrapper
import com.tarasfedyk.lunchplaces.biz.util.toLatLng
import kotlin.math.log2

// the higher the magnification, the larger the value (in abstract units)
private const val ZOOM_LEVEL_MAX: Float = 21f
// the higher the accuracy, the smaller the value (as a radius in meters)
private const val LOCATION_ACCURACY_MAX: Float = 4f

@Composable
fun MapScreen(
    mapContentTopPadding: Dp,
    locationState: LocationState,
    onDetermineCurrentLocation: () -> Unit
) {
    var mapProperties by remember {
        mutableStateOf(
            MapProperties(maxZoomPreference = ZOOM_LEVEL_MAX)
        )
    }
    val cameraPositionState = rememberCameraPositionState()
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = mapContentTopPadding),
        uiSettings = MapUiSettings(zoomControlsEnabled = false),
        properties = mapProperties,
        cameraPositionState = cameraPositionState
    )

    val onAllLocationPermissionsDenied = {
        mapProperties = mapProperties.copy(isMyLocationEnabled = false)
    }
    val onSomeLocationPermissionGranted = {
        mapProperties = mapProperties.copy(isMyLocationEnabled = true)
        onDetermineCurrentLocation()
    }
    LocationPermissionsRequest(
        onAllLocationPermissionsDenied,
        onSomeLocationPermissionGranted
    )

    LaunchedEffect(locationState) {
        locationState.currentLocation?.let { currentLocation ->
            val currentLatLng = currentLocation.toLatLng()
            val recommendedZoomLevel = recommendZoomLevel(currentLocation)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                currentLatLng, recommendedZoomLevel
            )
            cameraPositionState.animate(cameraUpdate)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LocationPermissionsRequest(
    onAllLocationPermissionsDenied: () -> Unit,
    onSomeLocationPermissionGranted: () -> Unit
) {
    val onLocationsPermissionsResult: (Map<String, Boolean>) -> Unit = { locationPermissionFlags ->
        if (locationPermissionFlags.areAllValuesFalse()) {
            onAllLocationPermissionsDenied()
        }
    }
    // TODO: in case the Preview mode starts supporting permissions,
    // TODO: replace this with a direct call to the Accompanist library function
    val locationPermissionsState = rememberMultiplePermissionsStateWrapper(
        permissions = listOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION),
        onPermissionsResult = onLocationsPermissionsResult
    )

    val isSolelyCoarseLocationPermissionGranted by remember {
        derivedStateOf {
            locationPermissionsState.isPermissionGranted(ACCESS_COARSE_LOCATION) &&
            !locationPermissionsState.isPermissionGranted(ACCESS_FINE_LOCATION)
        }
    }
    val isFineLocationPermissionGranted by remember {
        derivedStateOf {
            locationPermissionsState.isPermissionGranted(ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(isSolelyCoarseLocationPermissionGranted) {
        if (isSolelyCoarseLocationPermissionGranted) {
            onSomeLocationPermissionGranted()
        }
    }
    LaunchedEffect(isFineLocationPermissionGranted) {
        if (isFineLocationPermissionGranted) {
            onSomeLocationPermissionGranted()
        } else {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }
}

private fun recommendZoomLevel(location: Location): Float =
    // based on how close the location's accuracy is to the maximum accuracy
    // and the fact that the maximum accuracy should be accompanied by the maximum magnification
    ZOOM_LEVEL_MAX - (log2(location.accuracy) - log2(LOCATION_ACCURACY_MAX))

@Preview(showBackground = true)
@Composable
private fun MapPreview() {
    MapScreen(
        mapContentTopPadding = 0.dp,
        locationState = LocationState(),
        onDetermineCurrentLocation = {}
    )
}