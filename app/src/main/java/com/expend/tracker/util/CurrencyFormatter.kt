package com.expend.tracker.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

object CurrencyFormatter {

    private val symbols = DecimalFormatSymbols(Locale("en", "IN")).apply {
        currencySymbol = "₹"
    }

    private val formatWithDecimals = DecimalFormat("₹#,##,##0.00", symbols)
    private val formatWithoutDecimals = DecimalFormat("₹#,##,##0", symbols)

    /**
     * Formats integer minor units (paise) to display string.
     * e.g., 10050L -> "₹100.50", -5000L -> "-₹50.00", 0L -> "₹0.00"
     */
    fun formatPaise(paise: Long, forceDecimals: Boolean = true): String {
        val isNegative = paise < 0
        val absPaise = abs(paise)
        val rupees = absPaise / 100.0

        val formatted = if (forceDecimals || absPaise % 100 != 0L) {
            formatWithDecimals.format(rupees)
        } else {
            formatWithoutDecimals.format(rupees)
        }

        return if (isNegative) "-$formatted" else formatted
    }

    /**
     * Converts a user input text like "150.75" or "500" into paise Long (e.g. 15075L).
     * Prevents floating point inaccuracy by parsing integer and fractional parts directly.
     * Returns null if input is empty, invalid, or negative.
     */
    fun parseAmountToPaise(input: String): Long? {
        val trimmed = input.trim().replace("₹", "").replace(",", "")
        if (trimmed.isEmpty()) return null

        val parts = trimmed.split(".")
        if (parts.size > 2) return null // Multiple decimal dots

        val rupeesPart = parts[0].toLongOrNull() ?: return null
        if (rupeesPart < 0) return null

        val paisePart = if (parts.size == 2) {
            val decimals = parts[1]
            when {
                decimals.isEmpty() -> 0L
                decimals.length == 1 -> (decimals + "0").toLongOrNull() ?: return null
                decimals.length == 2 -> decimals.toLongOrNull() ?: return null
                else -> decimals.substring(0, 2).toLongOrNull() ?: return null
            }
        } else {
            0L
        }

        val totalPaise = rupeesPart * 100L + paisePart
        return totalPaise
    }

    /**
     * Converts paise Long back into raw editable string without symbols.
     * e.g., 10050L -> "100.50", 10000L -> "100"
     */
    fun paiseToEditableString(paise: Long?): String {
        if (paise == null) return ""
        val absPaise = abs(paise)
        val whole = absPaise / 100
        val frac = absPaise % 100
        return if (frac == 0L) {
            whole.toString()
        } else {
            String.format(Locale.US, "%d.%02d", whole, frac)
        }
    }
}
