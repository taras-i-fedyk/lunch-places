package com.tarasfedyk.lunchplaces.ui.nav

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tarasfedyk.lunchplaces.biz.data.SearchSettings
import com.tarasfedyk.lunchplaces.ui.SettingsScreen
import com.tarasfedyk.lunchplaces.ui.data.MapConfig

private const val SETTINGS_ROUTE = "settings"

fun NavGraphBuilder.settingsScreen(
    onGetCurrentRoute: () -> String?,
    onSetMapConfig: (MapConfig) -> Unit,
    searchSettings: SearchSettings?,
    onSetSearchSettings: (SearchSettings) -> Unit,
    onNavigateUp: () -> Unit
) {
    composable(SETTINGS_ROUTE) {
        val isCurrentDestination = onGetCurrentRoute() == SETTINGS_ROUTE
        SettingsScreen(
            isCurrentDestination,
            onSetMapConfig,
            searchSettings,
            onSetSearchSettings,
            onNavigateUp
        )
    }
}

fun NavController.navigateToSettings() {
    navigate(SETTINGS_ROUTE)
}