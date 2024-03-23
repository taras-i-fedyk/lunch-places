package com.tarasfedyk.lunchplaces.biz.data

import com.google.android.gms.maps.model.LatLng

data class Location(
    val latLng: LatLng,
    val accuracy: Float
)