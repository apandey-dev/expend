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
    version = 4,
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN iconId TEXT NOT NULL DEFAULT 'ic_cat_general'")
            }
        }

        private val defaultExpenseCategories = listOf(
            Pair("Food & Dining", "ic_cat_food"),
            Pair("Groceries", "ic_cat_groceries"),
            Pair("Transportation", "ic_cat_transport"),
            Pair("Shopping", "ic_cat_shopping"),
            Pair("Bills & Utilities", "ic_cat_bills"),
            Pair("Entertainment", "ic_cat_entertainment"),
            Pair("Health & Care", "ic_cat_health"),
            Pair("Education", "ic_cat_education"),
            Pair("General Expense", "ic_cat_general")
        )

        private val defaultIncomeCategories = listOf(
            Pair("Salary", "ic_cat_salary"),
            Pair("Freelance", "ic_cat_freelance"),
            Pair("Business", "ic_cat_business"),
            Pair("Interest", "ic_cat_interest"),
            Pair("Gift", "ic_cat_gift"),
            Pair("Other Income", "ic_cat_general")
        )

        fun getInstance(context: Context): ExpendDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpendDatabase::class.java,
                    "expend_tracker.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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
                    defaultExpenseCategories.forEach { (catName, iconId) ->
                        initialList.add(CategoryEntity(name = catName, type = "EXPENSE", iconId = iconId))
                    }
                    defaultIncomeCategories.forEach { (catName, iconId) ->
                        initialList.add(CategoryEntity(name = catName, type = "INCOME", iconId = iconId))
                    }
                    database.categoryDao().insertAll(initialList)
                }
            }
        }
    }
}
