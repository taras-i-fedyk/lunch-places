package com.tarasfedyk.lunchplaces.biz

import com.google.android.gms.maps.model.LatLng
import com.tarasfedyk.lunchplaces.biz.model.LunchPlace

interface Repo {
    suspend fun searchLunchPlaces(query: String, currentLatLng: LatLng): List<LunchPlace>
}