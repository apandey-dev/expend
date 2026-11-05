package com.expend.tracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String = "EXPENSE",
    val defaultAmountPaise: Long? = null,
    val title: String,
    val category: String,
    val defaultWalletId: Long? = null,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
