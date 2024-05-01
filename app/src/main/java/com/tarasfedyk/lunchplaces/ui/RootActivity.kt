package com.tarasfedyk.lunchplaces.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.tarasfedyk.lunchplaces.biz.GeoVM
import com.tarasfedyk.lunchplaces.biz.data.GeoState
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.biz.data.LocationPermissionsLevel
import com.tarasfedyk.lunchplaces.biz.data.SearchInput
import com.tarasfedyk.lunchplaces.ui.nav.SEARCH_ROUTE
import com.tarasfedyk.lunchplaces.ui.nav.detailsScreen
import com.tarasfedyk.lunchplaces.ui.nav.navigateToDetails
import com.tarasfedyk.lunchplaces.ui.nav.navigateToProximity
import com.tarasfedyk.lunchplaces.ui.nav.proximityScreen
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

        val onSetLocationPermissionsLevel = remember(geoVM) { geoVM::setLocationPermissionsLevel }
        val onDetermineCurrentLocation = remember(geoVM) { geoVM::determineCurrentLocation }
        val onSearchLunchPlaces = remember(geoVM) { geoVM::searchLunchPlaces }
        val onDiscardLunchPlaces = remember(geoVM) { geoVM::discardLunchPlaces }

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
        var isMapVisible by rememberSaveable { mutableStateOf(true)}
        val onSetMapVisibility: (Boolean) -> Unit = remember { { isMapVisible = it } }
        Box(modifier = Modifier.fillMaxSize()) {
            MapScreen(
                isMapVisible,
                locationPermissionsLevel,
                onDetermineCurrentLocation,
                currentLocationStatus = geoState.currentLocationStatus
            )
            NavGraph(
                onSetMapVisibility,
                onSearchLunchPlaces,
                onDiscardLunchPlaces,
                lunchPlacesStatus = geoState.lunchPlacesStatus
            )
        }

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
        onSetMapVisibility: (Boolean) -> Unit,
        onSearchLunchPlaces: (SearchInput) -> Unit,
        onDiscardLunchPlaces: () -> Unit,
        lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?
    ) {
        val navController = rememberNavController()
        val onNavigateUp: () -> Unit = remember { { navController.navigateUp() } }
        val onNavigateToDetails = remember { navController::navigateToDetails }
        val onNavigateToProximity = remember { navController::navigateToProximity }

        NavHost(navController, startDestination = SEARCH_ROUTE) {
            searchScreen(
                onSetMapVisibility,
                onSearchLunchPlaces,
                onDiscardLunchPlaces,
                lunchPlacesStatus,
                onNavigateToDetails
            )
            detailsScreen(lunchPlacesStatus, onNavigateUp, onNavigateToProximity)
            proximityScreen(onSetMapVisibility, lunchPlacesStatus, onNavigateUp)
        }
    }

    @Preview(showBackground = true)
    @Composable
    private fun RootContentPreview() {
        AppTheme {
            RootContentImpl(
                locationPermissionsLevel = LocationPermissionsLevel.FINE,
                onSetLocationPermissionsLevel = {},
                onDetermineCurrentLocation = {},
                onSearchLunchPlaces = {},
                onDiscardLunchPlaces = {},
                geoState = GeoState()
            )
        }
    }
}