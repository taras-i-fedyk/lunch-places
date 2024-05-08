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
import com.tarasfedyk.lunchplaces.biz.data.SearchSettings
import com.tarasfedyk.lunchplaces.biz.data.SizeLimit
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.ui.data.MapConfig
import com.tarasfedyk.lunchplaces.ui.theme.AppTheme
import com.tarasfedyk.lunchplaces.ui.util.CompactSearchBar
import com.tarasfedyk.lunchplaces.ui.util.PermanentErrorSnackbar

@Composable
fun SearchScreen(
    isCurrentDestination: Boolean,
    onSetMapConfig: (MapConfig) -> Unit,
    onSearchLunchPlaces: (SearchInput) -> Unit,
    onDiscardLunchPlaces: () -> Unit,
    lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?,
    onNavigateToSettings: () -> Unit,
    onNavigateToDetails: (Int) -> Unit
) {
    var isSearchBarActive by rememberSaveable { mutableStateOf(false) }
    val onSetSearchBarActive: (Boolean) -> Unit = remember { { isSearchBarActive = it } }

    var enteredQuery by rememberSaveable { mutableStateOf("") }
    val onSetEnteredQuery: (String) -> Unit = remember { { enteredQuery = it } }
    var appliedQuery by rememberSaveable { mutableStateOf("") }
    val onSetAppliedQuery: (String) -> Unit = remember { { appliedQuery = it } }

    SearchScreenImpl(
        isSearchBarActive = isSearchBarActive,
        onSetSearchBarActive = onSetSearchBarActive,
        enteredQuery = enteredQuery,
        onSetEnteredQuery = onSetEnteredQuery,
        appliedQuery = appliedQuery,
        onSetAppliedQuery = onSetAppliedQuery,
        onSearchLunchPlaces = onSearchLunchPlaces,
        onDiscardLunchPlaces = onDiscardLunchPlaces,
        lunchPlacesStatus = lunchPlacesStatus,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToDetails = onNavigateToDetails
    )

    LaunchedEffect(isCurrentDestination, isSearchBarActive, onSetMapConfig) {
        if (!isCurrentDestination) return@LaunchedEffect

        val mapConfig = MapConfig(isMapVisible = !isSearchBarActive)
        onSetMapConfig(mapConfig)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreenImpl(
    isSearchBarActive: Boolean,
    onSetSearchBarActive: (Boolean) -> Unit,
    enteredQuery: String,
    onSetEnteredQuery: (String) -> Unit,
    appliedQuery: String,
    onSetAppliedQuery: (String) -> Unit,
    onSearchLunchPlaces: (SearchInput) -> Unit,
    onDiscardLunchPlaces: () -> Unit,
    lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?,
    onNavigateToSettings: () -> Unit,
    onNavigateToDetails: (Int) -> Unit
) {
    val searchBarBottomPadding = if (isSearchBarActive) 0.dp else 16.dp

    val focusManager = LocalFocusManager.current
    val searchBarInteractionSource = remember { MutableInteractionSource() }
    val isSearchBarFocused by searchBarInteractionSource.collectIsFocusedAsState()

    val onClearCurrentQuery = remember(onSetEnteredQuery) { { onSetEnteredQuery("") } }

    val mediaLimits = mediaLimits()

    val onNavigateBack = remember(
        onSetSearchBarActive,
        focusManager,
        onSetEnteredQuery,
        appliedQuery,
        onSetAppliedQuery,
        onDiscardLunchPlaces
    ) {
        {
            navigateBack(
                onSetSearchBarActive = onSetSearchBarActive,
                focusManager = focusManager,
                isSearchBarFocused = isSearchBarFocused,
                onSetEnteredQuery = onSetEnteredQuery,
                onClearCurrentQuery = onClearCurrentQuery,
                appliedQuery = appliedQuery,
                onSetAppliedQuery = onSetAppliedQuery,
                onDiscardLunchPlaces = onDiscardLunchPlaces
            )
        }
    }

    val onTrySearch: (String) -> Unit = remember(
        focusManager,
        enteredQuery,
        onSetAppliedQuery,
        mediaLimits,
        onNavigateBack,
        onSearchLunchPlaces
    ) {
        {
            trySearch(
                focusManager,
                enteredQuery,
                onSetAppliedQuery,
                mediaLimits,
                onNavigateBack,
                onSearchLunchPlaces
            )
        }
    }
    val onRetrySearch = remember(onTrySearch, appliedQuery) {
        { onTrySearch(appliedQuery) }
    }

    // TODO: when it becomes possible, adjust the horizontal padding in a smooth way
    CompactSearchBar(
        modifier = Modifier
            // we're using a bottom padding for a smoother application of the shadow elevation
            .padding(bottom = searchBarBottomPadding)
            .fillMaxWidth(),
        shadowElevation = SearchBarDefaults.TonalElevation,
        isActive = isSearchBarActive,
        onActiveChanged = onSetSearchBarActive,
        interactionSource = searchBarInteractionSource,
        hint = stringResource(R.string.search_hint),
        query = enteredQuery,
        onQueryChanged = onSetEnteredQuery,
        onClearQuery = onClearCurrentQuery,
        onNavigateBack = onNavigateBack,
        onNavigateToSettings = onNavigateToSettings,
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
    onSetSearchBarActive: (Boolean) -> Unit,
    focusManager: FocusManager,
    isSearchBarFocused: Boolean,
    onSetEnteredQuery: (String) -> Unit,
    onClearCurrentQuery: () -> Unit,
    appliedQuery: String,
    onSetAppliedQuery: (String) -> Unit,
    onDiscardLunchPlaces: () -> Unit
) {
    if (isSearchBarFocused && appliedQuery.isNotEmpty()) {
        onSetEnteredQuery(appliedQuery)
        focusManager.clearFocus()
    } else {
        onSetAppliedQuery("")
        onClearCurrentQuery()
        onSetSearchBarActive(false)
        onDiscardLunchPlaces()
    }
}

private fun trySearch(
    focusManager: FocusManager,
    enteredQuery: String,
    onSetAppliedQuery: (String) -> Unit,
    mediaLimits: MediaLimits,
    onNavigateBack: () -> Unit,
    onSearchLunchPlaces: (SearchInput) -> Unit
) {
    onSetAppliedQuery(enteredQuery)
    if (enteredQuery.isNotEmpty()) {
        focusManager.clearFocus()
        onSearchLunchPlaces(
            SearchInput(enteredQuery, mediaLimits)
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
        ErrorType.LOCATION_PERMISSIONS -> stringResource(R.string.search_permissions_error_message)
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
private fun InactiveSearchScreenPreview() {
    SearchScreenPreview(
        isSearchBarActive = false,
        enteredQuery = "",
        appliedQuery = ""
    )
}

@Preview(showBackground = true)
@Composable
private fun ActiveSearchScreenPreview() {
    SearchScreenPreview(
        isSearchBarActive = true,
        enteredQuery = "pizza",
        appliedQuery = "pizza"
    )
}
@Composable
private fun SearchScreenPreview(
    isSearchBarActive: Boolean,
    enteredQuery: String,
    appliedQuery: String
) {
    AppTheme {
        SearchScreenImpl(
            isSearchBarActive = isSearchBarActive,
            onSetSearchBarActive = {},
            enteredQuery = enteredQuery,
            onSetEnteredQuery = {},
            appliedQuery = appliedQuery,
            onSetAppliedQuery = {},
            onSearchLunchPlaces = {},
            onDiscardLunchPlaces = {},
            lunchPlacesStatus = Status.Success(
                SearchFilter(
                    SearchInput(appliedQuery),
                    SearchSettings()
                ),
                result = listOf(
                    LunchPlace(
                        id = "ChIJRx5D7mzdOkcR8MgRrmieLvc",
                        name = "Pizza Calcio",
                        rating = 3.8,
                        point = LatLng(49.842306799999996, 24.034497899999998),
                        distance = 2923.3997f,
                        address = "вулиця Підвальна, 9, Львів, Львівська область, Україна, 79000",
                        isOpen = false,
                        thumbnailUri = Uri.parse("https://lh3.googleusercontent.com/places/ANXAkqFiFHd0LKC_e89MhGD3GjL6zEhZkkkowyR5_CxLn1keGgxNIBCcbNfNUzc7gqQoib29wBCkwN5M0INME092a5PLgCUtdSUZVn4=s4800-w192-h192"),
                        photoUri = Uri.parse("https://lh3.googleusercontent.com/places/ANXAkqFiFHd0LKC_e89MhGD3GjL6zEhZkkkowyR5_CxLn1keGgxNIBCcbNfNUzc7gqQoib29wBCkwN5M0INME092a5PLgCUtdSUZVn4=s4800-w1920-h1080")
                    )
                )
            ),
            onNavigateToSettings = {},
            onNavigateToDetails = {}
        )
    }
}