package com.tarasfedyk.lunchplaces.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tarasfedyk.lunchplaces.biz.SettingsRepo
import com.tarasfedyk.lunchplaces.biz.data.RankingCriterion
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
                val rankingCriterion = run {
                    val rankingCriterionName = prefs[Keys.RANKING_CRITERION_NAME] ?: return@map null
                    enumValueOf<RankingCriterion>(rankingCriterionName)
                }
                val preferredRadius = prefs[Keys.PREFERRED_RADIUS] ?: return@map null
                SearchSettings(rankingCriterion, preferredRadius)
            } catch (e: IllegalArgumentException) {
                null
            }
        }

    override suspend fun setSearchSettings(searchSettings: SearchSettings) {
        appContext.dataStore.edit { prefs ->
            prefs[Keys.RANKING_CRITERION_NAME] = searchSettings.rankingCriterion.name
            prefs[Keys.PREFERRED_RADIUS] = searchSettings.preferredRadius
        }
    }

    private object Keys {
        val RANKING_CRITERION_NAME: Preferences.Key<String> = stringPreferencesKey(name = "ranking_criterion_name")
        val PREFERRED_RADIUS: Preferences.Key<Double> = doublePreferencesKey(name = "preferred_radius")
    }
}