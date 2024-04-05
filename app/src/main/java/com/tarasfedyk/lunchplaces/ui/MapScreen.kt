package com.tarasfedyk.lunchplaces.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.tarasfedyk.lunchplaces.biz.data.GeoState
import com.tarasfedyk.lunchplaces.biz.data.LocationSnapshot
import com.tarasfedyk.lunchplaces.biz.data.Status
import kotlin.math.log2

// the higher the zoom level, the larger the value of this constant as the degree of magnification
private const val MAX_ZOOM_LEVEL: Float = 21f
// the higher the location accuracy, the smaller the value of this constant as a radius in meters
private const val MAX_LOCATION_ACCURACY: Float = 4f

@Composable
fun MapScreen(
    mapContentTopPadding: Dp,
    isCurrentLocationDisplayed: Boolean,
    geoState: GeoState,
    onDetermineCurrentLocation: () -> Unit
) {
    val mapProperties = MapProperties(
        maxZoomPreference = MAX_ZOOM_LEVEL,
        isMyLocationEnabled = isCurrentLocationDisplayed
    )
    val cameraPositionState = rememberCameraPositionState()
    val onCurrentLocationButtonClicked = {
        onDetermineCurrentLocation()
        true
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = mapContentTopPadding),
        uiSettings = MapUiSettings(zoomControlsEnabled = false),
        properties = mapProperties,
        cameraPositionState = cameraPositionState,
        onMyLocationButtonClick = onCurrentLocationButtonClicked
    )

    LaunchedEffect(key1 = isCurrentLocationDisplayed, key2 = geoState) {
        if (
            isCurrentLocationDisplayed &&
            geoState.currentLocationStatus is Status.Success<*, LocationSnapshot>
        ) {
            val currentLocation = geoState.currentLocationStatus.result
            val currentLatLng = currentLocation.latLng
            val currentZoomLevel = recommendZoomLevel(currentLocation.accuracy)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, currentZoomLevel)
            cameraPositionState.animate(cameraUpdate)
        } else if (!isCurrentLocationDisplayed) {
            val defaultCameraPosition = CameraPositionState().position
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(defaultCameraPosition)
            cameraPositionState.animate(cameraUpdate)
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
        isCurrentLocationDisplayed = true,
        geoState = GeoState(),
        onDetermineCurrentLocation = {}
    )
}