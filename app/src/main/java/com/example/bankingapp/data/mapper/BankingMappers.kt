package com.example.bankingapp.data.mapper

import com.example.bankingapp.data.local.entity.AccountEntity
import com.example.bankingapp.data.local.entity.TransactionEntity
import com.example.bankingapp.data.remote.dto.AccountDto
import com.example.bankingapp.data.remote.dto.LoginResponseDto
import com.example.bankingapp.data.remote.dto.TransactionDto
import com.example.bankingapp.data.remote.dto.TransferResponseDto
import com.example.bankingapp.domain.model.Account
import com.example.bankingapp.domain.model.Transaction
import com.example.bankingapp.domain.model.TransferReceipt
import com.example.bankingapp.domain.model.UserSession
import java.time.LocalDateTime

fun LoginResponseDto.toDomain() = UserSession(userId = userId, token = token, fullName = fullName)

fun AccountDto.toEntity() = AccountEntity(
    id = id,
    name = name,
    type = type,
    accountNumberMasked = accountNumberMasked,
    balance = balance,
    currency = currency,
)

fun AccountEntity.toDomain() = Account(
    id = id,
    name = name,
    type = type,
    accountNumberMasked = accountNumberMasked,
    balance = balance,
    currency = currency,
)

fun TransactionDto.toEntity() = TransactionEntity(
    id = id,
    accountId = accountId,
    title = title,
    description = description,
    amount = amount,
    timestampIso = timestampIso,
    isDebit = isDebit,
)

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    accountId = accountId,
    title = title,
    description = description,
    amount = amount,
    timestamp = LocalDateTime.parse(timestampIso),
    isDebit = isDebit,
)

fun TransferResponseDto.toDomain() = TransferReceipt(reference = reference, message = message)
