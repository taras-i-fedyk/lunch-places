package com.tarasfedyk.lunchplaces.ui.util

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.currentStateAsState
import com.tarasfedyk.lunchplaces.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactSearchBar(
    isActive: Boolean,
    onActivenessChanged: (Boolean) -> Unit,
    interactionSource: MutableInteractionSource,
    hint: String,
    query: String,
    onQueryChanged: (String) -> Unit,
    onClearQuery: () -> Unit,
    onNavigateBack: () -> Unit,
    onTrySearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    tonalElevation: Dp = SearchBarDefaults.TonalElevation,
    shadowElevation: Dp = SearchBarDefaults.ShadowElevation,
    searchStatusContent: @Composable ColumnScope.() -> Unit
) {
    val isFocused by interactionSource.collectIsFocusedAsState()

    SearchBar(
        modifier = modifier,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        placeholder = { Placeholder(hint) },
        leadingIcon = {
            if (isActive) {
                UpNavigationIcon(onNavigateUp = onNavigateBack)
            } else {
                SearchIcon()
            }
        },
        trailingIcon = {
            if (isFocused && query.isNotEmpty()) {
                QueryClearanceIcon(onClearQuery)
            }
        },
        active = isActive,
        onActiveChange = onActivenessChanged,
        interactionSource = interactionSource,
        query = query,
        onQueryChange = onQueryChanged,
        onSearch = onTrySearch
    ) {
        if (!isFocused) {
            searchStatusContent()
        }
        ResumableBackHandler(isEnabled = isActive, onNavigateBack)
    }
}

@Composable
private fun Placeholder(hint: String) {
    Text(hint)
}

@Composable
private fun SearchIcon() {
    Icon(
        imageVector = Icons.Default.Search,
        contentDescription = stringResource(R.string.search_icon_description)
    )
}

@Composable
private fun QueryClearanceIcon(onClearQuery: () -> Unit) {
    IconButton(onClick = onClearQuery) {
        Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = stringResource(R.string.query_clearance_icon_description)
        )
    }
}

@Composable
private fun ResumableBackHandler(isEnabled: Boolean, onNavigateBack: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentLifecycleState = lifecycleOwner.lifecycle.currentStateAsState()
    if (currentLifecycleState.value.isAtLeast(Lifecycle.State.RESUMED)) {
        BackHandler(isEnabled) {
            onNavigateBack()
        }
    }
}