package com.expend.tracker

import com.expend.tracker.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun parseAmountToPaise_validAmounts_returnsCorrectPaise() {
        assertEquals(10050L, CurrencyFormatter.parseAmountToPaise("100.50"))
        assertEquals(10000L, CurrencyFormatter.parseAmountToPaise("100"))
        assertEquals(10050L, CurrencyFormatter.parseAmountToPaise("100.5"))
        assertEquals(50L, CurrencyFormatter.parseAmountToPaise("0.50"))
        assertEquals(5L, CurrencyFormatter.parseAmountToPaise("0.05"))
        assertEquals(10000000L, CurrencyFormatter.parseAmountToPaise("100000"))
        assertEquals(123456L, CurrencyFormatter.parseAmountToPaise("1,234.56"))
        assertEquals(10050L, CurrencyFormatter.parseAmountToPaise("₹100.50"))
    }

    @Test
    fun parseAmountToPaise_invalidInputs_returnsNull() {
        assertNull(CurrencyFormatter.parseAmountToPaise(""))
        assertNull(CurrencyFormatter.parseAmountToPaise("   "))
        assertNull(CurrencyFormatter.parseAmountToPaise("abc"))
        assertNull(CurrencyFormatter.parseAmountToPaise("100.50.20"))
        assertNull(CurrencyFormatter.parseAmountToPaise("-50"))
    }

    @Test
    fun formatPaise_formatsCorrectly() {
        assertEquals("₹100.50", CurrencyFormatter.formatPaise(10050L))
        assertEquals("₹100.00", CurrencyFormatter.formatPaise(10000L))
        assertEquals("₹0.00", CurrencyFormatter.formatPaise(0L))
        assertEquals("-₹50.00", CurrencyFormatter.formatPaise(-5000L))
    }

    @Test
    fun paiseToEditableString_convertsCorrectly() {
        assertEquals("100.50", CurrencyFormatter.paiseToEditableString(10050L))
        assertEquals("100", CurrencyFormatter.paiseToEditableString(10000L))
        assertEquals("", CurrencyFormatter.paiseToEditableString(null))
    }
}
