package com.tarasfedyk.lunchplaces.ui.util

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalInspectionMode
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionsTracker(
    onNoLocationPermissionGranted: () -> Unit,
    onSolelyCoarseLocationPermissionGranted: () -> Unit,
    onFineLocationPermissionGranted: () -> Unit
) {
    // TODO: when it becomes possible, call a Preview-friendly library function instead
    val locationPermissionsState = safelyRememberMultiplePermissionsState(
        permissions = listOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
    )

    val isNoLocationPermissionGranted =
        !locationPermissionsState.isPermissionGranted(ACCESS_COARSE_LOCATION) &&
        !locationPermissionsState.isPermissionGranted(ACCESS_FINE_LOCATION)
    val isSolelyCoarseLocationPermissionGranted =
        locationPermissionsState.isPermissionGranted(ACCESS_COARSE_LOCATION) &&
        !locationPermissionsState.isPermissionGranted(ACCESS_FINE_LOCATION)
    val isFineLocationPermissionGranted =
        locationPermissionsState.isPermissionGranted(ACCESS_FINE_LOCATION)

    LaunchedEffect(isNoLocationPermissionGranted, onNoLocationPermissionGranted) {
        if (isNoLocationPermissionGranted) {
            onNoLocationPermissionGranted()
        }
    }
    LaunchedEffect(isSolelyCoarseLocationPermissionGranted, onSolelyCoarseLocationPermissionGranted) {
        if (isSolelyCoarseLocationPermissionGranted) {
            onSolelyCoarseLocationPermissionGranted()
        }
    }
    LaunchedEffect(isFineLocationPermissionGranted, onFineLocationPermissionGranted) {
        if (isFineLocationPermissionGranted) {
            onFineLocationPermissionGranted()
        } else {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun safelyRememberMultiplePermissionsState(
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

@OptIn(ExperimentalPermissionsApi::class)
private fun MultiplePermissionsState.isPermissionGranted(permission: String): Boolean =
    permissions.any { it.permission == permission && it.status.isGranted }
