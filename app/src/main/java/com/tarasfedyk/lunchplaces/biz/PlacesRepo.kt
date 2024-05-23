package com.tarasfedyk.lunchplaces.biz

import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter

interface PlacesRepo {
    suspend fun searchForLunchPlaces(searchFilter: SearchFilter): List<LunchPlace>
}