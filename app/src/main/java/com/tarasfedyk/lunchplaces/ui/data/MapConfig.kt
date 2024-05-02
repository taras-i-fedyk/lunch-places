package com.tarasfedyk.lunchplaces.ui.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MapConfig(
    val isMapVisible: Boolean = false,
    val mapTopPadding: Int = 0,
    val mapViewport: MapViewport? = null
) : Parcelable