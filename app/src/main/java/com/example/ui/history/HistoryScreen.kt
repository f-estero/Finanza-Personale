package com.example.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.data.MarginRepository
import com.example.data.TransactionWithCategory
import com.example.ui.dashboard.TransactionRow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HistoryFilter(
    val categoryId: Int? = null,
    val amountGreaterThan: Double? = null,
    val amountLessThan: Double? = null,
    val fromMonthZeroIndexed: Int? = null
)

class HistoryViewModel(private val repository: MarginRepository) : ViewModel() {
    private val allTransactions = repository.allTransactions
    val filter = MutableStateFlow(HistoryFilter())
    
    val transactions = combine(allTransactions, filter) { txs, f ->
        var filtered = txs
        if (f.categoryId != null) {
            filtered = filtered.filter { it.category.id == f.categoryId }
        }
        if (f.amountGreaterThan != null) {
            filtered = filtered.filter { it.transaction.amount > f.amountGreaterThan }
        }
        if (f.amountLessThan != null) {
            filtered = filtered.filter { it.transaction.amount < f.amountLessThan }
        }
        if (f.fromMonthZeroIndexed != null) {
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.MONTH, f.fromMonthZeroIndexed)
            cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            val startTime = cal.timeInMillis
            filtered = filtered.filter { it.transaction.dateMillis >= startTime }
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categories = repository.allCategories.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

class HistoryViewModelFactory(private val repository: MarginRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) return HistoryViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel, onBack: () -> Unit) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storico Movimenti", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Drop-down chips / filters
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box {
                    var expanded by remember { mutableStateOf(false) }
                        FilterChip(
                            selected = filter.categoryId != null,
                            onClick = { expanded = true },
                            label = { Text(if (filter.categoryId != null) categories.find { it.id == filter.categoryId }?.name ?: "Categoria" else "Tutte Le Categorie") },
                            leadingIcon = { if (filter.categoryId != null) Icon(Icons.Default.Check, contentDescription = null) }
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("Tutte Le Categorie") }, onClick = { viewModel.filter.value = filter.copy(categoryId = null); expanded = false })
                            categories.forEach { cat ->
                                DropdownMenuItem(text = { Text(cat.name) }, onClick = { viewModel.filter.value = filter.copy(categoryId = cat.id); expanded = false })
                            }
                        }
                    }
                
                Box {
                    var expanded by remember { mutableStateOf(false) }
                        FilterChip(
                            selected = filter.amountGreaterThan != null || filter.amountLessThan != null,
                            onClick = { expanded = true },
                            label = { 
                                val text = if (filter.amountGreaterThan != null) "> 50€" else if (filter.amountLessThan != null) "< 50€" else "Importo"
                                Text(text) 
                            },
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("Tutti") }, onClick = { viewModel.filter.value = filter.copy(amountGreaterThan = null, amountLessThan = null); expanded = false })
                            DropdownMenuItem(text = { Text("> 50€") }, onClick = { viewModel.filter.value = filter.copy(amountGreaterThan = 50.0, amountLessThan = null); expanded = false })
                            DropdownMenuItem(text = { Text("< 50€") }, onClick = { viewModel.filter.value = filter.copy(amountLessThan = 50.0, amountGreaterThan = null); expanded = false })
                        }
                    }
                
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
                    val monthNames = arrayOf("Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno", "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre")
                    FilterChip(
                        selected = filter.fromMonthZeroIndexed != null,
                        onClick = { expanded = true },
                        label = {
                            val monthIdx = filter.fromMonthZeroIndexed
                            Text(if (monthIdx != null) "Da: ${monthNames[monthIdx]}" else "Data")
                        }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("Tutti") }, onClick = { viewModel.filter.value = filter.copy(fromMonthZeroIndexed = null); expanded = false })
                        for (i in 0..currentMonth) {
                            DropdownMenuItem(text = { Text("Da: ${monthNames[i]}") }, onClick = { viewModel.filter.value = filter.copy(fromMonthZeroIndexed = i); expanded = false })
                        }
                    }
                }
            }

            if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nessun movimento trovato.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions, key = { it.transaction.id }) { tx ->
                        TransactionRow(tx, emptyList())
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}
