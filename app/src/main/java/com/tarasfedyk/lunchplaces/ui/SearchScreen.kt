package com.tarasfedyk.lunchplaces.ui

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
import com.tarasfedyk.lunchplaces.biz.data.Status

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onSearchBarBottomYChanged: (Dp) -> Unit,
    lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?,
    onSearchLunchPlaces: (SearchFilter) -> Unit,
    onDiscardLunchPlaces: () -> Unit
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
            onDiscardLunchPlaces()
        }
    }
    val onClear = { currentQuery = "" }
    val onSearch = { _: String ->
        sentQuery = currentQuery
        if (sentQuery.isNotEmpty()) {
            focusManager.clearFocus()
            onSearchLunchPlaces(SearchFilter(sentQuery))
        } else {
            onGoBack()
        }
    }
    val onRetrySearch = { onSearch(sentQuery) }

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
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 6.dp,
        placeholder = { SearchHint() },
        leadingIcon = if (isActive) upNavIconButton else searchIcon,
        trailingIcon = if (isFocused) clearanceIconButton else null,
        active = isActive,
        onActiveChange = { isActive = it },
        interactionSource = interactionSource,
        query = currentQuery,
        onQueryChange = { currentQuery = it },
        onSearch = onSearch
    ) {
        if (!isFocused) {
            SearchStatus(lunchPlacesStatus, onRetrySearch)
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

@Composable
private fun SearchStatus(
    lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?,
    onRetrySearch: () -> Unit
) {
    when (lunchPlacesStatus) {
        null -> {}
        is Status.Pending -> SearchProgress()
        is Status.Success -> SearchResult(lunchPlaces = lunchPlacesStatus.result)
        is Status.Failure -> SearchError(onRetrySearch)
    }
}

@Composable
private fun SearchProgress() {
    LinearProgressIndicator(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
    )
}

@Composable
private fun SearchResult(lunchPlaces: List<LunchPlace>) {
    LazyColumn(
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(count = lunchPlaces.size) { i ->
            Text(
                text = lunchPlaces[i].name,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun SearchError(onRetrySearch: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }

    val message = stringResource(R.string.search_error_message)
    val retryActionLabel = stringResource(R.string.search_error_retry_action_label)

    Scaffold(
        content = {},
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    )

    LaunchedEffect(snackbarHostState) {
        val result = snackbarHostState.showSnackbar(
            message,
            retryActionLabel,
            duration = SnackbarDuration.Indefinite
        )
        when (result) {
            SnackbarResult.ActionPerformed -> onRetrySearch()
            SnackbarResult.Dismissed -> {}
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchPreview() {
    SearchScreen(
        onSearchBarBottomYChanged = {},
        lunchPlacesStatus = null,
        onSearchLunchPlaces = {},
        onDiscardLunchPlaces = {}
    )
}