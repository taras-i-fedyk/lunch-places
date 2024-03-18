package com.tarasfedyk.lunchplaces.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.tarasfedyk.lunchplaces.biz.LocationViewModel
import com.tarasfedyk.lunchplaces.biz.data.LocationState
import com.tarasfedyk.lunchplaces.ui.nav.SEARCH_ROUTE
import com.tarasfedyk.lunchplaces.ui.nav.searchScreen
import com.tarasfedyk.lunchplaces.ui.theme.LunchPlacesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LunchPlacesTheme {
                MainContent()
            }
        }
    }

    @Composable
    fun MainContent(
        locationViewModel: LocationViewModel = hiltViewModel()
    ) {
        val locationState by locationViewModel.locationStateFlow.collectAsStateWithLifecycle()
        val onDetermineCurrentLocation = locationViewModel::determineCurrentLocation
        MapScreen(locationState, onDetermineCurrentLocation)
        NavGraph(locationState, onDetermineCurrentLocation)
    }

    @Composable
    private fun NavGraph(
        locationState: LocationState,
        onDetermineCurrentLocation: () -> Unit,
        navController: NavHostController = rememberNavController()
    ) {
        NavHost(navController, startDestination = SEARCH_ROUTE) {
            searchScreen(locationState, onDetermineCurrentLocation)
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun MainPreview() {
        LunchPlacesTheme {
            MainContent()
        }
    }
}