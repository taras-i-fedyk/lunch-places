package com.tarasfedyk.lunchplaces.ui.util

import android.content.res.ColorStateList
import android.widget.RatingBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun SmallRatingIndicator(
    rating: Float,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.inversePrimary,
    inactiveColor: Color = MaterialTheme.colorScheme.inverseSurface
) {
    val activeTintList = ColorStateList.valueOf(activeColor.toArgb())
    val inactiveTintList = ColorStateList.valueOf(inactiveColor.toArgb())
    AndroidView(
        modifier = modifier,
        factory = { context ->
            RatingBar(context, null, android.R.attr.ratingBarStyleSmall).apply {
                progressTintList = activeTintList
                secondaryProgressTintList = activeTintList
                progressBackgroundTintList = inactiveTintList
                setRating(rating)
            }
        }
    )
}