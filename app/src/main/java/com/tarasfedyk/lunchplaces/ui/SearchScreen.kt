package com.tarasfedyk.lunchplaces.ui

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.toArgb
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
import com.tarasfedyk.lunchplaces.ui.data.ThumbnailAspects
import com.tarasfedyk.lunchplaces.ui.util.CompactSearchBar
import com.tarasfedyk.lunchplaces.ui.util.PermanentErrorSnackbar

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
    val onClearCurrentQuery = remember { { onSetCurrentQuery("") } }
    var appliedQuery by rememberSaveable { mutableStateOf("") }
    val onSetAppliedQuery: (String) -> Unit = remember { { appliedQuery = it } }

    val mediaLimits = rememberMediaLimits()
    val thumbnailAspects = rememberThumbnailAspects()

    val onGoBack = remember(
        focusManager,
        isSearchBarFocused,
        appliedQuery,
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
        mediaLimits,
        onGoBack,
        onSearchLunchPlaces
    ) {
        {
            trySearch(
                focusManager,
                currentQuery,
                onSetAppliedQuery,
                mediaLimits,
                onGoBack,
                onSearchLunchPlaces
            )
        }
    }
    val onRetrySearch = remember(onTrySearch, appliedQuery) {
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
        onGoBack = onGoBack,
        onTrySearch = onTrySearch
    ) {
        if (!isSearchBarFocused) {
            SearchStatus(lunchPlacesStatus, thumbnailAspects, onRetrySearch)
        }
    }
}

@Composable
private fun rememberMediaLimits(): MediaLimits {
    val context = LocalContext.current
    return remember(context) {
        val thumbnailSize = context.resources.getDimensionPixelSize(R.dimen.thumbnail_size)
        MediaLimits(
            thumbnailSizeLimit = SizeLimit(
                maxWidth = thumbnailSize,
                maxHeight = thumbnailSize
            )
        )
    }
}

@Composable
private fun rememberThumbnailAspects(): ThumbnailAspects {
    val context = LocalContext.current
    val contentColor = LocalContentColor.current
    return remember(context, contentColor) {
        val cornerRadius = context.resources.getDimensionPixelSize(R.dimen.thumbnail_corner_radius)

        val rawPlaceholderDrawable = context.getDrawable(R.drawable.ic_thumbnail_placeholder)
        val placeholderDrawable = rawPlaceholderDrawable?.apply {
            mutate()
            colorFilter = PorterDuffColorFilter(contentColor.toArgb(), PorterDuff.Mode.SRC_IN)
        }

        ThumbnailAspects(cornerRadius, placeholderDrawable)
    }
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
    mediaLimits: MediaLimits,
    onGoBack: () -> Unit,
    onSearchLunchPlaces: (SearchInput) -> Unit
) {
    onSetAppliedQuery(currentQuery)
    if (currentQuery.isNotEmpty()) {
        focusManager.clearFocus()
        onSearchLunchPlaces(
            SearchInput(currentQuery, mediaLimits)
        )
    } else {
        onGoBack()
    }
}

@Composable
private fun SearchStatus(
    lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?,
    thumbnailAspects: ThumbnailAspects,
    onRetrySearch: () -> Unit
) {
    when (lunchPlacesStatus) {
        null -> {}
        is Status.Pending -> SearchProgress()
        is Status.Success -> SearchResult(
            lunchPlaces = lunchPlacesStatus.result,
            thumbnailAspects
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
private fun SearchResult(lunchPlaces: List<LunchPlace>, thumbnailAspects: ThumbnailAspects) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(lunchPlaces) { lunchPlace ->
            LunchPlaceItem(lunchPlace, thumbnailAspects)
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
private fun SearchPreview() {
    SearchScreen(
        onSearchLunchPlaces = {},
        onDiscardLunchPlaces = {},
        lunchPlacesStatus = null
    )
}