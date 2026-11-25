package cl.elgnocci.mascota.data

import cl.elgnocci.mascota.domain.PetMood
import cl.elgnocci.mascota.domain.Plan
import cl.elgnocci.mascota.domain.foliageFor
import cl.elgnocci.mascota.domain.islandStage
import cl.elgnocci.mascota.domain.limitFor
import cl.elgnocci.mascota.domain.logicalDayOf
import cl.elgnocci.mascota.domain.moodFor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Cuánto tiempo después de registrar un cigarro se puede deshacer. */
val UNDO_WINDOW: Duration = Duration.ofMinutes(10)

/** Máximo de cigarros que se cargan de una vez con "Sumar varios". */
const val MAX_BULK = 30

/** Todo lo que la app y el widget necesitan mostrar hoy. */
data class TodayStatus(
    val count: Int,
    val limit: Int,
    val mood: PetMood,
    val cravingsResisted: Int,
    val canUndo: Boolean,
    val stage: Int,
    val leaves: Int = 0,
    val flowers: Int = 0,
)

/** Lo que las pantallas y el widget usan del repositorio (permite probarlas con un falso). */
interface BroteRepository {
    fun status(): Flow<TodayStatus?>
    suspend fun logSmoke()
    suspend fun undoLast(): Boolean
    suspend fun logCraving(resisted: Boolean)
    suspend fun logMany(count: Int, day: LocalDate): Boolean
}

class SmokeRepository(
    db: SmokeDatabase,
    private val planStore: PlanStore,
    private val clock: Clock,
    private val refresher: WidgetRefresher,
) : BroteRepository {
    private val dao = db.smokeDao()

    private fun now(): Instant = clock.instant()
    private fun today(): LocalDate = logicalDayOf(now(), clock.zone)

    override suspend fun logSmoke() {
        dao.insertSmoke(SmokeEvent(atEpochMs = now().toEpochMilli()))
        refresher.refresh()
    }

    /** Borra el último cigarro si se registró hace 10 minutos o menos. */
    override suspend fun undoLast(): Boolean {
        val last = dao.lastSmoke() ?: return false
        val age = now().toEpochMilli() - last.atEpochMs
        if (age < 0 || age > UNDO_WINDOW.toMillis()) return false
        dao.deleteSmoke(last)
        refresher.refresh()
        return true
    }

    override suspend fun logCraving(resisted: Boolean) {
        dao.insertCraving(CravingEvent(atEpochMs = now().toEpochMilli(), resisted = resisted))
        refresher.refresh()
    }

    /**
     * "Sumar varios": carga [count] cigarros a [day] (por ejemplo, anoche en un carrete).
     * Si [day] es hoy quedan con la hora actual; si es un día anterior, a las 23:00 de ese día.
     */
    override suspend fun logMany(count: Int, day: LocalDate): Boolean {
        if (count !in 1..MAX_BULK) return false
        val today = today()
        if (day.isAfter(today)) return false
        val at = if (day == today) now() else day.atTime(23, 0).atZone(clock.zone).toInstant()
        dao.insertSmokes(List(count) { SmokeEvent(atEpochMs = at.toEpochMilli()) })
        refresher.refresh()
        return true
    }

    suspend fun savePlan(plan: Plan) {
        planStore.save(plan)
        refresher.refresh()
    }

    /** El estado de hoy, recalculado cada vez que cambian los datos. `null` si aún no hay plan. */
    override fun status(): Flow<TodayStatus?> =
        combine(planStore.plan, dao.allSmokes(), dao.resistedCount()) { plan, smokes, resisted ->
            plan?.let { computeStatus(it, smokes, resisted, now(), clock.zone) }
        }
}

internal fun computeStatus(
    plan: Plan,
    smokes: List<SmokeEvent>,
    cravingsResisted: Int,
    now: Instant,
    zone: ZoneId,
): TodayStatus {
    val today = logicalDayOf(now, zone)
    val counts = smokes.groupingBy { logicalDayOf(Instant.ofEpochMilli(it.atEpochMs), zone) }.eachCount()
    val limit = limitFor(today, plan, counts)
    val count = counts[today] ?: 0

    val yesterday = today.minusDays(1)
    val yesterdayOver = !yesterday.isBefore(plan.startDay) &&
        (counts[yesterday] ?: 0) > limitFor(yesterday, plan, counts)

    val last = smokes.maxOfOrNull { it.atEpochMs }
    val canUndo = last != null && (now.toEpochMilli() - last) in 0..UNDO_WINDOW.toMillis()

    val foliage = foliageFor(plan, limit, count)
    return TodayStatus(
        count = count,
        limit = limit,
        mood = moodFor(count, limit, yesterdayOver),
        cravingsResisted = cravingsResisted,
        canUndo = canUndo,
        stage = islandStage(plan, limit),
        leaves = foliage.leaves,
        flowers = foliage.flowers,
    )
}
