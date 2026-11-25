package cl.elgnocci.mascota.domain

enum class PetMood { HAPPY, TIRED, SICK, RECOVERING, CELEBRATING }

/**
 * Cómo está la mascota hoy. El daño nunca se arrastra más de un día:
 * solo importa si ayer te pasaste (para mostrarla recuperándose).
 */
fun moodFor(today: Int, limit: Int, yesterdayOver: Boolean): PetMood = when {
    today > limit -> PetMood.SICK
    limit == 0 -> PetMood.CELEBRATING
    yesterdayOver && today < limit -> PetMood.RECOVERING
    today == limit -> PetMood.TIRED
    else -> PetMood.HAPPY
}
