package com.tarasfedyk.lunchplaces.biz.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchSettings(
    val rankingCriterion: RankingCriterion = Defaults.RANKING_CRITERION,
    val preferredRadius: Double = Defaults.PREFERRED_RADIUS
) : Parcelable {

    object Defaults {
        val RANKING_CRITERION: RankingCriterion = RankingCriterion.RELEVANCE
        const val PREFERRED_RADIUS: Double = 10000.0
    }
}

enum class RankingCriterion(val displayName: String) {
    RELEVANCE(displayName = "Relevance"),
    DISTANCE(displayName = "Distance")
}