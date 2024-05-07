package com.tarasfedyk.lunchplaces.store.inject

import com.tarasfedyk.lunchplaces.biz.SettingsRepo
import com.tarasfedyk.lunchplaces.store.SettingsRepoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface SettingsRepoModule {

    @Singleton
    @Binds
    fun bindSettingsRepo(settingsRepoImpl: SettingsRepoImpl): SettingsRepo
}