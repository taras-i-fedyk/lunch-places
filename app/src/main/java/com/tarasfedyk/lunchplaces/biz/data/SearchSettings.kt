package com.tarasfedyk.lunchplaces.biz.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchSettings(
    val rankingCriterion: RankingCriterion = RankingCriterion.RELEVANCE,
    val preferredRadius: Double = 10000.0
) : Parcelable

enum class RankingCriterion {
    RELEVANCE,
    DISTANCE
}