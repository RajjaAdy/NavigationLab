package com.example.navigationlab.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.navigationlab.ui.Home.HomeUiState

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenDetail: (Int) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenFavorites: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Home",
                style = MaterialTheme.typography.headlineMedium
            )
            // TUGAS: tombol Refresh hanya mengirim event ke ViewModel
            Button(
                onClick = onRefresh,
                enabled = !uiState.isLoading
            ) {
                Text("Refresh")
            }
        }

        // Pencarian reaktif: tidak ada tombol Cari, request berjalan
        // otomatis setelah pengguna berhenti mengetik (debounce di ViewModel)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Cari profil") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        when {
            uiState.isLoading -> Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            uiState.errorMessage != null -> Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
            ) {
                Text(uiState.errorMessage)
                Button(onClick = onRetry) {
                    Text("Coba Lagi")
                }
            }

            uiState.students.isEmpty() -> Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Data tidak ditemukan")
            }

            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.students, key = { it.id }) { student ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenDetail(student.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("ID: ${student.id}")
                            Text(
                                text = student.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(student.university)
                        }
                    }
                }
            }
        }

        OutlinedButton(onClick = onOpenFavorites) {
            Text("Buka Favorit")
        }

        OutlinedButton(onClick = onOpenProfile) {
            Text("Buka Profile")
        }

        OutlinedButton(onClick = onOpenAbout) {
            Text("Buka About")
        }
    }
}