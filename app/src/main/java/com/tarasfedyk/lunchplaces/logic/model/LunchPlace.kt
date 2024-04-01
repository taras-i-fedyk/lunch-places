package com.tarasfedyk.lunchplaces.logic.model

import android.net.Uri
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class LunchPlace(
    val id: String,
    val name: String = "",
    val rating: Double? = null,
    val latLng: LatLng,
    val distance: Float = 0.0f,
    val address: String? = null,
    val isOpen: Boolean = false,
    val thumbnailUri: Uri? = null,
    val imageUri: Uri? = null
) : Parcelable