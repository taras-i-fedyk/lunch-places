package com.tarasfedyk.lunchplaces.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.tarasfedyk.lunchplaces.biz.data.LocationSnapshot
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.ui.util.isPermissionGranted
import com.tarasfedyk.lunchplaces.ui.util.rememberMultiplePermissionsStateWrapper
import kotlin.math.log2

// the higher the zoom level, the larger the value of this constant as the degree of magnification
private const val MAX_ZOOM_LEVEL: Float = 21f
// the higher the location accuracy, the smaller the value of this constant as a radius in meters
private const val MAX_LOCATION_ACCURACY: Float = 4f

@Composable
fun MapScreen(
    mapContentTopPadding: Dp,
    isSearchActive: Boolean,
    onDetermineCurrentLocation: () -> Unit,
    onDiscardCurrentLocation: () -> Unit,
    currentLocationStatus: Status<Unit, LocationSnapshot>?
) {
    var isCurrentLocationEnabled by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState()

    val mapContentPadding = PaddingValues(top = mapContentTopPadding)
    val mapUiSettings = MapUiSettings(zoomControlsEnabled = false)
    val mapProperties = MapProperties(
        maxZoomPreference = MAX_ZOOM_LEVEL,
        isMyLocationEnabled = isCurrentLocationEnabled
    )
    val onCurrentLocationButtonClicked = remember(onDetermineCurrentLocation) {
        {
            onDetermineCurrentLocation()
            true
        }
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        contentPadding = mapContentPadding,
        uiSettings = mapUiSettings,
        properties = mapProperties,
        cameraPositionState = cameraPositionState,
        onMyLocationButtonClick = onCurrentLocationButtonClicked
    )

    if (!isSearchActive) {
        val onAllLocationPermissionsDenied = remember(onDiscardCurrentLocation) {
            {
                isCurrentLocationEnabled = false
                onDiscardCurrentLocation()
            }
        }
        val onSomeLocationPermissionGranted = remember(onDetermineCurrentLocation) {
            {
                isCurrentLocationEnabled = true
                onDetermineCurrentLocation()
            }
        }
        LocationPermissionsRequest(
            onAllLocationPermissionsDenied,
            onSomeLocationPermissionGranted
        )
    }

    LaunchedEffect(isCurrentLocationEnabled, currentLocationStatus) {
        if (
            isCurrentLocationEnabled &&
            currentLocationStatus is Status.Success<*, LocationSnapshot>
        ) {
            val currentLocation = currentLocationStatus.result
            val currentLatLng = currentLocation.latLng
            val currentZoomLevel = recommendZoomLevel(currentLocation.accuracy)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, currentZoomLevel)
            cameraPositionState.animate(cameraUpdate)
        } else if (!isCurrentLocationEnabled) {
            val defaultCameraPosition = CameraPositionState().position
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(defaultCameraPosition)
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
    // TODO: replace this with a direct call in a Preview-friendly way
    val locationPermissionsState = rememberMultiplePermissionsStateWrapper(
        permissions = listOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
    )

    val areAllLocationPermissionsDenied =
        !locationPermissionsState.isPermissionGranted(ACCESS_COARSE_LOCATION) &&
        !locationPermissionsState.isPermissionGranted(ACCESS_FINE_LOCATION)
    val isSolelyCoarseLocationPermissionGranted =
        locationPermissionsState.isPermissionGranted(ACCESS_COARSE_LOCATION) &&
        !locationPermissionsState.isPermissionGranted(ACCESS_FINE_LOCATION)
    val isFineLocationPermissionGranted =
        locationPermissionsState.isPermissionGranted(ACCESS_FINE_LOCATION)

    LaunchedEffect(areAllLocationPermissionsDenied) {
        if (areAllLocationPermissionsDenied) {
            onAllLocationPermissionsDenied()
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

private fun recommendZoomLevel(locationAccuracy: Float): Float =
    // based on how close the given location accuracy is to the maximum location accuracy
    // and assuming the maximum location accuracy should be accompanied by the maximum zoom level
    MAX_ZOOM_LEVEL - (log2(locationAccuracy) - log2(MAX_LOCATION_ACCURACY))

@Preview(showBackground = true)
@Composable
private fun MapPreview() {
    MapScreen(
        mapContentTopPadding = 0.dp,
        isSearchActive = false,
        onDetermineCurrentLocation = {},
        onDiscardCurrentLocation = {},
        currentLocationStatus = null
    )
}