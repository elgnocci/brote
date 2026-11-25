package cl.elgnocci.mascota.ui.pet

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import cl.elgnocci.mascota.R
import cl.elgnocci.mascota.domain.LAST_STAGE
import cl.elgnocci.mascota.domain.PetMood

/** Imágenes generadas por tools/island/render.py: [etapa][ánimo en el orden de PetMood]. */
private val ISLAND = listOf(
        listOf(R.drawable.island_0_happy, R.drawable.island_0_tired, R.drawable.island_0_sick, R.drawable.island_0_recovering, R.drawable.island_0_celebrating),
        listOf(R.drawable.island_1_happy, R.drawable.island_1_tired, R.drawable.island_1_sick, R.drawable.island_1_recovering, R.drawable.island_1_celebrating),
        listOf(R.drawable.island_2_happy, R.drawable.island_2_tired, R.drawable.island_2_sick, R.drawable.island_2_recovering, R.drawable.island_2_celebrating),
        listOf(R.drawable.island_3_happy, R.drawable.island_3_tired, R.drawable.island_3_sick, R.drawable.island_3_recovering, R.drawable.island_3_celebrating),
        listOf(R.drawable.island_4_happy, R.drawable.island_4_tired, R.drawable.island_4_sick, R.drawable.island_4_recovering, R.drawable.island_4_celebrating),
        listOf(R.drawable.island_5_happy, R.drawable.island_5_tired, R.drawable.island_5_sick, R.drawable.island_5_recovering, R.drawable.island_5_celebrating),
        listOf(R.drawable.island_6_happy, R.drawable.island_6_tired, R.drawable.island_6_sick, R.drawable.island_6_recovering, R.drawable.island_6_celebrating),
)

@DrawableRes
fun islandImage(stage: Int, mood: PetMood): Int = ISLAND[stage.coerceIn(0, LAST_STAGE)][mood.ordinal]

fun moodDescription(mood: PetMood): String = when (mood) {
    PetMood.HAPPY -> "Brote está contento"
    PetMood.TIRED -> "Brote está cansado: llegaste al límite de hoy"
    PetMood.SICK -> "Brote se enfermó: te pasaste del límite"
    PetMood.RECOVERING -> "Brote se está recuperando"
    PetMood.CELEBRATING -> "¡Límite en cero! Brote está celebrando"
}

@Composable
fun PetImage(stage: Int, mood: PetMood, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(islandImage(stage, mood)),
        contentDescription = moodDescription(mood),
        modifier = modifier,
    )
}
