package com.tarasfedyk.lunchplaces.biz.inject

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
object FusedLocationClientModule {

    @Provides
    fun provideFusedLocationClient(
        @ApplicationContext appContext: Context
    ): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appContext)
}