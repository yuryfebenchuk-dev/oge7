package com.example.bankingapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bankingapp.presentation.dashboard.DashboardRoute
import com.example.bankingapp.presentation.dashboard.DashboardViewModel
import com.example.bankingapp.presentation.login.LoginRoute
import com.example.bankingapp.presentation.login.LoginViewModel
import com.example.bankingapp.presentation.transactions.TransactionsRoute
import com.example.bankingapp.presentation.transactions.TransactionsViewModel
import com.example.bankingapp.presentation.transfer.TransferRoute
import com.example.bankingapp.presentation.transfer.TransferViewModel

sealed class Destinations(val route: String) {
    data object Login : Destinations("login")
    data object Dashboard : Destinations("dashboard")
    data object Transactions : Destinations("transactions/{accountId}") {
        fun create(accountId: String) = "transactions/$accountId"
    }
    data object Transfer : Destinations("transfer")
}

@Composable
fun BankingNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Destinations.Login.route) {
        composable(Destinations.Login.route) {
            val viewModel: LoginViewModel = hiltViewModel()
            LoginRoute(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(Destinations.Dashboard.route) {
                        popUpTo(Destinations.Login.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Destinations.Dashboard.route) {
            val viewModel: DashboardViewModel = hiltViewModel()
            DashboardRoute(
                viewModel = viewModel,
                onAccountSelected = { navController.navigate(Destinations.Transactions.create(it)) },
                onTransferClick = { navController.navigate(Destinations.Transfer.route) },
            )
        }
        composable(
            route = Destinations.Transactions.route,
            arguments = listOf(navArgument("accountId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId").orEmpty()
            val viewModel: TransactionsViewModel = hiltViewModel()
            TransactionsRoute(viewModel = viewModel, accountId = accountId, onNavigateBack = navController::popBackStack)
        }
        composable(Destinations.Transfer.route) {
            val viewModel: TransferViewModel = hiltViewModel()
            TransferRoute(viewModel = viewModel, onNavigateBack = navController::popBackStack)
        }
    }
}
