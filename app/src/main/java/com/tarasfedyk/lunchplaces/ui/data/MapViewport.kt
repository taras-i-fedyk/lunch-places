package com.tarasfedyk.lunchplaces.ui.data

import android.os.Parcelable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class MapViewport(
    val originPoint: LatLng,
    val destinationPoint: LatLng
) : Parcelable {

    @IgnoredOnParcel
    val bounds: LatLngBounds = LatLngBounds
        .builder()
        .include(originPoint)
        .include(destinationPoint)
        .build()

    companion object {
        val Padding: Dp = 16.dp
    }
}