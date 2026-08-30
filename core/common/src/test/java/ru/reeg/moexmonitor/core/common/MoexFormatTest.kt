package ru.reeg.moexmonitor.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.reeg.moexmonitor.core.common.format.MoexFormat

class MoexFormatTest {

    @Test
    fun `null превращается в прочерк`() {
        assertEquals("—", MoexFormat.price(null))
        assertEquals("—", MoexFormat.percent(null))
        assertEquals("—", MoexFormat.compactMoney(null))
    }

    @Test
    fun `положительный процент получает знак плюс`() {
        assertTrue(MoexFormat.percent(1.254).startsWith("+"))
        assertTrue(MoexFormat.percent(-1.254).startsWith("-"))
    }

    @Test
    fun `дешёвые бумаги показываются с четырьмя знаками`() {
        // AFKS стоит около 7,398 рубля — два знака здесь потеряли бы точность.
        assertTrue(MoexFormat.price(7.398).contains("7,3980"))
        assertTrue(MoexFormat.price(281.39).contains("281,39"))
    }

    @Test
    fun `объём торгов сжимается до миллиардов`() {
        assertTrue(MoexFormat.compactMoney(7_816_252_250.0).contains("млрд"))
        assertTrue(MoexFormat.compactMoney(908_086_981.0).contains("млн"))
    }

    @Test
    fun `дата новости переводится в человеческий вид`() {
        val formatted = MoexFormat.newsDateTime("2026-08-28 21:14:31")
        assertTrue(formatted.contains("21:14"))
        assertTrue(formatted.contains("28"))
    }
}
