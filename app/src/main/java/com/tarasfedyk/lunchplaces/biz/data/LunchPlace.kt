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
    val point: LatLng,
    val distance: Float,
    val address: String?,
    val isOpen: Boolean?,
    val thumbnailUri: Uri?,
    val photoUri: Uri?
) : Parcelable