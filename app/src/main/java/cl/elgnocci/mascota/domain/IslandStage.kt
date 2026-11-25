package cl.elgnocci.mascota.domain

import kotlin.math.roundToInt

/** La isla tiene 7 etapas: 0 (pelada) a 6 (completa, límite en 0). */
const val LAST_STAGE = 6

/** Cuánto ha crecido la isla según cuánto ha bajado el límite desde el inicio. */
fun islandStage(plan: Plan, limit: Int): Int {
    if (plan.startLimit <= 0) return LAST_STAGE
    val progress = (plan.startLimit - limit).toDouble() / plan.startLimit
    return (LAST_STAGE * progress).roundToInt().coerceIn(0, LAST_STAGE)
}
