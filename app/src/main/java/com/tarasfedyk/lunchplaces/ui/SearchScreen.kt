package com.tarasfedyk.lunchplaces.ui

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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.tarasfedyk.lunchplaces.R
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
import com.tarasfedyk.lunchplaces.biz.data.SizeLimit
import com.tarasfedyk.lunchplaces.biz.data.Status
import com.tarasfedyk.lunchplaces.biz.util.roundToDecimalPlaces
import com.tarasfedyk.lunchplaces.ui.util.CompactSearchBar
import com.tarasfedyk.lunchplaces.ui.util.SmallRatingIndicator
import kotlin.math.roundToInt

@Composable
fun SearchScreen(
    onSearchBarBottomYChanged: (Dp) -> Unit,
    onSearchLunchPlaces: (SearchFilter) -> Unit,
    onDiscardLunchPlaces: () -> Unit,
    lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>?
) {
    var isSearchActive by rememberSaveable { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val searchBarInteractionSource = remember { MutableInteractionSource() }
    val isSearchBarFocused by searchBarInteractionSource.collectIsFocusedAsState()

    var currentQuery by rememberSaveable { mutableStateOf("") }
    var sentQuery by rememberSaveable { mutableStateOf("") }

    val thumbnailSizeLimit = thumbnailSizeLimit()

    val onSetSearchActiveness: (Boolean) -> Unit = remember {
        {
            isSearchActive = it
        }
    }

    val onSetCurrentQuery: (String) -> Unit = remember {
        {
            currentQuery = it
        }
    }
    val onClearCurrentQuery = remember(onSetCurrentQuery) {
        {
            onSetCurrentQuery("")
        }
    }

    val onGoBack = remember(
        focusManager,
        isSearchBarFocused,
        onSetCurrentQuery,
        onClearCurrentQuery,
        sentQuery,
        onDiscardLunchPlaces
    ) {
        {
            if (isSearchBarFocused && sentQuery.isNotEmpty()) {
                onSetCurrentQuery(sentQuery)
                focusManager.clearFocus()
            } else {
                sentQuery = ""
                onClearCurrentQuery()
                isSearchActive = false
                onDiscardLunchPlaces()
            }
        }
    }

    val onSearch: (String) -> Unit = remember(focusManager, onGoBack, onSearchLunchPlaces) {
        { query ->
            sentQuery = query
            if (query.isNotEmpty()) {
                focusManager.clearFocus()
                onSearchLunchPlaces(
                    SearchFilter(
                        query,
                        thumbnailSizeLimit = thumbnailSizeLimit
                    )
                )
            } else {
                onGoBack()
            }
        }
    }
    val onRetrySearch = remember(onSearch, sentQuery) {
        {
            onSearch(sentQuery)
        }
    }

    // TODO: adjust the horizontal padding in a smooth way
    CompactSearchBar(
        modifier = Modifier.fillMaxWidth(),
        onInputFieldBottomYChanged = onSearchBarBottomYChanged,
        isActive = isSearchActive,
        onActivenessChanged = onSetSearchActiveness,
        interactionSource = searchBarInteractionSource,
        hint = stringResource(R.string.search_hint),
        query = currentQuery,
        onQueryChanged = onSetCurrentQuery,
        onClearQuery = onClearCurrentQuery,
        onGoBack = onGoBack,
        onSearch = onSearch
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
private fun SearchError(onRetrySearch: () -> Unit) {
    val onRetrySearchCurrent by rememberUpdatedState(onRetrySearch)
    val snackbarHostState = remember { SnackbarHostState() }

    val message = stringResource(R.string.search_error_message)
    val retryActionLabel = stringResource(R.string.search_error_retry_action_label)

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = {}
    )

    LaunchedEffect(Unit) {
        val result = snackbarHostState.showSnackbar(
            message,
            actionLabel = retryActionLabel,
            duration = SnackbarDuration.Indefinite
        )
        when (result) {
            SnackbarResult.ActionPerformed -> onRetrySearchCurrent()
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