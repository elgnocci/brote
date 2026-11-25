package cl.elgnocci.mascota

import android.app.Application
import cl.elgnocci.mascota.data.DataStorePlanStore
import cl.elgnocci.mascota.data.SmokeDatabase
import cl.elgnocci.mascota.data.SmokeRepository
import cl.elgnocci.mascota.data.WidgetRefresher
import java.time.Clock

/** Arma las piezas compartidas de la app una sola vez. */
class BroteApp : Application() {
    val clock: Clock by lazy { Clock.systemDefaultZone() }

    val repository: SmokeRepository by lazy {
        SmokeRepository(
            db = SmokeDatabase.open(this),
            planStore = DataStorePlanStore(this),
            clock = clock,
            refresher = WidgetRefresher { }, // el widget llega en la Tarea 8
        )
    }
}
