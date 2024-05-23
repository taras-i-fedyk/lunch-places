package com.tarasfedyk.lunchplaces.store.inject

import com.tarasfedyk.lunchplaces.biz.PlacesRepo
import com.tarasfedyk.lunchplaces.store.PlacesRepoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface PlacesRepoModule {

    @Singleton
    @Binds
    fun bindPlacesRepo(placesRepoImpl: PlacesRepoImpl): PlacesRepo
}