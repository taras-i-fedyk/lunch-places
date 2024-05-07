package com.tarasfedyk.lunchplaces.biz

import com.tarasfedyk.lunchplaces.biz.data.SearchSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepo {
    val searchSettingsFlow: Flow<SearchSettings?>

    suspend fun setSearchSettings(searchSettings: SearchSettings)
}