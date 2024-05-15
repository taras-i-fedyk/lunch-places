package com.tarasfedyk.lunchplaces.ui.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.tarasfedyk.lunchplaces.R

@Composable
fun PermanentErrorSnackbar(
    snackbarHostState: SnackbarHostState,
    errorId: Int,
    errorMessage: String,
    onRetry: () -> Unit,
    isAppSettingsError: Boolean = false
) {
    val context = LocalContext.current

    var snackbarAppearanceId by remember { mutableStateOf(UByte.MIN_VALUE) }

    val actionLabel = if (isAppSettingsError) {
        stringResource(R.string.resolve_label)
    } else {
        stringResource(R.string.retry_label)
    }

    val onPerformAction = remember(context, isAppSettingsError, onRetry) {
        {
            if (isAppSettingsError) {
                ++snackbarAppearanceId
                context.goToAppSettings()
            } else {
                onRetry()
            }
        }
    }
    val currentOnPerformAction by rememberUpdatedState(onPerformAction)

    LaunchedEffect(snackbarHostState, snackbarAppearanceId, errorId, errorMessage, actionLabel) {
        val snackbarResult = snackbarHostState.showSnackbar(
            message = errorMessage,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Indefinite
        )
        when (snackbarResult) {
            SnackbarResult.ActionPerformed -> currentOnPerformAction()
            SnackbarResult.Dismissed -> {}
        }
    }
}