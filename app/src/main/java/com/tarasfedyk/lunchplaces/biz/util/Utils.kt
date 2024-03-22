package com.tarasfedyk.lunchplaces.biz.util

import android.location.Location
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.LatLng

fun Map<String, Boolean>.areAllValuesFalse(): Boolean = values.none { it }

@OptIn(ExperimentalPermissionsApi::class)
fun MultiplePermissionsState.isPermissionGranted(permission: String): Boolean =
    permissions.any {
        it.permission == permission && it.status.isGranted
    }

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberMultiplePermissionsStateWrapper(
    permissions: List<String>,
    onPermissionsResult: (Map<String, Boolean>) -> Unit = {},
): MultiplePermissionsState =
    if (LocalInspectionMode.current) {
        object : MultiplePermissionsState {
            override val permissions: List<PermissionState> = emptyList()
            override val revokedPermissions: List<PermissionState> = emptyList()
            override val allPermissionsGranted: Boolean = false
            override val shouldShowRationale: Boolean = false
            override fun launchMultiplePermissionRequest() {}
        }
    } else {
        rememberMultiplePermissionsState(permissions, onPermissionsResult)
    }

fun Location.toLatLng(): LatLng = LatLng(latitude, longitude)