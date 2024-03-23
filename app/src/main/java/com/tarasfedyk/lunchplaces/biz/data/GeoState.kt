package com.tarasfedyk.lunchplaces.biz.data

data class GeoState(
    val currentLocationStatus: Status<Location>? = null
)