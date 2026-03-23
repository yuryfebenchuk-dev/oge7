package com.example.bankingapp.domain.usecase

import com.example.bankingapp.domain.model.Account
import com.example.bankingapp.domain.model.Transaction
import com.example.bankingapp.domain.repository.BankingRepository
import kotlinx.coroutines.flow.Flow

class LoginUseCase(private val repository: BankingRepository) {
    suspend operator fun invoke(username: String, password: String) = repository.login(username, password)
}

class ObserveAccountsUseCase(private val repository: BankingRepository) {
    operator fun invoke(): Flow<List<Account>> = repository.cachedAccounts
}

class RefreshAccountsUseCase(private val repository: BankingRepository) {
    suspend operator fun invoke() = repository.refreshAccounts()
}

class ObserveTransactionsUseCase(private val repository: BankingRepository) {
    operator fun invoke(accountId: String): Flow<List<Transaction>> = repository.cachedTransactions(accountId)
}

class RefreshTransactionsUseCase(private val repository: BankingRepository) {
    suspend operator fun invoke(accountId: String) = repository.refreshTransactions(accountId)
}

class TransferMoneyUseCase(private val repository: BankingRepository) {
    suspend operator fun invoke(accountId: String, recipient: String, amount: Double, note: String) =
        repository.transferMoney(accountId, recipient, amount, note)
}
