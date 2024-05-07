package com.tarasfedyk.lunchplaces.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tarasfedyk.lunchplaces.biz.SettingsRepo
import com.tarasfedyk.lunchplaces.biz.data.RankPreference
import com.tarasfedyk.lunchplaces.biz.data.SearchSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepoImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
): SettingsRepo {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_repo_impl")

    override val searchSettingsFlow: Flow<SearchSettings?> =
        appContext.dataStore.data.map { prefs ->
            try {
                val rankPreference = run {
                    val rankPreferenceName = prefs[Keys.RankPreferenceName] ?: return@map null
                    enumValueOf<RankPreference>(rankPreferenceName)
                }
                val preferredRadius = prefs[Keys.PreferredRadius] ?: return@map null
                SearchSettings(rankPreference, preferredRadius)
            } catch (e: IllegalArgumentException) {
                null
            }
        }

    override suspend fun setSearchSettings(searchSettings: SearchSettings) {
        appContext.dataStore.edit { prefs ->
            prefs[Keys.RankPreferenceName] = searchSettings.rankPreference.name
            prefs[Keys.PreferredRadius] = searchSettings.preferredRadius
        }
    }

    private object Keys {
        val RankPreferenceName: Preferences.Key<String> = stringPreferencesKey(name = "rank_preference_name")
        val PreferredRadius: Preferences.Key<Double> = doublePreferencesKey(name = "preferred_radius")
    }
}