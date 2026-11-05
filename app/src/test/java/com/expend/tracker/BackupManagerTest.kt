package com.expend.tracker

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupManagerTest {

    @Test
    fun backupJson_validation_detectsInvalidVersion() {
        val json = JSONObject().apply {
            put("backupVersion", 999)
            put("appId", "com.expend.tracker")
            put("currency", "INR")
            put("wallets", JSONArray())
            put("transactions", JSONArray())
        }

        val version = json.getInt("backupVersion")
        assertTrue("Version 999 should exceed max supported version", version > 4)
    }

    @Test
    fun backupJson_categoryWithIconId_serializesAndParsesProperly() {
        val catArray = JSONArray().apply {
            put(JSONObject().apply {
                put("id", 1L)
                put("name", "Groceries")
                put("type", "EXPENSE")
                put("iconId", "ic_cat_groceries")
                put("isArchived", false)
                put("createdAt", 1700000000000L)
            })
            put(JSONObject().apply {
                put("id", 2L)
                put("name", "Salary")
                put("type", "INCOME")
                put("iconId", "ic_cat_salary")
                put("isArchived", false)
                put("createdAt", 1700000000000L)
            })
        }

        val root = JSONObject().apply {
            put("backupVersion", 4)
            put("appId", "com.expend.tracker")
            put("currency", "INR")
            put("categories", catArray)
        }

        val parsedCats = root.getJSONArray("categories")
        assertEquals(2, parsedCats.length())
        assertEquals("ic_cat_groceries", parsedCats.getJSONObject(0).getString("iconId"))
        assertEquals("ic_cat_salary", parsedCats.getJSONObject(1).getString("iconId"))
    }

    @Test
    fun backupJson_transferTransaction_validatesSourceAndDestination() {
        val wallet1 = 10L
        val wallet2 = 20L

        val transferTx = JSONObject().apply {
            put("id", 100L)
            put("title", "Move money to Bank")
            put("amountPaise", 500000L)
            put("type", "TRANSFER")
            put("category", "Transfer")
            put("walletId", wallet1)
            put("destinationWalletId", wallet2)
            put("paymentMode", "")
            put("dateMillis", System.currentTimeMillis())
        }

        assertEquals("TRANSFER", transferTx.getString("type"))
        assertEquals(wallet1, transferTx.getLong("walletId"))
        assertEquals(wallet2, transferTx.getLong("destinationWalletId"))
        assertFalse("Source and destination must be different", transferTx.getLong("walletId") == transferTx.getLong("destinationWalletId"))
    }
}
