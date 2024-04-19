package com.tarasfedyk.lunchplaces.ui

import androidx.annotation.MainThread
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class RootVM @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val searchActivenessFlow: StateFlow<Boolean> = savedStateHandle.getStateFlow(
        Keys.SEARCH_ACTIVENESS,
        initialValue = false
    )

    @MainThread
    fun setSearchActiveness(isSearchActive: Boolean) {
        savedStateHandle[Keys.SEARCH_ACTIVENESS] = isSearchActive
    }

    private object Keys {
        const val SEARCH_ACTIVENESS: String = "search_activeness"
    }
}