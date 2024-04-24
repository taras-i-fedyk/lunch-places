package com.tarasfedyk.lunchplaces.biz.util

import android.os.Parcel
import java.math.BigDecimal
import java.math.RoundingMode

fun UByte.circularInc(): UByte =
    if (this < UByte.MAX_VALUE) {
        this.inc()
    } else {
        UByte.MIN_VALUE
    }

fun Float.roundToDecimalPlaces(decimalPlaceCount: Int): Float =
    toDouble().roundToDecimalPlaces(decimalPlaceCount).toFloat()

fun Double.roundToDecimalPlaces(decimalPlaceCount: Int): Double {
    val bigDecimal = BigDecimal(this)
    val roundedBigDecimal = bigDecimal.setScale(decimalPlaceCount, RoundingMode.HALF_UP)
    return roundedBigDecimal.toDouble()
}

fun Parcel.readBool(): Boolean {
    return readInt() != 0
}

fun Parcel.writeBool(bool: Boolean) {
    writeInt(if (bool) 1 else 0)
}