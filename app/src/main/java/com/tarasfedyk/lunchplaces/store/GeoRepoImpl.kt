package com.tarasfedyk.lunchplaces.store

import android.location.Location
import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.FetchResolvedPhotoUriRequest
import com.google.android.libraries.places.api.net.IsOpenRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.tarasfedyk.lunchplaces.biz.GeoRepo
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.MediaLimits
import com.tarasfedyk.lunchplaces.biz.data.RankPreference
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter
import com.tarasfedyk.lunchplaces.biz.data.SizeLimit
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
class GeoRepoImpl @Inject constructor(
    private val placesClient: PlacesClient
) : GeoRepo {

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun searchLunchPlaces(searchFilter: SearchFilter): List<LunchPlace> {
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.RATING,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            Place.Field.BUSINESS_STATUS,
            Place.Field.CURRENT_OPENING_HOURS,
            Place.Field.OPENING_HOURS,
            Place.Field.UTC_OFFSET,
            Place.Field.PHOTO_METADATAS
        )
        val placeType = PlaceTypes.RESTAURANT
        val rankPreference = when (searchFilter.settings.rankPreference) {
            RankPreference.RELEVANCE -> SearchByTextRequest.RankPreference.RELEVANCE
            RankPreference.DISTANCE -> SearchByTextRequest.RankPreference.DISTANCE
        }
        val circularBounds = CircularBounds.newInstance(
            searchFilter.originPoint, searchFilter.settings.preferredRadius
        )

        val cancellationTokenSource = CancellationTokenSource()
        val searchByTextRequest = SearchByTextRequest
            .builder(searchFilter.input.query, placeFields)
            .setIncludedType(placeType)
            .setRankPreference(rankPreference)
            .setLocationBias(circularBounds)
            .setCancellationToken(cancellationTokenSource.token)
            .build()
        val searchByTextTask = placesClient.searchByText(searchByTextRequest)
        return searchByTextTask.await(cancellationTokenSource).places
            .toLunchPlaces(searchFilter.originPoint, searchFilter.input.mediaLimits)
    }

    private suspend fun List<Place>.toLunchPlaces(
        originPoint: LatLng,
        mediaLimits: MediaLimits
    ): List<LunchPlace> = coroutineScope {
        val lunchPlacesDeferred = map { place ->
            async { place.toLunchPlace(originPoint, mediaLimits) }
        }
        lunchPlacesDeferred.awaitAll()
    }

    private suspend fun Place.toLunchPlace(
        originPoint: LatLng,
        mediaLimits: MediaLimits
    ): LunchPlace = coroutineScope {
        val distanceDeferred = async { calculateDistance(originPoint) }
        val isOpenDeferred = async { determineIfOpen() }
        val thumbnailUriDeferred = async { obtainPhotoUri(mediaLimits.thumbnailSizeLimit) }
        val photoUriDeferred = async { obtainPhotoUri(mediaLimits.photoSizeLimit) }
        joinAll(distanceDeferred, isOpenDeferred, thumbnailUriDeferred, photoUriDeferred)
        val distance = distanceDeferred.await()
        val isOpen = isOpenDeferred.await()
        val thumbnailUri = thumbnailUriDeferred.await()
        val photoUri = photoUriDeferred.await()

        LunchPlace(
            id = id!!,
            name = name!!,
            rating = rating,
            point = latLng!!,
            distance = distance,
            address = address,
            isOpen = isOpen,
            thumbnailUri = thumbnailUri,
            photoUri = photoUri
        )
    }

    private suspend fun Place.calculateDistance(
        originPoint: LatLng
    ): Float = withContext(Dispatchers.Default) {
        val distanceResults = FloatArray(size = 3)
        Location.distanceBetween(
            latLng!!.latitude, latLng!!.longitude,
            originPoint.latitude, originPoint.longitude,
            distanceResults
        )
        distanceResults[0]
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun Place.determineIfOpen(): Boolean? {
        val cancellationTokenSource = CancellationTokenSource()
        val isOpenRequest = IsOpenRequest
            .builder(this)
            .setCancellationToken(cancellationTokenSource.token)
            .build()
        val isOpenTask = placesClient.isOpen(isOpenRequest)
        return isOpenTask.await(cancellationTokenSource).isOpen
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun Place.obtainPhotoUri(
        photoSizeLimit: SizeLimit
    ): Uri? {
        val photoMetadata = photoMetadatas?.firstOrNull() ?: return null

        val cancellationTokenSource = CancellationTokenSource()
        val photoUriRequest = FetchResolvedPhotoUriRequest
            .builder(photoMetadata)
            .setMaxWidth(photoSizeLimit.maxWidth)
            .setMaxHeight(photoSizeLimit.maxHeight)
            .setCancellationToken(cancellationTokenSource.token)
            .build()
        return placesClient.fetchResolvedPhotoUri(photoUriRequest).await(cancellationTokenSource).uri
    }
}