package cl.elgnocci.mascota.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FoliageTest {
    private val start = LocalDate.of(2026, 11, 24)

    @Test fun cadaCigarroBotaUnaHoja() = assertEquals(Foliage(leaves = 4, flowers = 0), foliageFor(Plan(6, start), limit = 6, count = 2))
    @Test fun alBajarElLimiteUnaHojaSeVuelveFlor() = assertEquals(Foliage(leaves = 5, flowers = 1), foliageFor(Plan(6, start), limit = 5, count = 0))
    @Test fun enCeroBroteEstaFlorecido() = assertEquals(Foliage(leaves = 0, flowers = 6), foliageFor(Plan(6, start), limit = 0, count = 0))
    @Test fun pasarseNoDejaHojasNegativas() = assertEquals(Foliage(leaves = 0, flowers = 0), foliageFor(Plan(6, start), limit = 6, count = 9))
    @Test fun lasFloresNoSeCaen() = assertEquals(Foliage(leaves = 0, flowers = 3), foliageFor(Plan(6, start), limit = 3, count = 5))
    @Test fun conLimiteAltoCadaHojaValeVarios() = assertEquals(Foliage(leaves = 8, flowers = 0), foliageFor(Plan(20, start), limit = 20, count = 4))
    @Test fun maximoDiezEspacios() = assertEquals(Foliage(leaves = 5, flowers = 5), foliageFor(Plan(20, start), limit = 10, count = 0))
}
