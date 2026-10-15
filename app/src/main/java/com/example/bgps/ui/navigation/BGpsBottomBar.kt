package com.example.bgps.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bgps.ui.theme.MoneyGreenDark

// Kiểu Money Lover: thanh điều hướng dưới với nút "+" tròn nổi lên ở giữa thay
// vì là một tab bình thường. Chỉ 2 tab có thật trong app (Giao dịch, Tổng hợp);
// không thêm tab giả cho tính năng chưa làm.
@Composable
fun BGpsBottomBar(
    currentRoute: String?,
    onNavigateTransactions: () -> Unit,
    onNavigateSummary: () -> Unit,
    onAddClick: () -> Unit
) {
    Box {
        NavigationBar {
            NavigationBarItem(
                selected = currentRoute == Routes.TRANSACTIONS,
                onClick = onNavigateTransactions,
                icon = { Icon(Icons.Filled.Receipt, contentDescription = null) },
                label = { Text("Giao dịch") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MoneyGreenDark,
                    selectedTextColor = MoneyGreenDark
                )
            )
            // Khoảng trống giữa cho nút "+" nổi lên trên.
            NavigationBarItem(
                selected = false,
                enabled = false,
                onClick = {},
                icon = {},
                label = {}
            )
            NavigationBarItem(
                selected = currentRoute == Routes.SUMMARY,
                onClick = onNavigateSummary,
                icon = { Icon(Icons.Filled.PieChart, contentDescription = null) },
                label = { Text("Tổng hợp") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MoneyGreenDark,
                    selectedTextColor = MoneyGreenDark
                )
            )
        }
        FloatingActionButton(
            onClick = onAddClick,
            containerColor = MoneyGreenDark,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-24).dp)
                .size(56.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Thêm giao dịch")
        }
    }
}
