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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.tarasfedyk.lunchplaces.biz.GeoVM
import com.tarasfedyk.lunchplaces.biz.data.GeoState
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.biz.data.LocationPermissionLevel
import com.tarasfedyk.lunchplaces.biz.data.SearchSettings
import com.tarasfedyk.lunchplaces.biz.data.isCoarseOrFine
import com.tarasfedyk.lunchplaces.ui.data.MapConfig
import com.tarasfedyk.lunchplaces.ui.nav.SEARCH_ROUTE
import com.tarasfedyk.lunchplaces.ui.nav.detailsScreen
import com.tarasfedyk.lunchplaces.ui.nav.navigateToDetails
import com.tarasfedyk.lunchplaces.ui.nav.navigateToProximity
import com.tarasfedyk.lunchplaces.ui.nav.navigateToSettings
import com.tarasfedyk.lunchplaces.ui.nav.proximityScreen
import com.tarasfedyk.lunchplaces.ui.nav.searchScreen
import com.tarasfedyk.lunchplaces.ui.nav.settingsScreen
import com.tarasfedyk.lunchplaces.ui.theme.AppTheme
import com.tarasfedyk.lunchplaces.ui.util.LocationPermissionsTracker
import com.tarasfedyk.lunchplaces.ui.util.isGooglePlayServicesAvailable
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
        val context = LocalContext.current

        var isGooglePlayServicesAvailable by remember { mutableStateOf(false) }

        val locationPermissionLevel by geoVM.locationPermissionLevelFlow.collectAsStateWithLifecycle()
        val isNoLocationPermissionGranted = !locationPermissionLevel.isCoarseOrFine
        val onSetLocationPermissionLevel = remember(geoVM) { geoVM::setLocationPermissionLevel }

        val searchSettings by geoVM.searchSettingsFlow.collectAsStateWithLifecycle()
        val onSetSearchSettings = remember(geoVM) { geoVM::setSearchSettings }

        val geoState by geoVM.geoStateFlow.collectAsStateWithLifecycle()
        val onDetermineCurrentLocation = remember(geoVM) { geoVM::determineCurrentLocation }
        val onSearchForLunchPlaces = remember(geoVM) { geoVM::searchForLunchPlaces }
        val onDiscardLunchPlaces = remember(geoVM) { geoVM::discardLunchPlaces }

        RootContentImpl(
            isGooglePlayServicesAvailable= isGooglePlayServicesAvailable,
            isNoLocationPermissionGranted = isNoLocationPermissionGranted,
            onSetLocationPermissionLevel = onSetLocationPermissionLevel,
            searchSettings = searchSettings,
            onSetSearchSettings = onSetSearchSettings,
            geoState = geoState,
            onDetermineCurrentLocation = onDetermineCurrentLocation,
            onSearchForLunchPlaces = onSearchForLunchPlaces,
            onDiscardLunchPlaces = onDiscardLunchPlaces
        )

        LifecycleEventEffect(Lifecycle.Event.ON_START) {
            isGooglePlayServicesAvailable = context.isGooglePlayServicesAvailable()
        }
    }

    @Composable
    private fun RootContentImpl(
        isGooglePlayServicesAvailable: Boolean,
        isNoLocationPermissionGranted: Boolean,
        onSetLocationPermissionLevel: (LocationPermissionLevel) -> Unit,
        searchSettings: SearchSettings?,
        onSetSearchSettings: (SearchSettings) -> Unit,
        geoState: GeoState,
        onDetermineCurrentLocation: () -> Unit,
        onSearchForLunchPlaces: (String) -> Unit,
        onDiscardLunchPlaces: () -> Unit
    ) {
        var mapConfig by rememberSaveable { mutableStateOf(MapConfig()) }
        val onSetMapConfig: (MapConfig) -> Unit = remember { { mapConfig = it } }
        Box(modifier = Modifier.fillMaxSize()) {
            if (isGooglePlayServicesAvailable) {
                MapScreen(
                    mapConfig = mapConfig,
                    isNoLocationPermissionGranted = isNoLocationPermissionGranted,
                    currentLocationStatus = geoState.currentLocationStatus,
                    onDetermineCurrentLocation = onDetermineCurrentLocation
                )
            }
            NavGraph(
                onSetMapConfig = onSetMapConfig,
                searchSettings = searchSettings,
                onSetSearchSettings = onSetSearchSettings,
                lunchPlacesStatus = geoState.lunchPlacesStatus,
                onSearchForLunchPlaces = onSearchForLunchPlaces,
                onDiscardLunchPlaces = onDiscardLunchPlaces
            )
        }

        val onNoLocationPermissionGranted = remember(onSetLocationPermissionLevel) {
            { onSetLocationPermissionLevel(LocationPermissionLevel.NONE) }
        }
        val onSolelyCoarseLocationPermissionGranted = remember(onSetLocationPermissionLevel) {
            { onSetLocationPermissionLevel(LocationPermissionLevel.COARSE_ONLY) }
        }
        val onFineLocationPermissionGranted = remember(onSetLocationPermissionLevel) {
            { onSetLocationPermissionLevel(LocationPermissionLevel.FINE) }
        }
        LocationPermissionsTracker(
            onNoLocationPermissionGranted = onNoLocationPermissionGranted,
            onSolelyCoarseLocationPermissionGranted = onSolelyCoarseLocationPermissionGranted,
            onFineLocationPermissionGranted = onFineLocationPermissionGranted
        )
    }

    @Composable
    private fun NavGraph(
        onSetMapConfig: (MapConfig) -> Unit,
        searchSettings: SearchSettings?,
        onSetSearchSettings: (SearchSettings) -> Unit,
        lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?,
        onSearchForLunchPlaces: (String) -> Unit,
        onDiscardLunchPlaces: () -> Unit
    ) {
        val navController = rememberNavController()

        val onGetCurrentRoute: () -> String? = remember {
            {
                navController.currentDestination?.route
            }
        }

        val onNavigateUp: () -> Unit = remember { { navController.navigateUp() } }
        val onNavigateToSettings = remember { navController::navigateToSettings }
        val onNavigateToDetails = remember { navController::navigateToDetails }
        val onNavigateToProximity = remember { navController::navigateToProximity }

        NavHost(navController, startDestination = SEARCH_ROUTE) {
            searchScreen(
                onGetCurrentRoute = onGetCurrentRoute,
                onSetMapConfig = onSetMapConfig,
                lunchPlacesStatus = lunchPlacesStatus,
                onSearchForLunchPlaces = onSearchForLunchPlaces,
                onDiscardLunchPlaces = onDiscardLunchPlaces,
                onNavigateToDetails = onNavigateToDetails,
                onNavigateToSettings = onNavigateToSettings
            )
            settingsScreen(
                onGetCurrentRoute,
                onSetMapConfig,
                searchSettings,
                onSetSearchSettings,
                onNavigateUp
            )
            detailsScreen(
                onGetCurrentRoute,
                onSetMapConfig,
                lunchPlacesStatus,
                onNavigateUp,
                onNavigateToProximity
            )
            proximityScreen(onGetCurrentRoute, onSetMapConfig, lunchPlacesStatus, onNavigateUp)
        }
    }

    @Preview(showBackground = true)
    @Composable
    private fun RootContentPreview() {
        AppTheme {
            RootContentImpl(
                isGooglePlayServicesAvailable = true,
                isNoLocationPermissionGranted = false,
                onSetLocationPermissionLevel = {},
                searchSettings = null,
                onSetSearchSettings = {},
                geoState = GeoState(),
                onDetermineCurrentLocation = {},
                onSearchForLunchPlaces = {},
                onDiscardLunchPlaces = {}
            )
        }
    }
}