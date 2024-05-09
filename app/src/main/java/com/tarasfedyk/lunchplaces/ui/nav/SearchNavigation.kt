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
    onSearchLunchPlaces: (String) -> Unit,
    onDiscardLunchPlaces: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDetails: (Int) -> Unit
) {
    composable(SEARCH_ROUTE) {
        val isCurrentDestination = onGetCurrentRoute() == SEARCH_ROUTE
        SearchScreen(
            isCurrentDestination = isCurrentDestination,
            onSetMapConfig = onSetMapConfig,
            lunchPlacesStatus = lunchPlacesStatus,
            onSearchLunchPlaces = onSearchLunchPlaces,
            onDiscardLunchPlaces = onDiscardLunchPlaces,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToDetails = onNavigateToDetails
        )
    }
}