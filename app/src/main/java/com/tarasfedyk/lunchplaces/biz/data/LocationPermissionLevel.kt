package com.tarasfedyk.lunchplaces.biz.data

enum class LocationPermissionLevel {
    NONE,
    COARSE,
    FINE
}

val LocationPermissionLevel?.isCoarseOrFine: Boolean
    get() = (this == LocationPermissionLevel.COARSE || this == LocationPermissionLevel.FINE)