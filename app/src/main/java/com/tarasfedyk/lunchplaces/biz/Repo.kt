package com.tarasfedyk.lunchplaces.biz

import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter

interface Repo {
    suspend fun searchLunchPlaces(searchFilter: SearchFilter): List<LunchPlace>
}