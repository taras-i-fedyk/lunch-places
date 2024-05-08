package com.tarasfedyk.lunchplaces.ui.inject

import android.content.Context
import com.tarasfedyk.lunchplaces.R
import com.tarasfedyk.lunchplaces.biz.data.MediaLimits
import com.tarasfedyk.lunchplaces.biz.data.SizeLimit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object MediaLimitsModule {

    @Provides
    fun provideMediaLimits(@ApplicationContext appContext: Context): MediaLimits {
        val thumbnailSize = appContext.resources.getDimensionPixelSize(R.dimen.thumbnail_size)
        return MediaLimits(
            thumbnailSizeLimit = SizeLimit(
                maxWidth = thumbnailSize,
                maxHeight = thumbnailSize
            )
        )
    }
}