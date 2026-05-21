package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        androidx.room.Index(value = ["categoryId"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val categoryId: Int,
    val note: String?,
    val dateMillis: Long,
    val isSuperfluous: Boolean,
    val isRecurring: Boolean = false,
    val recurrencePeriod: String? = null, // e.g. "MONTHLY"
    val recurringDayOfMonth: Int? = null,
    val recurringStartMillis: Long? = null,
    val recurringActive: Boolean = false
)
