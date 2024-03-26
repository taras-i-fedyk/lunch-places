package com.tarasfedyk.lunchplaces.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
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
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.Status

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onSearchBarBottomYChanged: (Dp) -> Unit,
    lunchPlacesStatus: Status<String, List<LunchPlace>>?,
    onSearchLunchPlaces: (String) -> Unit
) {
    var isActive by rememberSaveable { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

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
            onSearchLunchPlaces(sentQuery)
        } else {
            onGoBack()
        }
    }

    val searchIcon: @Composable () -> Unit = {
        SearchIcon()
    }
    val upNavIconButton: @Composable () -> Unit = {
        UpNavIconButton {
            onGoBack()
        }
    }
    val clearIconButton: @Composable () -> Unit = {
        ClearIconButton {
            currentQuery = ""
        }
    }

    // TODO: adjust the horizontal padding in a smooth way
    SearchBar(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 6.dp,
        placeholder = { SearchHint() },
        leadingIcon = if (isActive) upNavIconButton else searchIcon,
        trailingIcon = if (isFocused) clearIconButton else null,
        active = isActive,
        onActiveChange = { isActive = it },
        interactionSource = interactionSource,
        query = currentQuery,
        onQueryChange = { currentQuery = it },
        onSearch = onSearch
    ) {
        if (!isFocused) {
            SearchStatus(lunchPlacesStatus)
        }
        BackHandler(enabled = isActive) {
            onGoBack()
        }
    }

    // TODO: replace this with a solution not based on hard-coding any values
    LaunchedEffect(Unit) {
        val searchBarTopPadding = 8.dp
        val searchBarHeight = SearchBarDefaults.InputFieldHeight
        val searchBarBottomY = searchBarTopPadding + searchBarHeight
        onSearchBarBottomYChanged(searchBarBottomY)
    }
}

@Composable
private fun SearchHint() {
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

@Composable
private fun SearchStatus(
    lunchPlacesStatus: Status<String, List<LunchPlace>>?
) {
    when (lunchPlacesStatus) {
        null -> {}
        is Status.Pending -> {
            // TODO: display a progress bar
        }
        is Status.Success -> {
            val lunchPlaces = lunchPlacesStatus.result
            SearchResult(lunchPlaces)
        }
        is Status.Failure -> {
            // TODO: display a snackbar about the search error
        }
    }
}

@Composable
private fun SearchResult(
    lunchPlaces: List<LunchPlace>
) {
    LazyColumn(
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(count = lunchPlaces.size) { i ->
            Text(
                text = lunchPlaces[i].id,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchPreview() {
    SearchScreen(
        onSearchBarBottomYChanged = {},
        lunchPlacesStatus = null,
        onSearchLunchPlaces = {}
    )
}