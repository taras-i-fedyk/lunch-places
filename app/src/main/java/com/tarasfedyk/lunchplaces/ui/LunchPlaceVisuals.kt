package com.tarasfedyk.lunchplaces.ui

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.tarasfedyk.lunchplaces.R
import com.tarasfedyk.lunchplaces.biz.util.roundToDecimalPlaces
import com.tarasfedyk.lunchplaces.ui.util.SmallRatingIndicator
import kotlin.math.roundToInt

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LunchPlacePhoto(
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
fun LunchPlaceName(
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
fun LunchPlaceRating(
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
fun LunchPlaceDistance(
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
fun LunchPlaceAddress(
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
fun LunchPlaceOpenness(
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