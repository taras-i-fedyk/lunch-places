package com.tarasfedyk.lunchplaces.biz.util

import com.google.android.gms.maps.model.LatLng
import com.tarasfedyk.lunchplaces.biz.data.Location

typealias MutableLocation = android.location.Location

fun MutableLocation.asLocation(): Location = Location(
    latLng = LatLng(latitude, longitude),
    accuracy = accuracy
)