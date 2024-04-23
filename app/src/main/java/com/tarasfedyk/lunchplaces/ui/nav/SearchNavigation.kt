package com.tarasfedyk.lunchplaces.ui.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
import com.tarasfedyk.lunchplaces.biz.data.SearchInput
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.ui.SearchScreen

const val SEARCH_ROUTE = "search"

fun NavGraphBuilder.searchScreen(
    onSearchLunchPlaces: (SearchInput) -> Unit,
    onDiscardLunchPlaces: () -> Unit,
    lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?
) {
    composable(SEARCH_ROUTE) {
        SearchScreen(
            onSearchLunchPlaces,
            onDiscardLunchPlaces,
            lunchPlacesStatus
        )
    }
}