package com.tarasfedyk.lunchplaces.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

fun Context.goToAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.fromParts("package", packageName, null)
    startActivity(intent)
}