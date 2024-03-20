package com.tarasfedyk.lunchplaces.ui.nav

import androidx.compose.ui.unit.Dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tarasfedyk.lunchplaces.biz.data.LocationState
import com.tarasfedyk.lunchplaces.ui.SearchScreen

const val SEARCH_ROUTE = "search"

fun NavGraphBuilder.searchScreen(
    onSearchBarBottomYChanged: (Dp) -> Unit,
    locationState: LocationState,
    onDetermineCurrentLocation: () -> Unit,
    onNavigateUp: () -> Unit
) {
    composable(SEARCH_ROUTE) {
        SearchScreen(
            onSearchBarBottomYChanged,
            locationState,
            onDetermineCurrentLocation,
            onNavigateUp
        )
    }
}