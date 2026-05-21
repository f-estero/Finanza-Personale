package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MarginRepository
import com.example.data.TransactionEntity
import com.example.data.CategoryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddTransactionViewModel(private val repository: MarginRepository) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTransaction(amount: Double, categoryId: Int, note: String, isSuperfluous: Boolean, isRecurring: Boolean, recurringDayOfMonth: Int?, dateMillis: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    amount = amount,
                    categoryId = categoryId,
                    note = note,
                    dateMillis = dateMillis,
                    isSuperfluous = isSuperfluous,
                    isRecurring = isRecurring,
                    recurrencePeriod = if (isRecurring) "MONTHLY" else null,
                    recurringDayOfMonth = recurringDayOfMonth,
                    recurringStartMillis = if (isRecurring) dateMillis else null,
                    recurringActive = isRecurring
                )
            )
            onComplete()
        }
    }
}
