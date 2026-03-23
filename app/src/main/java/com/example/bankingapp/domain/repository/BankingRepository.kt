package com.example.bankingapp.domain.repository

import com.example.bankingapp.domain.model.Account
import com.example.bankingapp.domain.model.Transaction
import com.example.bankingapp.domain.model.TransferReceipt
import com.example.bankingapp.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

interface BankingRepository {
    val cachedAccounts: Flow<List<Account>>
    fun cachedTransactions(accountId: String): Flow<List<Transaction>>
    suspend fun login(username: String, password: String): Result<UserSession>
    suspend fun refreshAccounts(): Result<List<Account>>
    suspend fun refreshTransactions(accountId: String): Result<List<Transaction>>
    suspend fun transferMoney(accountId: String, recipient: String, amount: Double, note: String): Result<TransferReceipt>
}
