package cl.elgnocci.mascota.data

/** Avisa al widget que los datos cambiaron. La implementación con Glance llega en la Tarea 8. */
fun interface WidgetRefresher {
    suspend fun refresh()
}
