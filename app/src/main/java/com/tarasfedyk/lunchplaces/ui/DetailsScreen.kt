package com.tarasfedyk.lunchplaces.ui

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.model.LatLng
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(lunchPlace: LunchPlace, onNavigateUp: () -> Unit) {
    val topBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        topBar = { LunchPlaceBar(lunchPlace, topBarScrollBehavior, onNavigateUp) }
    ) { paddingValues ->
        LunchPlaceContent(
            modifier = Modifier.padding(paddingValues),
            lunchPlace = lunchPlace
        )
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