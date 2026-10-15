package com.example.bgps.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bgps.ui.summary.SummaryScreen
import com.example.bgps.ui.transactions.EditTransactionScreen
import com.example.bgps.ui.transactions.TransactionListScreen

object Routes {
    const val TRANSACTIONS = "transactions"
    const val SUMMARY = "summary"
    const val EDIT_TRANSACTION = "editTransaction/{transactionId}"

    fun editTransaction(transactionId: String) = "editTransaction/$transactionId"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.TRANSACTIONS) {
        composable(Routes.TRANSACTIONS) {
            TransactionListScreen(
                onOpenSummary = { navController.navigate(Routes.SUMMARY) },
                onEditTransaction = { id -> navController.navigate(Routes.editTransaction(id)) }
            )
        }
        composable(Routes.SUMMARY) {
            SummaryScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.EDIT_TRANSACTION,
            arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
        ) {
            EditTransactionScreen(onDone = { navController.popBackStack() })
        }
    }
}
