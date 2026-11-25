package cl.elgnocci.mascota.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PetMoodTest {
    @Test fun bajoElLimiteEstaContenta() = assertEquals(PetMood.HAPPY, moodFor(2, 8, false))
    @Test fun justoEnElLimiteEstaCansada() = assertEquals(PetMood.TIRED, moodFor(8, 8, false))
    @Test fun sobreElLimiteEstaEnferma() = assertEquals(PetMood.SICK, moodFor(9, 8, false))
    @Test fun ayerEnfermaYHoyBajoElLimiteSeRecupera() = assertEquals(PetMood.RECOVERING, moodFor(3, 8, true))
    @Test fun ayerEnfermaYHoySobreElLimiteSigueEnferma() = assertEquals(PetMood.SICK, moodFor(9, 8, true))
    @Test fun limiteCeroSinFumarCelebra() = assertEquals(PetMood.CELEBRATING, moodFor(0, 0, false))
    @Test fun limiteCeroYFumarEnferma() = assertEquals(PetMood.SICK, moodFor(1, 0, false))
}
