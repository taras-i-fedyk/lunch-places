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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactSearchBar(
    hint: @Composable () -> Unit,
    isActive: Boolean,
    onActivenessChanged: (Boolean) -> Unit,
    query: String,
    onQueryChanged: (String) -> Unit,
    onGoBack: () -> Unit,
    onClear: () -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    onInputFieldBottomYChanged: (Dp) -> Unit = {},
    interactionSource: MutableInteractionSource = MutableInteractionSource(),
    content: @Composable ColumnScope.() -> Unit
) {
    val isFocused by interactionSource.collectIsFocusedAsState()

    val searchIcon: @Composable () -> Unit = {
        SearchIcon()
    }
    val upNavIconButton: @Composable () -> Unit = {
        UpNavIconButton(onGoUp = onGoBack)
    }
    val clearanceIconButton: @Composable () -> Unit = {
        ClearanceIconButton(onClear)
    }

    // TODO: adjust the horizontal padding in a smooth way
    SearchBar(
        modifier = modifier,
        shadowElevation = 6.dp,
        placeholder = hint,
        leadingIcon = if (isActive) upNavIconButton else searchIcon,
        trailingIcon = if (isFocused) clearanceIconButton else null,
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
private fun ClearanceIconButton(onClear: () -> Unit) {
    IconButton(onClick = onClear) {
        Icon(Icons.Default.Clear, contentDescription = null)
    }
}