package com.example.bankingapp.data.remote.api

import com.example.bankingapp.data.remote.dto.AccountDto
import com.example.bankingapp.data.remote.dto.LoginRequestDto
import com.example.bankingapp.data.remote.dto.LoginResponseDto
import com.example.bankingapp.data.remote.dto.TransactionDto
import com.example.bankingapp.data.remote.dto.TransferRequestDto
import com.example.bankingapp.data.remote.dto.TransferResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BankingApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @GET("accounts")
    suspend fun getAccounts(): List<AccountDto>

    @GET("accounts/{accountId}/transactions")
    suspend fun getTransactions(@Path("accountId") accountId: String): List<TransactionDto>

    @POST("transfer")
    suspend fun transfer(@Body request: TransferRequestDto): TransferResponseDto
}
