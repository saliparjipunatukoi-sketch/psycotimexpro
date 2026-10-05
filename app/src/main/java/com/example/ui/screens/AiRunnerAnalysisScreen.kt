package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AiAnalysisRecordEntity
import com.example.ui.components.createSampleRunnerBitmap
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AiRunnerAnalysisScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val athletes by viewModel.athletes.collectAsState()
    val isAnalyzing by viewModel.isAiAnalyzing.collectAsState()
    val latestResult by viewModel.latestAiResult.collectAsState()
    val analysesHistory by viewModel.aiAnalyses.collectAsState()

    var selectedAthleteName by remember { mutableStateOf("") }
    var selectedPhase by remember { mutableStateOf("STARTING_BLOCK") }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(athletes) {
        if (selectedAthleteName.isEmpty() && athletes.isNotEmpty()) {
            selectedAthleteName = athletes.first().name
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
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI ANALISIS BIOMEKANIK PELARI",
                    color = NeonCyan,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "Membaca & menganalisis pergerakan pelari melalui AI Video Frame Capture",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // Selection & Capture Controls
        item {
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("1. PILIH ATLET & FASA PERGERAKAN", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Athlete Selector
                    if (athletes.isNotEmpty()) {
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        Box {
                            Surface(
                                color = Color.Black,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { dropdownExpanded = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Pelari: $selectedAthleteName",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                                }
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.background(SurfaceDark)
                            ) {
                                athletes.forEach { a ->
                                    DropdownMenuItem(
                                        text = { Text(a.name, color = TextPrimary, fontSize = 12.sp) },
                                        onClick = {
                                            selectedAthleteName = a.name
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Fasa Larian:", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Phase Selector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val phases = listOf(
                            "STARTING_BLOCK" to "Blok Permulaan",
                            "SPRINT_STRIDE" to "Hayunan Langkah",
                            "WARM_UP_POSTURE" to "Pemanasan",
                            "FINISH_DIP" to "Penamat"
                        )
                        phases.forEach { (key, label) ->
                            val isSel = selectedPhase == key
                            Surface(
                                color = if (isSel) NeonCyan.copy(alpha = 0.2f) else SurfaceVariantDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) NeonCyan else BorderDark),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedPhase = key }
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) NeonCyan else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Preview of captured frame or capture trigger
                    if (capturedBitmap != null) {
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = "Bingkai Pergerakan",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val bmp = createSampleRunnerBitmap(selectedAthleteName, selectedPhase)
                                capturedBitmap = bmp
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tangkap Bingkai", color = TextPrimary, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                val bmp = capturedBitmap ?: createSampleRunnerBitmap(selectedAthleteName, selectedPhase)
                                capturedBitmap = bmp
                                viewModel.analyzeRunnerFrame(bmp, selectedAthleteName, selectedPhase)
                            },
                            enabled = !isAnalyzing,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.2f).testTag("ai_analyze_button")
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Menganalisis...", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Jana Analisis AI", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Active Analysis Result Card
        latestResult?.let { result ->
            item {
                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("KEPUTUSAN ANALISIS BIOMEKANIK", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                Text("Atlet: $selectedAthleteName • ${result.phase}", color = TextSecondary, fontSize = 11.sp)
                            }

                            // Score Badge
                            Surface(
                                color = SprintGreen.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, SprintGreen),
                                shape = CircleShape,
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${result.score}", color = SprintGreen, fontWeight = FontWeight.Black, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                                        Text(result.grade, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderDark)

                        // Biomechanical Telemetry Chips
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            BiomechanicMetricChip(title = "Sudut Tolakan", value = result.reactionAngle, modifier = Modifier.weight(1f))
                            BiomechanicMetricChip(title = "Kekerapan Langkah", value = result.strideCadence, modifier = Modifier.weight(1f))
                            BiomechanicMetricChip(title = "Ayunan Lengan", value = result.armDriveAngle, modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Strengths & Faults
                        Surface(
                            color = Color.Black,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SprintGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kekuatan Teknik:", color = SprintGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(result.strengths, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.padding(start = 22.dp, top = 2.dp))

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kelemahan Dikesan:", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(result.faults, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.padding(start = 22.dp, top = 2.dp))

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Sports, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cadangan Latih Tubi Jurulatih:", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(result.recommendations, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.padding(start = 22.dp, top = 2.dp))
                            }
                        }
                    }
                }
            }
        }

        // Section: Rekod Arkib Analisis AI
        item {
            Text(
                text = "ARKIB ANALISIS BIOMEKANIK (${analysesHistory.size})",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        if (analysesHistory.isEmpty()) {
            item {
                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada arkib analisis. Tekan 'Jana Analisis AI' untuk mula menganalisis atlet.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(analysesHistory) { record ->
                AnalysisArchiveRow(record = record, onDelete = { viewModel.deleteAnalysis(record) })
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun BiomechanicMetricChip(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceVariantDark,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(title, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AnalysisArchiveRow(
    record: AiAnalysisRecordEntity,
    onDelete: () -> Unit
) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = record.athleteName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = "Fasa: ${record.phaseType} • ${record.reactionAngle}", color = TextSecondary, fontSize = 11.sp)
                Text(text = "Cadangan: ${record.coachRecommendations}", color = TextMuted, fontSize = 10.sp, maxLines = 1)
            }

            Surface(
                color = SprintGreen.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, SprintGreen.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "${record.score} (${record.grade})",
                    color = SprintGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = RacingRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            }
        }
    }
}
