@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package it.sfida52.app

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToLong

// ---------- Tema ----------

private val Ball = Color(0xFFF2B630)
private val BallInk = Color(0xFF2B2104)
private val Paid = Color(0xFF2E8B57)
private val Tnum = TextStyle(fontFeatureSettings = "tnum")

private val LightColors = lightColorScheme(
    primary = Color(0xFF1F5A4A), onPrimary = Color.White,
    background = Color(0xFFEDF1EC), onBackground = Color(0xFF16302A),
    surface = Color(0xFFFFFFFF), onSurface = Color(0xFF16302A),
    surfaceVariant = Color(0xFFE3E9E4), onSurfaceVariant = Color(0xFF5D706A),
    outline = Color(0xFFD3DCD6), secondaryContainer = Color(0xFFCFE5DC),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF5FBF9F), onPrimary = Color(0xFF0B1714),
    background = Color(0xFF0F1A17), onBackground = Color(0xFFE4EEE9),
    surface = Color(0xFF16241F), onSurface = Color(0xFFE4EEE9),
    surfaceVariant = Color(0xFF1E2F29), onSurfaceVariant = Color(0xFF93A8A0),
    outline = Color(0xFF2A3B35), secondaryContainer = Color(0xFF24433A),
)

@Composable
fun SfidaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors, content = content)
}

// ---------- Activity ----------

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SfidaTheme { App() } }
    }
}

@Composable
fun App(vm: ChallengeViewModel = viewModel()) {
    var editing by rememberSaveable { mutableStateOf(false) }
    val c = vm.challenge
    if (c == null || editing) {
        SetupScreen(
            hasExisting = c != null,
            onCancel = { editing = false },
            onStart = { amounts, freq, start ->
                vm.start(amounts, freq, start)
                editing = false
            },
        )
    } else {
        MainScreen(c, vm, onNew = { editing = true })
    }
}

// ---------- Impostazione sfida ----------

@Composable
fun SetupScreen(
    hasExisting: Boolean,
    onCancel: () -> Unit,
    onStart: (List<Long>, Frequency, LocalDate) -> Unit,
) {
    var targetText by rememberSaveable { mutableStateOf("1378") }
    var freq by rememberSaveable { mutableStateOf(Frequency.WEEKLY) }
    var roundEuro by rememberSaveable { mutableStateOf(true) }
    var start by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var confirmReplace by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val targetCents = targetText.replace(',', '.').toDoubleOrNull()?.let { (it * 100).roundToLong() }
    val amounts = targetCents?.takeIf { it >= 1000 }?.let { Amounts.build(it, freq.draws, roundEuro) }
    val previewText = when {
        targetCents == null || targetCents < 1000 -> "Inserisci un obiettivo di almeno 10 €."
        amounts == null -> "Con ${freq.draws} estrazioni l'obiettivo minimo è ${freq.draws} €."
        else -> "${freq.draws} importi da ${Fmt.money(amounts.first())} a ${Fmt.money(amounts.last())}, " +
            "totale ${Fmt.money(amounts.sum())}."
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Nuova sfida", fontWeight = FontWeight.Bold) }) }) { pad ->
        Column(
            Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = targetText,
                onValueChange = { v -> targetText = v.filter { it.isDigit() || it == ',' || it == '.' } },
                label = { Text("Obiettivo finale (€)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Frequenza estrazione", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Frequency.entries.forEach { f ->
                    FilterChip(
                        selected = freq == f,
                        onClick = { freq = f },
                        label = { Text("${f.label} · ${f.draws}") },
                    )
                }
            }

            OutlinedButton(onClick = {
                DatePickerDialog(
                    context,
                    { _, y, m, d -> start = LocalDate.of(y, m + 1, d) },
                    start.year, start.monthValue - 1, start.dayOfMonth,
                ).show()
            }) { Text("Data inizio: ${Fmt.date(start)}") }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { roundEuro = !roundEuro },
            ) {
                Checkbox(checked = roundEuro, onCheckedChange = { roundEuro = it })
                Text("Importi arrotondati all'euro")
            }

            Text(previewText, color = MaterialTheme.colorScheme.onSurfaceVariant, style = Tnum)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = amounts != null,
                    onClick = {
                        if (hasExisting) confirmReplace = true
                        else amounts?.let { onStart(it, freq, start) }
                    },
                ) { Text("Inizia la sfida") }
                if (hasExisting) TextButton(onClick = onCancel) { Text("Annulla") }
            }
        }
    }

    if (confirmReplace) {
        AlertDialog(
            onDismissRequest = { confirmReplace = false },
            title = { Text("Sostituire la sfida in corso?") },
            text = { Text("Le estrazioni fatte finora verranno cancellate.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmReplace = false
                    amounts?.let { onStart(it, freq, start) }
                }) { Text("Sostituisci") }
            },
            dismissButton = { TextButton(onClick = { confirmReplace = false }) { Text("Annulla") } },
        )
    }
}

