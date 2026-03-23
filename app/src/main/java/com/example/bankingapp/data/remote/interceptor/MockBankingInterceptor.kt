package com.example.bankingapp.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This interceptor simulates a backend while keeping Retrofit/OkHttp in the stack.
 * In production the interceptor can be removed and the rest of the app remains unchanged.
 */
@Singleton
class MockBankingInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val json = when {
            request.url.encodedPath.endsWith("auth/login") && request.method == "POST" -> loginResponse()
            request.url.encodedPath.endsWith("accounts") && request.method == "GET" -> accountsResponse()
            request.url.encodedPath.contains("transactions") && request.method == "GET" -> {
                val accountId = request.url.pathSegments[1]
                transactionsResponse(accountId)
            }
            request.url.encodedPath.endsWith("transfer") && request.method == "POST" -> transferResponse()
            else -> errorResponse()
        }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(if (json == errorResponse()) 404 else 200)
            .message(if (json == errorResponse()) "Not Found" else "OK")
            .body(json.toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun loginResponse() = """
        {"userId":"usr_001","token":"mock-token-123","fullName":"Alex Morgan"}
    """.trimIndent()

    private fun accountsResponse() = """
        [
          {"id":"acc_checking","name":"Primary Checking","type":"Checking","accountNumberMasked":"•••• 4821","balance":8450.42,"currency":"USD"},
          {"id":"acc_savings","name":"Rainy Day Savings","type":"Savings","accountNumberMasked":"•••• 9931","balance":17250.09,"currency":"USD"},
          {"id":"acc_credit","name":"Rewards Credit","type":"Credit","accountNumberMasked":"•••• 1208","balance":-420.11,"currency":"USD"}
        ]
    """.trimIndent()

    private fun transactionsResponse(accountId: String): String {
        val now = LocalDateTime.now()
        return """
            [
              {"id":"${accountId}_txn_1","accountId":"$accountId","title":"Coffee House","description":"Morning espresso","amount":5.65,"timestampIso":"${now.minusDays(1)}","isDebit":true},
              {"id":"${accountId}_txn_2","accountId":"$accountId","title":"Payroll","description":"Monthly salary","amount":4200.00,"timestampIso":"${now.minusDays(3)}","isDebit":false},
              {"id":"${accountId}_txn_3","accountId":"$accountId","title":"Electric Utility","description":"Home bill payment","amount":120.43,"timestampIso":"${now.minusDays(5)}","isDebit":true}
            ]
        """.trimIndent()
    }

    private fun transferResponse() = """
        {"reference":"${UUID.randomUUID()}","message":"Transfer submitted successfully."}
    """.trimIndent()

    private fun errorResponse() = """{"error":"Endpoint not found"}"""
}
