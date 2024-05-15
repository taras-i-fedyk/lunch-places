package com.tarasfedyk.lunchplaces.biz.data

enum class ErrorType {
    INVALID_CONFIG,
    LOCATION_SERVICES,
    LOCATION_PERMISSIONS,
    CURRENT_LOCATION,
    INTERNET_CONNECTION,
    QUERY_LIMIT,
    UNKNOWN
}