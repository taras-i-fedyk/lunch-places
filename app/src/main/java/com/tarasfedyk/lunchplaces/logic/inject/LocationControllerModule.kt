package com.tarasfedyk.lunchplaces.logic.inject

import com.tarasfedyk.lunchplaces.logic.LocationController
import com.tarasfedyk.lunchplaces.logic.LocationControllerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
interface LocationControllerModule {

    @Binds
    fun bindLocationController(locationControllerImpl: LocationControllerImpl): LocationController
}