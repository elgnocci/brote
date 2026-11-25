package cl.elgnocci.mascota.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class IslandStageTest {
    private val start = LocalDate.of(2026, 11, 24)

    @Test fun alPartirLaIslaEstaPelada() = assertEquals(0, islandStage(Plan(6, start), 6))
    @Test fun aMitadDeCaminoVaEnLaEtapa3() = assertEquals(3, islandStage(Plan(6, start), 3))
    @Test fun enCeroLaIslaEstaCompleta() = assertEquals(6, islandStage(Plan(6, start), 0))
    @Test fun escalaConOtroLimiteInicial() = assertEquals(3, islandStage(Plan(12, start), 6))
    @Test fun nuncaBajaDe0() = assertEquals(0, islandStage(Plan(6, start), 8))
    @Test fun limiteInicialCeroEsIslaCompleta() = assertEquals(6, islandStage(Plan(0, start), 0))
}
