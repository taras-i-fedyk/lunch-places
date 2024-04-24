package com.tarasfedyk.lunchplaces.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.tarasfedyk.lunchplaces.biz.GeoVM
import com.tarasfedyk.lunchplaces.biz.data.GeoState
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.biz.data.LocationPermissionsLevel
import com.tarasfedyk.lunchplaces.biz.data.SearchInput
import com.tarasfedyk.lunchplaces.biz.data.isCoarseOrFine
import com.tarasfedyk.lunchplaces.ui.nav.SEARCH_ROUTE
import com.tarasfedyk.lunchplaces.ui.nav.searchScreen
import com.tarasfedyk.lunchplaces.ui.theme.AppTheme
import com.tarasfedyk.lunchplaces.ui.util.LocationPermissionsTracker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RootActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                RootContent()
            }
        }
    }

    @Composable
    private fun RootContent(
        geoVM: GeoVM = hiltViewModel()
    ) {
        val locationPermissionsLevel by geoVM.locationPermissionsLevelFlow.collectAsStateWithLifecycle()
        val geoState by geoVM.geoStateFlow.collectAsStateWithLifecycle()

        val onSetLocationPermissionsLevel = geoVM::setLocationPermissionsLevel
        val onDetermineCurrentLocation = geoVM::determineCurrentLocation
        val onSearchLunchPlaces = geoVM::searchLunchPlaces
        val onDiscardLunchPlaces = geoVM::discardLunchPlaces

        RootContentImpl(
            locationPermissionsLevel = locationPermissionsLevel,
            onSetLocationPermissionsLevel = onSetLocationPermissionsLevel,
            onDetermineCurrentLocation = onDetermineCurrentLocation,
            onSearchLunchPlaces = onSearchLunchPlaces,
            onDiscardLunchPlaces = onDiscardLunchPlaces,
            geoState = geoState
        )
    }

    @Composable
    private fun RootContentImpl(
        locationPermissionsLevel: LocationPermissionsLevel?,
        onSetLocationPermissionsLevel: (LocationPermissionsLevel) -> Unit,
        onDetermineCurrentLocation: () -> Unit,
        onSearchLunchPlaces: (SearchInput) -> Unit,
        onDiscardLunchPlaces: () -> Unit,
        geoState: GeoState
    ) {
        val isCurrentLocationEnabled = locationPermissionsLevel.isCoarseOrFine
        MapScreen(
            isCurrentLocationEnabled,
            onDetermineCurrentLocation,
            geoState.currentLocationStatus
        )

        NavGraph(
            onSearchLunchPlaces,
            onDiscardLunchPlaces,
            geoState.lunchPlacesStatus
        )

        val onAllLocationPermissionsDenied = remember(onSetLocationPermissionsLevel) {
            { onSetLocationPermissionsLevel(LocationPermissionsLevel.NONE) }
        }
        val onSolelyCoarseLocationPermissionGranted = remember(onSetLocationPermissionsLevel) {
            { onSetLocationPermissionsLevel(LocationPermissionsLevel.COARSE_ONLY) }
        }
        val onFineLocationPermissionGranted = remember(onSetLocationPermissionsLevel) {
            { onSetLocationPermissionsLevel(LocationPermissionsLevel.FINE) }
        }
        LocationPermissionsTracker(
            onAllLocationPermissionsDenied = onAllLocationPermissionsDenied,
            onSolelyCoarseLocationPermissionGranted = onSolelyCoarseLocationPermissionGranted,
            onFineLocationPermissionGranted = onFineLocationPermissionGranted
        )
    }

    @Composable
    private fun NavGraph(
        onSearchLunchPlaces: (SearchInput) -> Unit,
        onDiscardLunchPlaces: () -> Unit,
        lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?,
        navController: NavHostController = rememberNavController()
    ) {
        NavHost(
            navController = navController,
            startDestination = SEARCH_ROUTE
        ) {
            searchScreen(
                onSearchLunchPlaces,
                onDiscardLunchPlaces,
                lunchPlacesStatus
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    private fun RootPreview() {
        AppTheme {
            RootContentImpl(
                locationPermissionsLevel = LocationPermissionsLevel.NONE,
                onSetLocationPermissionsLevel = {},
                onDetermineCurrentLocation = {},
                onSearchLunchPlaces = {},
                onDiscardLunchPlaces = {},
                geoState = GeoState()
            )
        }
    }
}