package com.expend.tracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.expend.tracker.data.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC, id DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC, id DESC LIMIT :limit")
    fun getRecentTransactionsFlow(limit: Int = 5): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE walletId = :walletId OR destinationWalletId = :walletId ORDER BY dateMillis DESC, id DESC")
    fun getTransactionsByWalletFlow(walletId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT COUNT(*) FROM transactions WHERE walletId = :walletId OR destinationWalletId = :walletId")
    suspend fun getTransactionCountForWallet(walletId: Long): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE category = :categoryName")
    suspend fun getTransactionCountForCategory(categoryName: String): Int

    @Query("SELECT DISTINCT paymentMode FROM transactions WHERE paymentMode != '' AND paymentMode IS NOT NULL ORDER BY paymentMode ASC")
    fun getDistinctPaymentModesFlow(): Flow<List<String>>

    @Query("UPDATE transactions SET category = :newCategory, updatedAt = :timestamp WHERE category = :oldCategory")
    suspend fun reassignCategoryTransactions(
        oldCategory: String,
        newCategory: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE transactions SET walletId = :newWalletId, updatedAt = :timestamp WHERE walletId = :oldWalletId")
    suspend fun reassignSourceWalletTransactions(
        oldWalletId: Long,
        newWalletId: Long,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE transactions SET destinationWalletId = :newWalletId, updatedAt = :timestamp WHERE destinationWalletId = :oldWalletId")
    suspend fun reassignDestinationWalletTransactions(
        oldWalletId: Long,
        newWalletId: Long,
        timestamp: Long = System.currentTimeMillis()
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions WHERE walletId = :walletId OR destinationWalletId = :walletId")
    suspend fun deleteTransactionsByWalletId(walletId: Long)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}
