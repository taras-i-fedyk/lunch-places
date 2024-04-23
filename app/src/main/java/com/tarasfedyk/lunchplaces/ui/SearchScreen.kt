package com.tarasfedyk.lunchplaces.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.tarasfedyk.lunchplaces.R
import com.tarasfedyk.lunchplaces.biz.data.ErrorType
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
import com.tarasfedyk.lunchplaces.biz.data.SearchInput
import com.tarasfedyk.lunchplaces.biz.data.SizeLimit
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.ui.util.CompactSearchBar
import com.tarasfedyk.lunchplaces.ui.util.isPermissionGranted
import com.tarasfedyk.lunchplaces.ui.util.safelyRememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SearchScreen(
    onSearchLunchPlaces: (SearchInput) -> Unit,
    onDiscardLunchPlaces: () -> Unit,
    lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?
) {
    var isSearchBarActive by rememberSaveable { mutableStateOf(false) }
    val onSetSearchBarActiveness: (Boolean) -> Unit = remember { { isSearchBarActive = it } }

    val focusManager = LocalFocusManager.current
    val searchBarInteractionSource = remember { MutableInteractionSource() }
    val isSearchBarFocused by searchBarInteractionSource.collectIsFocusedAsState()

    var currentQuery by rememberSaveable { mutableStateOf("") }
    val onSetCurrentQuery: (String) -> Unit = remember { { currentQuery = it } }
    val onClearCurrentQuery = remember(onSetCurrentQuery) { { onSetCurrentQuery("") } }
    var sentQuery by rememberSaveable { mutableStateOf("") }
    val onSetSentQuery: (String) -> Unit = remember { { sentQuery = it } }

    val onGoBack = remember(
        onSetSearchBarActiveness,
        focusManager,
        isSearchBarFocused,
        onSetCurrentQuery,
        onClearCurrentQuery,
        sentQuery,
        onSetSentQuery,
        onDiscardLunchPlaces
    ) {
        {
            goBack(
                onSetSearchBarActiveness = onSetSearchBarActiveness,
                focusManager = focusManager,
                isSearchBarFocused = isSearchBarFocused,
                onSetCurrentQuery = onSetCurrentQuery,
                onClearCurrentQuery = onClearCurrentQuery,
                sentQuery = sentQuery,
                onSetSentQuery = onSetSentQuery,
                onDiscardLunchPlaces = onDiscardLunchPlaces
            )
        }
    }

    val thumbnailSizeLimit = thumbnailSizeLimit()
    val onLocationPermissionsResult: (Map<String, Boolean>) -> Unit = remember(
        sentQuery, thumbnailSizeLimit, onSearchLunchPlaces
    ) {
        { searchLunchPlacesCurrently(sentQuery, thumbnailSizeLimit, onSearchLunchPlaces) }
    }
    val locationPermissionsState = safelyRememberMultiplePermissionsState(
        permissions = listOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION),
        onPermissionsResult = onLocationPermissionsResult
    )
    val onTrySearch: (String) -> Unit = remember(
        focusManager,
        currentQuery,
        onSetSentQuery,
        onGoBack,
        thumbnailSizeLimit,
        locationPermissionsState,
        onSearchLunchPlaces
    ) {
        {
            trySearch(
                focusManager,
                currentQuery,
                onSetSentQuery,
                onGoBack,
                thumbnailSizeLimit,
                locationPermissionsState,
                onSearchLunchPlaces
            )
        }
    }
    val onRetrySearch = remember(onTrySearch, sentQuery) { { onTrySearch(sentQuery) } }

    // TODO: adjust the horizontal padding in a smooth way
    CompactSearchBar(
        modifier = Modifier.fillMaxWidth(),
        isActive = isSearchBarActive,
        onActivenessChanged = onSetSearchBarActiveness,
        interactionSource = searchBarInteractionSource,
        hint = stringResource(R.string.search_hint),
        query = currentQuery,
        onQueryChanged = onSetCurrentQuery,
        onClearQuery = onClearCurrentQuery,
        onGoBack = onGoBack,
        onTrySearch = onTrySearch
    ) {
        if (!isSearchBarFocused) {
            SearchStatus(lunchPlacesStatus, onRetrySearch)
        }
    }
}

