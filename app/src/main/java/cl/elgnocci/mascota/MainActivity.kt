package cl.elgnocci.mascota

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.elgnocci.mascota.data.TodayStatus
import cl.elgnocci.mascota.ui.onboarding.OnboardingScreen
import cl.elgnocci.mascota.ui.onboarding.OnboardingViewModel
import cl.elgnocci.mascota.ui.theme.BroteTheme
import kotlinx.coroutines.flow.map

private sealed interface Screen {
    data object Loading : Screen
    data object Onboarding : Screen
    data class Home(val status: TodayStatus) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as BroteApp
        setContent {
            BroteTheme {
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                    val flow = remember {
                        app.repository.status().map { s -> if (s == null) Screen.Onboarding else Screen.Home(s) }
                    }
                    val screen by flow.collectAsStateWithLifecycle(initialValue = Screen.Loading)
                    when (val current = screen) {
                        Screen.Loading -> Unit
                        Screen.Onboarding -> OnboardingScreen(
                            viewModel { OnboardingViewModel(app.repository::savePlan, app.clock) }
                        )
                        is Screen.Home -> HomePlaceholder(current.status)
                    }
                }
            }
        }
    }
}

/** Provisorio hasta la Tarea 6. */
@Composable
private fun HomePlaceholder(status: TodayStatus) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Hoy: ${status.count} de ${status.limit}", style = MaterialTheme.typography.headlineMedium)
    }
}
