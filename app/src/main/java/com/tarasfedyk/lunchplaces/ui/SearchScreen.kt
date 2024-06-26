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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.tarasfedyk.lunchplaces.biz.data.SearchSettings
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.ui.data.MapConfig
import com.tarasfedyk.lunchplaces.ui.theme.AppTheme
import com.tarasfedyk.lunchplaces.ui.util.CompactSearchBar
import com.tarasfedyk.lunchplaces.ui.util.PermanentErrorSnackbar

@Composable
fun SearchScreen(
    isCurrentDestination: Boolean,
    onSetMapConfig: (MapConfig) -> Unit,
    lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?,
    onSearchForLunchPlaces: (String) -> Unit,
    onDiscardLunchPlaces: () -> Unit,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val searchBarActivenessState = rememberSaveable { mutableStateOf(false) }

    val enteredQueryState = rememberSaveable { mutableStateOf("") }
    val appliedQueryState = rememberSaveable { mutableStateOf("") }

    SearchScreenImpl(
        searchBarActivenessState = searchBarActivenessState,
        enteredQueryState = enteredQueryState,
        appliedQueryState = appliedQueryState,
        lunchPlacesStatus = lunchPlacesStatus,
        onSearchForLunchPlaces = onSearchForLunchPlaces,
        onDiscardLunchPlaces = onDiscardLunchPlaces,
        onNavigateToDetails = onNavigateToDetails,
        onNavigateToSettings = onNavigateToSettings
    )

    LaunchedEffect(isCurrentDestination, searchBarActivenessState.value, onSetMapConfig) {
        if (!isCurrentDestination) return@LaunchedEffect

        val mapConfig = MapConfig(isMapVisible = !searchBarActivenessState.value)
        onSetMapConfig(mapConfig)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreenImpl(
    searchBarActivenessState: MutableState<Boolean>,
    enteredQueryState: MutableState<String>,
    appliedQueryState: MutableState<String>,
    lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?,
    onSearchForLunchPlaces: (String) -> Unit,
    onDiscardLunchPlaces: () -> Unit,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val searchBarBottomPadding = if (searchBarActivenessState.value) 0.dp else 16.dp

    val focusManager = LocalFocusManager.current
    val searchBarInteractionSource = remember { MutableInteractionSource() }
    val isSearchBarFocused by searchBarInteractionSource.collectIsFocusedAsState()

    val onNavigateBack = remember(focusManager, onDiscardLunchPlaces) {
        {
            if (isSearchBarFocused && appliedQueryState.value.isNotEmpty()) {
                enteredQueryState.value = appliedQueryState.value
                focusManager.clearFocus()
            } else {
                appliedQueryState.value = ""
                enteredQueryState.value = ""
                searchBarActivenessState.value = false
                onDiscardLunchPlaces()
            }
        }
    }

    val onSearch: (String) -> Unit = remember(focusManager, onNavigateBack, onSearchForLunchPlaces) {
        { enteredQuery ->
            appliedQueryState.value = enteredQuery
            if (enteredQuery.isNotEmpty()) {
                focusManager.clearFocus()
                onSearchForLunchPlaces(enteredQuery)
            } else {
                onNavigateBack()
            }
        }
    }
    val onRetrySearch = remember(onSearch) {
        {
            onSearch(appliedQueryState.value)
        }
    }

    // TODO: when it becomes possible, adjust the horizontal padding in a smooth way
    CompactSearchBar(
        modifier = Modifier
            // we're using a bottom padding for a smoother application of the shadow elevation
            .padding(bottom = searchBarBottomPadding)
            .fillMaxWidth(),
        shadowElevation = SearchBarDefaults.TonalElevation,
        hint = stringResource(R.string.search_hint),
        interactionSource = searchBarInteractionSource,
        activenessState = searchBarActivenessState,
        queryState = enteredQueryState,
        onNavigateBack = onNavigateBack,
        onSearch = onSearch,
        onNavigateToSettings = onNavigateToSettings
    ) {
        SearchStatus(lunchPlacesStatus, onNavigateToDetails, onRetrySearch)
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
        is Status.Success -> SearchResults(
            lunchPlaces = lunchPlacesStatus.result,
            onNavigateToDetails = onNavigateToDetails
        )
        is Status.Failure -> SearchError(
            errorId = lunchPlacesStatus.id,
            errorType = lunchPlacesStatus.errorType,
            onRetrySearch = onRetrySearch
        )
    }
}

@Composable
private fun SearchProgress() {
    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
}

@Composable
private fun SearchResults(
    lunchPlaces: List<LunchPlace>,
    onNavigateToDetails: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        itemsIndexed(lunchPlaces) { lunchPlaceIndex, lunchPlace ->
            SearchResultsItem(lunchPlaceIndex, lunchPlace, onNavigateToDetails)
        }
    }
}

@Composable
private fun SearchResultsItem(
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

        Column(modifier = Modifier.padding(start = 16.dp)) {
            LunchPlaceName(lunchPlace.name, isForTopBar = false)

            val isForLargeBody = false
            LunchPlaceRating(lunchPlace.rating, isForLargeBody = isForLargeBody)
            Row(verticalAlignment = Alignment.CenterVertically) {
                LunchPlaceDistance(lunchPlace.distance, isForLargeBody = isForLargeBody)
                LunchPlaceOpenness(
                    lunchPlace.isOpen,
                    isForLargeBody = isForLargeBody,
                    modifier = Modifier.padding(start = 1.dp)
                )
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun SearchError(errorId: Int, errorType: ErrorType, onRetrySearch: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }

    val isAppSettingsError = errorType == ErrorType.LOCATION_PERMISSION
    val errorMessage = when (errorType) {
        ErrorType.INVALID_CONFIG -> stringResource(R.string.search_config_error_message)
        ErrorType.LOCATION_SERVICES -> stringResource(R.string.search_services_error_message)
        ErrorType.LOCATION_PERMISSION -> stringResource(R.string.search_permission_error_message)
        ErrorType.CURRENT_LOCATION -> stringResource(R.string.search_location_error_message)
        ErrorType.INTERNET_CONNECTION -> stringResource(R.string.search_connection_error_message)
        ErrorType.QUERY_LIMITS -> stringResource(R.string.search_query_error_message)
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
            errorId = errorId,
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
            searchBarActivenessState = remember { mutableStateOf(isSearchBarActive) },
            enteredQueryState = remember { mutableStateOf(enteredQuery) },
            appliedQueryState = remember { mutableStateOf(appliedQuery) },
            lunchPlacesStatus = Status.Success(
                id = 0,
                arg = SearchFilter(
                    appliedQuery,
                    MediaLimits(),
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
            onSearchForLunchPlaces = {},
            onDiscardLunchPlaces = {},
            onNavigateToDetails = {},
            onNavigateToSettings = {}
        )
    }
}