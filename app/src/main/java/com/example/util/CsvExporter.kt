package com.example.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.TransactionWithCategory
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {
    fun exportToCsv(context: Context, transactions: List<TransactionWithCategory>): Uri {
        val fileName = "Export_Margine_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALY).format(Date())}.csv"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        FileWriter(file).use { writer ->
            writer.append("Data;Categoria;Tipo;Importo;Nota;Superflua;Ricorrente\n")
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALY)
            
            for (tx in transactions) {
                val date = dateFormat.format(Date(tx.transaction.dateMillis))
                val category = tx.category.name.replace(";", ",")
                val type = tx.category.type.name
                val amount = "%.2f".format(Locale.ITALY, tx.transaction.amount) // Uses comma in Italy locale
                val note = (tx.transaction.note ?: "").replace(";", ",")
                val superflua = if (tx.transaction.isSuperfluous) "Si" else "No"
                val ricorrente = if (tx.transaction.isRecurring) "Si" else "No"
                
                writer.append("$date;$category;$type;$amount;$note;$superflua;$ricorrente\n")
            }
        }
        
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
}
