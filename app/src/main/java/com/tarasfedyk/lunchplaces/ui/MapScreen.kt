package com.tarasfedyk.lunchplaces.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.tarasfedyk.lunchplaces.biz.LocationViewModel
import com.tarasfedyk.lunchplaces.biz.data.LocationState
import com.tarasfedyk.lunchplaces.biz.util.isAnyPermissionGranted
import com.tarasfedyk.lunchplaces.biz.util.isPermissionGranted

@Composable
fun MapScreen(
    locationViewModel: LocationViewModel
) {
    val locationState: LocationState by locationViewModel.locationStateFlow.collectAsStateWithLifecycle()

    var mapProperties by remember { mutableStateOf(MapProperties()) }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        properties = mapProperties
    )

    val onAllLocationPermissionsDenied = {
        mapProperties = mapProperties.copy(isMyLocationEnabled = false)
    }
    val onSomeLocationPermissionGranted = remember(locationViewModel) {
        {
            mapProperties = mapProperties.copy(isMyLocationEnabled = true)
            locationViewModel.determineCurrentLocation()
        }
    }
    LocationPermissionsRequest(onAllLocationPermissionsDenied, onSomeLocationPermissionGranted)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LocationPermissionsRequest(
    onAllLocationPermissionsDenied: () -> Unit,
    onSomeLocationPermissionGranted: () -> Unit
) {
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
    )
    val areAllLocationPermissionsDenied by remember {
        derivedStateOf {
            !locationPermissionsState.isAnyPermissionGranted()
        }
    }
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