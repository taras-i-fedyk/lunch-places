package com.tarasfedyk.lunchplaces.biz.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchSettings(
    val shouldRankByDistance: Boolean = false,
    val preferredRadius: Double = 10000.0
) : Parcelable