package com.tarasfedyk.lunchplaces.store

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.IsOpenRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.tarasfedyk.lunchplaces.biz.Repo
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
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
            Place.Field.ADDRESS,
            Place.Field.BUSINESS_STATUS,
            Place.Field.CURRENT_OPENING_HOURS,
            Place.Field.OPENING_HOURS,
            Place.Field.UTC_OFFSET
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

    private suspend fun List<Place>.toLunchPlaces(
        currentLatLng: LatLng
    ): List<LunchPlace> = coroutineScope {
        val lunchPlacesDeferred = map { place ->
            async { place.toLunchPlace(currentLatLng) }
        }
        lunchPlacesDeferred.awaitAll()
    }

    private suspend fun Place.toLunchPlace(
        currentLatLng: LatLng
    ): LunchPlace = coroutineScope {
        val distanceDeferred = async { distanceTo(currentLatLng) }
        val isOpenDeferred = async { isOpenNow() }
        joinAll(distanceDeferred, isOpenDeferred)
        val distance = distanceDeferred.await()
        val isOpen = isOpenDeferred.await()

        LunchPlace(
            id = id!!,
            name = name!!,
            rating = rating,
            latLng = latLng!!,
            distance = distance,
            address = address,
            isOpen = isOpen
        )
    }

    private suspend fun Place.distanceTo(
        currentLatLng: LatLng
    ): Float = withContext(Dispatchers.Default) {
        val distanceResults = FloatArray(size = 3)
        Location.distanceBetween(
            latLng!!.latitude, latLng!!.longitude,
            currentLatLng.latitude, currentLatLng.longitude,
            distanceResults
        )
        distanceResults[0]
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun Place.isOpenNow(): Boolean? {
        val cancellationTokenSource = CancellationTokenSource()
        val isOpenRequest = IsOpenRequest
            .builder(this)
            .setCancellationToken(cancellationTokenSource.token)
            .build()
        val isOpenTask = placesClient.isOpen(isOpenRequest)
        return isOpenTask.await(cancellationTokenSource).isOpen
    }
}