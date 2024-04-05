package com.tarasfedyk.lunchplaces.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
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
import com.tarasfedyk.lunchplaces.biz.GeoVM
import com.tarasfedyk.lunchplaces.biz.data.GeoState
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
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
        geoVM: GeoVM = hiltViewModel()
    ) {
        val geoState by geoVM.geoStateFlow.collectAsStateWithLifecycle()
        val onDetermineCurrentLocation = geoVM::determineCurrentLocation
        val onSearchLunchPlaces = geoVM::searchLunchPlaces
        val onRefreshLunchPlaces = geoVM::refreshLunchPlaces
        val onDiscardLunchPlaces = geoVM::discardLunchPlaces
        MainContentImpl(
            geoState,
            onDetermineCurrentLocation,
            onSearchLunchPlaces,
            onRefreshLunchPlaces,
            onDiscardLunchPlaces
        )
    }

    @Composable
    private fun MainContentImpl(
        geoState: GeoState,
        onDetermineCurrentLocation: () -> Unit,
        onSearchLunchPlaces: (SearchFilter) -> Unit,
        onRefreshLunchPlaces: () -> Unit,
        onDiscardLunchPlaces: () -> Unit
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
            onSearchLunchPlaces,
            onDiscardLunchPlaces
        )

        val onAllLocationPermissionsDenied = {
            isCurrentLocationDisplayed = false
            onDetermineCurrentLocation()
            onRefreshLunchPlaces()
            // TODO: display a snackbar about no location access
        }
        val onSomeLocationPermissionGranted = {
            isCurrentLocationDisplayed = true
            onDetermineCurrentLocation()
            onRefreshLunchPlaces()
            // TODO: hide the snackbar about no location access
        }
        LocationPermissionsRequest(
            onAllLocationPermissionsDenied,
            onSomeLocationPermissionGranted
        )
    }

    @Composable
    private fun NavGraph(
        onSearchBarBottomYChanged: (Dp) -> Unit,
        lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?,
        onSearchLunchPlaces: (SearchFilter) -> Unit,
        onDiscardLunchPlaces: () -> Unit,
        navController: NavHostController = rememberNavController()
    ) {
        NavHost(
            navController,
            startDestination = SEARCH_ROUTE
        ) {
            searchScreen(
                onSearchBarBottomYChanged,
                lunchPlacesStatus,
                onSearchLunchPlaces,
                onDiscardLunchPlaces
            )
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun LocationPermissionsRequest(
        onAllLocationPermissionsDenied: () -> Unit,
        onSomeLocationPermissionGranted: () -> Unit
    ) {
        val onLocationsPermissionsResult: (Map<String, Boolean>) -> Unit =
            { locationPermissionFlags ->
                if (locationPermissionFlags.areAllValuesFalse()) {
                    onAllLocationPermissionsDenied()
                }
            }
        // TODO: replace this with a direct call in a Preview-friendly way
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

    @Preview(showBackground = true)
    @Composable
    private fun MainPreview() {
        AppTheme {
            MainContentImpl(
                geoState = GeoState(),
                onDetermineCurrentLocation = {},
                onSearchLunchPlaces = {},
                onRefreshLunchPlaces = {},
                onDiscardLunchPlaces = {}
            )
        }
    }
}