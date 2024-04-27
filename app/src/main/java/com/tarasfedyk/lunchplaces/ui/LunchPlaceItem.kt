package com.tarasfedyk.lunchplaces.ui

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
import com.tarasfedyk.lunchplaces.biz.util.roundToDecimalPlaces
import com.tarasfedyk.lunchplaces.ui.data.ThumbnailAspects
import com.tarasfedyk.lunchplaces.ui.util.SmallRatingIndicator
import kotlin.math.roundToInt

@Composable
fun LunchPlaceItem(lunchPlace: LunchPlace, thumbnailAspects: ThumbnailAspects) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LunchPlaceThumbnail(lunchPlace.thumbnailUri, thumbnailAspects)

        Spacer(modifier = Modifier.size(16.dp))

        Column {
            LunchPlaceName(lunchPlace.name)

            val spacerSize = 2.dp
            LunchPlaceRating(lunchPlace.rating, spacerSize)
            Row(verticalAlignment = Alignment.CenterVertically) {
                LunchPlaceDistance(lunchPlace.distance)
                Spacer(modifier = Modifier.size(spacerSize))
                LunchPlaceOpenness(lunchPlace.isOpen)
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun LunchPlaceThumbnail(thumbnailUri: Uri?, thumbnailAspects: ThumbnailAspects) {
    GlideImage(
        modifier = Modifier.size(dimensionResource(R.dimen.thumbnail_size)),
        model = thumbnailUri,
        loading = placeholder(thumbnailAspects.placeholderDrawable),
        failure = placeholder(thumbnailAspects.placeholderDrawable),
        transition = CrossFade,
        contentDescription = stringResource(R.string.lunch_place_thumbnail_description)
    ) {
        it.transform(
            MultiTransformation(
                CenterCrop(),
                RoundedCorners(thumbnailAspects.cornerRadius)
            )
        )
    }
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
private fun LunchPlaceRating(rating: Double?, spacerSize: Dp) {
    if (rating == null) return

    val roundedRating = rating.roundToDecimalPlaces(decimalPlaceCount = 1)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = roundedRating.toString(),
        )
        Spacer(modifier = Modifier.size(spacerSize))
        SmallRatingIndicator(
            modifier = Modifier.padding(bottom = 2.dp),
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
private fun LunchPlaceOpenness(isOpen: Boolean?) {
    if (isOpen != false) return

    Icon(
        painter = painterResource(R.drawable.ic_closed),
        tint = MaterialTheme.colorScheme.error,
        contentDescription = null
    )
}