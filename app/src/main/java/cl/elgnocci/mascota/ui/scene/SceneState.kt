package cl.elgnocci.mascota.ui.scene

import cl.elgnocci.mascota.data.TodayStatus

/** El estado que entiende BroteScene.setState() en tools/scene/scene.js. */
fun TodayStatus.toSceneJson(): String =
    """{"stage":$stage,"mood":"${mood.name.lowercase()}","leaves":$leaves,"flowers":$flowers,"fireflies":$cravingsResisted}"""
