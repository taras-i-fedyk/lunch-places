package com.tarasfedyk.lunchplaces.store

import com.tarasfedyk.lunchplaces.biz.Repo
import com.tarasfedyk.lunchplaces.biz.data.LunchPlace
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepoImpl @Inject constructor() : Repo {

    override suspend fun searchLunchPlaces(query: String): List<LunchPlace> {
        // TODO: replace this with meaningful logic
        return List(size = 100) { i ->
            LunchPlace(id = (i + 1).toString())
        }
    }
}