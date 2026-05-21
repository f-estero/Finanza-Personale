package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [CategoryEntity::class, TransactionEntity::class], version = 5, exportSchema = false)
abstract class MarginDatabase : RoomDatabase() {
    abstract fun marginDao(): MarginDao

    private class MarginDatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val defaultCategories = listOf(
                        CategoryEntity(name = "Stipendio", type = TransactionType.ENTRATA, colorHex = "#4CAF50", iconName = "payments"),
                        CategoryEntity(name = "Rimborsi Lavoro", type = TransactionType.ENTRATA, colorHex = "#81C784", iconName = "work"),
                        CategoryEntity(name = "Dividendi", type = TransactionType.ENTRATA, colorHex = "#A5D6A7", iconName = "trending_up"),
                        CategoryEntity(name = "Entrate Occasionali", type = TransactionType.ENTRATA, colorHex = "#C8E6C9", iconName = "attach_money"),

                        CategoryEntity(name = "Affitto", type = TransactionType.SPESA_FISSA, colorHex = "#F44336", iconName = "home"),
                        CategoryEntity(name = "Utenze", type = TransactionType.SPESA_FISSA, colorHex = "#EF9A9A", iconName = "bolt"),
                        CategoryEntity(name = "Abbonamenti", type = TransactionType.SPESA_FISSA, colorHex = "#E57373", iconName = "subscriptions"),
                        CategoryEntity(name = "Assicurazioni", type = TransactionType.SPESA_FISSA, colorHex = "#EF5350", iconName = "shield"),
                        CategoryEntity(name = "Telefono", type = TransactionType.SPESA_FISSA, colorHex = "#FF5252", iconName = "phone"),

                        CategoryEntity(name = "Supermercato", type = TransactionType.SPESA_VARIABILE, colorHex = "#FF9800", iconName = "local_grocery_store"),
                        CategoryEntity(name = "Ristoranti", type = TransactionType.SPESA_VARIABILE, colorHex = "#FFB74D", iconName = "restaurant"),
                        CategoryEntity(name = "Trasporti", type = TransactionType.SPESA_VARIABILE, colorHex = "#FFA726", iconName = "directions_car"),
                        CategoryEntity(name = "Salute", type = TransactionType.SPESA_VARIABILE, colorHex = "#FFCC02", iconName = "local_hospital"),
                        CategoryEntity(name = "Abbigliamento", type = TransactionType.SPESA_VARIABILE, colorHex = "#FFE082", iconName = "checkroom"),
                        CategoryEntity(name = "Svago", type = TransactionType.SPESA_VARIABILE, colorHex = "#FF7043", iconName = "mood"),
                        CategoryEntity(name = "Viaggi", type = TransactionType.SPESA_VARIABILE, colorHex = "#FF8A65", iconName = "flight_takeoff"),
                        CategoryEntity(name = "Spese Digitali", type = TransactionType.SPESA_VARIABILE, colorHex = "#FFAB40", iconName = "devices"),

                        CategoryEntity(name = "PAC S&P 500", type = TransactionType.INVESTIMENTO, colorHex = "#2196F3", iconName = "show_chart"),
                        CategoryEntity(name = "PAC MSCI World", type = TransactionType.INVESTIMENTO, colorHex = "#42A5F5", iconName = "public"),
                        CategoryEntity(name = "PAC Emerging", type = TransactionType.INVESTIMENTO, colorHex = "#64B5F6", iconName = "terrain"),
                        CategoryEntity(name = "PAC Obbligazioni", type = TransactionType.INVESTIMENTO, colorHex = "#90CAF9", iconName = "account_balance"),
                        CategoryEntity(name = "PAC Gold", type = TransactionType.INVESTIMENTO, colorHex = "#BBDEFB", iconName = "diamond"),
                        CategoryEntity(name = "Investimento Occasionale", type = TransactionType.INVESTIMENTO, colorHex = "#E3F2FD", iconName = "payments")
                    )
                    database.marginDao().insertCategories(defaultCategories)
                }
            }
        }
    }

    companion object {
        @Volatile
        var INSTANCE: MarginDatabase? = null

        fun getDatabase(context: Context): MarginDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MarginDatabase::class.java,
                    "margin_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(MarginDatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
