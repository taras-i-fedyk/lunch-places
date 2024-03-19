package com.tarasfedyk.lunchplaces.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tarasfedyk.lunchplaces.R
import com.tarasfedyk.lunchplaces.biz.data.LocationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onSearchBarBottomYChanged: (Dp) -> Unit,
    locationState: LocationState,
    onDetermineCurrentLocation: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    var isActive by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 6.dp,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            placeholder = { Text(stringResource(R.string.search_hint)) },
            active = isActive,
            onActiveChange = { isActive = it },
            query = query,
            onQueryChange = { query = it },
            content = {},
            onSearch = { focusManager.clearFocus() }
        )
    }

    LaunchedEffect(Unit) {
        // the y coordinate of the bottom edge of the search bar
        // (when the search bar is inactive and is not yet replaced with the search view)
        // TODO: replace this with a solution that is not based on hard-coding any values
        val searchBarBottomY = 64.dp
        onSearchBarBottomYChanged(searchBarBottomY)
    }
}