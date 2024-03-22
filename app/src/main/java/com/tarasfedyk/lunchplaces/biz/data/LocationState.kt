package com.tarasfedyk.lunchplaces.biz.data

import android.location.Location

data class LocationState(
    val currentLocationStatus: Status<Location>? = null
)