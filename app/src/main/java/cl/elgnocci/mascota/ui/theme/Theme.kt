package cl.elgnocci.mascota.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Sprout = Color(0xFF3F9B4A)
val Pot = Color(0xFFE07352)
val Sky = Color(0xFFDCEBF0)
val Cream = Color(0xFFF4EFE4)
val Ink = Color(0xFF22201A)

private val colors = lightColorScheme(
    primary = Sprout,
    onPrimary = Color.White,
    secondary = Pot,
    onSecondary = Color.White,
    background = Cream,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = Sky,
)

@Composable
fun BroteTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, content = content)
}
