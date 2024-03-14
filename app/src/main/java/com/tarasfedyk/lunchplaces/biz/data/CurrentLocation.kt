package com.tarasfedyk.lunchplaces.biz.data

import android.location.Location

data class CurrentLocation(
    val value: Location? = null,
    val exception: Exception? = null
)
