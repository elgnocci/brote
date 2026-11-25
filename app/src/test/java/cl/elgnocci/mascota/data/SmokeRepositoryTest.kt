package cl.elgnocci.mascota.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import cl.elgnocci.mascota.domain.PetMood
import cl.elgnocci.mascota.domain.Plan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

private val ZONE: ZoneId = ZoneId.of("America/Santiago")

private class MutableClock(var now: Instant) : Clock() {
    override fun getZone(): ZoneId = ZONE
    override fun withZone(zone: ZoneId?): Clock = this
    override fun instant(): Instant = now
    fun advance(d: Duration) { now = now.plus(d) }
}

private class FakePlanStore(initial: Plan?) : PlanStore {
    val state = MutableStateFlow(initial)
    override val plan = state
    override suspend fun save(plan: Plan) { state.value = plan }
}

private class CountingRefresher : WidgetRefresher {
    var calls = 0
    override suspend fun refresh() { calls++ }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SmokeRepositoryTest {
    private fun local(s: String) = LocalDateTime.parse(s).atZone(ZONE).toInstant()

    private lateinit var db: SmokeDatabase
    private lateinit var clock: MutableClock
    private lateinit var plans: FakePlanStore
    private lateinit var refresher: CountingRefresher
    private lateinit var repo: SmokeRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), SmokeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        clock = MutableClock(local("2026-11-24T12:00"))
        plans = FakePlanStore(Plan(6, LocalDate.of(2026, 11, 21)))
        refresher = CountingRefresher()
        repo = SmokeRepository(db, plans, clock, refresher)
    }

    @After
    fun tearDown() = db.close()

    private suspend fun status() = repo.status().first()!!

    @Test
    fun dosRegistrosCuentanDos() = runTest {
        repo.logSmoke()
        repo.logSmoke()
        assertEquals(2, status().count)
    }

    @Test
    fun registroALas0330CuentaParaElDiaAnterior() = runTest {
        clock.now = local("2026-11-25T03:30")
        repo.logSmoke()
        assertEquals(1, status().count)
        clock.now = local("2026-11-25T12:00")
        assertEquals(0, status().count)
    }

    @Test
    fun deshacerA9MinutosBorraElUltimo() = runTest {
        repo.logSmoke()
        clock.advance(Duration.ofMinutes(9))
        assertTrue(repo.undoLast())
        assertEquals(0, status().count)
    }

    @Test
    fun deshacerA11MinutosNoHaceNada() = runTest {
        repo.logSmoke()
        clock.advance(Duration.ofMinutes(11))
        assertFalse(repo.undoLast())
        assertEquals(1, status().count)
        assertFalse(status().canUndo)
    }

    @Test
    fun cadaCambioRefrescaElWidgetUnaVez() = runTest {
        repo.logSmoke()
        assertEquals(1, refresher.calls)
        repo.undoLast()
        assertEquals(2, refresher.calls)
        repo.logCraving(resisted = true)
        assertEquals(3, refresher.calls)
        repo.logMany(2, LocalDate.of(2026, 11, 24))
        assertEquals(4, refresher.calls)
    }

    @Test
    fun sumarVariosCargaAyerYLaMascotaSeRecupera() = runTest {
        assertTrue(repo.logMany(8, LocalDate.of(2026, 11, 23)))
        val s = status()
        assertEquals(0, s.count)
        assertEquals(PetMood.RECOVERING, s.mood)
    }

    @Test
    fun sumarVariosRechazaCantidadesFueraDeRango() = runTest {
        assertFalse(repo.logMany(0, LocalDate.of(2026, 11, 23)))
        assertFalse(repo.logMany(31, LocalDate.of(2026, 11, 23)))
        assertFalse(repo.logMany(3, LocalDate.of(2026, 11, 25)))
        assertEquals(0, refresher.calls)
        assertEquals(PetMood.HAPPY, status().mood)
    }

    @Test
    fun lasGanasVencidasSeCuentan() = runTest {
        repo.logCraving(resisted = true)
        repo.logCraving(resisted = false)
        assertEquals(1, status().cravingsResisted)
    }

    @Test
    fun sinPlanNoHayEstado() = runTest {
        plans.state.value = null
        assertNull(repo.status().first())
    }
}
