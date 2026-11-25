package cl.elgnocci.mascota.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cl.elgnocci.mascota.domain.Plan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** Dónde vive el plan (límite inicial y día de inicio). `null` hasta la primera apertura. */
interface PlanStore {
    val plan: Flow<Plan?>
    suspend fun save(plan: Plan)
}

private val Context.planDataStore by preferencesDataStore(name = "plan")

class DataStorePlanStore(private val context: Context) : PlanStore {
    private val startLimit = intPreferencesKey("start_limit")
    private val startDay = stringPreferencesKey("start_day")

    override val plan: Flow<Plan?> = context.planDataStore.data.map { prefs ->
        val limit = prefs[startLimit]
        val day = prefs[startDay]
        if (limit == null || day == null) null else Plan(limit, LocalDate.parse(day))
    }

    override suspend fun save(plan: Plan) {
        context.planDataStore.edit {
            it[startLimit] = plan.startLimit
            it[startDay] = plan.startDay.toString()
        }
    }
}
