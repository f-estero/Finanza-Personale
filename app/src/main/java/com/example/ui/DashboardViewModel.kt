package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MarginRepository
import com.example.data.TransactionEntity
import com.example.data.TransactionWithCategory
import com.example.data.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Calendar

import com.example.data.MonthlyTrend

data class TrendPoint(val monthLabel: String, val margin: Double, val isCurrentMonth: Boolean)

data class DashboardState(
    val entrate: Double = 0.0,
    val speseFisse: Double = 0.0,
    val investimenti: Double = 0.0,
    val speseVariabili: Double = 0.0,
    val budgetDisponibile: Double = 0.0,
    val margineReale: Double = 0.0,
    val speseSuperflue: Double = 0.0,
    val percentualSuperflua: Double = 0.0,
    val recentTransactions: List<TransactionWithCategory> = emptyList()
)

class DashboardViewModel(private val repository: MarginRepository) : ViewModel() {

    private val _selectedCalendar = MutableStateFlow(Calendar.getInstance())
    
    val selectedMonthLabel: StateFlow<String> = _selectedCalendar.map { cal ->
        val monthStr = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.ITALY) ?: ""
        val year = cal.get(Calendar.YEAR)
        "${monthStr.replaceFirstChar { it.uppercase() }} $year"
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")
    
    val isCurrentMonth: StateFlow<Boolean> = _selectedCalendar.map { cal ->
        val now = Calendar.getInstance()
        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) && cal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
    }.stateIn(viewModelScope, SharingStarted.Lazily, true)

    val trendData: StateFlow<List<TrendPoint>> = repository.getMonthlyTrends(
        Calendar.getInstance().apply { add(Calendar.MONTH, -5); set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }.timeInMillis
    ).map { trends ->
        // trends is ordered by monthYear ASC
        // We aggregate Margine Reale for each monthYear:
        // Entrata - Spesa Fissa - Investimento - Spesa Variabile
        val grouped = trends.groupBy { it.monthYear }
        val nowMonth = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.ITALY).format(java.util.Date())
        
        grouped.map { (monthYear, records) ->
            val entrate = records.filter { it.type == TransactionType.ENTRATA }.sumOf { it.total }
            val sumOut = records.filter { it.type != TransactionType.ENTRATA }.sumOf { it.total }
            val margine = entrate - sumOut
            val label = if (monthYear.length >= 7) monthYear.substring(5) else monthYear
            TrendPoint(monthLabel = label, margin = margine, isCurrentMonth = monthYear == nowMonth)
        }.takeLast(6)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val dashboardState: StateFlow<DashboardState> = _selectedCalendar.flatMapLatest { cal ->
        val startCal = cal.clone() as Calendar
        startCal.set(Calendar.DAY_OF_MONTH, 1)
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)
        val startOfMonth = startCal.timeInMillis

        val endCal = cal.clone() as Calendar
        endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH))
        endCal.set(Calendar.HOUR_OF_DAY, 23)
        endCal.set(Calendar.MINUTE, 59)
        endCal.set(Calendar.SECOND, 59)
        endCal.set(Calendar.MILLISECOND, 999)
        val endOfMonth = endCal.timeInMillis

        repository.getTransactionsForPeriod(startOfMonth, endOfMonth).map { transactions ->
            val entrate = transactions.filter { it.category.type == TransactionType.ENTRATA }.sumOf { it.transaction.amount }
            val speseFisse = transactions.filter { it.category.type == TransactionType.SPESA_FISSA }.sumOf { it.transaction.amount }
            val investimenti = transactions.filter { it.category.type == TransactionType.INVESTIMENTO }.sumOf { it.transaction.amount }
            val speseVariabili = transactions.filter { it.category.type == TransactionType.SPESA_VARIABILE }.sumOf { it.transaction.amount }
            
            val budgetDisponibile = entrate - speseFisse - investimenti
            val margineReale = budgetDisponibile - speseVariabili
            
            val speseSuperflue = transactions.filter { it.category.type == TransactionType.SPESA_VARIABILE && it.transaction.isSuperfluous }.sumOf { it.transaction.amount }
            val percentualSuperflua = if (speseVariabili > 0) (speseSuperflue / speseVariabili) * 100 else 0.0

            DashboardState(
                entrate = entrate,
                speseFisse = speseFisse,
                investimenti = investimenti,
                speseVariabili = speseVariabili,
                budgetDisponibile = budgetDisponibile,
                margineReale = margineReale,
                speseSuperflue = speseSuperflue,
                percentualSuperflua = percentualSuperflua,
                recentTransactions = transactions
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardState()
    )

    fun goToPreviousMonth() {
        _selectedCalendar.value = (_selectedCalendar.value.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
    }
    
    fun goToNextMonth() {
        val now = Calendar.getInstance()
        val next = (_selectedCalendar.value.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
        if (next.before(now) || (next.get(Calendar.YEAR) == now.get(Calendar.YEAR) && next.get(Calendar.MONTH) == now.get(Calendar.MONTH))) {
             _selectedCalendar.value = next
        }
    }
    
    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}
