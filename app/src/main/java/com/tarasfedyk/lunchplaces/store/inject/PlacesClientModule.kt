package com.tarasfedyk.lunchplaces.store.inject

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.tarasfedyk.lunchplaces.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlacesClientModule {

    @Singleton
    @Provides
    fun providePlacesClient(@ApplicationContext appContext: Context): PlacesClient {
        val placesApiKey = BuildConfig.PLACES_API_KEY
        Places.initializeWithNewPlacesApiEnabled(appContext, placesApiKey)
        return Places.createClient(appContext)
    }
}