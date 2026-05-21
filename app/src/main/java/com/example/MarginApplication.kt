package com.example

import android.app.Application
import com.example.data.MarginDatabase
import com.example.data.MarginRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.example.worker.BudgetAlertWorker
import java.util.concurrent.TimeUnit

class MarginApplication : Application() {
    private val database by lazy { MarginDatabase.getDatabase(this) }
    val repository by lazy { MarginRepository(database.marginDao()) }

    override fun onCreate() {
        super.onCreate()
        
        val workRequest = PeriodicWorkRequestBuilder<BudgetAlertWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "budget_alert_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        
        CoroutineScope(SupervisorJob()).launch {
            repository.checkAndInsertRecurringTransactions()
        }
    }
}
