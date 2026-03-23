package com.example.bankingapp.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bankingapp.domain.model.Account
import com.example.bankingapp.domain.model.Transaction
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankingTopBar(title: String) {
    TopAppBar(title = { Text(title) })
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorBanner(message: String?, onRetry: (() -> Unit)? = null) {
    if (message == null) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(message, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onErrorContainer)
            if (onRetry != null) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Retry",
                    modifier = Modifier.clickable(onClick = onRetry).padding(start = 12.dp),
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
fun LabeledField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        supportingText = supportingText?.let { { Text(it) } },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().height(52.dp)) {
        Text(text)
    }
}

@Composable
fun AccountCard(account: Account, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(account.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(account.accountNumberMasked, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(14.dp))
            Text(account.balanceLabel(), style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(account.type, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
fun TransactionRow(transaction: Transaction) {
    val formattedAmount = NumberFormat.getCurrencyInstance(Locale.US).format(transaction.amount)
    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.title, fontWeight = FontWeight.SemiBold)
            Text(transaction.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(transaction.timestamp.toLocalDate().toString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = if (transaction.isDebit) "-$formattedAmount" else "+$formattedAmount",
            color = if (transaction.isDebit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
    }
}
