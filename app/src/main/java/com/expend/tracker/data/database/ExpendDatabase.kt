package com.expend.tracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.expend.tracker.data.dao.CategoryDao
import com.expend.tracker.data.dao.TemplateDao
import com.expend.tracker.data.dao.TransactionDao
import com.expend.tracker.data.dao.WalletDao
import com.expend.tracker.data.entities.CategoryEntity
import com.expend.tracker.data.entities.TemplateEntity
import com.expend.tracker.data.entities.TransactionEntity
import com.expend.tracker.data.entities.WalletEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        WalletEntity::class,
        TransactionEntity::class,
        TemplateEntity::class,
        CategoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ExpendDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun templateDao(): TemplateDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: ExpendDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add destinationWalletId to transactions table
                db.execSQL("ALTER TABLE transactions ADD COLUMN destinationWalletId INTEGER DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_destinationWalletId ON transactions(destinationWalletId)")

                // Create categories table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL,
                        isArchived INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_categories_type_name ON categories(type, name)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN paymentMode TEXT NOT NULL DEFAULT 'Cash'")
            }
        }

        private val defaultExpenseCategories = listOf(
            "Food & Dining",
            "Groceries",
            "Transportation",
            "Shopping",
            "Bills & Utilities",
            "Entertainment",
            "Health & Care",
            "Education",
            "General Expense"
        )

        private val defaultIncomeCategories = listOf(
            "Salary",
            "Freelance",
            "Business",
            "Interest",
            "Gift",
            "Other Income"
        )

        fun getInstance(context: Context): ExpendDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpendDatabase::class.java,
                    "expend_tracker.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            seedDefaults(context)
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            seedDefaults(context)
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun seedDefaults(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val database = getInstance(context)
                // Note: Per user specification, never automatically seed or insert default wallets.
                // Wallets must only be created explicitly by the user.

                if (database.categoryDao().getCategoryCount() == 0) {
                    val initialList = mutableListOf<CategoryEntity>()
                    defaultExpenseCategories.forEach { cat ->
                        initialList.add(CategoryEntity(name = cat, type = "EXPENSE"))
                    }
                    defaultIncomeCategories.forEach { cat ->
                        initialList.add(CategoryEntity(name = cat, type = "INCOME"))
                    }
                    database.categoryDao().insertAll(initialList)
                }
            }
        }
    }
}
