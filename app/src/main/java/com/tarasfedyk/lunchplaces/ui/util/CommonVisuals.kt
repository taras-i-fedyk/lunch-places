package com.tarasfedyk.lunchplaces.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tarasfedyk.lunchplaces.R

@Composable
fun UpNavigationIcon(onNavigateUp: () -> Unit) {
    IconButton(onClick = onNavigateUp) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = stringResource(R.string.up_navigation_icon_description)
        )
    }
}