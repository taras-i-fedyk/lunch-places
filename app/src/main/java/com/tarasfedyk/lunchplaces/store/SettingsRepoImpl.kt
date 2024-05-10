package com.tarasfedyk.lunchplaces.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
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

    override val searchSettingsFlow: Flow<SearchSettings> =
        appContext.dataStore.data.map { prefs ->
            val rankingCriterion = run {
                try {
                    val rankingCriterionName =
                        prefs[Keys.RANKING_CRITERION_NAME] ?:
                        SearchSettings.DEFAULT_RANKING_CRITERION.name
                    enumValueOf<RankingCriterion>(rankingCriterionName)
                } catch (e: IllegalArgumentException) {
                    SearchSettings.DEFAULT_RANKING_CRITERION
                }
            }
            val preferredRadius =
                prefs[Keys.PREFERRED_RADIUS] ?:
                SearchSettings.DEFAULT_PREFERRED_RADIUS
            SearchSettings(rankingCriterion, preferredRadius)
        }

    override suspend fun setSearchSettings(searchSettings: SearchSettings) {
        appContext.dataStore.edit { prefs ->
            prefs[Keys.RANKING_CRITERION_NAME] = searchSettings.rankingCriterion.name
            prefs[Keys.PREFERRED_RADIUS] = searchSettings.preferredRadius
        }
    }

    private object Keys {
        val RANKING_CRITERION_NAME: Preferences.Key<String> =
            stringPreferencesKey(name = "ranking_criterion_name")
        val PREFERRED_RADIUS: Preferences.Key<Float> =
            floatPreferencesKey(name = "preferred_radius")
    }
}