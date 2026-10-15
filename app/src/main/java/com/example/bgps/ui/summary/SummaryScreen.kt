@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.summary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bgps.domain.CategoryTotal
import com.example.bgps.domain.SpendingSummary
import com.example.bgps.domain.SummaryPeriod
import com.example.bgps.ui.common.formatVnd
import com.example.bgps.ui.theme.ExpenseOrange
import com.example.bgps.ui.theme.IncomeBlue
import kotlin.math.roundToInt

@Composable
fun SummaryScreen(viewModel: SummaryViewModel = hiltViewModel()) {
    val summary by viewModel.summary.collectAsState()
    val period by viewModel.period.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Tổng hợp") }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { PeriodTabs(selected = period, onSelect = viewModel::setPeriod) }
            item { NetBalanceCard(summary) }
            item { IncomeExpenseChart(income = summary.totalIncome, expense = summary.totalExpense) }
            if (summary.byCategory.isEmpty()) {
                item { Text("Chưa có chi tiêu trong kỳ này") }
            } else {
                items(summary.byCategory, key = { it.category }) { CategoryRow(it) }
            }
        }
    }
}

@Composable
private fun PeriodTabs(
    selected: SummaryPeriod,
    onSelect: (SummaryPeriod) -> Unit
) {
    val periods = listOf(SummaryPeriod.WEEK to "Tuần này", SummaryPeriod.MONTH to "Tháng này")
    val selectedIndex = periods.indexOfFirst { it.first == selected }.coerceAtLeast(0)

    TabRow(selectedTabIndex = selectedIndex) {
        periods.forEach { (periodValue, label) ->
            Tab(
                selected = periodValue == selected,
                onClick = { onSelect(periodValue) },
                text = { Text(label) }
            )
        }
    }
}

@Composable
private fun NetBalanceCard(summary: SpendingSummary) {
    val net = summary.totalIncome - summary.totalExpense
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Thu nhập ròng", style = MaterialTheme.typography.labelLarge)
            Text(
                text = formatVnd(net),
                style = MaterialTheme.typography.headlineMedium,
                color = if (net >= 0) IncomeBlue else ExpenseOrange
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Khoản thu", style = MaterialTheme.typography.labelMedium)
                    Text(
                        formatVnd(summary.totalIncome),
                        color = IncomeBlue,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column {
                    Text("Khoản chi", style = MaterialTheme.typography.labelMedium)
                    Text(
                        formatVnd(summary.totalExpense),
                        color = ExpenseOrange,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

// Biểu đồ cột phân kỳ kiểu Money Lover: cột thu màu xanh đi lên, cột chi màu
// cam đi xuống từ một đường chuẩn giữa, chiều cao theo tỉ lệ so với giá trị
// lớn nhất. Không dùng thư viện chart ngoài vì chỉ cần so sánh 2 giá trị.
@Composable
private fun IncomeExpenseChart(income: Long, expense: Long) {
    val maxValue = maxOf(income, expense, 1L).toFloat()
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        val centerY = size.height / 2f
        val maxBarHeight = centerY - 8.dp.toPx()
        val barWidth = size.width * 0.18f
        val incomeX = size.width * 0.35f
        val expenseX = size.width * 0.65f

        drawLine(
            color = gridColor,
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 1.dp.toPx()
        )

        val incomeHeight = (income / maxValue) * maxBarHeight
        drawRect(
            color = IncomeBlue,
            topLeft = Offset(incomeX - barWidth / 2, centerY - incomeHeight),
            size = Size(barWidth, incomeHeight)
        )

        val expenseHeight = (expense / maxValue) * maxBarHeight
        drawRect(
            color = ExpenseOrange,
            topLeft = Offset(expenseX - barWidth / 2, centerY),
            size = Size(barWidth, expenseHeight)
        )
    }
}

@Composable
private fun CategoryRow(item: CategoryTotal) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(item.category)
            Text("${formatVnd(item.total)} (${(item.percent * 100).roundToInt()}%)")
        }
        LinearProgressIndicator(
            progress = { item.percent },
            color = ExpenseOrange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
