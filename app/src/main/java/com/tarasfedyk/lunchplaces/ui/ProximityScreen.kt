package com.tarasfedyk.lunchplaces.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.ui.data.MapConfig
import com.tarasfedyk.lunchplaces.ui.data.MapViewport
import com.tarasfedyk.lunchplaces.ui.theme.AppTheme
import com.tarasfedyk.lunchplaces.ui.util.LocationIcon
import com.tarasfedyk.lunchplaces.ui.util.TopBarDefaults
import com.tarasfedyk.lunchplaces.ui.util.UpNavigationIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProximityScreen(
    onSetMapConfig: (MapConfig) -> Unit,
    originPoint: LatLng,
    lunchPlace: LunchPlace,
    onNavigateUp: () -> Unit
) {
    val onSwitchMapToProximityMode = remember(onSetMapConfig, originPoint, lunchPlace.point) {
        {
            val mapViewport = MapViewport(
                originPoint = originPoint, destinationPoint = lunchPlace.point
            )
            val mapConfig = MapConfig(
                isMapVisible = true, mapViewport = mapViewport
            )
            onSetMapConfig(mapConfig)
        }
    }
    val onSwitchMapToDefaultMode = remember(onSetMapConfig) {
        {
            val mapConfig = MapConfig()
            onSetMapConfig(mapConfig)
        }
    }

    Surface(tonalElevation = TopBarDefaults.TonalElevation) {
        Column {
            TopAppBar(
                title = { LunchPlaceName(lunchPlace.name, isTextLarge = true) },
                navigationIcon = { UpNavigationIcon(onNavigateUp) },
                actions = { LocationIcon(onExploreLocation = onSwitchMapToProximityMode) },
            )
            LunchPlaceDistance(lunchPlace.distance, modifier = Modifier.padding(16.dp))
        }
    }

    DisposableEffect(onSetMapConfig, originPoint, lunchPlace.point) {
        onSwitchMapToProximityMode()

        onDispose {
            onSwitchMapToDefaultMode()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProximityScreenPreview() {
    AppTheme {
        ProximityScreen(
            onSetMapConfig = {},
            originPoint = LatLng(0.0, 0.0),
            lunchPlace = LunchPlace(
                id = "ChIJRx5D7mzdOkcR8MgRrmieLvc",
                name = "Pizza Calcio",
                rating = 3.8,
                point = LatLng(49.842306799999996, 24.034497899999998),
                distance = 2923.3997f,
                address = "вулиця Підвальна, 9, Львів, Львівська область, Україна, 79000",
                isOpen = false,
                thumbnailUri = Uri.parse("https://lh3.googleusercontent.com/places/ANXAkqFiFHd0LKC_e89MhGD3GjL6zEhZkkkowyR5_CxLn1keGgxNIBCcbNfNUzc7gqQoib29wBCkwN5M0INME092a5PLgCUtdSUZVn4=s4800-w192-h192"),
                photoUri = Uri.parse("https://lh3.googleusercontent.com/places/ANXAkqFiFHd0LKC_e89MhGD3GjL6zEhZkkkowyR5_CxLn1keGgxNIBCcbNfNUzc7gqQoib29wBCkwN5M0INME092a5PLgCUtdSUZVn4=s4800-w1920-h1080")
            ),
            onNavigateUp = {}
        )
    }
}