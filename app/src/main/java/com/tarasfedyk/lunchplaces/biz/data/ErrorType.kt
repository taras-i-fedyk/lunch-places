package com.tarasfedyk.lunchplaces.biz.data

enum class ErrorType {
    INVALID_CONFIG,
    LOCATION_SERVICES,
    LOCATION_PERMISSION,
    CURRENT_LOCATION,
    INTERNET_CONNECTION,
    QUERY_LIMITS,
    UNKNOWN
}