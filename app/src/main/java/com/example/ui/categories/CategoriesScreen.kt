package com.example.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.data.CategoryEntity
import com.example.data.MarginRepository
import com.example.data.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CategoriesViewModel(private val repository: MarginRepository) : ViewModel() {
    val categories = repository.allCategories.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addCategory(name: String, type: TransactionType, color: String, monthlyBudget: Double?) {
        viewModelScope.launch {
            repository.insertCategories(listOf(CategoryEntity(name = name, type = type, colorHex = color, iconName = "lens", monthlyBudget = monthlyBudget)))
        }
    }
}

class CategoriesViewModelFactory(private val repository: MarginRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoriesViewModel::class.java)) return CategoriesViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(viewModel: CategoriesViewModel, onBack: () -> Unit) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorie") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Indietro") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Nuova Categoria")
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp)) {
            val types = TransactionType.values()
            for (type in types) {
                val typeCats = categories.filter { it.type == type }
                if (typeCats.isNotEmpty()) {
                    item {
                        Text(type.displayName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                    }
                    items(typeCats) { cat ->
                        CategoryRow(cat)
                    }
                }
            }
        }

        if (showAddSheet) {
            AddCategorySheet(onDismiss = { showAddSheet = false }, onSave = { name, type, color, budget -> viewModel.addCategory(name, type, color, budget); showAddSheet = false })
        }
    }
}

@Composable
fun CategoryRow(category: CategoryEntity) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor(category.colorHex))))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(category.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                if (category.monthlyBudget != null && category.monthlyBudget > 0) {
                    Text("Budget: ${currencyFormatter.format(category.monthlyBudget)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategorySheet(onDismiss: () -> Unit, onSave: (String, TransactionType, String, Double?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.SPESA_VARIABILE) }
    var budgetStr by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#FF9800") }
    
    val colors = listOf("#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800", "#FF5722")

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Nuova Categoria", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
            
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TransactionType.values().forEachIndexed { index, type ->
                    SegmentedButton(selected = selectedType == type, onClick = { selectedType = type }, shape = SegmentedButtonDefaults.itemShape(index = index, count = TransactionType.values().size)) {
                        Text(type.displayName, fontSize = 12.sp)
                    }
                }
            }
            
            if (selectedType != TransactionType.ENTRATA) {
                OutlinedTextField(value = budgetStr, onValueChange = { budgetStr = it }, label = { Text("Budget Mensile (Opzionale)") }, modifier = Modifier.fillMaxWidth())
            }

            Text("Colore", style = MaterialTheme.typography.labelLarge)
            fun chunkColors() = colors.chunked(8)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (rowColors in chunkColors()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        for (color in rowColors) {
                            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor(color)))
                                .then(if (selectedColor == color) Modifier.background(Color.Black.copy(alpha=0.3f)) else Modifier)
                                .clickable { selectedColor = color })
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onSave(name, selectedType, selectedColor, budgetStr.toDoubleOrNull()) }, modifier = Modifier.fillMaxWidth(), enabled = name.isNotBlank()) {
                Text("Salva")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
