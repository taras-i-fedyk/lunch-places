package com.tarasfedyk.lunchplaces.data.inject

import com.tarasfedyk.lunchplaces.biz.Repo
import com.tarasfedyk.lunchplaces.data.RepoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepoModule {

    @Singleton
    @Binds
    fun bindRepo(repoImpl: RepoImpl): Repo
}