package com.tarasfedyk.lunchplaces.biz.util

import android.location.Location
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.maps.model.LatLng

fun Map<String, Boolean>.areAllValuesFalse(): Boolean = values.none { it }

@OptIn(ExperimentalPermissionsApi::class)
fun MultiplePermissionsState.isPermissionGranted(permission: String): Boolean =
    permissions.any {
        it.permission == permission && it.status.isGranted
    }

fun Location.toLatLng(): LatLng = LatLng(latitude, longitude)