package com.example.bankingapp.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.domain.model.Account
import com.example.bankingapp.domain.usecase.ObserveAccountsUseCase
import com.example.bankingapp.domain.usecase.RefreshAccountsUseCase
import com.example.bankingapp.presentation.component.AccountCard
import com.example.bankingapp.presentation.component.BankingTopBar
import com.example.bankingapp.presentation.component.ErrorBanner
import com.example.bankingapp.presentation.component.LoadingState
import com.example.bankingapp.presentation.component.SectionTitle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send

/**
 * The dashboard merges cached Room data with refresh state.
 * Keeping the stream in the ViewModel means Compose stays stateless and testable.
 */

data class DashboardUiState(
    val accounts: List<Account> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeAccountsUseCase: ObserveAccountsUseCase,
    private val refreshAccountsUseCase: RefreshAccountsUseCase,
) : ViewModel() {
    private val metaState = MutableStateFlow(DashboardUiState())

    val uiState: StateFlow<DashboardUiState> = observeAccountsUseCase()
        .combine(metaState) { accounts, meta -> meta.copy(accounts = accounts) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun refresh() {
        viewModelScope.launch {
            metaState.update { it.copy(isLoading = true, error = null) }
            refreshAccountsUseCase()
                .onSuccess { metaState.update { it.copy(isLoading = false) } }
                .onFailure { error -> metaState.update { it.copy(isLoading = false, error = error.message ?: "Unable to load accounts.") } }
        }
    }
}

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel,
    onAccountSelected: (String) -> Unit,
    onTransferClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }
    DashboardScreen(state = state, onRetry = viewModel::refresh, onAccountSelected = onAccountSelected, onTransferClick = onTransferClick)
}

@Composable
private fun DashboardScreen(
    state: DashboardUiState,
    onRetry: () -> Unit,
    onAccountSelected: (String) -> Unit,
    onTransferClick: () -> Unit,
) {
    Scaffold(
        topBar = { BankingTopBar(title = "Dashboard") },
        floatingActionButton = {
            FloatingActionButton(onClick = onTransferClick) {
                Icon(Icons.Default.Send, contentDescription = "Transfer money")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Welcome back, Alex", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Monitor balances and recent activity across all accounts.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            ErrorBanner(message = state.error, onRetry = onRetry)
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("Your Accounts")
            Spacer(modifier = Modifier.height(12.dp))
            if (state.isLoading && state.accounts.isEmpty()) {
                LoadingState()
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.accounts, key = { it.id }) { account ->
                        AccountCard(account = account) { onAccountSelected(account.id) }
                    }
                }
            }
        }
    }
}
