package com.tarasfedyk.lunchplaces.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tarasfedyk.lunchplaces.R
import com.tarasfedyk.lunchplaces.biz.data.RankingCriterion
import com.tarasfedyk.lunchplaces.biz.data.SearchSettings
import com.tarasfedyk.lunchplaces.ui.data.MapConfig
import com.tarasfedyk.lunchplaces.ui.theme.AppTheme
import com.tarasfedyk.lunchplaces.ui.util.UpNavigationIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isCurrentDestination: Boolean,
    onSetMapConfig: (MapConfig) -> Unit,
    searchSettings: SearchSettings?,
    onSetSearchSettings: (SearchSettings) -> Unit,
    onNavigateUp: () -> Unit
) {
    val topBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val (configuredSearchSettings, onSetConfiguredSearchSettings) = remember(searchSettings) {
        mutableStateOf(searchSettings)
    }
    val onSetConfiguredRankingCriterion: (RankingCriterion) -> Unit = remember(
        configuredSearchSettings, onSetConfiguredSearchSettings
    ) {
        { selectedRankingCriterion ->
            onSetConfiguredSearchSettings(
                configuredSearchSettings?.copy(rankingCriterion = selectedRankingCriterion)
            )
        }
    }

    val onSaveConfiguredSearchSettings = remember(
        configuredSearchSettings, searchSettings, onSetSearchSettings
    ) {
        {
            if (configuredSearchSettings != null && configuredSearchSettings != searchSettings) {
                onSetSearchSettings(configuredSearchSettings)
            }
        }
    }
    val currentOnSaveConfiguredSearchSettings by rememberUpdatedState(onSaveConfiguredSearchSettings)

    Scaffold(
        modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_settings_title)) },
                navigationIcon = { UpNavigationIcon(onNavigateUp) },
                scrollBehavior = topBarScrollBehavior
            )
        }
    ) { paddingValues ->
        if (configuredSearchSettings == null) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(paddingValues)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
        ) {
            RankingCriterionSelector(
                selectedRankingCriterion = configuredSearchSettings.rankingCriterion,
                onSetSelectedRankingCriterion = onSetConfiguredRankingCriterion
            )
        }
    }

    LaunchedEffect(isCurrentDestination, onSetMapConfig) {
        if (!isCurrentDestination) return@LaunchedEffect

        val mapConfig = MapConfig()
        onSetMapConfig(mapConfig)
    }

    DisposableEffect(Unit) {
        onDispose {
            currentOnSaveConfiguredSearchSettings()
        }
    }
}

@Composable
private fun RankingCriterionSelector(
    selectedRankingCriterion: RankingCriterion,
    onSetSelectedRankingCriterion: (RankingCriterion) -> Unit
) {
    val isForLargeBody = true
    Column(modifier = Modifier.selectableGroup()) {
        Text(
            text = stringResource(R.string.ranking_criterion_caption),
            style = bodyTextStyle(isForLargeBody),
            modifier = Modifier.padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.size(12.dp))

        RankingCriterion.entries.forEach { rankingCriterion ->
            val isRankingCriterionSelected = (rankingCriterion == selectedRankingCriterion)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .selectable(
                        role = Role.RadioButton,
                        selected = isRankingCriterionSelected,
                        onClick = { onSetSelectedRankingCriterion(rankingCriterion) }
                    )
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isRankingCriterionSelected,
                    onClick = null
                )
                Text(
                    text = rankingCriterion.displayName,
                    style = bodyTextStyle(isForLargeBody)
                )
            }
        }

        Spacer(modifier = Modifier.size(12.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(
            isCurrentDestination = true,
            onSetMapConfig = {},
            searchSettings = SearchSettings(),
            onSetSearchSettings = {},
            onNavigateUp = {}
        )
    }
}