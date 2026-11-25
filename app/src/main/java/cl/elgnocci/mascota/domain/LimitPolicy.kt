package cl.elgnocci.mascota.domain

import java.time.LocalDate

/** Días de cada bloque y cuántos deben cumplirse para que el límite baje. */
const val BLOCK_DAYS = 7L
const val DAYS_TO_PASS = 5

/** Punto de partida: cuántos cigarros al día se permiten al comienzo, y desde qué día. */
data class Plan(val startLimit: Int, val startDay: LocalDate)

/**
 * El límite vigente en [day]. Recorre los bloques de 7 días completos desde el inicio:
 * si en un bloque se cumplieron al menos 5 días (cigarros ≤ límite de ese bloque;
 * un día sin registro cuenta 0), el límite baja en 1, sin bajar nunca de 0.
 */
fun limitFor(day: LocalDate, plan: Plan, counts: Map<LocalDate, Int>): Int {
    var limit = plan.startLimit
    var blockStart = plan.startDay
    while (!blockStart.plusDays(BLOCK_DAYS).isAfter(day)) {
        val passed = (0 until BLOCK_DAYS).count { i ->
            (counts[blockStart.plusDays(i)] ?: 0) <= limit
        }
        if (passed >= DAYS_TO_PASS) limit = maxOf(0, limit - 1)
        blockStart = blockStart.plusDays(BLOCK_DAYS)
    }
    return limit
}
