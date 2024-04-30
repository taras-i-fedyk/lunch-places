package com.tarasfedyk.lunchplaces.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.ui.theme.AppTheme
import com.tarasfedyk.lunchplaces.ui.util.UpNavigationIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(lunchPlace: LunchPlace, onNavigateUp: () -> Unit) {
    val topBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    LunchPlaceName(
                        lunchPlace.name,
                        isTextLarge = true,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                scrollBehavior = topBarScrollBehavior,
                navigationIcon = { UpNavigationIcon(onNavigateUp) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(all = 16.dp)
        ) {
            LunchPlacePhoto(lunchPlace.photoUri)
            Spacer(modifier = Modifier.size(16.dp))
            LunchPlaceRating(lunchPlace.rating)
            LunchPlaceDistance(lunchPlace.distance)
            LunchPlaceAddress(lunchPlace.address)
            LunchPlaceOpenness(lunchPlace.isOpen, shouldShowText = true)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailsScreenPreview() {
    AppTheme {
        DetailsScreen(
            lunchPlace = LunchPlace(
                id = "ChIJRx5D7mzdOkcR8MgRrmieLvc",
                name = "Pizza Calcio",
                rating = 3.8,
                latLng = LatLng(49.842306799999996, 24.034497899999998),
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