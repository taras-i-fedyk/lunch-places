package com.tarasfedyk.lunchplaces.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tarasfedyk.lunchplaces.R

object TopBarDefaults {
    val TonalElevation: Dp = 3.dp
}

@Composable
fun UpNavigationIcon(onNavigateUp: () -> Unit) {
    IconButton(onClick = onNavigateUp) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = stringResource(R.string.up_navigation_icon_description)
        )
    }
}