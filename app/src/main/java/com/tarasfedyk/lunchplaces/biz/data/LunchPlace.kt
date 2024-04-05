package com.tarasfedyk.lunchplaces.biz.data

import android.net.Uri
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class LunchPlace(
    val id: String,
    val name: String,
    val rating: Double?,
    val latLng: LatLng,
    val distance: Float,
    val address: String?,
    val isOpen: Boolean?,
    val thumbnailUri: Uri? = null,
    val imageUri: Uri? = null
) : Parcelable