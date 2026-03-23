package com.example.bankingapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val accountNumberMasked: String,
    val balance: Double,
    val currency: String,
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val title: String,
    val description: String,
    val amount: Double,
    val timestampIso: String,
    val isDebit: Boolean,
)
