package com.tarasfedyk.lunchplaces.ui.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.ui.SearchScreen
import com.tarasfedyk.lunchplaces.ui.data.MapConfig

const val SEARCH_ROUTE = "search"

fun NavGraphBuilder.searchScreen(
    onGetCurrentRoute: () -> String?,
    onSetMapConfig: (MapConfig) -> Unit,
    lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?,
    onSearchForLunchPlaces: (String) -> Unit,
    onDiscardLunchPlaces: () -> Unit,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    composable(SEARCH_ROUTE) {
        val currentRoute = onGetCurrentRoute()
        val isCurrentDestination = currentRoute == SEARCH_ROUTE

        SearchScreen(
            isCurrentDestination = isCurrentDestination,
            onSetMapConfig = onSetMapConfig,
            lunchPlacesStatus = lunchPlacesStatus,
            onSearchForLunchPlaces = onSearchForLunchPlaces,
            onDiscardLunchPlaces = onDiscardLunchPlaces,
            onNavigateToDetails = onNavigateToDetails,
            onNavigateToSettings = onNavigateToSettings
        )
    }
}