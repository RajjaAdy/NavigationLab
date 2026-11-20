package com.example.navigationlab.ui.Home

import com.example.navigationlab.data.Student

data class HomeUiState(
    val students: List<Student> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)