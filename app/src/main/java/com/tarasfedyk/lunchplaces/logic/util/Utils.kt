package com.tarasfedyk.lunchplaces.logic.util

import android.os.Parcel

fun Parcel.readBool(): Boolean {
    return readInt() != 0
}

fun Parcel.writeBool(bool: Boolean) {
    writeInt(if (bool) 1 else 0)
}