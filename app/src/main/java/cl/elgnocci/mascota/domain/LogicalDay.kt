package cl.elgnocci.mascota.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Hora local (reloj de pared) en que empieza un día nuevo para la app. */
const val DAY_START_HOUR = 4L

/**
 * El día al que pertenece un momento: cambia a las 04:00 hora local, no a medianoche,
 * para que un cigarro a la 1 AM de un carrete cuente para esa noche.
 */
fun logicalDayOf(instant: Instant, zone: ZoneId): LocalDate =
    instant.atZone(zone).toLocalDateTime().minusHours(DAY_START_HOUR).toLocalDate()
