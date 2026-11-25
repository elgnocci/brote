package cl.elgnocci.mascota.ui.onboarding

import androidx.lifecycle.ViewModel
import cl.elgnocci.mascota.domain.Plan
import cl.elgnocci.mascota.domain.logicalDayOf
import java.time.Clock

/** Rango aceptado para "¿cuántos cigarros fumas en un día normal?". */
val CIGS_PER_DAY_RANGE = 1..60

class OnboardingViewModel(
    private val savePlan: suspend (Plan) -> Unit,
    private val clock: Clock,
) : ViewModel() {

    /** Guarda el plan con el límite inicial = [cigsPerDay], desde hoy. `false` si está fuera de rango. */
    suspend fun submit(cigsPerDay: Int): Boolean {
        if (cigsPerDay !in CIGS_PER_DAY_RANGE) return false
        savePlan(Plan(cigsPerDay, logicalDayOf(clock.instant(), clock.zone)))
        return true
    }
}
