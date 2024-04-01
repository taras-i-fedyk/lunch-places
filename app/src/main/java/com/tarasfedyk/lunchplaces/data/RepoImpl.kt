package com.tarasfedyk.lunchplaces.data

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.tarasfedyk.lunchplaces.logic.Repo
import com.tarasfedyk.lunchplaces.logic.model.LunchPlace
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepoImpl @Inject constructor(
    private val placesClient: PlacesClient
) : Repo {

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun searchLunchPlaces(
        query: String,
        currentLatLng: LatLng,
        radius: Double,
        shouldRankByDistance: Boolean
    ): List<LunchPlace> {
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.RATING,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS
        )
        val placeType = PlaceTypes.RESTAURANT
        val circularBounds = CircularBounds.newInstance(currentLatLng, radius)
        val rankPreference = if (shouldRankByDistance) {
            SearchByTextRequest.RankPreference.DISTANCE
        } else {
            SearchByTextRequest.RankPreference.RELEVANCE
        }
        val cancellationTokenSource = CancellationTokenSource()

        val searchByTextRequest = SearchByTextRequest
            .builder(query, placeFields)
            .setIncludedType(placeType)
            .setLocationBias(circularBounds)
            .setRankPreference(rankPreference)
            .setCancellationToken(cancellationTokenSource.token)
            .build()
        val searchByTextTask = placesClient.searchByText(searchByTextRequest)
        return searchByTextTask.await(cancellationTokenSource).places.toLunchPlaces()
    }

    private fun List<Place>.toLunchPlaces(): List<LunchPlace> =
        map { place ->
            LunchPlace(
                id = place.id!!,
                name = place.name!!,
                rating = place.rating,
                latLng = place.latLng!!,
                address = place.address
            )
        }
}