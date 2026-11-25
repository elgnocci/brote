package cl.elgnocci.mascota.ui

import cl.elgnocci.mascota.data.TodayStatus
import cl.elgnocci.mascota.domain.PetMood
import cl.elgnocci.mascota.ui.scene.toSceneJson
import org.junit.Assert.assertEquals
import org.junit.Test

class SceneStateTest {
    @Test
    fun elEstadoSeTraduceALaEscena() {
        val s = TodayStatus(
            count = 7, limit = 5, mood = PetMood.SICK, cravingsResisted = 3,
            canUndo = false, stage = 1, leaves = 4, flowers = 1,
        )
        assertEquals("""{"stage":1,"mood":"sick","leaves":4,"flowers":1,"fireflies":3}""", s.toSceneJson())
    }
}
