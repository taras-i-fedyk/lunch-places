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
import com.tarasfedyk.lunchplaces.biz.LocationViewModel
import com.tarasfedyk.lunchplaces.biz.data.LocationState
import com.tarasfedyk.lunchplaces.ui.nav.SEARCH_ROUTE
import com.tarasfedyk.lunchplaces.ui.nav.searchScreen
import com.tarasfedyk.lunchplaces.ui.theme.AppTheme
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
    fun MainContent(
        locationViewModel: LocationViewModel = hiltViewModel()
    ) {
        var mapPaddingTop by remember { mutableStateOf(0.dp) }
        val onSearchBarBottomYChanged: (Dp) -> Unit = { searchBarBottomY ->
           mapPaddingTop = searchBarBottomY
        }

        val locationState by locationViewModel.locationStateFlow.collectAsStateWithLifecycle()
        val onDetermineCurrentLocation = locationViewModel::determineCurrentLocation

        MapScreen(
            mapPaddingTop,
            locationState,
            onDetermineCurrentLocation
        )
        NavGraph(
            onSearchBarBottomYChanged,
            locationState,
            onDetermineCurrentLocation
        )
    }

    @Composable
    fun NavGraph(
        onSearchBarBottomYChanged: (Dp) -> Unit,
        locationState: LocationState,
        onDetermineCurrentLocation: () -> Unit,
        navController: NavHostController = rememberNavController()
    ) {
        NavHost(
            navController,
            startDestination = SEARCH_ROUTE
        ) {
            searchScreen(
                onSearchBarBottomYChanged,
                locationState,
                onDetermineCurrentLocation
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun MainPreview() {
        AppTheme {
            MainContent()
        }
    }
}