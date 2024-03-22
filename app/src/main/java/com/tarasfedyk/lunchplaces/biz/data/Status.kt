package com.tarasfedyk.lunchplaces.biz.data

sealed class Status {
    data object Pending : Status()
    sealed class Terminal : Status()
    data class Success<T>(val result: T) : Terminal()
    data class Failure(val exception: Exception? = null) : Terminal()
    data object Cancel : Terminal()
}