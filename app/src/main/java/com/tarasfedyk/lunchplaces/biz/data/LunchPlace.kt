package com.tarasfedyk.lunchplaces.biz.data

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class LunchPlace(
    val id: String,
    val name: String = "",
    val rating: Float = 0.0f,
    val photoReference: String = "",
    val latLng: LatLng = LatLng(0.0, 0.0),
    val distance: Float = 0.0f,
    val isOpen: Boolean = false
) : Parcelable