// ---------- Schermata principale ----------

private enum class Filter(val label: String) { ALL("Tutti"), LEFT("Da estrarre"), OUT("Estratti") }

@Composable
fun MainScreen(c: Challenge, vm: ChallengeViewModel, onNew: () -> Unit) {
    var filter by rememberSaveable { mutableStateOf(Filter.ALL) }
    var rolling by remember { mutableStateOf(false) }
    var rollValue by remember { mutableStateOf<Long?>(null) }
    var flashIndex by remember { mutableStateOf<Int?>(null) }
    var askEarly by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val due = c.nextDue
    val isDue = due != null && !due.isAfter(LocalDate.now())
    val last = c.draws.lastOrNull()

    fun doDraw() {
        if (rolling || c.isComplete) return
        rolling = true
        scope.launch {
            val left = c.remainingIndexes
            repeat(14) { i ->
                val r = left.random()
                rollValue = c.amounts[r]
                flashIndex = r
                delay(70L + i * 8L)
            }
            vm.draw()
            flashIndex = null
            rolling = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sfida 52 Random", fontWeight = FontWeight.Bold) },
                actions = { TextButton(onClick = onNew) { Text("Nuova sfida") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { pad ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = pad.calculateTopPadding() + 4.dp,
                bottom = pad.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            // Riepilogo
            item {
                Section {
                    Row(Modifier.fillMaxWidth()) {
                        Stat("Versato", Fmt.money(c.paidCents), Modifier.weight(1f))
                        Stat("Estratto", Fmt.money(c.drawnCents), Modifier.weight(1f))
                        Stat("Obiettivo", Fmt.money(c.targetCents), Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    ProgressBar(
                        paid = c.paidCents.toFloat() / c.targetCents,
                        drawnOnly = (c.drawnCents - c.paidCents).toFloat() / c.targetCents,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Legend(Paid, "Versato")
                        Legend(Ball, "Da versare")
                        Legend(MaterialTheme.colorScheme.surfaceVariant, "Da estrarre")
                    }
                }
            }

            // Estrazione
            item {
                Section(horizontalAlignment = Alignment.CenterHorizontally) {
                    Label(if (c.isComplete) "Sfida completata" else "Estrazione ${c.draws.size + 1} di ${c.amounts.size}")
                    Spacer(Modifier.height(12.dp))
                    val idle = !rolling && last == null
                    Box(
                        Modifier
                            .size(136.dp)
                            .clip(CircleShape)
                            .background(
                                if (idle) Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                                else Brush.radialGradient(listOf(Color(0xFFFFE7A3), Ball, Color(0xFFC98E10))),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val value = when {
                                rolling -> rollValue?.let(Fmt::money) ?: "?"
                                last != null -> Fmt.money(c.amounts[last.index])
                                else -> "?"
                            }
                            val sub = when {
                                rolling -> "estrazione…"
                                last == null -> "pronta"
                                last.paid -> "versato"
                                else -> "da versare"
                            }
                            val ink = if (idle) MaterialTheme.colorScheme.onSurfaceVariant else BallInk
                            Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = ink, style = Tnum)
                            Text(sub.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ink, letterSpacing = 1.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        when {
                            due == null -> "Hai raggiunto l'obiettivo di ${Fmt.money(c.targetCents)}."
                            isDue -> "Estrazione disponibile da ${Fmt.date(due)}."
                            else -> "Prossima estrazione: ${Fmt.date(due)}."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!c.isComplete) {
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { doDraw() }, enabled = isDue && !rolling) { Text("Estrai l'importo") }
                        if (!isDue) {
                            TextButton(onClick = { askEarly = true }, enabled = !rolling) { Text("Estrai in anticipo") }
                        }
                    }
                }
            }

            // Importi
            item {
                Section {
                    Text("Importi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Filter.entries.forEach { f ->
                            val count = when (f) {
                                Filter.ALL -> c.amounts.size
                                Filter.LEFT -> c.amounts.size - c.draws.size
                                Filter.OUT -> c.draws.size
                            }
                            FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text("${f.label} ($count)") })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    val drawnMap = c.draws.associateBy { it.index }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        c.amounts.forEachIndexed { i, a ->
                            val d = drawnMap[i]
                            val show = when (filter) {
                                Filter.ALL -> true
                                Filter.LEFT -> d == null
                                Filter.OUT -> d != null
                            }
                            if (show) {
                                val (bg, fg) = when {
                                    d == null -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurface
                                    d.paid -> Paid to Color.White
                                    else -> Ball to BallInk
                                }
                                val outlined = i == flashIndex || (!rolling && i == last?.index)
                                Box(
                                    Modifier
                                        .widthIn(min = 70.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(bg)
                                        .border(2.dp, if (outlined) MaterialTheme.colorScheme.onSurface else bg, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 6.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(Fmt.money(a), color = fg, fontWeight = FontWeight.Bold, fontSize = 14.sp, style = Tnum)
                                }
                            }
                        }
                    }
                }
            }

            // Storico
            item {
                Column {
                    Text("Estrazioni", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (c.draws.isEmpty()) {
                        Text("Nessuna estrazione ancora.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            items(c.draws.indices.reversed().toList()) { pos ->
                val d = c.draws[pos]
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("#${pos + 1}", Modifier.width(44.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = Tnum)
                        Column(Modifier.weight(1f)) {
                            Text(Fmt.money(c.amounts[d.index]), fontWeight = FontWeight.Bold, style = Tnum)
                            Text(Fmt.date(d.date), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        FilterChip(
                            selected = d.paid,
                            onClick = { vm.togglePaid(pos) },
                            label = { Text(if (d.paid) "✓ Versato" else "Segna versato") },
                        )
                    }
                }
            }
        }
    }

    if (askEarly) {
        AlertDialog(
            onDismissRequest = { askEarly = false },
            title = { Text("Anticipare l'estrazione?") },
            text = { Text("La prossima era prevista per ${due?.let(Fmt::date) ?: "-"}.") },
            confirmButton = {
                TextButton(onClick = {
                    askEarly = false
                    doDraw()
                }) { Text("Sì, estrai") }
            },
            dismissButton = { TextButton(onClick = { askEarly = false }) { Text("No") } },
        )
    }
}

// ---------- Componenti ----------

@Composable
private fun Section(
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = horizontalAlignment) { content() }
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Label(label)
        Text(value, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, style = Tnum)
    }
}

@Composable
private fun ProgressBar(paid: Float, drawnOnly: Float) {
    val p = paid.coerceIn(0f, 1f)
    val d = drawnOnly.coerceIn(0f, 1f - p)
    val rest = 1f - p - d
    Row(
        Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        if (p > 0f) Box(Modifier.fillMaxHeight().weight(p).background(Paid))
        if (d > 0f) Box(Modifier.fillMaxHeight().weight(d).background(Ball))
        if (rest > 0f) Spacer(Modifier.weight(rest))
    }
}

@Composable
private fun Legend(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
