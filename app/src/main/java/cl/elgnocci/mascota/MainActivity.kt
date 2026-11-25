package cl.elgnocci.mascota

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.elgnocci.mascota.data.TodayStatus
import cl.elgnocci.mascota.ui.home.HomeScreen
import cl.elgnocci.mascota.ui.home.HomeViewModel
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
                        is Screen.Home -> HomeScreen(
                            viewModel { HomeViewModel(app.repository, app.clock) }
                        )
                    }
                }
            }
        }
    }
}
