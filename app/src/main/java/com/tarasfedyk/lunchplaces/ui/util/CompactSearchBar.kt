package com.tarasfedyk.lunchplaces.ui.util

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    onInputFieldBottomYChanged: (Dp) -> Unit = {},
    interactionSource: MutableInteractionSource = MutableInteractionSource(),
    content: @Composable ColumnScope.() -> Unit
) {
    val isFocused by interactionSource.collectIsFocusedAsState()

    val placeholder: @Composable () -> Unit = remember(hint) {
        {
            Placeholder(hint)
        }
    }
    val searchIcon: @Composable () -> Unit = remember {
        {
            SearchIcon()
        }
    }
    val upNavIconButton: @Composable () -> Unit = remember(onGoBack) {
        {
            UpNavIconButton(onGoUp = onGoBack)
        }
    }
    val queryClearanceIconButton: @Composable () -> Unit = remember(onClearQuery) {
        {
            QueryClearanceIconButton(onClearQuery)
        }
    }

    SearchBar(
        modifier = modifier,
        shadowElevation = 6.dp,
        placeholder = placeholder,
        leadingIcon = if (isActive) upNavIconButton else searchIcon,
        trailingIcon = if (isFocused) queryClearanceIconButton else null,
        active = isActive,
        onActiveChange = onActivenessChanged,
        interactionSource = interactionSource,
        query = query,
        onQueryChange = onQueryChanged,
        onSearch = onSearch
    ) {
        content()
        BackHandler(enabled = isActive) {
            onGoBack()
        }
    }

    // TODO: replace this with a solution not based on hard-coding any values
    LaunchedEffect(Unit) {
        val inputFieldTopPadding = 8.dp
        val inputFieldHeight = SearchBarDefaults.InputFieldHeight
        val inputFieldBottomY = inputFieldTopPadding + inputFieldHeight
        onInputFieldBottomYChanged(inputFieldBottomY)
    }
}

@Composable
private fun Placeholder(hint: String) {
    Text(hint)
}

@Composable
private fun SearchIcon() {
    Icon(Icons.Default.Search, contentDescription = null)
}

@Composable
private fun UpNavIconButton(onGoUp: () -> Unit) {
    IconButton(onClick = onGoUp) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
    }
}

@Composable
private fun QueryClearanceIconButton(onClearQuery: () -> Unit) {
    IconButton(onClick = onClearQuery) {
        Icon(Icons.Default.Clear, contentDescription = null)
    }
}