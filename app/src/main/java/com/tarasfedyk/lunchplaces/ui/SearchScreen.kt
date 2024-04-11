package com.tarasfedyk.lunchplaces.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tarasfedyk.lunchplaces.R
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.ui.util.CompactSearchBar

@Composable
fun SearchScreen(
    onSearchBarBottomYChanged: (Dp) -> Unit,
    onSearchLunchPlaces: (SearchFilter) -> Unit,
    onDiscardLunchPlaces: () -> Unit,
    lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?
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
    val onClear = {
        currentQuery = ""
    }
    val onSearch: (String) -> Unit = {
        sentQuery = currentQuery
        if (sentQuery.isNotEmpty()) {
            focusManager.clearFocus()
            onSearchLunchPlaces(SearchFilter(sentQuery))
        } else {
            onGoBack()
        }
    }
    val onRetrySearch = {
        onSearch(sentQuery)
    }

    CompactSearchBar(
        modifier = Modifier.fillMaxWidth(),
        onInputFieldBottomYChanged = onSearchBarBottomYChanged,
        hint = { SearchHint() },
        isActive = isActive,
        onActivenessChanged = { isActive = it },
        interactionSource = interactionSource,
        query = currentQuery,
        onQueryChanged = { currentQuery = it },
        onGoBack = onGoBack,
        onClear = onClear,
        onSearch = onSearch
    ) {
        if (!isFocused) {
            SearchStatus(lunchPlacesStatus, onRetrySearch)
        }
    }
}

@Composable
private fun SearchHint() {
    Text(stringResource(R.string.search_hint))
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
            .padding(top = 4.dp)
            .fillMaxWidth()
    )
}

@Composable
private fun SearchResult(lunchPlaces: List<LunchPlace>) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(lunchPlaces) {
            SearchResultItem(it)
        }
    }
}

@Composable
private fun SearchResultItem(lunchPlace: LunchPlace) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 8.dp)
    ) {
        LunchPlaceName(lunchPlace.name)
        if (lunchPlace.isOpen == false) {
            LunchPlaceUnavailability()
        }
    }
}

@Composable
private fun LunchPlaceName(text: String) {
    Text(
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyLarge,
        text = text
    )
}

@Composable
private fun LunchPlaceUnavailability() {
    Text(
        style = MaterialTheme.typography.bodySmall,
        text = stringResource(R.string.unavailability_label),
        color = MaterialTheme.colorScheme.error
    )
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
            actionLabel = retryActionLabel,
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
        onSearchLunchPlaces = {},
        onDiscardLunchPlaces = {},
        lunchPlacesStatus = null
    )
}