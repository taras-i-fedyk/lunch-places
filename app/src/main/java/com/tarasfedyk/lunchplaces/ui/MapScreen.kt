package com.tarasfedyk.lunchplaces.ui

import android.Manifest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.maps.android.compose.GoogleMap
import com.tarasfedyk.lunchplaces.biz.LocationViewModel
import com.tarasfedyk.lunchplaces.biz.data.LocationState
import com.tarasfedyk.lunchplaces.biz.util.isPermissionGranted

@Composable
fun MapScreen(
    locationViewModel: LocationViewModel
) {
    val locationState: LocationState by locationViewModel.locationStateFlow.collectAsStateWithLifecycle()
    GoogleMap(
        modifier = Modifier.fillMaxSize()
    )

    val onSomeLocationPermissionGranted = remember(locationViewModel) {
        return@remember locationViewModel::determineCurrentLocation
    }
    LocationPermissionsRequest(onSomeLocationPermissionGranted)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LocationPermissionsRequest(
    onSomeLocationPermissionGranted: () -> Unit
) {
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    )
    val permitsCoarseLocationOnly by remember {
        derivedStateOf {
            locationPermissionsState.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    !locationPermissionsState.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    val permitsFineLocation by remember {
        derivedStateOf {
            locationPermissionsState.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(permitsCoarseLocationOnly) {
        if (permitsCoarseLocationOnly) {
            onSomeLocationPermissionGranted()
        }
    }
    LaunchedEffect(permitsFineLocation) {
        if (permitsFineLocation) {
            onSomeLocationPermissionGranted()
        } else {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }
}