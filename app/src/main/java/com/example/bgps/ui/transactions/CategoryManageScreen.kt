@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bgps.data.database.CategoryEntity
import com.example.bgps.domain.DefaultCategories

private val COLOR_PALETTE = listOf(
    "#FF7043", "#42A5F5", "#AB47BC", "#26A69A", "#EC407A",
    "#66BB6A", "#5C6BC0", "#8D6E63", "#29B6F6", "#FFCA28", "#9E9E9E"
)

// Quản lý danh mục (B3 phần 2): thêm, đổi tên, đổi icon/màu, xoá (gộp sang
// danh mục khác trước khi xoá — không mất dữ liệu giao dịch/ngân sách đang
// dùng danh mục đó). "Khác" luôn giữ lại làm đích gộp mặc định.
@Composable
fun CategoryManageScreen(
    onBack: () -> Unit,
    viewModel: CategoryManageViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<CategoryEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<CategoryEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh mục") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Quay lại") } },
                actions = { TextButton(onClick = { showAddDialog = true }) { Text("+ Thêm") } }
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            items(categories, key = { it.id }) { category ->
                CategoryRow(
                    category = category,
                    onEdit = { editTarget = category },
                    onDelete = { deleteTarget = category }
                )
            }
        }
    }

    if (showAddDialog) {
        CategoryEditDialog(
            title = "Thêm danh mục",
            initialName = "",
            initialIcon = "",
            initialColor = COLOR_PALETTE.first(),
            onDismiss = { showAddDialog = false },
            onConfirm = { name, icon, color ->
                viewModel.add(name, icon.ifBlank { null }, color)
                showAddDialog = false
            }
        )
    }

    editTarget?.let { category ->
        CategoryEditDialog(
            title = "Sửa danh mục",
            initialName = category.displayName,
            initialIcon = category.icon ?: "",
            initialColor = category.colorHex ?: COLOR_PALETTE.first(),
            onDismiss = { editTarget = null },
            onConfirm = { name, icon, color ->
                if (name != category.displayName) {
                    viewModel.rename(category, name)
                }
                viewModel.updateIconColor(category, icon.ifBlank { null }, color)
                editTarget = null
            }
        )
    }

    deleteTarget?.let { category ->
        CategoryDeleteDialog(
            category = category,
            otherCategoryNames = categories.filter { it.id != category.id }.map { it.displayName },
            onDismiss = { deleteTarget = null },
            onConfirm = { reassignTo ->
                viewModel.delete(category, reassignTo)
                deleteTarget = null
            }
        )
    }
}

@Composable
private fun CategoryRow(category: CategoryEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ColorSwatch(colorHex = category.colorHex, size = 32.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${category.icon.orEmpty()} ${category.displayName}".trim(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            TextButton(onClick = onEdit) { Text("Sửa") }
            if (category.displayName != DefaultCategories.FALLBACK_NAME) {
                TextButton(onClick = onDelete) { Text("Xoá") }
            }
        }
    }
}

@Composable
private fun ColorSwatch(colorHex: String?, size: androidx.compose.ui.unit.Dp) {
    val color = runCatching { Color(android.graphics.Color.parseColor(colorHex ?: "#9E9E9E")) }
        .getOrDefault(Color.Gray)
    Box(modifier = Modifier.size(size).clip(CircleShape).background(color))
}

@Composable
private fun CategoryEditDialog(
    title: String,
    initialName: String,
    initialIcon: String,
    initialColor: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String, color: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var icon by remember { mutableStateOf(initialIcon) }
    var color by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên danh mục (VD: Thú cưng, Học phí)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it.take(2) },
                    label = { Text("Icon (emoji, tuỳ chọn)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Màu", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    COLOR_PALETTE.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .clickable { color = hex }
                        ) {
                            if (color == hex) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.25f))
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name.trim(), icon.trim(), color) }
            ) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Huỷ") } }
    )
}

@Composable
private fun CategoryDeleteDialog(
    category: CategoryEntity,
    otherCategoryNames: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (reassignTo: String) -> Unit
) {
    var reassignTo by remember {
        mutableStateOf(
            otherCategoryNames.find { it == DefaultCategories.FALLBACK_NAME }
                ?: otherCategoryNames.firstOrNull()
                ?: DefaultCategories.FALLBACK_NAME
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xoá \"${category.displayName}\"") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Giao dịch và ngân sách đang gắn danh mục này sẽ chuyển sang danh mục bạn chọn dưới đây.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    otherCategoryNames.forEach { name ->
                        FilterChip(
                            selected = reassignTo == name,
                            onClick = { reassignTo = name },
                            label = { Text(name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(reassignTo) }) { Text("Xoá và gộp") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Huỷ") } }
    )
}
