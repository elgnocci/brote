package cl.elgnocci.mascota.domain

import kotlin.math.ceil
import kotlin.math.roundToInt

/** Brote tiene como máximo 10 espacios para hojas o flores. */
const val MAX_SLOTS = 10

/** Hojas que quedan hoy (el cupo) y flores ganadas (cada vez que bajó el límite). */
data class Foliage(val leaves: Int, val flowers: Int)

/**
 * Brote tiene tantos espacios como su límite inicial (máximo 10). Cada vez que el límite baja,
 * una hoja se vuelve flor para siempre. Las hojas restantes son el cupo de hoy: cada cigarro
 * bota una (con límites altos, una hoja vale varios cigarros). Las flores nunca se caen.
 */
fun foliageFor(plan: Plan, limit: Int, count: Int): Foliage {
    val start = plan.startLimit
    if (start <= 0) return Foliage(0, 0)
    val slots = minOf(start, MAX_SLOTS)
    val flowers = (slots * (start - limit).toDouble() / start).roundToInt().coerceIn(0, slots)
    val cigsPerLeaf = start.toDouble() / slots
    val fallen = ceil(count / cigsPerLeaf).toInt()
    return Foliage(leaves = (slots - flowers - fallen).coerceAtLeast(0), flowers = flowers)
}
