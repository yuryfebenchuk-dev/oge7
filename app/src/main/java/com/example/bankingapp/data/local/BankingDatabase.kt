package com.example.bankingapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.bankingapp.data.local.dao.BankingDao
import com.example.bankingapp.data.local.entity.AccountEntity
import com.example.bankingapp.data.local.entity.TransactionEntity

@Database(
    entities = [AccountEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class BankingDatabase : RoomDatabase() {
    abstract fun bankingDao(): BankingDao
}
