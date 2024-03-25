package com.tarasfedyk.lunchplaces.biz.data

import android.location.Location
import androidx.compose.runtime.Stable

@Stable
data class GeoState(
    val currentLocationStatus: Status<Unit, Location>? = null,
    val lunchPlacesStatus: Status<String, List<LunchPlace>>? = null
)