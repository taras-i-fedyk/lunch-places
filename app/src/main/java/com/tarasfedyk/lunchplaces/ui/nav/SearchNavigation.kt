package com.tarasfedyk.lunchplaces.ui.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tarasfedyk.lunchplaces.biz.data.LocationState
import com.tarasfedyk.lunchplaces.ui.SearchScreen

const val SEARCH_ROUTE = "search"

fun NavGraphBuilder.searchScreen(
    locationState: LocationState,
    onDetermineCurrentLocation: () -> Unit
) {
    composable(SEARCH_ROUTE) {
        SearchScreen(
            locationState,
            onDetermineCurrentLocation
        )
    }
}