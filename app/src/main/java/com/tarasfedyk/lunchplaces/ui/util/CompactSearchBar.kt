package com.tarasfedyk.lunchplaces.ui.util

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tarasfedyk.lunchplaces.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactSearchBar(
    isActive: Boolean,
    onActivenessChanged: (Boolean) -> Unit,
    hint: String,
    query: String,
    onQueryChanged: (String) -> Unit,
    onClearQuery: () -> Unit,
    onGoBack: () -> Unit,
    onTrySearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = MutableInteractionSource(),
    content: @Composable ColumnScope.() -> Unit
) {
    val isFocused by interactionSource.collectIsFocusedAsState()

    SearchBar(
        modifier = modifier,
        shadowElevation = 6.dp,
        placeholder = { Placeholder(hint) },
        leadingIcon = {
            if (isActive) {
                UpNavigationButton(onGoUp = onGoBack)
            } else {
                SearchIcon()
            }
        },
        trailingIcon = {
            if (isFocused && query.isNotEmpty()) {
                QueryClearanceButton(onClearQuery)
            }
        },
        active = isActive,
        onActiveChange = onActivenessChanged,
        interactionSource = interactionSource,
        query = query,
        onQueryChange = onQueryChanged,
        onSearch = onTrySearch
    ) {
        content()
        BackHandler(enabled = isActive) {
            onGoBack()
        }
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
private fun UpNavigationButton(onGoUp: () -> Unit) {
    IconButton(onClick = onGoUp) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = stringResource(R.string.up_navigation_button_description)
        )
    }
}

@Composable
private fun QueryClearanceButton(onClearQuery: () -> Unit) {
    IconButton(onClick = onClearQuery) {
        Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = stringResource(R.string.query_clearance_button_description)
        )
    }
}