package com.tarasfedyk.lunchplaces.biz

import com.google.android.gms.maps.model.LatLng
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import com.tarasfedyk.lunchplaces.biz.data.SearchFilter

interface Repo {
    suspend fun searchLunchPlaces(
        searchFilter: SearchFilter,
        currentLatLng: LatLng,
        radius: Double = 10000.0,
        shouldRankByDistance: Boolean = false
    ): List<LunchPlace>
}