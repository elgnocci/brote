package cl.elgnocci.mascota.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.elgnocci.mascota.data.MAX_BULK
import cl.elgnocci.mascota.data.TodayStatus
import cl.elgnocci.mascota.ui.pet.PetImage
import cl.elgnocci.mascota.ui.pet.moodDescription

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val status by viewModel.state.collectAsStateWithLifecycle()
    val s = status ?: return
    HomeContent(s, viewModel::onSmoke, viewModel::onUndo, viewModel::onAddMany)
}

@Composable
private fun HomeContent(
    s: TodayStatus,
    onSmoke: () -> Unit,
    onUndo: () -> Unit,
    onAddMany: (Int, Boolean) -> Unit,
) {
    var addingMany by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PetImage(s.stage, s.mood, Modifier.fillMaxWidth().aspectRatio(1f))
        Text(
            moodDescription(s.mood),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "${s.count} de ${s.limit}",
            fontSize = 56.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (s.count > s.limit) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground,
        )
        Text("cigarros hoy", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onSmoke,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text("Fumé uno", fontSize = 18.sp) }
        if (s.canUndo) {
            TextButton(onClick = onUndo) { Text("Deshacer el último") }
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { addingMany = true }) { Text("Sumar varios") }
        Spacer(Modifier.height(8.dp))
        Text(
            "Ganas vencidas: ${s.cravingsResisted}",
            style = MaterialTheme.typography.bodyMedium,
        )
    }

    if (addingMany) {
        AddManyDialog(
            onDismiss = { addingMany = false },
            onConfirm = { count, lastNight ->
                onAddMany(count, lastNight)
                addingMany = false
            },
        )
    }
}

@Composable
private fun AddManyDialog(onDismiss: () -> Unit, onConfirm: (Int, Boolean) -> Unit) {
    var count by remember { mutableIntStateOf(3) }
    var lastNight by remember { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sumar varios") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Para cuando perdiste la cuenta, por ejemplo en un carrete.")
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = lastNight, onClick = { lastNight = true }, label = { Text("Anoche") })
                    FilterChip(selected = !lastNight, onClick = { lastNight = false }, label = { Text("Hoy") })
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = { count = (count - 1).coerceIn(1, MAX_BULK) },
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp).semantics { contentDescription = "Uno menos" },
                    ) { Text("−") }
                    Text(
                        "$count",
                        fontSize = 40.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(min = 96.dp),
                    )
                    FilledTonalButton(
                        onClick = { count = (count + 1).coerceIn(1, MAX_BULK) },
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp).semantics { contentDescription = "Uno más" },
                    ) { Text("+") }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(count, lastNight) }) { Text("Sumar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
