package com.tarasfedyk.lunchplaces.biz.data

sealed class Status<out T> {
    data object Pending : Status<Nothing>()
    sealed class Terminal<T> : Status<T>()
    data class Success<T>(val result: T) : Terminal<T>()
    data class Failure(val exception: Exception? = null) : Terminal<Nothing>()
}