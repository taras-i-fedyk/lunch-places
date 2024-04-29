package com.tarasfedyk.lunchplaces.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tarasfedyk.lunchplaces.R
import com.tarasfedyk.lunchplaces.biz.data.ErrorType
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.MediaLimits
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
import com.tarasfedyk.lunchplaces.biz.data.SearchInput
import com.tarasfedyk.lunchplaces.biz.data.SizeLimit
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.ui.util.CompactSearchBar
import com.tarasfedyk.lunchplaces.ui.util.PermanentErrorSnackbar

@Composable
fun SearchScreen(
    onSetMapVisibility: (Boolean) -> Unit,
    onSearchLunchPlaces: (SearchInput) -> Unit,
    onDiscardLunchPlaces: () -> Unit,
    lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?,
    onNavigateToDetails: (Int) -> Unit
) {
    var isSearchBarActive by rememberSaveable { mutableStateOf(false) }
    val onSetSearchBarActiveness: (Boolean) -> Unit = remember { { isSearchBarActive = it } }

    val focusManager = LocalFocusManager.current
    val searchBarInteractionSource = remember { MutableInteractionSource() }
    val isSearchBarFocused by searchBarInteractionSource.collectIsFocusedAsState()

    var currentQuery by rememberSaveable { mutableStateOf("") }
    val onSetCurrentQuery: (String) -> Unit = remember { { currentQuery = it } }
    val onClearCurrentQuery = remember { { onSetCurrentQuery("") } }
    var appliedQuery by rememberSaveable { mutableStateOf("") }
    val onSetAppliedQuery: (String) -> Unit = remember { { appliedQuery = it } }

    val mediaLimits = mediaLimits()

    val onNavigateBack = remember(
        focusManager,
        onDiscardLunchPlaces
    ) {
        {
            navigateBack(
                onSetSearchBarActiveness = onSetSearchBarActiveness,
                focusManager = focusManager,
                isSearchBarFocused = isSearchBarFocused,
                onSetCurrentQuery = onSetCurrentQuery,
                onClearCurrentQuery = onClearCurrentQuery,
                appliedQuery = appliedQuery,
                onSetAppliedQuery = onSetAppliedQuery,
                onDiscardLunchPlaces = onDiscardLunchPlaces
            )
        }
    }

    val onTrySearch: (String) -> Unit = remember(
        focusManager,
        mediaLimits,
        onNavigateBack,
        onSearchLunchPlaces
    ) {
        {
            trySearch(
                focusManager,
                currentQuery,
                onSetAppliedQuery,
                mediaLimits,
                onNavigateBack,
                onSearchLunchPlaces
            )
        }
    }
    val onRetrySearch = remember(onTrySearch) {
        { onTrySearch(appliedQuery) }
    }

    // TODO: when it becomes possible, set the horizontal padding of an inactive search bar
    CompactSearchBar(
        modifier = Modifier.fillMaxWidth(),
        isActive = isSearchBarActive,
        onActivenessChanged = onSetSearchBarActiveness,
        interactionSource = searchBarInteractionSource,
        hint = stringResource(R.string.search_hint),
        query = currentQuery,
        onQueryChanged = onSetCurrentQuery,
        onClearQuery = onClearCurrentQuery,
        onNavigateBack = onNavigateBack,
        onTrySearch = onTrySearch
    ) {
        SearchStatus(lunchPlacesStatus, onNavigateToDetails, onRetrySearch)
    }

    LaunchedEffect(isSearchBarActive) {
        val isMapVisible = !isSearchBarActive
        onSetMapVisibility(isMapVisible)
    }
}

@Composable
private fun mediaLimits(): MediaLimits {
    val context = LocalContext.current
    val thumbnailSize = context.resources.getDimensionPixelSize(R.dimen.thumbnail_size)
    return MediaLimits(
        thumbnailSizeLimit = SizeLimit(
            maxWidth = thumbnailSize,
            maxHeight = thumbnailSize
        )
    )
}

private fun navigateBack(
    onSetSearchBarActiveness: (Boolean) -> Unit,
    focusManager: FocusManager,
    isSearchBarFocused: Boolean,
    onSetCurrentQuery: (String) -> Unit,
    onClearCurrentQuery: () -> Unit,
    appliedQuery: String,
    onSetAppliedQuery: (String) -> Unit,
    onDiscardLunchPlaces: () -> Unit
) {
    if (isSearchBarFocused && appliedQuery.isNotEmpty()) {
        onSetCurrentQuery(appliedQuery)
        focusManager.clearFocus()
    } else {
        onSetAppliedQuery("")
        onClearCurrentQuery()
        onSetSearchBarActiveness(false)
        onDiscardLunchPlaces()
    }
}

private fun trySearch(
    focusManager: FocusManager,
    currentQuery: String,
    onSetAppliedQuery: (String) -> Unit,
    mediaLimits: MediaLimits,
    onNavigateBack: () -> Unit,
    onSearchLunchPlaces: (SearchInput) -> Unit
) {
    onSetAppliedQuery(currentQuery)
    if (currentQuery.isNotEmpty()) {
        focusManager.clearFocus()
        onSearchLunchPlaces(
            SearchInput(currentQuery, mediaLimits)
        )
    } else {
        onNavigateBack()
    }
}

@Composable
private fun SearchStatus(
    lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?,
    onNavigateToDetails: (Int) -> Unit,
    onRetrySearch: () -> Unit
) {
    when (lunchPlacesStatus) {
        null -> {}
        is Status.Pending -> SearchProgress()
        is Status.Success -> SearchResult(
            lunchPlaces = lunchPlacesStatus.result,
            onNavigateToDetails
        )
        is Status.Failure -> SearchError(
            errorType = lunchPlacesStatus.errorType,
            onRetrySearch
        )
    }
}

@Composable
private fun SearchProgress() {
    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
}

@Composable
private fun SearchResult(lunchPlaces: List<LunchPlace>, onNavigateToDetails: (Int) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        itemsIndexed(lunchPlaces) { index, lunchPlace ->
            LunchPlaceItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToDetails(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                largeSpacerSize = 16.dp,
                smallSpacerSize = 2.dp,
                lunchPlace = lunchPlace
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun SearchError(errorType: ErrorType, onRetrySearch: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }

    val isAppSettingsError = errorType == ErrorType.LOCATION_PERMISSIONS
    val errorMessage = when (errorType) {
        ErrorType.LOCATION_PERMISSIONS -> stringResource(R.string.search_location_permissions_error_message)
        ErrorType.CURRENT_LOCATION -> stringResource(R.string.search_location_error_message)
        ErrorType.INTERNET_CONNECTION -> stringResource(R.string.search_connection_error_message)
        else -> stringResource(R.string.search_error_message)
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) {
        PermanentErrorSnackbar(
            snackbarHostState = snackbarHostState,
            isAppSettingsError = isAppSettingsError,
            errorMessage = errorMessage,
            onRetry = onRetrySearch
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    SearchScreen(
        onSetMapVisibility = {},
        onSearchLunchPlaces = {},
        onDiscardLunchPlaces = {},
        lunchPlacesStatus = null,
        onNavigateToDetails = {}
    )
}