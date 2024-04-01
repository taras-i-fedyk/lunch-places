package com.tarasfedyk.lunchplaces.ui.nav

import androidx.compose.ui.unit.Dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tarasfedyk.lunchplaces.logic.model.LunchPlace
import com.tarasfedyk.lunchplaces.logic.model.Status
import com.tarasfedyk.lunchplaces.ui.SearchScreen

const val SEARCH_ROUTE = "search"

fun NavGraphBuilder.searchScreen(
    onSearchBarBottomYChanged: (Dp) -> Unit,
    lunchPlacesStatus: Status<String, List<LunchPlace>>?,
    onSearchLunchPlaces: (String) -> Unit,
    onDiscardLunchPlaces: () -> Unit
) {
    composable(SEARCH_ROUTE) {
        SearchScreen(
            onSearchBarBottomYChanged,
            lunchPlacesStatus,
            onSearchLunchPlaces,
            onDiscardLunchPlaces
        )
    }
}