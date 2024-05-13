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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlin.math.roundToInt

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

    var configuredSearchSettings by remember(searchSettings) { mutableStateOf(searchSettings) }
    val onSetConfiguredRankingCriterion: (RankingCriterion) -> Unit = remember {
        { configuredRankingCriterion ->
            configuredSearchSettings = configuredSearchSettings!!.copy(
                rankingCriterion = configuredRankingCriterion
            )
        }
    }
    val onSetConfiguredPreferredRadius: (Float) -> Unit = remember {
        { configuredPreferredRadius ->
            configuredSearchSettings = configuredSearchSettings!!.copy(
                preferredRadius = configuredPreferredRadius
            )
        }
    }

    val onSaveConfiguredSearchSettings: () -> Unit = remember(onSetSearchSettings, onNavigateUp) {
        {
            configuredSearchSettings?.let { onSetSearchSettings(it) }
            onNavigateUp()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_settings_title)) },
                navigationIcon = { ClosureIcon(onClose = onNavigateUp) },
                actions = { SaveButton(onSave = onSaveConfiguredSearchSettings) },
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
            val isForLargeBody = true

            RankingCriterionSelector(
                selectedRankingCriterion = configuredSearchSettings!!.rankingCriterion,
                onSetSelectedRankingCriterion = onSetConfiguredRankingCriterion,
                isForLargeBody
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            PreferredRadiusSelector(
                selectedPreferredRadius = configuredSearchSettings!!.preferredRadius,
                onSetSelectedPreferredRadius = onSetConfiguredPreferredRadius,
                isForLargeBody
            )
        }
    }

    LaunchedEffect(isCurrentDestination, onSetMapConfig) {
        if (!isCurrentDestination) return@LaunchedEffect

        val mapConfig = MapConfig()
        onSetMapConfig(mapConfig)
    }
}

@Composable
fun ClosureIcon(onClose: () -> Unit) {
    IconButton(onClick = onClose) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.closure_icon_description)
        )
    }
}

@Composable
fun SaveButton(onSave: () -> Unit) {
    TextButton(onClick = onSave) {
        Text(stringResource(R.string.save_action_label))
    }
}

@Composable
private fun RankingCriterionSelector(
    selectedRankingCriterion: RankingCriterion,
    onSetSelectedRankingCriterion: (RankingCriterion) -> Unit,
    isForLargeBody: Boolean
) {
    Column {
        Caption(
            CaptionType.RANKING_CRITERION,
            isForLargeBody,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.size(8.dp))

        Column(modifier = Modifier.selectableGroup()) {
            RankingCriterion.entries.forEach { rankingCriterion ->
                val isRankingCriterionSelected = (rankingCriterion == selectedRankingCriterion)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
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
        }
    }
}

@Composable
private fun PreferredRadiusSelector(
    selectedPreferredRadius: Float,
    onSetSelectedPreferredRadius: (Float) -> Unit,
    isForLargeBody: Boolean
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Caption(CaptionType.PREFERRED_RADIUS, isForLargeBody)

        Spacer(modifier = Modifier.size(2.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(
                    R.string.preferred_radius_meters_template,
                    SearchSettings.MIN_PREFERRED_RADIUS.roundToInt()
                ),
                style = bodyTextStyle(isForLargeBody)
            )
            Slider(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1f),
                valueRange = SearchSettings.MIN_PREFERRED_RADIUS ..SearchSettings.MAX_PREFERRED_RADIUS,
                value = selectedPreferredRadius,
                onValueChange = onSetSelectedPreferredRadius
            )
            Text(
                text = stringResource(
                    R.string.preferred_radius_meters_template,
                    SearchSettings.MAX_PREFERRED_RADIUS.roundToInt()
                ),
                style = bodyTextStyle(isForLargeBody)
            )
        }

        Spacer(modifier = Modifier.size(2.dp))

        Text(
            text = stringResource(
                R.string.preferred_radius_meters_template,
                selectedPreferredRadius.roundToInt()
            ),
            style = bodyTextStyle(isForLargeBody)
        )
    }
}

@Composable
private fun Caption(
    captionType: CaptionType,
    isForLargeBody: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(
            when (captionType) {
                CaptionType.RANKING_CRITERION -> R.string.ranking_criterion_caption
                CaptionType.PREFERRED_RADIUS -> R.string.preferred_radius_caption
            }
        ),
        style = bodyTextStyle(isForLargeBody),
        modifier = modifier
    )
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

private enum class CaptionType {
    RANKING_CRITERION,
    PREFERRED_RADIUS
}