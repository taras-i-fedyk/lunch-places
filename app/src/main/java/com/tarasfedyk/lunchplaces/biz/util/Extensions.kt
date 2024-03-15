package com.tarasfedyk.lunchplaces.biz.util

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class)
fun MultiplePermissionsState.isAnyPermissionGranted(): Boolean =
    permissions.any {
        it.status.isGranted
    }

@OptIn(ExperimentalPermissionsApi::class)
fun MultiplePermissionsState.isPermissionGranted(permission: String): Boolean =
    permissions.any {
        it.permission == permission && it.status.isGranted
    }