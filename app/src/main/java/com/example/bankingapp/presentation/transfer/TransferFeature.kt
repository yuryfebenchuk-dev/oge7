package com.example.bankingapp.presentation.transfer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.domain.model.Account
import com.example.bankingapp.domain.usecase.ObserveAccountsUseCase
import com.example.bankingapp.domain.usecase.TransferMoneyUseCase
import com.example.bankingapp.presentation.component.BankingTopBar
import com.example.bankingapp.presentation.component.ErrorBanner
import com.example.bankingapp.presentation.component.LabeledField
import com.example.bankingapp.presentation.component.PrimaryButton
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransferUiState(
    val accounts: List<Account> = emptyList(),
    val selectedAccountId: String = "",
    val recipient: String = "",
    val amount: String = "",
    val note: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
) {
    val isFormValid: Boolean
        get() = selectedAccountId.isNotBlank() && recipient.isNotBlank() && amount.toDoubleOrNull()?.let { it > 0 } == true
}

@HiltViewModel
class TransferViewModel @Inject constructor(
    observeAccountsUseCase: ObserveAccountsUseCase,
    private val transferMoneyUseCase: TransferMoneyUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()
    val accounts: StateFlow<List<Account>> = observeAccountsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            accounts.collect { accountsList ->
                _uiState.update { state ->
                    state.copy(
                        accounts = accountsList,
                        selectedAccountId = state.selectedAccountId.ifBlank { accountsList.firstOrNull()?.id.orEmpty() },
                    )
                }
            }
        }
    }

    fun onRecipientChanged(value: String) = _uiState.update { it.copy(recipient = value, error = null) }
    fun onAmountChanged(value: String) = _uiState.update { it.copy(amount = value.filter { ch -> ch.isDigit() || ch == '.' }, error = null) }
    fun onNoteChanged(value: String) = _uiState.update { it.copy(note = value, error = null) }
    fun onAccountSelected(accountId: String) = _uiState.update { it.copy(selectedAccountId = accountId, error = null) }
    fun clearSuccess() = _uiState.update { it.copy(successMessage = null) }

    fun submitTransfer() {
        val state = uiState.value
        if (!state.isFormValid) {
            _uiState.update { it.copy(error = "Please complete all required fields with a valid amount.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null, successMessage = null) }
            transferMoneyUseCase(
                accountId = state.selectedAccountId,
                recipient = state.recipient,
                amount = state.amount.toDouble(),
                note = state.note,
            ).onSuccess { receipt ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        recipient = "",
                        amount = "",
                        note = "",
                        successMessage = receipt.message,
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isSubmitting = false, error = error.message ?: "Transfer failed.") }
            }
        }
    }
}

@Composable
fun TransferRoute(viewModel: TransferViewModel, onNavigateBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    TransferScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onRecipientChanged = viewModel::onRecipientChanged,
        onAmountChanged = viewModel::onAmountChanged,
        onNoteChanged = viewModel::onNoteChanged,
        onAccountSelected = viewModel::onAccountSelected,
        onSubmit = viewModel::submitTransfer,
    )
}

@Composable
private fun TransferScreen(
    state: TransferUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onRecipientChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onAccountSelected: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Scaffold(
        topBar = { BankingTopBar(title = "Transfer Money") },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Move funds between accounts or to a saved recipient.", style = MaterialTheme.typography.bodyLarge)
            ErrorBanner(message = state.error)
            AccountDropdown(accounts = state.accounts, selectedAccountId = state.selectedAccountId, onAccountSelected = onAccountSelected)
            LabeledField(value = state.recipient, onValueChange = onRecipientChanged, label = "Recipient name")
            LabeledField(value = state.amount, onValueChange = onAmountChanged, label = "Amount (USD)", supportingText = "Amount must be greater than zero")
            LabeledField(value = state.note, onValueChange = onNoteChanged, label = "Note")
            Spacer(modifier = Modifier.height(8.dp))
            PrimaryButton(text = if (state.isSubmitting) "Submitting..." else "Submit Transfer", enabled = !state.isSubmitting && state.isFormValid, onClick = onSubmit)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDropdown(
    accounts: List<Account>,
    selectedAccountId: String,
    onAccountSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = accounts.firstOrNull { it.id == selectedAccountId }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected?.name ?: "Select account",
            onValueChange = {},
            readOnly = true,
            label = { Text("From account") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.name) },
                    onClick = {
                        expanded = false
                        onAccountSelected(account.id)
                    },
                )
            }
        }
    }
}
