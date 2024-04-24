package com.tarasfedyk.lunchplaces.biz.data

enum class LocationPermissionsLevel {
    NONE, COARSE_ONLY, FINE
}

val LocationPermissionsLevel?.isCoarseOrFine: Boolean
    get() = (this == LocationPermissionsLevel.COARSE_ONLY || this == LocationPermissionsLevel.FINE)