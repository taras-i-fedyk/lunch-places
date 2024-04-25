package com.tarasfedyk.lunchplaces.ui.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.tarasfedyk.lunchplaces.R
import com.tarasfedyk.lunchplaces.biz.util.circularInc

@Composable
fun PermanentErrorSnackbar(
    snackbarHostState: SnackbarHostState,
    errorMessage: String,
    onRetry: () -> Unit,
    isAppSettingsError: Boolean = false
) {
    val context = LocalContext.current
    var snackbarAppearanceId by remember { mutableStateOf(UByte.MIN_VALUE) }

    val actionLabel = if (isAppSettingsError) {
        stringResource(R.string.resolve_action_label)
    } else {
        stringResource(R.string.retry_action_label)
    }

    LaunchedEffect(snackbarAppearanceId) {
        val snackbarResult = snackbarHostState.showSnackbar(
            message = errorMessage,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Indefinite
        )
        when (snackbarResult) {
            SnackbarResult.ActionPerformed -> {
                if (isAppSettingsError) {
                    snackbarAppearanceId = snackbarAppearanceId.circularInc()
                    context.goToAppSettings()
                } else {
                    onRetry()
                }
            }
            SnackbarResult.Dismissed -> {}
        }
    }
}