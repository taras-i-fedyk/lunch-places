package com.tarasfedyk.lunchplaces.biz.data

enum class LocationPermissionLevel {
    NONE,
    COARSE_ONLY,
    FINE
}

val LocationPermissionLevel?.isCoarseOrFine: Boolean
    get() = (this == LocationPermissionLevel.COARSE_ONLY || this == LocationPermissionLevel.FINE)