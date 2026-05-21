package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class MonthlyTrend(
    val monthYear: String,
    val type: TransactionType,
    val total: Double
)

@Dao
interface MarginDao {
    @Query("""
        SELECT strftime('%Y-%m', t.dateMillis / 1000, 'unixepoch') as monthYear, 
               c.type as type, 
               SUM(t.amount) as total 
        FROM transactions t 
        INNER JOIN categories c ON t.categoryId = c.id 
        WHERE t.dateMillis >= :startDate 
        GROUP BY monthYear, type
        ORDER BY monthYear ASC
    """)
    fun getMonthlyTrends(startDate: Long): Flow<List<MonthlyTrend>>

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE dateMillis >= :startDate AND dateMillis <= :endDate ORDER BY dateMillis DESC")
    fun getTransactionsForPeriod(startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE isRecurring = 1 ORDER BY dateMillis DESC")
    fun getRecurringTransactions(): Flow<List<TransactionWithCategory>>

    @Query("SELECT * FROM transactions WHERE isRecurring = 1 AND recurringActive = 1")
    suspend fun getActiveRecurringTransactions(): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId AND dateMillis >= :startOfMonth AND dateMillis <= :endOfMonth AND isRecurring = 1")
    suspend fun countRecurringInMonth(categoryId: Int, startOfMonth: Long, endOfMonth: Long): Int

    @Transaction
    @Query("SELECT * FROM transactions WHERE dateMillis >= :startDate AND dateMillis <= :endDate ORDER BY dateMillis DESC")
    suspend fun getTransactionsForPeriodSync(startDate: Long, endDate: Long): List<TransactionWithCategory>

    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesSync(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}
