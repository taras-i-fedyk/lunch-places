package com.tarasfedyk.lunchplaces.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.maps.android.compose.GoogleMap
import com.tarasfedyk.lunchplaces.biz.LocationViewModel
import com.tarasfedyk.lunchplaces.biz.data.LocationState
import com.tarasfedyk.lunchplaces.biz.util.isPermissionGranted
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
            onSomeLocationPermissionGranted = { locationViewModel.determineCurrentLocation() }
        )
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun LocationPermissionsRequest(
        onSomeLocationPermissionGranted: () -> Unit
    ) {
        val locationPermissionsState = rememberMultiplePermissionsState(
            permissions = listOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
        )
        val supportsCoarseLocationOnly by remember {
            derivedStateOf {
                locationPermissionsState.isPermissionGranted(ACCESS_COARSE_LOCATION) &&
                !locationPermissionsState.isPermissionGranted(ACCESS_FINE_LOCATION)
            }
        }
        val supportsFineLocation by remember {
            derivedStateOf {
                locationPermissionsState.isPermissionGranted(ACCESS_FINE_LOCATION)
            }
        }

        LaunchedEffect(supportsCoarseLocationOnly) {
            if (supportsCoarseLocationOnly) {
                onSomeLocationPermissionGranted()
            }
        }
        LaunchedEffect(supportsFineLocation) {
            if (supportsFineLocation) {
                onSomeLocationPermissionGranted()
            } else {
                locationPermissionsState.launchMultiplePermissionRequest()
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun MainPreview() {
        MainContent()
    }
}