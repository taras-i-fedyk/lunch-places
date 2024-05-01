package com.tarasfedyk.lunchplaces.ui

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.tarasfedyk.lunchplaces.R
import com.tarasfedyk.lunchplaces.biz.data.ErrorType
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.MediaLimits
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
import com.tarasfedyk.lunchplaces.biz.data.SearchInput
import com.tarasfedyk.lunchplaces.biz.data.SizeLimit
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.ui.theme.AppTheme
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

    SearchScreenImpl(
        isSearchBarActive,
        onSetSearchBarActiveness,
        onSearchLunchPlaces,
        onDiscardLunchPlaces,
        lunchPlacesStatus,
        onNavigateToDetails
    )

    LaunchedEffect(isSearchBarActive) {
        val isMapVisible = !isSearchBarActive
        onSetMapVisibility(isMapVisible)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenImpl(
    isSearchBarActive: Boolean,
    onSetSearchBarActiveness: (Boolean) -> Unit,
    onSearchLunchPlaces: (SearchInput) -> Unit,
    onDiscardLunchPlaces: () -> Unit,
    lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?,
    onNavigateToDetails: (Int) -> Unit
) {
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
        shadowElevation = SearchBarDefaults.TonalElevation,
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
private fun SearchResult(
    lunchPlaces: List<LunchPlace>,
    onNavigateToDetails: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        itemsIndexed(lunchPlaces) { lunchPlaceIndex, lunchPlace ->
            SearchResultItem(lunchPlaceIndex, lunchPlace, onNavigateToDetails)
        }
    }
}

@Composable
private fun SearchResultItem(
    lunchPlaceIndex: Int,
    lunchPlace: LunchPlace,
    onNavigateToDetails: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToDetails(lunchPlaceIndex) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LunchPlacePhoto(lunchPlace.photoUri, isThumbnail = true)
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            LunchPlaceName(lunchPlace.name)
            LunchPlaceRating(lunchPlace.rating)
            Row(verticalAlignment = Alignment.CenterVertically) {
                LunchPlaceDistance(lunchPlace.distance)
                LunchPlaceOpenness(
                    lunchPlace.isOpen,
                    modifier = Modifier.padding(start = 1.dp)
                )
            }
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
    AppTheme {
        SearchScreenImpl(
            isSearchBarActive = true,
            onSetSearchBarActiveness = {},
            onSearchLunchPlaces = {},
            onDiscardLunchPlaces = {},
            lunchPlacesStatus = Status.Success(
                SearchFilter(
                    SearchInput(query = "burger")
                ),
                result = listOf(
                    LunchPlace(
                        id = "ChIJRx5D7mzdOkcR8MgRrmieLvc",
                        name = "Pizza Calcio",
                        rating = 3.8,
                        latLng = LatLng(49.842306799999996, 24.034497899999998),
                        distance = 2923.3997f,
                        address = "вулиця Підвальна, 9, Львів, Львівська область, Україна, 79000",
                        isOpen = false,
                        thumbnailUri = Uri.parse("https://lh3.googleusercontent.com/places/ANXAkqFiFHd0LKC_e89MhGD3GjL6zEhZkkkowyR5_CxLn1keGgxNIBCcbNfNUzc7gqQoib29wBCkwN5M0INME092a5PLgCUtdSUZVn4=s4800-w192-h192"),
                        photoUri = Uri.parse("https://lh3.googleusercontent.com/places/ANXAkqFiFHd0LKC_e89MhGD3GjL6zEhZkkkowyR5_CxLn1keGgxNIBCcbNfNUzc7gqQoib29wBCkwN5M0INME092a5PLgCUtdSUZVn4=s4800-w1920-h1080")
                    )
                )
            ),
            onNavigateToDetails = {}
        )
    }
}