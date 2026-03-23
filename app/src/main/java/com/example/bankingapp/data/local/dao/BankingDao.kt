package com.example.bankingapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bankingapp.data.local.entity.AccountEntity
import com.example.bankingapp.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BankingDao {
    @Query("SELECT * FROM accounts ORDER BY balance DESC")
    fun observeAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY timestampIso DESC")
    fun observeTransactions(accountId: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAccounts(accounts: List<AccountEntity>)

    @Query("DELETE FROM accounts")
    suspend fun clearAccounts()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransactions(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE accountId = :accountId")
    suspend fun clearTransactions(accountId: String)
}
