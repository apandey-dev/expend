package com.expend.tracker.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["walletId"]),
        Index(value = ["destinationWalletId"]),
        Index(value = ["dateMillis"]),
        Index(value = ["type"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amountPaise: Long, // Positive integer minor units (paise)
    val type: String,      // "EXPENSE", "INCOME", or "TRANSFER"
    val category: String,  // Category name or "Transfer"
    val walletId: Long,    // Source wallet for EXPENSE and TRANSFER; target wallet for INCOME
    val destinationWalletId: Long? = null, // Only used when type == "TRANSFER"
    val paymentMode: String = "",      // User-entered mode, initially empty for new transactions
    val dateMillis: Long,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
