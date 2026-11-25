package cl.elgnocci.mascota.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class LimitPolicyTest {
    private val start = LocalDate.of(2026, 11, 24)
    private fun day(n: Long) = start.plusDays(n)

    /** Cigarros por día de la primera semana, desde [start]. */
    private fun week(vararg counts: Int, from: Long = 0) =
        counts.withIndex().associate { (i, c) -> day(from + i) to c }

    @Test
    fun elPrimerDiaUsaElLimiteInicial() {
        assertEquals(8, limitFor(start, Plan(8, start), emptyMap()))
    }

    @Test
    fun cincoDeSieteCumplidosBajaUno() {
        val counts = week(8, 8, 8, 8, 8, 9, 9)
        assertEquals(7, limitFor(day(7), Plan(8, start), counts))
    }

    @Test
    fun cuatroDeSieteCumplidosNoBaja() {
        val counts = week(8, 8, 8, 8, 9, 9, 9)
        assertEquals(8, limitFor(day(7), Plan(8, start), counts))
    }

    @Test
    fun laSemanaIncompletaNoCuenta() {
        val counts = week(0, 0, 0, 0, 0, 0)
        assertEquals(8, limitFor(day(6), Plan(8, start), counts))
    }

    @Test
    fun laSegundaSemanaSeMideContraElLimiteNuevo() {
        val counts = week(7, 7, 7, 7, 7, 7, 7) + week(8, 8, 8, 8, 8, 8, 8, from = 7)
        assertEquals(7, limitFor(day(14), Plan(8, start), counts))
    }

    @Test
    fun nuncaBajaDeCero() {
        assertEquals(0, limitFor(day(14), Plan(1, start), emptyMap()))
    }

    @Test
    fun antesDelInicioUsaElLimiteInicial() {
        assertEquals(6, limitFor(day(-3), Plan(6, start), emptyMap()))
    }
}
