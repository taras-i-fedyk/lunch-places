package com.tarasfedyk.lunchplaces.ui

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.tarasfedyk.lunchplaces.biz.GeoViewModel
import com.tarasfedyk.lunchplaces.biz.data.GeoState
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.ui.nav.SEARCH_ROUTE
import com.tarasfedyk.lunchplaces.ui.nav.searchScreen
import com.tarasfedyk.lunchplaces.ui.theme.AppTheme
import com.tarasfedyk.lunchplaces.ui.util.areAllValuesFalse
import com.tarasfedyk.lunchplaces.ui.util.isPermissionGranted
import com.tarasfedyk.lunchplaces.ui.util.rememberMultiplePermissionsStateWrapper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                MainContent()
            }
        }
    }

    @Composable
    private fun MainContent(
        geoViewModel: GeoViewModel = hiltViewModel()
    ) {
        val geoState by geoViewModel.geoStateFlow.collectAsStateWithLifecycle()
        val onDetermineCurrentLocation = geoViewModel::determineCurrentLocation
        val onSearchLunchPlaces = geoViewModel::searchLunchPlaces
        MainContentImpl(geoState, onDetermineCurrentLocation, onSearchLunchPlaces)
    }

    @Composable
    private fun MainContentImpl(
        geoState: GeoState,
        onDetermineCurrentLocation: () -> Unit,
        onSearchLunchPlaces: (String) -> Unit
    ) {
        var isCurrentLocationDisplayed by remember { mutableStateOf(false) }

        var mapContentTopPadding by remember { mutableStateOf(0.dp) }
        MapScreen(
            mapContentTopPadding,
            isCurrentLocationDisplayed,
            geoState,
            onDetermineCurrentLocation
        )

        val onSearchBarBottomYChanged: (Dp) -> Unit = { searchBarBottomY ->
            mapContentTopPadding = searchBarBottomY
        }
        NavGraph(
            onSearchBarBottomYChanged,
            geoState.lunchPlacesStatus,
            onSearchLunchPlaces
        )

        val onAllLocationPermissionsDenied = {
            isCurrentLocationDisplayed = false
            // TODO: display a snackbar about no location access

            onDetermineCurrentLocation()
        }
        val onSomeLocationPermissionGranted = {
            isCurrentLocationDisplayed = true
            // TODO: hide the snackbar about no location access

            onDetermineCurrentLocation()
        }
        LocationPermissionsRequest(
            onAllLocationPermissionsDenied,
            onSomeLocationPermissionGranted
        )
    }

    @Composable
    private fun NavGraph(
        onSearchBarBottomYChanged: (Dp) -> Unit,
        lunchPlacesStatus: Status<String, List<LunchPlace>>?,
        onSearchLunchPlaces: (String) -> Unit,
        navController: NavHostController = rememberNavController()
    ) {
        NavHost(navController, startDestination = SEARCH_ROUTE) {
            searchScreen(onSearchBarBottomYChanged, lunchPlacesStatus, onSearchLunchPlaces)
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
            permissions = listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            onPermissionsResult = onLocationsPermissionsResult
        )

        val isSolelyCoarseLocationPermissionGranted by remember {
            derivedStateOf {
                locationPermissionsState.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                !locationPermissionsState.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
        val isFineLocationPermissionGranted by remember {
            derivedStateOf {
                locationPermissionsState.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
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

    @Preview(showBackground = true)
    @Composable
    private fun MainPreview() {
        AppTheme {
            MainContentImpl(
                geoState = GeoState(),
                onDetermineCurrentLocation = {},
                onSearchLunchPlaces = {}
            )
        }
    }
}