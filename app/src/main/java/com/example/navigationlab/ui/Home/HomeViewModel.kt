package com.example.navigationlab.ui.Home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.navigationlab.data.StudentRepository
import com.example.navigationlab.data.preference.UserPreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: StudentRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // State yang bisa diubah hanya ada di dalam ViewModel (private)
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            // 1) Pulihkan query terakhir dari DataStore SEBELUM pencarian dimulai,
            //    sehingga request pertama langsung memakai query yang dipulihkan
            _searchQuery.value = preferencesRepository.lastSearchQuery.first()
            // 2) Mulai mengamati query untuk memuat data
            observeSearch()
            // 3) Simpan query ke DataStore setiap kali berubah
            observeSaveQuery()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    // Simpan query ke DataStore dengan debounce supaya tidak menulis per karakter
    @OptIn(FlowPreview::class)
    private fun observeSaveQuery() {
        viewModelScope.launch {
            searchQuery
                .debounce(500)
                .distinctUntilChanged()
                .collectLatest { value ->
                    preferencesRepository.saveLastSearchQuery(value)
                }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        viewModelScope.launch {
            searchQuery
                .debounce(500)
                .distinctUntilChanged()
                .collectLatest { query ->
                    loadData(query)
                }
        }
    }

    private var refreshJob: Job? = null

    // TUGAS: dipanggil tombol Refresh di HomeScreen.
    // Query kosong -> muat daftar awal, query berisi -> ulangi pencarian
    // dengan query aktif (diputuskan di dalam loadData).
    fun refresh() {
        refreshJob?.cancel() // batalkan refresh sebelumnya yang masih berjalan
        refreshJob = viewModelScope.launch {
            loadData(searchQuery.value)
        }
    }

    // Retry (tombol "Coba Lagi") memakai mekanisme yang sama dengan refresh
    fun retry() = refresh()

    private suspend fun loadData(query: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        try {
            val data = if (query.isBlank()) {
                repository.getStudents()
            } else {
                repository.searchStudents(query.trim())
            }
            _uiState.value = HomeUiState(students = data)
        } catch (e: CancellationException) {
            throw e // jangan diubah menjadi error UI
        } catch (e: Exception) {
            _uiState.value = HomeUiState(
                errorMessage = e.message ?: "Gagal mengambil data"
            )
        }
    }

    companion object {
        fun factory(
            repository: StudentRepository,
            preferencesRepository: UserPreferencesRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(repository, preferencesRepository) as T
                }
            }
    }
}