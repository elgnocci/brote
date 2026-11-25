package cl.elgnocci.mascota.ui

import cl.elgnocci.mascota.data.BroteRepository
import cl.elgnocci.mascota.data.TodayStatus
import cl.elgnocci.mascota.domain.PetMood
import cl.elgnocci.mascota.ui.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

private class FakeRepo : BroteRepository {
    val state = MutableStateFlow<TodayStatus?>(
        TodayStatus(count = 0, limit = 6, mood = PetMood.HAPPY, cravingsResisted = 0, canUndo = false, stage = 0)
    )
    var lastMany: Pair<Int, LocalDate>? = null

    override fun status(): Flow<TodayStatus?> = state
    override suspend fun logSmoke() {
        state.value = state.value!!.let { it.copy(count = it.count + 1, canUndo = true) }
    }
    override suspend fun undoLast(): Boolean {
        state.value = state.value!!.let { it.copy(count = it.count - 1, canUndo = false) }
        return true
    }
    override suspend fun logCraving(resisted: Boolean) = Unit
    override suspend fun logMany(count: Int, day: LocalDate): Boolean {
        lastMany = count to day
        return true
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val zone = ZoneId.of("America/Santiago")
    private val clock = Clock.fixed(LocalDateTime.parse("2026-11-25T02:00").atZone(zone).toInstant(), zone)
    private lateinit var repo: FakeRepo
    private lateinit var vm: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repo = FakeRepo()
        vm = HomeViewModel(repo, clock, ticks = flowOf(Unit))
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun fumeUnoSubeElConteoYPermiteDeshacer() {
        vm.onSmoke()
        assertEquals(1, vm.state.value!!.count)
        assertTrue(vm.state.value!!.canUndo)
    }

    @Test
    fun deshacerBajaElConteo() {
        vm.onSmoke()
        vm.onUndo()
        assertEquals(0, vm.state.value!!.count)
        assertFalse(vm.state.value!!.canUndo)
    }

    @Test
    fun sumarVariosAnocheVaAlDiaAnterior() {
        // A las 02:00 del 25 el día lógico es el 24, así que "anoche" es el 23.
        vm.onAddMany(5, lastNight = true)
        assertEquals(5 to LocalDate.of(2026, 11, 23), repo.lastMany)
    }

    @Test
    fun sumarVariosHoyVaAlDiaLogico() {
        vm.onAddMany(2, lastNight = false)
        assertEquals(2 to LocalDate.of(2026, 11, 24), repo.lastMany)
    }
}
