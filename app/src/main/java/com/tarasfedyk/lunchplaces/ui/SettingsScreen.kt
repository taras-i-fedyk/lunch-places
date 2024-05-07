package com.tarasfedyk.lunchplaces.ui

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.tarasfedyk.lunchplaces.ui.data.MapConfig

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(
    isCurrentDestination: Boolean,
    onSetMapConfig: (MapConfig) -> Unit,
    onNavigateUp: () -> Unit
) {
    Scaffold(content = {})

    LaunchedEffect(isCurrentDestination, onSetMapConfig) {
        if (!isCurrentDestination) return@LaunchedEffect

        val mapConfig = MapConfig()
        onSetMapConfig(mapConfig)
    }
}