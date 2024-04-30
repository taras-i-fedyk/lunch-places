package com.tarasfedyk.lunchplaces.ui

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
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
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.gms.maps.model.LatLng
import com.tarasfedyk.lunchplaces.R
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.util.roundToDecimalPlaces
import com.tarasfedyk.lunchplaces.ui.theme.AppTheme
import com.tarasfedyk.lunchplaces.ui.util.SmallRatingIndicator
import com.tarasfedyk.lunchplaces.ui.util.UpNavigationIcon
import kotlin.math.roundToInt

private val MockLunchPlace = LunchPlace(
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

object LunchPlaceItemPadding {
    val horizontal: Dp = 16.dp
    val vertical: Dp = 8.dp
}

val LunchPlaceContentPadding: Dp = 16.dp

@Composable
fun LunchPlaceItem(
    index: Int,
    lunchPlace: LunchPlace,
    onNavigateToDetails: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onNavigateToDetails(index) }
            .padding(
                horizontal = LunchPlaceItemPadding.horizontal,
                vertical = LunchPlaceItemPadding.vertical
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LunchPlacePhoto(lunchPlace.photoUri, isThumbnail = true)
        Column(
            modifier = Modifier.padding(start = LunchPlaceItemPadding.horizontal)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunchPlaceBar(
    lunchPlace: LunchPlace,
    scrollBehavior: TopAppBarScrollBehavior?,
    onNavigateUp: () -> Unit
) {
    LargeTopAppBar(
        title = {
            LunchPlaceName(
                lunchPlace.name,
                isTextLarge = true,
                modifier = Modifier.padding(end = 16.dp)
            )
        },
        scrollBehavior = scrollBehavior,
        navigationIcon = { UpNavigationIcon(onNavigateUp) }
    )
}

@Composable
fun LunchPlaceContent(lunchPlace: LunchPlace, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(all = LunchPlaceContentPadding)
    ) {
        LunchPlacePhoto(lunchPlace.photoUri)
        Spacer(modifier = Modifier.size(LunchPlaceContentPadding))
        LunchPlaceRating(lunchPlace.rating)
        LunchPlaceDistance(lunchPlace.distance)
        LunchPlaceAddress(lunchPlace.address)
        LunchPlaceOpenness(lunchPlace.isOpen, shouldShowText = true)
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun LunchPlacePhoto(
    uri: Uri?,
    modifier: Modifier = Modifier,
    isThumbnail: Boolean = false
) {
    val cornerRadius = photoCornerRadius(isThumbnail)
    val placeholderDrawable = photoPlaceholderDrawable()

    GlideImage(
        modifier = if (isThumbnail) {
            modifier.size(dimensionResource(R.dimen.thumbnail_size))
        } else {
            modifier.fillMaxWidth()
        },
        model = uri,
        loading = placeholder(placeholderDrawable),
        failure = placeholder(placeholderDrawable),
        transition = CrossFade,
        contentDescription = if (isThumbnail) {
            stringResource(R.string.lunch_place_thumbnail_description)
        } else {
            stringResource(R.string.lunch_place_photo_description)
        }
    ) {
        it.transform(
            MultiTransformation(
                if (isThumbnail) CenterCrop() else FitCenter(),
                RoundedCorners(cornerRadius)
            )
        )
    }
}

@Composable
private fun LunchPlaceName(
    name: String,
    modifier: Modifier = Modifier,
    isTextLarge: Boolean = false
) {
    Text(
        modifier = modifier,
        maxLines = if (isTextLarge) 2 else 1,
        overflow = TextOverflow.Ellipsis,
        style = titleTextStyle(isTextLarge),
        text = name
    )
}

@Composable
private fun LunchPlaceRating(
    rating: Double?,
    modifier: Modifier = Modifier,
    isTextLarge: Boolean = false
) {
    if (rating == null) return

    val roundedRating = rating.roundToDecimalPlaces(decimalPlaceCount = 1)

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            style = bodyTextStyle(isTextLarge),
            text = roundedRating.toString(),
        )
        SmallRatingIndicator(
            modifier = Modifier.padding(start = 2.dp, bottom = 2.dp),
            rating = roundedRating.toFloat()
        )
    }
}

@Composable
private fun LunchPlaceDistance(
    distance: Float,
    modifier: Modifier = Modifier,
    isTextLarge: Boolean = false
) {
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
        modifier = modifier,
        style = bodyTextStyle(isTextLarge),
        text = distanceText
    )
}

@Composable
private fun LunchPlaceAddress(
    address: String?,
    modifier: Modifier = Modifier,
    isTextLarge: Boolean = false
) {
    if (address == null) return

    Text(
        modifier = modifier,
        style = bodyTextStyle(isTextLarge),
        text = address
    )
}

@Composable
private fun LunchPlaceOpenness(
    isOpen: Boolean?,
    modifier: Modifier = Modifier,
    shouldShowText: Boolean = false,
    isTextLarge: Boolean = false
) {
    if (isOpen != false) return

    val tintColor = MaterialTheme.colorScheme.error
    if (shouldShowText) {
        Text(
            modifier = modifier,
            style = bodyTextStyle(isTextLarge),
            color = tintColor,
            text = stringResource(R.string.closedness_label)
        )
    } else {
        Icon(
            modifier = modifier,
            painter = painterResource(R.drawable.ic_closed),
            tint = tintColor,
            contentDescription = stringResource(R.string.closedness_icon_description)
        )
    }
}

@Composable
private fun photoCornerRadius(isThumbnail: Boolean = false): Int {
    val context = LocalContext.current
    return context.resources.getDimensionPixelSize(
        if (isThumbnail) R.dimen.thumbnail_corner_radius else R.dimen.photo_corner_radius
    )
}

@Composable
private fun photoPlaceholderDrawable(): Drawable? {
    val context = LocalContext.current
    val inactiveColor = MaterialTheme.colorScheme.secondary

    val rawDrawable = context.getDrawable(R.drawable.ic_photo_placeholder)
    return rawDrawable?.apply {
        mutate()
        colorFilter = PorterDuffColorFilter(inactiveColor.toArgb(), PorterDuff.Mode.SRC_IN)
    }
}

@Composable
private fun titleTextStyle(isTextLarge: Boolean): TextStyle =
    if (isTextLarge) {
        MaterialTheme.typography.headlineMedium
    } else {
        MaterialTheme.typography.bodyLarge
    }

@Composable
private fun bodyTextStyle(isTextLarge: Boolean): TextStyle =
    if (isTextLarge) {
        MaterialTheme.typography.bodyLarge
    } else {
        MaterialTheme.typography.bodyMedium
    }

@Preview(showBackground = true)
@Composable
private fun LunchPlaceItemPreview() {
    AppTheme {
        LunchPlaceItem(
            index = 0,
            lunchPlace = MockLunchPlace,
            onNavigateToDetails = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun LunchPlaceBarPreview() {
    AppTheme {
        LunchPlaceBar(
            lunchPlace = MockLunchPlace,
            scrollBehavior = null,
            onNavigateUp = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LunchPlaceContentPreview() {
    AppTheme {
        LunchPlaceContent(lunchPlace = MockLunchPlace)
    }
}