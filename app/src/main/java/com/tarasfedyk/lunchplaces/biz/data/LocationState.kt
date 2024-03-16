package com.tarasfedyk.lunchplaces.biz.data

import android.location.Location

data class LocationState(
    val currentLocation: Location? = null,
    val currentLocationError: Exception? = null
)