package com.tarasfedyk.lunchplaces.store

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.tarasfedyk.lunchplaces.biz.Repo
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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
        return searchByTextTask.await(cancellationTokenSource)
            .places.toLunchPlaces(currentLatLng)
    }

    private suspend fun List<Place>.toLunchPlaces(currentLatLng: LatLng,): List<LunchPlace> =
        withContext(Dispatchers.Default) {
            map { place ->
                val distanceResults = FloatArray(size = 3)
                Location.distanceBetween(
                    currentLatLng.latitude, currentLatLng.longitude,
                    place.latLng!!.latitude, place.latLng!!.longitude,
                    distanceResults
                )

                LunchPlace(
                    id = place.id!!,
                    name = place.name!!,
                    rating = place.rating,
                    latLng = place.latLng!!,
                    distance = distanceResults[0],
                    address = place.address
                )
            }
        }
}