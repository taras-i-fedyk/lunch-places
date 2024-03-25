package com.tarasfedyk.lunchplaces.biz.data

import com.google.android.gms.maps.model.LatLng

data class LunchPlace(
    val id: String,
    val name: String = "",
    val rating: Float = 0.0f,
    val photoReference: String = "",
    val latLng: LatLng = LatLng(0.0, 0.0),
    val distance: Float = 0.0f,
    val isOpen: Boolean = false
)