package com.tarasfedyk.lunchplaces.biz

import com.tarasfedyk.lunchplaces.biz.data.LunchPlace

interface Repo {
    suspend fun searchLunchPlaces(query: String): List<LunchPlace>
}