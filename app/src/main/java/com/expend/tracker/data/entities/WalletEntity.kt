package com.expend.tracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.expend.tracker.data.models.WalletType

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val initialBalancePaise: Long = 0L,
    val walletType: String = "CASH",
    val description: String = "",
    val isArchived: Boolean = false,
    val includeInIncome: Boolean = false,
    val creditLimitPaise: Long = 0L,
    val openingOutstandingPaise: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isCreditCard: Boolean
        get() = WalletType.fromString(walletType) == WalletType.CREDIT_CARD
}
