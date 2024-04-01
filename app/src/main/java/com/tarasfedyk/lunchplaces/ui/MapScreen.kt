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
import com.tarasfedyk.lunchplaces.logic.model.GeoState
import com.tarasfedyk.lunchplaces.logic.model.LocationSnapshot
import com.tarasfedyk.lunchplaces.logic.model.Status
import kotlin.math.log2

// the higher the magnification, the larger the value (in abstract units)
private const val ZOOM_LEVEL_MAX: Float = 21f
// the higher the accuracy, the smaller the value (as a radius in meters)
private const val LOCATION_ACCURACY_MAX: Float = 4f

@Composable
fun MapScreen(
    mapContentTopPadding: Dp,
    isCurrentLocationDisplayed: Boolean,
    geoState: GeoState,
    onDetermineCurrentLocation: () -> Unit
) {
    val mapProperties = MapProperties(
        maxZoomPreference = ZOOM_LEVEL_MAX,
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
    // based on how close the given accuracy is to the maximum accuracy
    // and the fact that the maximum accuracy should be accompanied by the maximum magnification
    ZOOM_LEVEL_MAX - (log2(locationAccuracy) - log2(LOCATION_ACCURACY_MAX))

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