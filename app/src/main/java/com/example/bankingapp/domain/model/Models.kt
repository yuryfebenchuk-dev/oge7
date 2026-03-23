package com.example.bankingapp.domain.model

import java.text.NumberFormat
import java.time.LocalDateTime
import java.util.Locale

data class UserSession(
    val userId: String,
    val token: String,
    val fullName: String,
)

data class Account(
    val id: String,
    val name: String,
    val type: String,
    val accountNumberMasked: String,
    val balance: Double,
    val currency: String,
) {
    fun balanceLabel(): String = NumberFormat.getCurrencyInstance(Locale.US).format(balance)
}

data class Transaction(
    val id: String,
    val accountId: String,
    val title: String,
    val description: String,
    val amount: Double,
    val timestamp: LocalDateTime,
    val isDebit: Boolean,
)

data class TransferReceipt(
    val reference: String,
    val message: String,
)
