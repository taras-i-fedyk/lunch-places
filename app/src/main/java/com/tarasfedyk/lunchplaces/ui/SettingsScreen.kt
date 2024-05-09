package com.tarasfedyk.lunchplaces.ui

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.tarasfedyk.lunchplaces.R
import com.tarasfedyk.lunchplaces.biz.data.SearchSettings
import com.tarasfedyk.lunchplaces.ui.data.MapConfig
import com.tarasfedyk.lunchplaces.ui.theme.AppTheme
import com.tarasfedyk.lunchplaces.ui.util.UpNavigationIcon

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(
    isCurrentDestination: Boolean,
    onSetMapConfig: (MapConfig) -> Unit,
    searchSettings: SearchSettings?,
    onSetSearchSettings: (SearchSettings) -> Unit,
    onNavigateUp: () -> Unit
) {
    val topBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val onSaveSearchSettings = remember(searchSettings, onSetSearchSettings) {
        {
            // TODO: replace this with meaningful logic
            searchSettings?.let { onSetSearchSettings(it) }
        }
    }
    val currentOnSaveSearchSettings by rememberUpdatedState(onSaveSearchSettings)

    Scaffold(
        modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Title() },
                navigationIcon = { UpNavigationIcon(onNavigateUp) },
                scrollBehavior = topBarScrollBehavior
            )
        },
        content = {}
    )

    LaunchedEffect(isCurrentDestination, onSetMapConfig) {
        if (!isCurrentDestination) return@LaunchedEffect

        val mapConfig = MapConfig()
        onSetMapConfig(mapConfig)
    }

    DisposableEffect(Unit) {
        onDispose {
            currentOnSaveSearchSettings()
        }
    }
}

@Composable
private fun Title() {
    Text(
        text = stringResource(R.string.search_settings_title)
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(
            isCurrentDestination = true,
            onSetMapConfig = {},
            searchSettings = null,
            onSetSearchSettings = {},
            onNavigateUp = {}
        )
    }
}