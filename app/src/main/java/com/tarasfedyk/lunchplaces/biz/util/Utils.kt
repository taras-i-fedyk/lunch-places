package com.tarasfedyk.lunchplaces.biz.util

import android.location.Location
import android.os.Parcel
import com.google.android.gms.maps.model.LatLng
import com.tarasfedyk.lunchplaces.biz.data.LocationSnapshot

fun Parcel.readBool(): Boolean {
    return readInt() != 0
}

fun Parcel.writeBool(bool: Boolean) {
    writeInt(if (bool) 1 else 0)
}

fun Location.toLocationSnapshot() = LocationSnapshot(LatLng(latitude, longitude), accuracy)