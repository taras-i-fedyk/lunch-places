package com.tarasfedyk.lunchplaces.biz.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchSettings(
    val rankingCriterion: RankingCriterion = DEFAULT_RANKING_CRITERION,
    val preferredRadius: Float = DEFAULT_PREFERRED_RADIUS
) : Parcelable {

    companion object {
        val DEFAULT_RANKING_CRITERION: RankingCriterion = RankingCriterion.RELEVANCE

        const val MIN_PREFERRED_RADIUS: Float = 0f
        const val MAX_PREFERRED_RADIUS: Float = 50000f
        const val DEFAULT_PREFERRED_RADIUS: Float = 10000f
    }
}

enum class RankingCriterion(val displayName: String) {
    RELEVANCE(displayName = "Relevance"),
    DISTANCE(displayName = "Distance")
}