package com.example.navigationlab.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.navigationlab.data.preference.UserPreferencesRepository
import com.example.navigationlab.data.repository.FavoriteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoriteRepository: FavoriteRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Gabungkan dua sumber reaktif: daftar favorit (Room) + urutan (DataStore).
    // Setiap perubahan di salah satunya otomatis menghasilkan UI state baru,
    // jadi hapus favorit dari DetailScreen langsung tercermin di sini.
    val uiState: StateFlow<FavoritesUiState> = combine(
        favoriteRepository.observeFavorites(),
        preferencesRepository.sortAscending
    ) { favorites, ascending ->
        FavoritesUiState(
            favorites = if (ascending) {
                favorites.sortedBy { it.firstName.lowercase() }
            } else {
                favorites.sortedByDescending { it.firstName.lowercase() }
            },
            sortAscending = ascending,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FavoritesUiState()
    )

    fun toggleSortOrder() {
        viewModelScope.launch {
            val current = preferencesRepository.sortAscending.first()
            preferencesRepository.saveSortAscending(!current)
        }
    }

    companion object {
        fun factory(
            favoriteRepository: FavoriteRepository,
            preferencesRepository: UserPreferencesRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FavoritesViewModel(favoriteRepository, preferencesRepository) as T
                }
            }
    }
}