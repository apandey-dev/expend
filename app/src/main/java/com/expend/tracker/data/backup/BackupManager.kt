package com.expend.tracker.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.expend.tracker.data.database.ExpendDatabase
import com.expend.tracker.data.entities.CategoryEntity
import com.expend.tracker.data.entities.TemplateEntity
import com.expend.tracker.data.entities.TransactionEntity
import com.expend.tracker.data.entities.WalletEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class BackupManager(
    private val context: Context,
    private val database: ExpendDatabase
) {
    companion object {
        const val CURRENT_BACKUP_VERSION = 3
        const val APP_ID = "com.expend.tracker"
        const val CURRENCY = "INR"
    }

    suspend fun generateBackupJson(): String = withContext(Dispatchers.IO) {
        val wallets = database.walletDao().getAllWalletsFlow().first()
        val transactions = database.transactionDao().getAllTransactionsFlow().first()
        val templates = database.templateDao().getAllTemplatesFlow().first()
        val categories = database.categoryDao().getAllCategoriesFlow().first()

        val root = JSONObject().apply {
            put("backupVersion", CURRENT_BACKUP_VERSION)
            put("appId", APP_ID)
            put("currency", CURRENCY)
            put("exportedAt", System.currentTimeMillis())

            // Wallets
            val walletsArray = JSONArray()
            wallets.forEach { w ->
                val wObj = JSONObject().apply {
                    put("id", w.id)
                    put("name", w.name)
                    put("initialBalancePaise", w.initialBalancePaise)
                    put("walletType", w.walletType)
                    put("description", w.description)
                    put("isArchived", w.isArchived)
                    put("createdAt", w.createdAt)
                    put("updatedAt", w.updatedAt)
                }
                walletsArray.put(wObj)
            }
            put("wallets", walletsArray)

            // Categories
            val catArray = JSONArray()
            categories.forEach { c ->
                val cObj = JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("type", c.type)
                    put("isArchived", c.isArchived)
                    put("createdAt", c.createdAt)
                }
                catArray.put(cObj)
            }
            put("categories", catArray)

            // Transactions
            val txArray = JSONArray()
            transactions.forEach { tx ->
                val txObj = JSONObject().apply {
                    put("id", tx.id)
                    put("title", tx.title)
                    put("amountPaise", tx.amountPaise)
                    put("type", tx.type)
                    put("category", tx.category)
                    put("walletId", tx.walletId)
                    put("paymentMode", tx.paymentMode)
                    if (tx.destinationWalletId != null) {
                        put("destinationWalletId", tx.destinationWalletId)
                    } else {
                        put("destinationWalletId", JSONObject.NULL)
                    }
                    put("dateMillis", tx.dateMillis)
                    put("note", tx.note)
                    put("createdAt", tx.createdAt)
                    put("updatedAt", tx.updatedAt)
                }
                txArray.put(txObj)
            }
            put("transactions", txArray)

            // Templates
            val tplArray = JSONArray()
            templates.forEach { tpl ->
                val tplObj = JSONObject().apply {
                    put("id", tpl.id)
                    put("name", tpl.name)
                    put("type", tpl.type)
                    if (tpl.defaultAmountPaise != null) {
                        put("defaultAmountPaise", tpl.defaultAmountPaise)
                    } else {
                        put("defaultAmountPaise", JSONObject.NULL)
                    }
                    put("title", tpl.title)
                    put("category", tpl.category)
                    if (tpl.defaultWalletId != null) {
                        put("defaultWalletId", tpl.defaultWalletId)
                    } else {
                        put("defaultWalletId", JSONObject.NULL)
                    }
                    put("note", tpl.note)
                    put("createdAt", tpl.createdAt)
                }
                tplArray.put(tplObj)
            }
            put("templates", tplArray)
        }

        root.toString(2)
    }

    suspend fun exportToFile(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = generateBackupJson()
            context.contentResolver.openOutputStream(uri)?.use { os ->
                OutputStreamWriter(os, Charsets.UTF_8).use { writer ->
                    writer.write(json)
                    writer.flush()
                }
            } ?: return@withContext Result.failure(Exception("Cannot open destination stream"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun readJsonFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                reader.readText()
            }
        } ?: throw IllegalArgumentException("Could not open input stream")
    }

    suspend fun restoreFromJson(jsonString: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)

            if (!root.has("backupVersion")) {
                return@withContext Result.failure(IllegalArgumentException("Invalid backup: missing backupVersion"))
            }
            val version = root.getInt("backupVersion")
            if (version > CURRENT_BACKUP_VERSION) {
                return@withContext Result.failure(IllegalArgumentException("Unsupported backup version: $version (maximum supported: $CURRENT_BACKUP_VERSION)"))
            }

            if (!root.has("wallets") || !root.has("transactions")) {
                return@withContext Result.failure(IllegalArgumentException("Invalid backup: missing required data arrays"))
            }

            // Parse Wallets
            val walletsArray = root.getJSONArray("wallets")
            val wallets = ArrayList<WalletEntity>()
            val walletIds = HashSet<Long>()

            for (i in 0 until walletsArray.length()) {
                val obj = walletsArray.getJSONObject(i)
                val id = obj.getLong("id")
                val name = obj.getString("name").trim()
                if (name.isEmpty()) {
                    return@withContext Result.failure(IllegalArgumentException("Wallet record at index $i has an empty name"))
                }
                val initialBalance = obj.optLong("initialBalancePaise", 0L)
                val walletType = obj.optString("walletType", "CASH")
                val description = obj.optString("description", "")
                val isArchived = obj.optBoolean("isArchived", false)
                val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                val updatedAt = obj.optLong("updatedAt", createdAt)

                wallets.add(
                    WalletEntity(
                        id = id,
                        name = name,
                        initialBalancePaise = initialBalance,
                        walletType = walletType,
                        description = description,
                        isArchived = isArchived,
                        createdAt = createdAt,
                        updatedAt = updatedAt
                    )
                )
                walletIds.add(id)
            }

            if (wallets.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("Backup contains no wallets"))
            }

            // Parse Categories
            val categories = ArrayList<CategoryEntity>()
            if (root.has("categories")) {
                val catArray = root.getJSONArray("categories")
                for (i in 0 until catArray.length()) {
                    val obj = catArray.getJSONObject(i)
                    val id = obj.getLong("id")
                    val name = obj.getString("name").trim()
                    val type = obj.getString("type")
                    val isArchived = obj.optBoolean("isArchived", false)
                    val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    categories.add(
                        CategoryEntity(
                            id = id,
                            name = name,
                            type = type,
                            isArchived = isArchived,
                            createdAt = createdAt
                        )
                    )
                }
            }

            // Parse Transactions
            val txArray = root.getJSONArray("transactions")
            val transactions = ArrayList<TransactionEntity>()

            for (i in 0 until txArray.length()) {
                val obj = txArray.getJSONObject(i)
                val id = obj.getLong("id")
                val title = obj.getString("title").trim()
                if (title.isEmpty()) {
                    return@withContext Result.failure(IllegalArgumentException("Transaction at index $i has an empty title"))
                }
                val amount = obj.getLong("amountPaise")
                if (amount <= 0) {
                    return@withContext Result.failure(IllegalArgumentException("Transaction '$title' has invalid amount: $amount"))
                }
                val type = obj.getString("type")
                if (type != "EXPENSE" && type != "INCOME" && type != "TRANSFER") {
                    return@withContext Result.failure(IllegalArgumentException("Transaction '$title' has invalid type: $type"))
                }
                val category = obj.optString("category", if (type == "TRANSFER") "Transfer" else "General")
                val walletId = obj.getLong("walletId")
                if (!walletIds.contains(walletId)) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Transaction '$title' references non-existent wallet ID: $walletId")
                    )
                }

                val destWalletId = if (type == "TRANSFER" && obj.has("destinationWalletId") && !obj.isNull("destinationWalletId")) {
                    val destId = obj.getLong("destinationWalletId")
                    if (!walletIds.contains(destId)) {
                        return@withContext Result.failure(
                            IllegalArgumentException("Transfer '$title' references non-existent destination wallet ID: $destId")
                        )
                    }
                    if (destId == walletId) {
                        return@withContext Result.failure(
                            IllegalArgumentException("Transfer '$title' source and destination wallets cannot be the same")
                        )
                    }
                    destId
                } else null

                val dateMillis = obj.getLong("dateMillis")
                val paymentMode = obj.optString("paymentMode", "")
                val note = obj.optString("note", "")
                val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                val updatedAt = obj.optLong("updatedAt", createdAt)

                transactions.add(
                    TransactionEntity(
                        id = id,
                        title = title,
                        amountPaise = amount,
                        type = type,
                        category = category,
                        walletId = walletId,
                        destinationWalletId = destWalletId,
                        paymentMode = paymentMode,
                        dateMillis = dateMillis,
                        note = note,
                        createdAt = createdAt,
                        updatedAt = updatedAt
                    )
                )
            }

            // Parse Templates
            val templates = ArrayList<TemplateEntity>()
            if (root.has("templates")) {
                val tplArray = root.getJSONArray("templates")
                for (i in 0 until tplArray.length()) {
                    val obj = tplArray.getJSONObject(i)
                    val id = obj.getLong("id")
                    val name = obj.getString("name").trim()
                    val type = obj.optString("type", "EXPENSE")
                    val defaultAmount = if (obj.has("defaultAmountPaise") && !obj.isNull("defaultAmountPaise")) {
                        obj.getLong("defaultAmountPaise")
                    } else null
                    val title = obj.optString("title", name)
                    val category = obj.optString("category", "General")
                    val defaultWalletId = if (obj.has("defaultWalletId") && !obj.isNull("defaultWalletId")) {
                        obj.getLong("defaultWalletId")
                    } else null
                    val note = obj.optString("note", "")
                    val createdAt = obj.optLong("createdAt", System.currentTimeMillis())

                    templates.add(
                        TemplateEntity(
                            id = id,
                            name = name,
                            type = type,
                            defaultAmountPaise = defaultAmount,
                            title = title,
                            category = category,
                            defaultWalletId = defaultWalletId,
                            note = note,
                            createdAt = createdAt
                        )
                    )
                }
            }

            // Atomic restore transaction
            database.withTransaction {
                database.transactionDao().clearAll()
                database.templateDao().clearAll()
                database.walletDao().clearAll()
                if (categories.isNotEmpty()) {
                    database.categoryDao().clearAll()
                    database.categoryDao().insertAll(categories)
                }

                database.walletDao().insertAll(wallets)
                database.transactionDao().insertAll(transactions)
                database.templateDao().insertAll(templates)
            }

            val summaryMessage = "Restored ${wallets.size} wallets, ${transactions.size} transactions, and ${templates.size} templates successfully."
            Result.success(summaryMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
