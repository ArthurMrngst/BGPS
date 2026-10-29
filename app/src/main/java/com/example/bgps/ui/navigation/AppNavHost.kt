package com.example.bgps.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bgps.ui.location.LocationAccessScreen
import com.example.bgps.ui.notifications.NotificationAccessScreen
import com.example.bgps.ui.receipt.ReceiptCaptureScreen
import com.example.bgps.ui.receipt.ReceiptConfirmScreen
import com.example.bgps.ui.summary.SummaryScreen
import com.example.bgps.ui.transactions.AddTransactionDialog
import com.example.bgps.ui.transactions.EditTransactionScreen
import com.example.bgps.ui.transactions.TransactionListScreen
import com.example.bgps.ui.transactions.TransactionViewModel
import com.example.bgps.ui.trips.TripListScreen

object Routes {
    const val TRANSACTIONS = "transactions"
    const val SUMMARY = "summary"
    const val EDIT_TRANSACTION = "editTransaction/{transactionId}"
    const val NOTIFICATION_ACCESS = "notificationAccess"
    const val RECEIPT_CAPTURE = "receiptCapture"
    const val RECEIPT_CONFIRM = "receiptConfirm/{imageUri}"
    const val TRIPS = "trips"
    const val LOCATION_ACCESS = "locationAccess"

    fun editTransaction(transactionId: String) = "editTransaction/$transactionId"
    fun receiptConfirm(imageUri: String) = "receiptConfirm/${Uri.encode(imageUri)}"
}

// Các tab có thanh điều hướng dưới; Sửa giao dịch và Cấp quyền thông báo là
// màn được "push" lên trên nên không hiện thanh dưới.
private val BOTTOM_BAR_ROUTES = setOf(Routes.TRANSACTIONS, Routes.SUMMARY)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Nút "+" ở thanh dưới dùng chung cho cả hai tab nên dialog + ViewModel
    // đặt ở đây, không đặt riêng trong TransactionListScreen.
    val transactionViewModel: TransactionViewModel = hiltViewModel()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (currentRoute in BOTTOM_BAR_ROUTES) {
                BGpsBottomBar(
                    currentRoute = currentRoute,
                    onNavigateTransactions = {
                        navController.navigate(Routes.TRANSACTIONS) {
                            popUpTo(Routes.TRANSACTIONS) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateSummary = {
                        navController.navigate(Routes.SUMMARY) {
                            popUpTo(Routes.TRANSACTIONS) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAddClick = { showAddDialog = true }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TRANSACTIONS,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.TRANSACTIONS) {
                TransactionListScreen(
                    onEditTransaction = { id -> navController.navigate(Routes.editTransaction(id)) },
                    onOpenNotificationAccess = { navController.navigate(Routes.NOTIFICATION_ACCESS) },
                    onOpenTrips = { navController.navigate(Routes.TRIPS) }
                )
            }
            composable(Routes.SUMMARY) {
                SummaryScreen()
            }
            composable(Routes.NOTIFICATION_ACCESS) {
                NotificationAccessScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Routes.EDIT_TRANSACTION,
                arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
            ) {
                EditTransactionScreen(onDone = { navController.popBackStack() })
            }
            composable(Routes.RECEIPT_CAPTURE) {
                ReceiptCaptureScreen(
                    onBack = { navController.popBackStack() },
                    onImageCaptured = { uri ->
                        navController.navigate(Routes.receiptConfirm(uri.toString())) {
                            popUpTo(Routes.RECEIPT_CAPTURE) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = Routes.RECEIPT_CONFIRM,
                arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
            ) {
                ReceiptConfirmScreen(onDone = { navController.popBackStack() })
            }
            composable(Routes.TRIPS) {
                TripListScreen(onOpenLocationAccess = { navController.navigate(Routes.LOCATION_ACCESS) })
            }
            composable(Routes.LOCATION_ACCESS) {
                LocationAccessScreen(onBack = { navController.popBackStack() })
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, type, category, merchant, note ->
                transactionViewModel.addTransaction(amount, type, category, merchant, note)
                showAddDialog = false
            },
            onScanReceipt = {
                showAddDialog = false
                navController.navigate(Routes.RECEIPT_CAPTURE)
            }
        )
    }
}
