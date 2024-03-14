package com.tarasfedyk.lunchplaces.ui

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.maps.android.compose.GoogleMap
import com.tarasfedyk.lunchplaces.biz.LocationViewModel
import com.tarasfedyk.lunchplaces.biz.data.LocationState
import com.tarasfedyk.lunchplaces.ui.theme.LunchPlacesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainContent()
        }
    }

    @Composable
    private fun MainContent() {
        LunchPlacesTheme {
            MapScreen()
        }
    }

    @Composable
    private fun MapScreen(
        locationViewModel: LocationViewModel = hiltViewModel()
    ) {
        val locationState: LocationState by locationViewModel.locationStateFlow.collectAsStateWithLifecycle()
        GoogleMap(
            modifier = Modifier.fillMaxSize()
        )
        LocationPermissionsRequest(
            onAnyLocationPermissionGranted = { locationViewModel.determineCurrentLocation() }
        )
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun LocationPermissionsRequest(
        onAnyLocationPermissionGranted: () -> Unit
    ) {
        val locationPermissionsState = rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
        LaunchedEffect(locationPermissionsState) {
            if (!locationPermissionsState.allPermissionsGranted) {
                locationPermissionsState.launchMultiplePermissionRequest()
            }
            if (locationPermissionsState.permissions.any { it.status.isGranted }) {
                onAnyLocationPermissionGranted()
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun MainPreview() {
        MainContent()
    }
}