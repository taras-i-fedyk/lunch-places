package com.tarasfedyk.lunchplaces.biz.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationSnapshot(
    val latLng: LatLng,
    val accuracy: Float
) : Parcelable