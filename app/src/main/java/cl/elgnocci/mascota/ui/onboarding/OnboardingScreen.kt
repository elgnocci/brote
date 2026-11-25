package cl.elgnocci.mascota.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.elgnocci.mascota.R
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel) {
    var cigs by remember { mutableIntStateOf(10) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.island_0_happy),
            contentDescription = "Brote en su isla",
            modifier = Modifier.size(240.dp),
        )
        Text(
            "Hola, soy Brote",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "¿Cuántos cigarros fumas en un día normal?",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalButton(
                onClick = { cigs = (cigs - 1).coerceIn(CIGS_PER_DAY_RANGE) },
                shape = CircleShape,
                modifier = Modifier.size(56.dp).semantics { contentDescription = "Uno menos" },
            ) { Text("−", fontSize = 24.sp) }
            Text(
                "$cigs",
                fontSize = 56.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 120.dp),
            )
            FilledTonalButton(
                onClick = { cigs = (cigs + 1).coerceIn(CIGS_PER_DAY_RANGE) },
                shape = CircleShape,
                modifier = Modifier.size(56.dp).semantics { contentDescription = "Uno más" },
            ) { Text("+", fontSize = 24.sp) }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Ese será tu límite para partir. Bajará de a uno cada semana que cumplas.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = {
                saving = true
                scope.launch { if (!viewModel.submit(cigs)) saving = false }
            },
            enabled = !saving,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text("Empezar", fontSize = 18.sp) }
    }
}
