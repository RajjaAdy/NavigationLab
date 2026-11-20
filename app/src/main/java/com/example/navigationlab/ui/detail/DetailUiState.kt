package com.example.navigationlab.ui.detail

import com.example.navigationlab.data.Student

data class DetailUiState(
    val student: Student? = null,
    val isLoading: Boolean = false,
    val isFavorite: Boolean = false,
    val errorMessage: String? = null
)