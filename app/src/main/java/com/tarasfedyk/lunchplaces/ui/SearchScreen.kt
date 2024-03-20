package com.tarasfedyk.lunchplaces.ui

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

    val searchIcon: @Composable () -> Unit = {
        SearchIcon()
    }
    val upIcon: @Composable () -> Unit = {
        UpIcon {
            query = ""
            isActive = false
        }
    }
    val clearIcon: @Composable () -> Unit = {
        ClearIcon {
            query = ""
        }
    }

    SearchBar(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 6.dp,
        placeholder = { Hint() },
        leadingIcon = if (isActive) upIcon else searchIcon,
        trailingIcon = if (isActive) clearIcon else null,
        active = isActive,
        onActiveChange = { isActive = it },
        query = query,
        onQueryChange = { query = it },
        content = {},
        onSearch = {
            if (query.isEmpty())
                isActive = false
            else
                focusManager.clearFocus()
        }
    )

    LaunchedEffect(Unit) {
        // TODO: replace this with a solution that is not based on hard-coding any values
        val searchBarPaddingTop = 8.dp
        val searchBarHeight = SearchBarDefaults.InputFieldHeight
        val searchBarBottomY = searchBarPaddingTop + searchBarHeight
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
private fun UpIcon(onClicked: () -> Unit) {
    IconButton(onClick = onClicked) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
    }
}

@Composable
private fun ClearIcon(onClicked: () -> Unit) {
    IconButton(onClick = onClicked) {
        Icon(Icons.Default.Clear, contentDescription = null)
    }
}