
package com.example.bgps.ui.common

import java.text.NumberFormat
import java.util.Locale

fun formatVnd(amount: Long): String =
    NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")).format(amount) + " ₫"