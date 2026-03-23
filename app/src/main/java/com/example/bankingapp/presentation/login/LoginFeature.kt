package com.example.bankingapp.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.domain.usecase.LoginUseCase
import com.example.bankingapp.presentation.component.ErrorBanner
import com.example.bankingapp.presentation.component.LabeledField
import com.example.bankingapp.presentation.component.PrimaryButton
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val username: String = "demo.user",
    val password: String = "1234",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChanged(value: String) = _uiState.update { it.copy(username = value) }
    fun onPasswordChanged(value: String) = _uiState.update { it.copy(password = value) }

    fun login() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            loginUseCase(uiState.value.username, uiState.value.password)
                .onSuccess { _uiState.update { state -> state.copy(isLoading = false, isAuthenticated = true) } }
                .onFailure { error -> _uiState.update { state -> state.copy(isLoading = false, error = error.message) } }
        }
    }
}

@Composable
fun LoginRoute(viewModel: LoginViewModel, onLoginSuccess: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) onLoginSuccess()
    }
    LoginScreen(
        state = state,
        onUsernameChanged = viewModel::onUsernameChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onLoginClick = viewModel::login,
    )
}

@Composable
private fun LoginScreen(
    state: LoginUiState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Secure banking at your fingertips", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Use the mock credentials below to sign in.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        ErrorBanner(message = state.error)
        if (state.error != null) Spacer(modifier = Modifier.height(16.dp))
        LabeledField(value = state.username, onValueChange = onUsernameChanged, label = "Username", modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        LabeledField(value = state.password, onValueChange = onPasswordChanged, label = "Password", supportingText = "Minimum 4 characters")
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryButton(text = if (state.isLoading) "Signing in..." else "Login", enabled = !state.isLoading, onClick = onLoginClick)
    }
}
