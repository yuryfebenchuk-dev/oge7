package com.example.bankingapp.data.repository

import com.example.bankingapp.data.local.dao.BankingDao
import com.example.bankingapp.data.mapper.toDomain
import com.example.bankingapp.data.mapper.toEntity
import com.example.bankingapp.data.remote.api.BankingApiService
import com.example.bankingapp.data.remote.dto.LoginRequestDto
import com.example.bankingapp.data.remote.dto.TransferRequestDto
import com.example.bankingapp.domain.model.Account
import com.example.bankingapp.domain.model.Transaction
import com.example.bankingapp.domain.model.TransferReceipt
import com.example.bankingapp.domain.model.UserSession
import com.example.bankingapp.domain.repository.BankingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
/**
 * Repository is the single coordination point between remote and local data sources.
 * Room stays the source of truth so presentation can always collect stable flows,
 * even while refresh operations are happening in the background.
 */
class BankingRepositoryImpl @Inject constructor(
    private val api: BankingApiService,
    private val dao: BankingDao,
) : BankingRepository {

    override val cachedAccounts: Flow<List<Account>> = dao.observeAccounts().map { entities ->
        entities.map { it.toDomain() }
    }

    override fun cachedTransactions(accountId: String): Flow<List<Transaction>> =
        dao.observeTransactions(accountId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun login(username: String, password: String): Result<UserSession> = runCatching {
        require(username.isNotBlank()) { "Username is required." }
        require(password.length >= 4) { "Password must be at least 4 characters." }
        withContext(Dispatchers.IO) {
            api.login(LoginRequestDto(username = username, password = password)).toDomain()
        }
    }

    override suspend fun refreshAccounts(): Result<List<Account>> = runCatching {
        withContext(Dispatchers.IO) {
            val accounts = api.getAccounts()
            dao.clearAccounts()
            dao.upsertAccounts(accounts.map { it.toEntity() })
            accounts.map { it.toEntity().toDomain() }
        }
    }

    override suspend fun refreshTransactions(accountId: String): Result<List<Transaction>> = runCatching {
        withContext(Dispatchers.IO) {
            val transactions = api.getTransactions(accountId)
            dao.clearTransactions(accountId)
            dao.upsertTransactions(transactions.map { it.toEntity() })
            transactions.map { it.toEntity().toDomain() }
        }
    }

    override suspend fun transferMoney(
        accountId: String,
        recipient: String,
        amount: Double,
        note: String,
    ): Result<TransferReceipt> = runCatching {
        require(accountId.isNotBlank()) { "Please choose an account." }
        require(recipient.isNotBlank()) { "Recipient is required." }
        require(amount > 0.0) { "Amount must be greater than zero." }
        withContext(Dispatchers.IO) {
            api.transfer(
                TransferRequestDto(
                    accountId = accountId,
                    recipient = recipient.trim(),
                    amount = amount,
                    note = note.trim(),
                ),
            ).toDomain()
        }
    }
}
