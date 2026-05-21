package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.MarginDatabase
import com.example.data.TransactionType
import java.util.Calendar

class BudgetAlertWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        // We only want to trigger this strictly on the 20th
        if (day != 20) return Result.success()

        val db = MarginDatabase.getDatabase(appContext)
        val startCal = calendar.clone() as Calendar
        startCal.set(Calendar.DAY_OF_MONTH, 1)
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)
        
        val endCal = calendar.clone() as Calendar
        endCal.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        endCal.set(Calendar.HOUR_OF_DAY, 23)
        endCal.set(Calendar.MINUTE, 59)
        endCal.set(Calendar.SECOND, 59)
        
        val txs = db.marginDao().getTransactionsForPeriodSync(startCal.timeInMillis, endCal.timeInMillis)
        
        val categories = db.marginDao().getAllCategoriesSync()
        
        // Sum total monthly budget for SPESA_VARIABILE
        val varBudgets = categories.filter { it.type == TransactionType.SPESA_VARIABILE }.mapNotNull { it.monthlyBudget }.sum()
        
        if (varBudgets <= 0) return Result.success() // No budget set
        
        val varSpent = txs.filter { it.category.type == TransactionType.SPESA_VARIABILE }.sumOf { it.transaction.amount }
        
        val percent = varSpent / varBudgets
        
        if (percent > 0.8) {
            sendNotification("Attenzione Budget", "Hai superato l'80% del budget per le tue spese variabili!")
        }

        return Result.success()
    }

    private fun sendNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && 
            ContextCompat.checkSelfPermission(appContext, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        
        val channelId = "budget_alerts"
        val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Avvisi Budget", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
        
        val builder = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            
        NotificationManagerCompat.from(appContext).notify(101, builder.build())
    }
}
