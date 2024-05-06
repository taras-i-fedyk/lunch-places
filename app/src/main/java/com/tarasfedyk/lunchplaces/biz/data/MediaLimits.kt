package com.tarasfedyk.lunchplaces.biz.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaLimits(
    val thumbnailSizeLimit: SizeLimit = SizeLimit(),
    val photoSizeLimit: SizeLimit = SizeLimit()
) : Parcelable

@Parcelize
data class SizeLimit(
    val maxWidth: Int? = null,
    val maxHeight: Int? = null
) : Parcelable