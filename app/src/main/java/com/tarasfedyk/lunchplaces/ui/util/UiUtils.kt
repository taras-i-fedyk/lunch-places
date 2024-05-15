package com.tarasfedyk.lunchplaces.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

fun Context.isGooglePlayServicesAvailable(): Boolean {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val connectionResult = googleApiAvailability.isGooglePlayServicesAvailable(this)
    return connectionResult == ConnectionResult.SUCCESS
}

fun Context.goToAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.fromParts("package", packageName, null)
    startActivity(intent)
}