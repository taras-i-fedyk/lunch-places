package com.tarasfedyk.lunchplaces.ui

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
import com.tarasfedyk.lunchplaces.R
import com.tarasfedyk.lunchplaces.biz.data.ErrorType
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
import com.tarasfedyk.lunchplaces.biz.data.SearchInput
import com.tarasfedyk.lunchplaces.biz.data.SizeLimit
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.biz.util.circularInc
import com.tarasfedyk.lunchplaces.ui.util.CompactSearchBar
import com.tarasfedyk.lunchplaces.ui.util.goToAppSettings

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
    var appliedQuery by rememberSaveable { mutableStateOf("") }
    val onSetAppliedQuery: (String) -> Unit = remember { { appliedQuery = it } }

    val thumbnailSizeLimit = thumbnailSizeLimit()

    val onGoBack = remember(
        onSetSearchBarActiveness,
        focusManager,
        isSearchBarFocused,
        onSetCurrentQuery,
        onClearCurrentQuery,
        appliedQuery,
        onSetAppliedQuery,
        onDiscardLunchPlaces
    ) {
        {
            goBack(
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
        currentQuery,
        onSetAppliedQuery,
        onGoBack,
        thumbnailSizeLimit,
        onSearchLunchPlaces
    ) {
        {
            trySearch(
                focusManager,
                currentQuery,
                onSetAppliedQuery,
                onGoBack,
                thumbnailSizeLimit,
                onSearchLunchPlaces
            )
        }
    }
    val onRetrySearch = remember(onTrySearch, appliedQuery) { { onTrySearch(appliedQuery) } }

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
        onGoBack = onGoBack,
        onTrySearch = onTrySearch
    ) {
        if (!isSearchBarFocused) {
            SearchStatus(lunchPlacesStatus, onRetrySearch)
        }
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

private fun goBack(
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
    onGoBack: () -> Unit,
    thumbnailSizeLimit: SizeLimit,
    onSearchLunchPlaces: (SearchInput) -> Unit
) {
    onSetAppliedQuery(currentQuery)
    if (currentQuery.isNotEmpty()) {
        focusManager.clearFocus()
        onSearchLunchPlaces(
            SearchInput(
                query = currentQuery,
                thumbnailSizeLimit = thumbnailSizeLimit
            )
        )
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
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarAppearanceId by remember { mutableStateOf(UByte.MIN_VALUE) }
    val errorMessage = when (errorType) {
        ErrorType.LOCATION_PERMISSIONS -> stringResource(R.string.search_permissions_error_message)
        ErrorType.CURRENT_LOCATION -> stringResource(R.string.search_location_error_message)
        ErrorType.INTERNET_CONNECTION -> stringResource(R.string.search_connection_error_message)
        ErrorType.UNKNOWN -> stringResource(R.string.search_error_message)
    }
    val actionLabel = if (errorType == ErrorType.LOCATION_PERMISSIONS) {
        stringResource(R.string.resolve_action_label)
    } else {
        stringResource(R.string.retry_action_label)
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = {}
    )

    LaunchedEffect(snackbarAppearanceId) {
        val snackbarResult = snackbarHostState.showSnackbar(
            message = errorMessage,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Indefinite
        )
        when (snackbarResult) {
            SnackbarResult.ActionPerformed -> {
                if (errorType == ErrorType.LOCATION_PERMISSIONS) {
                    snackbarAppearanceId = snackbarAppearanceId.circularInc()
                    context.goToAppSettings()
                } else {
                    onRetrySearch()
                }
            }
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