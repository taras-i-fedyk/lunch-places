package com.tarasfedyk.lunchplaces.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tarasfedyk.lunchplaces.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onSearchBarBottomYChanged: (Dp) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current

    var isActive by rememberSaveable { mutableStateOf(false) }
    var currentQuery by rememberSaveable { mutableStateOf("") }
    var sentQuery by rememberSaveable { mutableStateOf("") }

    val onGoBack = {
        if (isFocused && sentQuery.isNotEmpty()) {
            currentQuery = sentQuery
            focusManager.clearFocus()
        } else {
            sentQuery = ""
            currentQuery = ""
            isActive = false
        }
    }
    val onSearch = { _: String ->
        sentQuery = currentQuery
        if (sentQuery.isNotEmpty()) {
            focusManager.clearFocus()
        } else {
            isActive = false
        }
    }

    val searchIcon: @Composable () -> Unit = {
        SearchIcon()
    }
    val clearIconButton: @Composable () -> Unit = {
        ClearIconButton {
            currentQuery = ""
        }
    }
    val upNavIconButton: @Composable () -> Unit = {
        UpNavIconButton {
            onGoBack()
        }
    }

    SearchBar(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 6.dp,
        placeholder = { Hint() },
        leadingIcon = if (isActive) upNavIconButton else searchIcon,
        trailingIcon = if (isActive) clearIconButton else null,
        interactionSource = interactionSource,
        active = isActive,
        onActiveChange = { isActive = it },
        query = currentQuery,
        onQueryChange = { currentQuery = it },
        onSearch = onSearch
    ) {
        BackHandler(enabled = isActive) {
            onGoBack()
        }
    }

    LaunchedEffect(Unit) {
        // TODO: replace this with a solution not based on hard-coding any values
        val searchBarTopPadding = 8.dp
        val searchBarHeight = SearchBarDefaults.InputFieldHeight
        val searchBarBottomY = searchBarTopPadding + searchBarHeight
        onSearchBarBottomYChanged(searchBarBottomY)
    }
}

@Composable
private fun Hint() {
    Text(stringResource(R.string.search_hint))
}

@Composable
private fun SearchIcon() {
    Icon(Icons.Default.Search, contentDescription = null)
}

@Composable
private fun UpNavIconButton(onClicked: () -> Unit) {
    IconButton(onClick = onClicked) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
    }
}

@Composable
private fun ClearIconButton(onClicked: () -> Unit) {
    IconButton(onClick = onClicked) {
        Icon(Icons.Default.Clear, contentDescription = null)
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchPreview() {
    SearchScreen(
        onSearchBarBottomYChanged = {}
    )
}