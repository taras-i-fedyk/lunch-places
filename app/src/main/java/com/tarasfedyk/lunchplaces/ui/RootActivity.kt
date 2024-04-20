package com.tarasfedyk.lunchplaces.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
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
import com.tarasfedyk.lunchplaces.biz.GeoVM
import com.tarasfedyk.lunchplaces.biz.data.GeoState
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.ui.data.LocationAccessLevel
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
        val locationAccessLevel by geoVM.locationAccessLevelFlow.collectAsStateWithLifecycle()
        val geoState by geoVM.geoStateFlow.collectAsStateWithLifecycle()

        val onSetLocationAccessLevel = geoVM::setLocationAccessLevel
        val onDetermineCurrentLocation = geoVM::determineCurrentLocation
        val onSearchLunchPlaces = geoVM::searchLunchPlaces
        val onDiscardLunchPlaces = geoVM::discardLunchPlaces

        RootContentImpl(
            locationAccessLevel = locationAccessLevel,
            onSetLocationAccessLevel = onSetLocationAccessLevel,
            onDetermineCurrentLocation = onDetermineCurrentLocation,
            onSearchLunchPlaces = onSearchLunchPlaces,
            onDiscardLunchPlaces = onDiscardLunchPlaces,
            geoState = geoState
        )
    }

    @Composable
    private fun RootContentImpl(
        locationAccessLevel: LocationAccessLevel,
        onSetLocationAccessLevel: (LocationAccessLevel) -> Unit,
        onDetermineCurrentLocation: () -> Unit,
        onSearchLunchPlaces: (SearchFilter) -> Unit,
        onDiscardLunchPlaces: () -> Unit,
        geoState: GeoState
    ) {
        var mapContentTopPadding by remember { mutableStateOf(0.dp) }
        val isCurrentLocationEnabled = locationAccessLevel != LocationAccessLevel.NONE
        MapScreen(
            mapContentTopPadding,
            isCurrentLocationEnabled,
            onDetermineCurrentLocation,
            geoState.currentLocationStatus
        )

        val onSearchBarBottomYChanged: (Dp) -> Unit = remember {
            { searchBarBottomY ->
                mapContentTopPadding = searchBarBottomY
            }
        }
        NavGraph(
            onSearchBarBottomYChanged,
            onSearchLunchPlaces,
            onDiscardLunchPlaces,
            geoState.lunchPlacesStatus
        )

        val onAllLocationPermissionsDenied = remember(onSetLocationAccessLevel) {
            {
                onSetLocationAccessLevel(LocationAccessLevel.NONE)
            }
        }
        val onSolelyCoarseLocationPermissionGranted = remember(onSetLocationAccessLevel) {
            {
                onSetLocationAccessLevel(LocationAccessLevel.COARSE_ONLY)
            }
        }
        val onFineLocationPermissionGranted = remember(onSetLocationAccessLevel) {
            {
                onSetLocationAccessLevel(LocationAccessLevel.FINE)
            }
        }
        LocationPermissionsTracker(
            onAllLocationPermissionsDenied = onAllLocationPermissionsDenied,
            onSolelyCoarseLocationPermissionGranted = onSolelyCoarseLocationPermissionGranted,
            onFineLocationPermissionGranted = onFineLocationPermissionGranted
        )
    }

    @Composable
    private fun NavGraph(
        onSearchBarBottomYChanged: (Dp) -> Unit,
        onSearchLunchPlaces: (SearchFilter) -> Unit,
        onDiscardLunchPlaces: () -> Unit,
        lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?,
        navController: NavHostController = rememberNavController()
    ) {
        NavHost(
            navController,
            startDestination = SEARCH_ROUTE
        ) {
            searchScreen(
                onSearchBarBottomYChanged,
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
                locationAccessLevel = LocationAccessLevel.NONE,
                onSetLocationAccessLevel = {},
                onDetermineCurrentLocation = {},
                onSearchLunchPlaces = {},
                onDiscardLunchPlaces = {},
                geoState = GeoState()
            )
        }
    }
}