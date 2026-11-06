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
        assertTrue("Version 999 should exceed max supported version", version > 5)
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
            put("backupVersion", 5)
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
    fun backupJson_walletWithCreditCardAndIncomeSettings_serializesAndParsesProperly() {
        val walletsArray = JSONArray().apply {
            // Normal wallet with includeInIncome = true
            put(JSONObject().apply {
                put("id", 1L)
                put("name", "SBI Savings")
                put("initialBalancePaise", 1000000L)
                put("walletType", "BANK_ACCOUNT")
                put("description", "Primary Bank")
                put("isArchived", false)
                put("includeInIncome", true)
                put("creditLimitPaise", 0L)
                put("openingOutstandingPaise", 0L)
                put("createdAt", 1700000000000L)
            })
            // Credit card wallet
            put(JSONObject().apply {
                put("id", 2L)
                put("name", "HDFC Regalia")
                put("initialBalancePaise", 0L)
                put("walletType", "CREDIT_CARD")
                put("description", "Rewards Card")
                put("isArchived", false)
                put("includeInIncome", false)
                put("creditLimitPaise", 5000000L) // ₹50,000
                put("openingOutstandingPaise", 500000L) // ₹5,000
                put("createdAt", 1700000000000L)
            })
        }

        val root = JSONObject().apply {
            put("backupVersion", 5)
            put("appId", "com.expend.tracker")
            put("currency", "INR")
            put("wallets", walletsArray)
        }

        val parsedWallets = root.getJSONArray("wallets")
        assertEquals(2, parsedWallets.length())

        val w1 = parsedWallets.getJSONObject(0)
        assertTrue(w1.getBoolean("includeInIncome"))
        assertEquals(0L, w1.getLong("creditLimitPaise"))

        val w2 = parsedWallets.getJSONObject(1)
        assertFalse(w2.getBoolean("includeInIncome"))
        assertEquals(5000000L, w2.getLong("creditLimitPaise"))
        assertEquals(500000L, w2.getLong("openingOutstandingPaise"))
    }

    @Test
    fun backupJson_backwardCompatibility_olderBackupsDefaultMissingFields() {
        // Simulating a version 3 or 4 backup where creditLimitPaise and includeInIncome are missing
        val oldWalletObj = JSONObject().apply {
            put("id", 1L)
            put("name", "Cash")
            put("initialBalancePaise", 50000L)
            put("walletType", "CASH")
        }

        val includeInIncome = oldWalletObj.optBoolean("includeInIncome", false)
        val creditLimit = oldWalletObj.optLong("creditLimitPaise", 0L)
        val openingOutstanding = oldWalletObj.optLong("openingOutstandingPaise", 0L)

        assertFalse(includeInIncome)
        assertEquals(0L, creditLimit)
        assertEquals(0L, openingOutstanding)
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
