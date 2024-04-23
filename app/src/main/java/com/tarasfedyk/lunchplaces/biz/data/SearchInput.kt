package com.tarasfedyk.lunchplaces.biz.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchInput(
    val query: String,
    val thumbnailSizeLimit: SizeLimit = SizeLimit(),
    val photoSizeLimit: SizeLimit = SizeLimit()
) : Parcelable