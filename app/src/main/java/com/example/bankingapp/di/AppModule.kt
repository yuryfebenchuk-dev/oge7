package com.example.bankingapp.di

import android.content.Context
import androidx.room.Room
import com.example.bankingapp.data.local.BankingDatabase
import com.example.bankingapp.data.local.dao.BankingDao
import com.example.bankingapp.data.remote.api.BankingApiService
import com.example.bankingapp.data.remote.interceptor.MockBankingInterceptor
import com.example.bankingapp.data.repository.BankingRepositoryImpl
import com.example.bankingapp.domain.repository.BankingRepository
import com.example.bankingapp.domain.usecase.LoginUseCase
import com.example.bankingapp.domain.usecase.ObserveAccountsUseCase
import com.example.bankingapp.domain.usecase.ObserveTransactionsUseCase
import com.example.bankingapp.domain.usecase.RefreshAccountsUseCase
import com.example.bankingapp.domain.usecase.RefreshTransactionsUseCase
import com.example.bankingapp.domain.usecase.TransferMoneyUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BankingDatabase =
        Room.databaseBuilder(context, BankingDatabase::class.java, "banking.db").build()

    @Provides
    fun provideBankingDao(database: BankingDatabase): BankingDao = database.bankingDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(mockBankingInterceptor: MockBankingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(mockBankingInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()

    @Provides
    @Singleton
    fun provideBankingApi(client: OkHttpClient): BankingApiService =
        Retrofit.Builder()
            .baseUrl("https://mock.bank.local/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BankingApiService::class.java)

    @Provides
    @Singleton
    fun provideBankingRepository(api: BankingApiService, dao: BankingDao): BankingRepository =
        BankingRepositoryImpl(api = api, dao = dao)

    @Provides fun provideLoginUseCase(repository: BankingRepository) = LoginUseCase(repository)
    @Provides fun provideObserveAccountsUseCase(repository: BankingRepository) = ObserveAccountsUseCase(repository)
    @Provides fun provideRefreshAccountsUseCase(repository: BankingRepository) = RefreshAccountsUseCase(repository)
    @Provides fun provideObserveTransactionsUseCase(repository: BankingRepository) = ObserveTransactionsUseCase(repository)
    @Provides fun provideRefreshTransactionsUseCase(repository: BankingRepository) = RefreshTransactionsUseCase(repository)
    @Provides fun provideTransferMoneyUseCase(repository: BankingRepository) = TransferMoneyUseCase(repository)
}
