package com.tarasfedyk.lunchplaces.biz.data

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationSnapshot(
    val point: LatLng,
    val accuracy: Float
) : Parcelable