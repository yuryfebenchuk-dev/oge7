package com.example.bankingapp.presentation.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.domain.model.Transaction
import com.example.bankingapp.domain.usecase.ObserveTransactionsUseCase
import com.example.bankingapp.domain.usecase.RefreshTransactionsUseCase
import com.example.bankingapp.presentation.component.BankingTopBar
import com.example.bankingapp.presentation.component.ErrorBanner
import com.example.bankingapp.presentation.component.LoadingState
import com.example.bankingapp.presentation.component.TransactionRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionsUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeTransactionsUseCase: ObserveTransactionsUseCase,
    private val refreshTransactionsUseCase: RefreshTransactionsUseCase,
) : ViewModel() {
    private val accountId = MutableStateFlow(savedStateHandle.get<String>("accountId").orEmpty())
    private val metaState = MutableStateFlow(TransactionsUiState())

    val uiState: StateFlow<TransactionsUiState> = accountId
        .flatMapLatest { observeTransactionsUseCase(it) }
        .combine(metaState) { transactions, meta -> meta.copy(transactions = transactions) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsUiState())

    fun load(account: String) {
        if (accountId.value != account) accountId.value = account
        refresh(account)
    }

    fun refresh(account: String = accountId.value) {
        viewModelScope.launch {
            metaState.update { it.copy(isLoading = true, error = null) }
            refreshTransactionsUseCase(account)
                .onSuccess { metaState.update { it.copy(isLoading = false) } }
                .onFailure { error -> metaState.update { it.copy(isLoading = false, error = error.message ?: "Unable to load transactions.") } }
        }
    }
}

@Composable
fun TransactionsRoute(viewModel: TransactionsViewModel, accountId: String, onNavigateBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(accountId) { viewModel.load(accountId) }
    TransactionsScreen(state = state, onRetry = { viewModel.refresh(accountId) }, onNavigateBack = onNavigateBack)
}

@Composable
private fun TransactionsScreen(
    state: TransactionsUiState,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            BankingTopBar(title = "Transactions")
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.height(8.dp))
            ErrorBanner(message = state.error, onRetry = onRetry)
            Spacer(modifier = Modifier.height(12.dp))
            if (state.isLoading && state.transactions.isEmpty()) {
                LoadingState()
            } else if (state.transactions.isEmpty()) {
                Text("No transactions available.")
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.transactions, key = { it.id }) { transaction ->
                        TransactionRow(transaction)
                    }
                }
            }
        }
    }
}
