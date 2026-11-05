package com.expend.tracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.expend.tracker.data.entities.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets ORDER BY isArchived ASC, name ASC")
    fun getAllWalletsFlow(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE isArchived = 0 ORDER BY name ASC")
    fun getActiveWalletsFlow(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    suspend fun getWalletById(id: Long): WalletEntity?

    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    fun getWalletByIdFlow(id: Long): Flow<WalletEntity?>

    @Query("SELECT COUNT(*) FROM wallets")
    suspend fun getWalletCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(wallets: List<WalletEntity>)

    @Update
    suspend fun updateWallet(wallet: WalletEntity)

    @Query("UPDATE wallets SET isArchived = 1, updatedAt = :timestamp WHERE id = :walletId")
    suspend fun archiveWallet(walletId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE wallets SET isArchived = 0, updatedAt = :timestamp WHERE id = :walletId")
    suspend fun unarchiveWallet(walletId: Long, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteWallet(wallet: WalletEntity)

    @Query("DELETE FROM wallets WHERE id = :id")
    suspend fun deleteWalletById(id: Long)

    @Query("DELETE FROM wallets")
    suspend fun clearAll()
}
