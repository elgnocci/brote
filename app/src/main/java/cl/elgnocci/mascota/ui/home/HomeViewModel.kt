package cl.elgnocci.mascota.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.elgnocci.mascota.data.BroteRepository
import cl.elgnocci.mascota.data.TodayStatus
import cl.elgnocci.mascota.domain.logicalDayOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock

/** Cada 30 s se recalcula el estado: así "Deshacer" se esconde y el día cambia a las 04:00 solos. */
fun every30Seconds(): Flow<Unit> = flow {
    while (true) {
        emit(Unit)
        delay(30_000)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repo: BroteRepository,
    private val clock: Clock,
    ticks: Flow<Unit> = every30Seconds(),
) : ViewModel() {

    val state: StateFlow<TodayStatus?> = ticks
        .flatMapLatest { repo.status() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun onSmoke() {
        viewModelScope.launch { repo.logSmoke() }
    }

    fun onUndo() {
        viewModelScope.launch { repo.undoLast() }
    }

    /** "Sumar varios": [lastNight] carga al día anterior; si no, a hoy. */
    fun onAddMany(count: Int, lastNight: Boolean) {
        val today = logicalDayOf(clock.instant(), clock.zone)
        viewModelScope.launch { repo.logMany(count, if (lastNight) today.minusDays(1) else today) }
    }
}
