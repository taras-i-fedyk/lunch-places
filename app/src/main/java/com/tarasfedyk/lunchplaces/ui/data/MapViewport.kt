package com.tarasfedyk.lunchplaces.ui.data

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class MapViewport(
    val isFocused: Boolean,
    val originPoint: LatLng,
    val destinationPoint: LatLng
) : Parcelable {

    @IgnoredOnParcel
    val bounds: LatLngBounds = LatLngBounds
        .builder()
        .include(originPoint)
        .include(destinationPoint)
        .build()
}