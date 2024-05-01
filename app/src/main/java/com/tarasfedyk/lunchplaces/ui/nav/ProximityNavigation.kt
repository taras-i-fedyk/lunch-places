package com.tarasfedyk.lunchplaces.ui.nav

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.ui.ProximityScreen

private const val LUNCH_PLACE_INDEX_KEY: String = "lunchPlaceIndex"
private const val INVALID_LUNCH_PLACE_INDEX: Int = -1
private const val PROXIMITY_ROUTE_BASIS = "proximity/"
private const val PROXIMITY_ROUTE = "$PROXIMITY_ROUTE_BASIS{$LUNCH_PLACE_INDEX_KEY}"

fun NavGraphBuilder.proximityScreen(
    onSetMapVisible: (Boolean) -> Unit,
    lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?,
    onNavigateUp: () -> Unit
) {
    composable(
        route = PROXIMITY_ROUTE,
        arguments = listOf(
            navArgument(name = LUNCH_PLACE_INDEX_KEY) { type = NavType.IntType }
        )
    ) { backStackEntry ->
        val lunchPlaceIndex = backStackEntry.arguments?.getInt(
            LUNCH_PLACE_INDEX_KEY, INVALID_LUNCH_PLACE_INDEX
        )
        if (lunchPlaceIndex == null || lunchPlaceIndex == INVALID_LUNCH_PLACE_INDEX) {
            error("The $LUNCH_PLACE_INDEX_KEY is not provided!")
        }

        val lunchPlaces =
            (lunchPlacesStatus as? Status.Success)?.result ?:
            error("The $LUNCH_PLACE_INDEX_KEY is not applicable!")

        val lunchPlace =
            lunchPlaces.getOrNull(lunchPlaceIndex) ?:
            error("The $LUNCH_PLACE_INDEX_KEY is out of bounds!")

        ProximityScreen(onSetMapVisible, lunchPlace, onNavigateUp)
    }
}

fun NavController.navigateToProximity(lunchPlaceIndex: Int) {
    navigate(route = "$PROXIMITY_ROUTE_BASIS$lunchPlaceIndex")
}