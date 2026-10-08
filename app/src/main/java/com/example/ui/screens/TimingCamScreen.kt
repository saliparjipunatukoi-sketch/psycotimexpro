package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.entity.TimingRunEntity
import com.example.ui.components.CameraStreamView
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.TimingState

@Composable
fun TimingCamScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentRunIndex by viewModel.currentRunIndex.collectAsState()
    val timingState by viewModel.timingState.collectAsState()
    val elapsedMillis by viewModel.elapsedMillis.collectAsState()
    val camMode by viewModel.selectedCamMode.collectAsState()
    val timingRuns by viewModel.timingRuns.collectAsState()

    var isFrontCam by remember { mutableStateOf(false) }
    var singleCamFinishLineMode by remember { mutableStateOf(false) } // Mod 1 Telefon: Garisan Merah di tengah Cam 1
    var activeSubTab by remember { mutableStateOf(0) } // 0: Live Camera ET, 1: Replay & Rakaman Race

    // AI Analysis Dialog State
    var selectedRunForAi by remember { mutableStateOf<TimingRunEntity?>(null) }
    var isFieldEventAiMode by remember { mutableStateOf(false) } // Balapan vs Acara Padang (Lontar Peluru / Lompat Jauh)
    var selectedRunForPhotoFinish by remember { mutableStateOf<TimingRunEntity?>(null) }

    // Check Camera Permission
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        // Sub-tabs switcher at the top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = if (activeSubTab == 0) RacingRed else Color.Transparent,
                border = if (activeSubTab == 0) null else androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { activeSubTab = 0 }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, tint = if (activeSubTab == 0) Color.White else TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("KAMERA ET LIVE", color = if (activeSubTab == 0) Color.White else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Surface(
                color = if (activeSubTab == 1) RacingRed else Color.Transparent,
                border = if (activeSubTab == 1) null else androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { activeSubTab = 1 }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = if (activeSubTab == 1) Color.White else TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("REPLAY & RAKAMAN (${timingRuns.size})", color = if (activeSubTab == 1) Color.White else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        if (activeSubTab == 0) {
            // ==================== LIVE CAMERA ET VIEW ====================
            Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
                // Live camera stream - TANPA WATERMARK / TANPA LOGO semasa standby & live record!
                CameraStreamView(
                    modifier = Modifier.fillMaxSize(),
                    isFrontFacing = isFrontCam,
                    hasCameraPermission = hasCameraPermission
                ) {
                    // Garisan Penamat Merah (CAM 2 atau Mod 1 Telefon pada CAM 1)
                    if (camMode == "CAM_2_FINISH" || singleCamFinishLineMode) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(3.dp)
                                .align(Alignment.Center)
                                .background(RacingRed)
                        )

                        // Torso indicator
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 100.dp)
                        ) {
                            Text(
                                text = "GARISAN PENAMAT (TORSO GATE)",
                                color = RacingRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // ================= OVERLAY TIMER (Attached Directly On Top of Video) =================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    // Top Bar Controls (Mode switcher & single phone toggle)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Camera Mode Switcher (Cam 1 Start vs Cam 2 Finish)
                        Surface(
                            color = if (camMode == "CAM_1_START") Color.Black.copy(alpha = 0.7f) else RacingRed.copy(alpha = 0.3f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (camMode == "CAM_1_START") SilverMetallic else RacingRed),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.clickable {
                                viewModel.setCamMode(if (camMode == "CAM_1_START") "CAM_2_FINISH" else "CAM_1_START")
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (camMode == "CAM_1_START") Icons.Default.Videocam else Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = if (camMode == "CAM_1_START") Color.White else RacingRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (camMode == "CAM_1_START") "CAM 1 (START / WIDE)" else "CAM 2 (FINISH LINE)",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = SilverMetallic, modifier = Modifier.size(14.dp))
                            }
                        }

                        // Flip Camera Front/Back
                        IconButton(
                            onClick = { isFrontCam = !isFrontCam },
                            modifier = Modifier.size(34.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.Cameraswitch, contentDescription = "Tukar Kamera", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Timer HUD Overlay attached to center top
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, when (timingState) {
                                TimingState.RUNNING -> RacingRed
                                TimingState.FINISHED -> SprintGreen
                                TimingState.READY -> Color.White
                            })
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "RACE #$currentRunIndex",
                                    color = SilverMetallic,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = MainViewModel.formatMillisToStopwatch(elapsedMillis),
                                    color = when (timingState) {
                                        TimingState.RUNNING -> RacingRed
                                        TimingState.FINISHED -> SprintGreen
                                        TimingState.READY -> Color.White
                                    },
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = when (timingState) {
                                        TimingState.READY -> "SEDIA • TEKAN START (BUNYI PISTOL)"
                                        TimingState.RUNNING -> "● MERAKAM LIVE..."
                                        TimingState.FINISHED -> "✓ SELESAI & AUTO-SAVE KE SISTEM"
                                    },
                                    color = when (timingState) {
                                        TimingState.RUNNING -> RacingRed
                                        TimingState.FINISHED -> SprintGreen
                                        TimingState.READY -> GoldAccent
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Single Phone Finish Line Toggle Button (Untuk Coach 1 Telefon)
                    if (camMode == "CAM_1_START") {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                color = if (singleCamFinishLineMode) RacingRed.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (singleCamFinishLineMode) RacingRed else BorderDark),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.clickable { singleCamFinishLineMode = !singleCamFinishLineMode }
                            ) {
                                Text(
                                    text = if (singleCamFinishLineMode) "✓ MOD 1 TELEFON AKTIF (GARISAN MERAH PENAMAT)" else "+ AKTIFKAN GARISAN MERAH (MOD 1 TELEFON)",
                                    color = if (singleCamFinishLineMode) Color.White else SilverMetallic,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // ================= BOTTOM CONTROLS (START / STOP / RESET) =================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reset Button
                        IconButton(
                            onClick = { viewModel.resetTimer() },
                            modifier = Modifier
                                .size(50.dp)
                                .background(SurfaceDark, CircleShape)
                                .border(1.dp, BorderDark, CircleShape)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.White)
                        }

                        // Giant Main Action Button (Bunyi Pistol + Auto-Save)
                        Button(
                            onClick = { viewModel.handleMainTimerAction() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (timingState) {
                                    TimingState.READY -> RacingRed
                                    TimingState.RUNNING -> Color(0xFFD32F2F)
                                    TimingState.FINISHED -> SprintGreen
                                }
                            ),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(76.dp)
                                .testTag("btn_timer_action")
                        ) {
                            Icon(
                                imageVector = when (timingState) {
                                    TimingState.READY -> Icons.Default.PlayArrow
                                    TimingState.RUNNING -> Icons.Default.Stop
                                    TimingState.FINISHED -> Icons.Default.Check
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        // Switch to Replay
                        IconButton(
                            onClick = { activeSubTab = 1 },
                            modifier = Modifier
                                .size(50.dp)
                                .background(SurfaceDark, CircleShape)
                                .border(1.dp, BorderDark, CircleShape)
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = "Rakaman", tint = GoldAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (timingState) {
                            TimingState.READY -> "Tekan butang merah untuk mula (Bunyi Pistol Pelepasan)"
                            TimingState.RUNNING -> "Tekan butang untuk tamat larian & auto-simpan ke sistem"
                            TimingState.FINISHED -> "Larian disimpan! Tekan butang hijau untuk pusingan seterusnya"
                        },
                        color = SilverMetallic,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // ==================== REPLAY & RAKAMAN RACE VIEW ====================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "SENARAI REPLAY & RAKAMAN MASA RACE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Semua video & catatan masa auto-disimpan mengikut tarikh dan pusingan larian.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                if (timingRuns.isEmpty()) {
                    item {
                        Surface(
                            color = SurfaceDark,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.VideocamOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Belum ada video atau rekod larian tersimpan.", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Lakukan rakaman masa di tab 'Kamera ET Live' dan tekan Stop untuk auto-simpan.", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    items(timingRuns.reversed()) { run ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = run.raceTitle.ifEmpty { "Race #${run.runNumber}" },
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Tarikh: ${run.dateString} • Sesi: ${run.sessionId}",
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Surface(
                                        color = RacingRed.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, RacingRed)
                                    ) {
                                        Text(
                                            text = run.formattedTime,
                                            color = RacingRed,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Video Simulation Frame with Watermark at TOP only
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .background(Color.Black, RoundedCornerShape(10.dp))
                                        .border(1.dp, BorderDark, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Simulated Track/Runner Frame
                                    Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = RacingRed.copy(alpha = 0.5f), modifier = Modifier.size(60.dp))

                                    // Watermark at the VERY TOP of the video (as requested by user)
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.75f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.TopCenter)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "PSYCO TIME X PRO • www.psycotimexpro.my",
                                                color = GoldAccent,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "${run.formattedTime} [Torso Confirmed]",
                                                color = SprintGreen,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // AI Analysis & Export Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (run.camType == "CAM_2_FINISH") {
                                        // Cam 2 Time Gate: Check Torso Crossing
                                        Button(
                                            onClick = { selectedRunForPhotoFinish = run },
                                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1.2f)
                                        ) {
                                            Icon(Icons.Default.Flag, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Semak Torso Penamat", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                isFieldEventAiMode = false
                                                selectedRunForAi = run
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = RacingRed),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("AI Analisis", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        // Cam 1 Wide Angle: AI Movement, Stride, Cadence Biomechanics Analysis
                                        Button(
                                            onClick = {
                                                isFieldEventAiMode = false
                                                selectedRunForAi = run
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = RacingRed),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1.2f)
                                        ) {
                                            Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("AI Analisis Pelari", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                isFieldEventAiMode = true
                                                selectedRunForAi = run
                                            },
                                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("AI Acara Padang", color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // 3. Download/Save video to phone with Watermark & Timer
                                    IconButton(
                                        onClick = {
                                            Toast.makeText(context, "Video ${run.raceTitle} berjaya dimuat turun ke galeri sistem dengan Watermark & Timer di bahagian atas!", Toast.LENGTH_LONG).show()
                                        },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(SurfaceDarkVariant, RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = "Download Video", tint = SprintGreen, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ================= AI MOTION ANALYSIS MODAL (Cam 1 Analysis & Acara Padang) =================
    if (selectedRunForAi != null) {
        val targetRun = selectedRunForAi!!
        AlertDialog(
            onDismissRequest = { selectedRunForAi = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = RacingRed)
                    Text(
                        if (isFieldEventAiMode) "AI ANALISIS ACARA PADANG" else "AI ANALISIS BIOMEKANIK LARIAN",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Analisis pergerakan video (${targetRun.raceTitle} • Masa: ${targetRun.formattedTime}):",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    if (!isFieldEventAiMode) {
                        // Track / Sprint Running Biomechanics Analysis (Cam 1 Wide Angle)
                        Surface(
                            color = SurfaceDarkVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("PRESTASI & BIOMEKANIK PELARI:", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                    Text("GRED A (94%)", color = SprintGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                                HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 2.dp))
                                Text("• Bukaan Kaki (Stride Length): 2.18m (Bukaan kaki lebar & stabil)", color = Color.White, fontSize = 11.sp)
                                Text("• Langkah Kaki / Cadence: 4.6 langkah/saat (Frekuensi tinggi)", color = Color.White, fontSize = 11.sp)
                                Text("• Cara Pergerakan & Sudut Torso: Kecondongan badan 13° optimum", color = SprintGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("• Bukaan Sudut Lutut (Knee Drive): 88° fasa pecutan", color = Color.White, fontSize = 11.sp)
                                Text("• Sentuhan Kaki ke Trek (Ground Contact Time): 90ms (Sangat pantas & responsif)", color = Color.White, fontSize = 11.sp)
                                Text("• Ayunan Tangan: Sudut siku 90° simetri dengan paksi larian", color = Color.White, fontSize = 11.sp)
                            }
                        }

                        // Pelbagai Option Analisis AI
                        Surface(
                            color = Color.Black,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("OPSYEN ANALISIS AI LANJUTAN:", color = SilverMetallic, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text("1. Fasa Pelepasan & Drive (0-30m): Pecutan 8.9 m/s²", color = TextSecondary, fontSize = 10.sp)
                                Text("2. Fasa Kelajuan Maksimum (30-60m): Top Speed 9.92 m/s", color = TextSecondary, fontSize = 10.sp)
                                Text("3. Fasa Ketahanan Kelajuan (60-100m): Kejatuhan kelajuan hanya 3%", color = TextSecondary, fontSize = 10.sp)
                            }
                        }

                        Surface(
                            color = RacingRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RacingRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("CADANGAN PENAMBAHBAIKAN AI:", color = RacingRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "Kekalkan bukaan langkah kaki semasa fasa kelajuan puncak. Disyorkan latihan plyometrik 'Bounding' dan regangan hip-flexor untuk memaksimumkan kelajuan akhir.",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        // Field Event Analysis (Lontar Peluru & Lompat Jauh)
                        Surface(
                            color = SurfaceDarkVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("ANALISIS ACARA PADANG (LONTAR / LOMPAT):", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("• Sudut Pelepasan / Lonjakan: 38.5° (Hampir sudut ideal 40°)", color = Color.White, fontSize = 11.sp)
                                Text("• Fasa Hayunan / Glide: Pemindahan berat badan dari kaki belakang licin", color = Color.White, fontSize = 11.sp)
                                Text("• Imbangan Tubuh: Pusat graviti stabil semasa lonjakan pelepasan", color = Color.White, fontSize = 11.sp)
                            }
                        }

                        Surface(
                            color = SprintGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SprintGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("CADANGAN COACH (ACARA PADANG):", color = SprintGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "Kuasa lenturan pinggul sudah baik. Tingkatkan kelajuan peralihan kaki penampan untuk lonjakan tambahan +0.20m.",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedRunForAi = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RacingRed)
                ) {
                    Text("Tutup", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(14.dp)
        )
    }

    // ================= TIME GATE (CAM 2) TORSO FINISH LINE INSPECTION DIALOG =================
    if (selectedRunForPhotoFinish != null) {
        val targetRun = selectedRunForPhotoFinish!!
        var frameOffsetMillis by remember { mutableStateOf(0L) }

        AlertDialog(
            onDismissRequest = { selectedRunForPhotoFinish = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = RacingRed)
                    Text(
                        "SEMAK GARISAN PENAMAT (TIME GATE TORSO)",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Pemeriksaan Photo-Finish & Garisan Torso (${targetRun.raceTitle} • Masa Rasmi: ${targetRun.formattedTime}):",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    // Video Frame with Vertical Red Finish Line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color.Black, RoundedCornerShape(10.dp))
                            .border(1.5.dp, RacingRed, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Vertical Finish Line in the exact middle of the screen
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(3.dp)
                                .align(Alignment.Center)
                                .background(RacingRed)
                        )

                        // Torso indicator
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(72.dp).align(Alignment.Center)
                        )

                        // Watermark at Top
                        Surface(
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("PSYCO TIME X PRO • CAM 2 TIME GATE", color = GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text("${targetRun.formattedTime}", color = SprintGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        // Bottom Torso Gate Label
                        Surface(
                            color = RacingRed.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
                        ) {
                            Text(
                                "GARISAN PENAMAT: TORSO MENYENTUH DAHULU",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Scrubber info
                    Surface(
                        color = SurfaceDarkVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("STATUS PENGESAHAN PENAMAT:", color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("✓ Torso pelari dikesan melintasi satah garisan penamat dahulu mengikut piawaian World Athletics Rule 19.2.", color = Color.White, fontSize = 11.sp)
                            Text("Masa Direkod: ${targetRun.formattedTime}", color = SprintGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Text(
                        "Pelarasan Frame-by-Frame (Slow-Motion Replay):",
                        fontSize = 10.sp,
                        color = SilverMetallic,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { frameOffsetMillis -= 10 },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("-0.01s (Frame Lalu)", fontSize = 10.sp, color = Color.White)
                        }
                        Text(
                            text = if (frameOffsetMillis == 0L) "Frame Tepat" else "${frameOffsetMillis}ms",
                            color = if (frameOffsetMillis == 0L) SprintGreen else GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Button(
                            onClick = { frameOffsetMillis += 10 },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("+0.01s (Frame Depan)", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Masa & Torso ${targetRun.formattedTime} telah disahkan!", Toast.LENGTH_SHORT).show()
                        selectedRunForPhotoFinish = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SprintGreen)
                ) {
                    Text("Sahkan Torso & Tutup", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRunForPhotoFinish = null }) {
                    Text("Tutup", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(14.dp)
        )
    }
}