private fun goBack(
    onSetSearchBarActiveness: (Boolean) -> Unit,
    focusManager: FocusManager,
    isSearchBarFocused: Boolean,
    onSetCurrentQuery: (String) -> Unit,
    onClearCurrentQuery: () -> Unit,
    sentQuery: String,
    onSetSentQuery: (String) -> Unit,
    onDiscardLunchPlaces: () -> Unit
) {
    if (isSearchBarFocused && sentQuery.isNotEmpty()) {
        onSetCurrentQuery(sentQuery)
        focusManager.clearFocus()
    } else {
        onSetSentQuery("")
        onClearCurrentQuery()
        onSetSearchBarActiveness(false)
        onDiscardLunchPlaces()
    }
}

@Composable
private fun thumbnailSizeLimit(): SizeLimit {
    val context = LocalContext.current
    val thumbnailSize = context.resources.getDimensionPixelSize(R.dimen.thumbnail_size)
    return SizeLimit(
        maxWidth = thumbnailSize,
        maxHeight = thumbnailSize
    )
}

private fun searchLunchPlacesCurrently(
    sentQuery: String,
    thumbnailSizeLimit: SizeLimit,
    onSearchLunchPlaces: (SearchInput) -> Unit
) {
    onSearchLunchPlaces(
        SearchInput(
            query = sentQuery,
            thumbnailSizeLimit = thumbnailSizeLimit
        )
    )
}

@OptIn(ExperimentalPermissionsApi::class)
private fun trySearch(
    focusManager: FocusManager,
    currentQuery: String,
    onSetSentQuery: (String) -> Unit,
    onGoBack: () -> Unit,
    thumbnailSizeLimit: SizeLimit,
    locationPermissionsState: MultiplePermissionsState,
    onSearchLunchPlaces: (SearchInput) -> Unit
) {
    onSetSentQuery(currentQuery)
    if (currentQuery.isNotEmpty()) {
        focusManager.clearFocus()
        if (!locationPermissionsState.isPermissionGranted(ACCESS_FINE_LOCATION)) {
            locationPermissionsState.launchMultiplePermissionRequest()
        } else {
            searchLunchPlacesCurrently(
                sentQuery = currentQuery,
                thumbnailSizeLimit = thumbnailSizeLimit,
                onSearchLunchPlaces = onSearchLunchPlaces
            )
        }
    } else {
        onGoBack()
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
        is Status.Failure -> SearchError(lunchPlacesStatus.errorType, onRetrySearch)
    }
}

@Composable
private fun SearchProgress() {
    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
}

@Composable
private fun SearchResult(lunchPlaces: List<LunchPlace>) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(lunchPlaces) {
            LunchPlaceItem(it)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun SearchError(errorType: ErrorType, onRetrySearch: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = when (errorType) {
        ErrorType.LOCATION_PERMISSIONS -> stringResource(R.string.search_permissions_error_message)
        ErrorType.CURRENT_LOCATION -> stringResource(R.string.search_location_error_message)
        ErrorType.INTERNET_CONNECTION -> stringResource(R.string.search_connection_error_message)
        ErrorType.UNKNOWN -> stringResource(R.string.search_error_message)
    }
    val retryActionLabel = stringResource(R.string.retry_action_label)

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = {}
    )

    LaunchedEffect(Unit) {
        val result = snackbarHostState.showSnackbar(
            message = errorMessage,
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
        onSearchLunchPlaces = {},
        onDiscardLunchPlaces = {},
        lunchPlacesStatus = null
    )
}