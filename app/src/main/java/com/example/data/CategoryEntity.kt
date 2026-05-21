package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: TransactionType,
    val colorHex: String,
    val iconName: String,
    val monthlyBudget: Double? = null
)

enum class TransactionType(val displayName: String) {
    ENTRATA("Entrata"),
    SPESA_FISSA("Fissa"),
    SPESA_VARIABILE("Quotidiana"),
    INVESTIMENTO("Investimento")
}
