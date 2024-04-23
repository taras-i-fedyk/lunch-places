package com.tarasfedyk.lunchplaces.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.tarasfedyk.lunchplaces.R
import com.tarasfedyk.lunchplaces.biz.data.ErrorType
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
import com.tarasfedyk.lunchplaces.biz.data.SearchInput
import com.tarasfedyk.lunchplaces.biz.data.SizeLimit
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.biz.util.roundToDecimalPlaces
import com.tarasfedyk.lunchplaces.ui.util.CompactSearchBar
import com.tarasfedyk.lunchplaces.ui.util.SmallRatingIndicator
import com.tarasfedyk.lunchplaces.ui.util.isPermissionGranted
import com.tarasfedyk.lunchplaces.ui.util.safelyRememberMultiplePermissionsState
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SearchScreen(
    onSearchBarBottomYChanged: (Dp) -> Unit,
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
        onInputFieldBottomYChanged = onSearchBarBottomYChanged,
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
                thumbnailSizeLimit,
                onSearchLunchPlaces
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
            SearchResultItem(it)
        }
    }
}

@Composable
private fun SearchResultItem(lunchPlace: LunchPlace) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LunchPlaceThumbnail(lunchPlace.thumbnailUri)
        Spacer(modifier = Modifier.size(16.dp))
        Column {
            LunchPlaceName(lunchPlace.name)
            LunchPlaceRating(lunchPlace.rating)
            Row(verticalAlignment = Alignment.CenterVertically) {
                LunchPlaceDistance(lunchPlace.distance)
                Spacer(modifier = Modifier.size(8.dp))
                LunchPlaceAvailability(lunchPlace.isOpen)
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun LunchPlaceThumbnail(thumbnailUri: Uri?) {
    val thumbnailPlaceholderDrawable = thumbnailPlaceholderDrawable()
    val thumbnailCornerRadius = thumbnailCornerRadius()
    GlideImage(
        modifier = Modifier.size(dimensionResource(R.dimen.thumbnail_size)),
        model = thumbnailUri,
        loading = placeholder(thumbnailPlaceholderDrawable),
        failure = placeholder(thumbnailPlaceholderDrawable),
        transition = CrossFade,
        contentDescription = stringResource(R.string.lunch_place_thumbnail_description)
    ) {
        it.transform(
            MultiTransformation(
                CenterCrop(),
                RoundedCorners(thumbnailCornerRadius)
            )
        )
    }
}

@Composable
private fun thumbnailPlaceholderDrawable(): Drawable? {
    val context = LocalContext.current
    val contentColor = LocalContentColor.current
    val rawThumbnailPlaceholderDrawable = context.getDrawable(R.drawable.ic_thumbnail_placeholder)
    return rawThumbnailPlaceholderDrawable?.apply {
        mutate()
        colorFilter = PorterDuffColorFilter(contentColor.toArgb(), PorterDuff.Mode.SRC_IN)
    }
}

@Composable
private fun thumbnailCornerRadius(): Int {
    val context = LocalContext.current
    return context.resources.getDimensionPixelSize(R.dimen.thumbnail_corner_radius)
}

@Composable
private fun LunchPlaceName(name: String) {
    Text(
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyLarge,
        text = name
    )
}

@Composable
private fun LunchPlaceRating(rating: Double?) {
    if (rating == null) return

    val roundedRating = rating.roundToDecimalPlaces(decimalPlaceCount = 1)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = roundedRating.toString(),
        )
        SmallRatingIndicator(
            modifier = Modifier.padding(start = 2.dp, bottom = 2.dp),
            rating = roundedRating.toFloat()
        )
    }
}

@Composable
private fun LunchPlaceDistance(distance: Float) {
    val distanceAccuracy = 5
    val roundedDistance = (distance / distanceAccuracy).roundToInt() * distanceAccuracy

    val kilometer = 1000

    val distanceText = if (roundedDistance < kilometer) {
        stringResource(R.string.meters_distance_template, roundedDistance)
    } else {
        val kilometersDistance = roundedDistance.toFloat() / kilometer
        val roundedKilometersDistance = kilometersDistance.roundToDecimalPlaces(
            decimalPlaceCount = 1
        )

        stringResource(R.string.kilometers_distance_template, roundedKilometersDistance)
    }

    Text(
        style = MaterialTheme.typography.bodyMedium,
        text = distanceText
    )
}

@Composable
private fun LunchPlaceAvailability(isOpen: Boolean?) {
    if (isOpen != false) return

    Text(
        style = MaterialTheme.typography.bodyMedium,
        text = stringResource(R.string.unavailability_label),
        color = MaterialTheme.colorScheme.error
    )
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
        onSearchBarBottomYChanged = {},
        onSearchLunchPlaces = {},
        onDiscardLunchPlaces = {},
        lunchPlacesStatus = null
    )
}