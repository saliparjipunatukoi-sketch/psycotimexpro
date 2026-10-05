package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.RoutineDrill
import com.example.ai.RoutinePhase
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiRoutineScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val warmUpPlan by viewModel.warmUpPlan.collectAsState()
    val isGenerating by viewModel.isGeneratingRoutine.collectAsState()

    var selectedEvent by remember { mutableStateOf("100m Pecut") }
    var selectedLevel by remember { mutableStateOf("Remaja / Sukma") }
    var specificFocusInput by remember { mutableStateOf("Knee punch tinggi & pelepasan blok eksplosif") }

    // Interactive Track Drill Timer State
    var drillTimerSeconds by remember { mutableStateOf(30) }
    var isTimerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && drillTimerSeconds > 0) {
            delay(1000)
            drillTimerSeconds -= 1
        }
        if (drillTimerSeconds == 0) {
            isTimerRunning = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = SprintGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI RUTIN LATIHAN & PEMANASAN",
                    color = SprintGreen,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "Cadangan rutin harian dan pemanasan khusus sebelum latihan pecut",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // Live Track Drill Timer Card
        item {
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("PEMASA LATIH TUBI DI TREK", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(
                            text = String.format("%02d:%02d", drillTimerSeconds / 60, drillTimerSeconds % 60),
                            color = if (drillTimerSeconds <= 5) RacingRed else Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { isTimerRunning = !isTimerRunning },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isTimerRunning) RacingRed else SprintGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isTimerRunning) "Jeda" else "Mula", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                isTimerRunning = false
                                drillTimerSeconds = 30
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("30s", color = TextPrimary, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                isTimerRunning = false
                                drillTimerSeconds = 60
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("60s", color = TextPrimary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Generator Setup Card
        item {
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("KONFIGURASI RUTIN HARIAN", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Acara Sukan:", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("100m Pecut", "200m Pecut", "400m Pecut", "110m Pagar").forEach { ev ->
                            val isSel = selectedEvent == ev
                            Surface(
                                color = if (isSel) SprintGreen.copy(alpha = 0.2f) else SurfaceVariantDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) SprintGreen else BorderDark),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedEvent = ev }
                            ) {
                                Text(
                                    text = ev,
                                    color = if (isSel) SprintGreen else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Tahap Atlet:", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Pemula / Sekolah", "Remaja / Sukma", "Elit Kebangsaan").forEach { lvl ->
                            val isSel = selectedLevel == lvl
                            Surface(
                                color = if (isSel) NeonCyan.copy(alpha = 0.2f) else SurfaceVariantDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) NeonCyan else BorderDark),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedLevel = lvl }
                            ) {
                                Text(
                                    text = lvl,
                                    color = if (isSel) NeonCyan else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = specificFocusInput,
                        onValueChange = { specificFocusInput = it },
                        label = { Text("Fokus Khusus Jurulatih") },
                        placeholder = { Text("cth: Dorsifleksi kaki, explosiveness pelepasan blok") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SprintGreen,
                            unfocusedBorderColor = BorderDark
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.generateWarmUpPlan(selectedEvent, selectedLevel, specificFocusInput)
                        },
                        enabled = !isGenerating,
                        modifier = Modifier.fillMaxWidth().testTag("generate_routine_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = SprintGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Menjana Rutin AI...", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Jana Rutin Pemanasan AI", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active Routine Plan Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = warmUpPlan.title.uppercase(),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Jumlah Masa: ~${warmUpPlan.totalDurationMinutes} Minit • ${warmUpPlan.athleteLevel}",
                        color = NeonCyan,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Routine Phases
        items(warmUpPlan.phases) { phase ->
            RoutinePhaseCard(phase = phase)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RoutinePhaseCard(phase: RoutinePhase) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = phase.phaseName,
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = NeonCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${phase.durationMinutes} min",
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            phase.drills.forEach { drill ->
                DrillItemRow(drill = drill)
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
fun DrillItemRow(drill: RoutineDrill) {
    Surface(
        color = Color.Black,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(drill.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Isyarat Jurulatih: ${drill.coachingCue}", color = TextSecondary, fontSize = 10.sp)
            }

            Surface(
                color = SprintGreen.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, SprintGreen.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = drill.repsOrDistance,
                    color = SprintGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}
