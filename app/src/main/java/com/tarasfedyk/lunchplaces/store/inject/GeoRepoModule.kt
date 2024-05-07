package com.tarasfedyk.lunchplaces.store.inject

import com.tarasfedyk.lunchplaces.biz.GeoRepo
import com.tarasfedyk.lunchplaces.store.GeoRepoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface GeoRepoModule {

    @Singleton
    @Binds
    fun bindGeoRepo(geoRepoImpl: GeoRepoImpl): GeoRepo
}