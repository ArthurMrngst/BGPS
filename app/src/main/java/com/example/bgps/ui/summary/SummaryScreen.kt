@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bgps.domain.CategoryTotal
import com.example.bgps.domain.SpendingSummary
import com.example.bgps.domain.SummaryPeriod
import com.example.bgps.ui.common.formatVnd
import kotlin.math.roundToInt

@Composable
fun SummaryScreen(
    onBack: () -> Unit,
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val summary by viewModel.summary.collectAsState()
    val period by viewModel.period.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tổng hợp") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Quay lại") } }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { PeriodSelector(selected = period, onSelect = viewModel::setPeriod) }
            item { TotalsCard(summary) }
            if (summary.byCategory.isEmpty()) {
                item { Text("Chưa có chi tiêu trong kỳ này") }
            } else {
                items(summary.byCategory, key = { it.category }) { CategoryRow(it) }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selected: SummaryPeriod,
    onSelect: (SummaryPeriod) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selected == SummaryPeriod.WEEK,
            onClick = { onSelect(SummaryPeriod.WEEK) },
            label = { Text("Tuần này") }
        )
        FilterChip(
            selected = selected == SummaryPeriod.MONTH,
            onClick = { onSelect(SummaryPeriod.MONTH) },
            label = { Text("Tháng này") }
        )
    }
}

@Composable
private fun TotalsCard(summary: SpendingSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Tổng chi", style = MaterialTheme.typography.labelLarge)
            Text(
                text = formatVnd(summary.totalExpense),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Tổng thu: ${formatVnd(summary.totalIncome)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
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
            modifier = Modifier.fillMaxWidth()
        )
    }
}
