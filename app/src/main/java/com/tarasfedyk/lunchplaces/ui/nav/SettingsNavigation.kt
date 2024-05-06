package com.tarasfedyk.lunchplaces.ui.nav

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tarasfedyk.lunchplaces.ui.SettingsScreen
import com.tarasfedyk.lunchplaces.ui.data.MapConfig

private const val SETTINGS_ROUTE = "settings"

fun NavGraphBuilder.settingsScreen(
    onSetMapConfig: (MapConfig) -> Unit,
    onNavigateUp: () -> Unit
) {
    composable(SETTINGS_ROUTE) {
        SettingsScreen(onSetMapConfig, onNavigateUp)
    }
}

fun NavController.navigateToSettings() {
    navigate(SETTINGS_ROUTE)
}