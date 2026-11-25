package cl.elgnocci.mascota.ui

import cl.elgnocci.mascota.domain.Plan
import cl.elgnocci.mascota.ui.onboarding.OnboardingViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class OnboardingViewModelTest {
    private val zone = ZoneId.of("America/Santiago")
    private fun clockAt(local: String) = Clock.fixed(LocalDateTime.parse(local).atZone(zone).toInstant(), zone)

    private var saved: Plan? = null
    private fun vm(local: String = "2026-11-24T12:00") = OnboardingViewModel({ saved = it }, clockAt(local))

    @Test
    fun guardaElPlanDesdeHoy() = runTest {
        assertTrue(vm().submit(8))
        assertEquals(Plan(8, LocalDate.of(2026, 11, 24)), saved)
    }

    @Test
    fun alas2AMElPlanEmpiezaElDiaAnterior() = runTest {
        assertTrue(vm("2026-11-25T02:00").submit(6))
        assertEquals(Plan(6, LocalDate.of(2026, 11, 24)), saved)
    }

    @Test
    fun rechazaCeroYMasDe60() = runTest {
        assertFalse(vm().submit(0))
        assertFalse(vm().submit(61))
        assertNull(saved)
    }
}
