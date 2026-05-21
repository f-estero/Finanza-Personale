package com.example.data

import kotlinx.coroutines.flow.Flow

class MarginRepository(private val marginDao: MarginDao) {

    val allCategories: Flow<List<CategoryEntity>> = marginDao.getAllCategories()
    val allTransactions: Flow<List<TransactionWithCategory>> = marginDao.getAllTransactions()

    fun getTransactionsForPeriod(startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>> {
        return marginDao.getTransactionsForPeriod(startDate, endDate)
    }

    fun getMonthlyTrends(startDate: Long): Flow<List<MonthlyTrend>> {
        return marginDao.getMonthlyTrends(startDate)
    }

    suspend fun insertTransaction(transaction: TransactionEntity) {
        marginDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        marginDao.deleteTransaction(transaction)
    }

    fun getRecurringTransactions(): Flow<List<TransactionWithCategory>> = marginDao.getRecurringTransactions()

    suspend fun getActiveRecurringTransactions(): List<TransactionEntity> = marginDao.getActiveRecurringTransactions()

    suspend fun countRecurringInMonth(categoryId: Int, startOfMonth: Long, endOfMonth: Long): Int = marginDao.countRecurringInMonth(categoryId, startOfMonth, endOfMonth)

    suspend fun insertCategories(categories: List<CategoryEntity>) {
        marginDao.insertCategories(categories)
    }

    suspend fun checkAndInsertRecurringTransactions() {
        // Find current month start and end
        val calendar = java.util.Calendar.getInstance()
        val today = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        calendar.add(java.util.Calendar.MONTH, 1)
        calendar.add(java.util.Calendar.MILLISECOND, -1)
        val endOfMonth = calendar.timeInMillis

        val activeRecurring = getActiveRecurringTransactions()
        
        for (transaction in activeRecurring) {
            val day = transaction.recurringDayOfMonth ?: continue
            // Only insert if the recurring day has passed or is today
            if (today >= day) {
                val count = countRecurringInMonth(transaction.categoryId, startOfMonth, endOfMonth)
                if (count == 0) {
                    // We need to insert for this month
                    val insertCalendar = java.util.Calendar.getInstance()
                    insertCalendar.set(java.util.Calendar.DAY_OF_MONTH, day)
                    // Insert the copy
                    insertTransaction(
                        transaction.copy(
                            id = 0,
                            dateMillis = insertCalendar.timeInMillis
                        )
                    )
                }
            }
        }
    }
}
