package com.example.bankingapp.domain.usecase

import com.example.bankingapp.domain.model.UserSession
import com.example.bankingapp.domain.repository.BankingRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginUseCaseTest {
    private val repository = mockk<BankingRepository>()
    private val useCase = LoginUseCase(repository)

    @Test
    fun `returns successful login result`() = runTest {
        coEvery { repository.login("demo.user", "1234") } returns Result.success(
            UserSession(userId = "1", token = "token", fullName = "Alex")
        )

        val result = useCase("demo.user", "1234")

        assertTrue(result.isSuccess)
    }
}
