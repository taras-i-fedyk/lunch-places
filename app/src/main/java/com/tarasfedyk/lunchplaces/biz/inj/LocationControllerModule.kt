package com.tarasfedyk.lunchplaces.biz.inj

import com.tarasfedyk.lunchplaces.biz.LocationController
import com.tarasfedyk.lunchplaces.biz.LocationControllerImpl
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