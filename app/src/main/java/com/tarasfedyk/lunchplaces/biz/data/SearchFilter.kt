package com.tarasfedyk.lunchplaces.biz.data

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchFilter(
    val query: String,
    val mediaLimits: MediaLimits,
    val settings: SearchSettings,
    val originPoint: LatLng = LatLng(0.0, 0.0)
) : Parcelable