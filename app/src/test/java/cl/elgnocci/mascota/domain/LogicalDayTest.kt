package cl.elgnocci.mascota.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class LogicalDayTest {
    private val zone = ZoneId.of("America/Santiago")
    private fun at(local: String) = LocalDateTime.parse(local).atZone(zone).toInstant()

    @Test
    fun antesDeLas4CuentaParaElDiaAnterior() {
        assertEquals(LocalDate.of(2026, 11, 23), logicalDayOf(at("2026-11-24T03:59"), zone))
    }

    @Test
    fun desdeLas4EsElDiaNuevo() {
        assertEquals(LocalDate.of(2026, 11, 24), logicalDayOf(at("2026-11-24T04:00"), zone))
    }

    @Test
    fun finDelHorarioDeVeranoNoMueveElDia() {
        assertEquals(LocalDate.of(2026, 4, 4), logicalDayOf(at("2026-04-05T03:30"), zone))
    }
}
