package com.tarasfedyk.lunchplaces.ui

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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
import com.google.android.gms.maps.model.LatLng
import com.tarasfedyk.lunchplaces.R
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.util.roundToDecimalPlaces
import com.tarasfedyk.lunchplaces.ui.theme.AppTheme
import com.tarasfedyk.lunchplaces.ui.util.SmallRatingIndicator
import kotlin.math.roundToInt

private val MOCK_LUNCH_PLACE = LunchPlace(
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

@Composable
fun LunchPlaceItem(
    lunchPlace: LunchPlace,
    largeSpacerSize: Dp,
    smallSpacerSize: Dp,
    modifier: Modifier = Modifier
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        LunchPlaceThumbnail(lunchPlace.thumbnailUri)
        Spacer(modifier = Modifier.size(largeSpacerSize))
        Column {
            LunchPlaceTitle(lunchPlace.name)
            LunchPlaceRating(lunchPlace.rating, smallSpacerSize)
            Row(verticalAlignment = Alignment.CenterVertically) {
                LunchPlaceDistance(lunchPlace.distance)
                Spacer(modifier = Modifier.size(smallSpacerSize))
                LunchPlaceOpenness(lunchPlace.isOpen)
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun LunchPlaceThumbnail(thumbnailUri: Uri?) {
    val thumbnailCornerRadius = thumbnailCornerRadius()
    val thumbnailPlaceholderDrawable = thumbnailPlaceholderDrawable()

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
private fun thumbnailCornerRadius(): Int {
    val context = LocalContext.current
    return context.resources.getDimensionPixelSize(R.dimen.thumbnail_corner_radius)
}

@Composable
private fun thumbnailPlaceholderDrawable(): Drawable? {
    val context = LocalContext.current
    val contentColor = LocalContentColor.current

    val rawPlaceholderDrawable = context.getDrawable(R.drawable.ic_thumbnail_placeholder)
    return rawPlaceholderDrawable?.apply {
        mutate()
        colorFilter = PorterDuffColorFilter(contentColor.toArgb(), PorterDuff.Mode.SRC_IN)
    }
}

@Composable
private fun LunchPlaceTitle(text: String) {
    Text(
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyLarge,
        text = text
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

@Preview(showBackground = true)
@Composable
private fun LunchPlaceItemPreview() {
    AppTheme {
        LunchPlaceItem(
            lunchPlace = MOCK_LUNCH_PLACE,
            largeSpacerSize = 16.dp,
            smallSpacerSize = 2.dp
        )
    }
}

@Composable
fun LunchPlaceHeadline(text: String) {
    Text(
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.headlineMedium,
        text = text
    )
}

@Preview(showBackground = true)
@Composable
private fun LunchPlaceHeadlinePreview() {
    AppTheme {
        LunchPlaceHeadline(text = MOCK_LUNCH_PLACE.name)
    }
}