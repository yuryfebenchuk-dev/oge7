package com.example.bankingapp.data.remote.dto

data class LoginRequestDto(
    val username: String,
    val password: String,
)

data class LoginResponseDto(
    val userId: String,
    val token: String,
    val fullName: String,
)

data class AccountDto(
    val id: String,
    val name: String,
    val type: String,
    val accountNumberMasked: String,
    val balance: Double,
    val currency: String,
)

data class TransactionDto(
    val id: String,
    val accountId: String,
    val title: String,
    val description: String,
    val amount: Double,
    val timestampIso: String,
    val isDebit: Boolean,
)

data class TransferRequestDto(
    val accountId: String,
    val recipient: String,
    val amount: Double,
    val note: String,
)

data class TransferResponseDto(
    val reference: String,
    val message: String,
)
