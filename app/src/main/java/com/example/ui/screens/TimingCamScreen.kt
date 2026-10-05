package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import coil.compose.AsyncImage
import com.example.data.local.entity.PhotoProofEntity
import com.example.ui.components.CameraStreamView
import com.example.ui.theme.*
import com.example.ui.viewmodel.LaneConfig
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.TimingState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimingCamScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessionId by viewModel.sessionId.collectAsState()
    val runIndex by viewModel.currentRunIndex.collectAsState()
    val timingState by viewModel.timingState.collectAsState()
    val elapsedMillis by viewModel.elapsedMillis.collectAsState()
    val laneConfigs by viewModel.laneConfigs.collectAsState()
    val camMode by viewModel.selectedCamMode.collectAsState()
    val athletes by viewModel.athletes.collectAsState()
    val photoProofs by viewModel.photoProofs.collectAsState()

    var isFrontCam by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showGalleryDialog by remember { mutableStateOf(false) }
    var showSnapshotSavedToast by remember { mutableStateOf(false) }
    var lastSavedMessage by remember { mutableStateOf("") }

    // Check Camera Permission
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
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

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        // Camera View with dynamic overlay
        CameraStreamView(
            modifier = Modifier.fillMaxSize(),
            isFrontFacing = isFrontCam,
            hasCameraPermission = hasCameraPermission
        ) {
            // Distinctive Red Line for Finish Line (CAM 2)
            if (camMode == "CAM_2_FINISH") {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(3.dp)
                        .align(Alignment.Center)
                        .background(RacingRed)
                )
            }

            // Watermark overlay with official logo
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                com.example.ui.components.PsycotimexproLogoBadge(size = 70.dp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ONLY FOR TRAINING PURPOSE",
                    color = Color.White.copy(alpha = 0.28f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "www.psycotimexpro.my",
                    color = NeonCyan.copy(alpha = 0.35f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Top Control Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.92f), Color.Transparent)
                    )
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Role / Cam Mode Badge
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    com.example.ui.components.PsycotimexproLogoBadge(size = 32.dp)
                    Surface(
                        color = if (camMode == "CAM_1_START") NeonCyan.copy(alpha = 0.15f) else RacingRed.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (camMode == "CAM_1_START") NeonCyan else RacingRed
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (camMode == "CAM_1_START") Icons.Default.Videocam else Icons.Default.Flag,
                                contentDescription = null,
                                tint = if (camMode == "CAM_1_START") NeonCyan else RacingRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (camMode == "CAM_1_START") "START CAM" else "FINISH LINE CAM",
                                color = if (camMode == "CAM_1_START") NeonCyan else RacingRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Mode Switcher Button
                    IconButton(
                        onClick = {
                            viewModel.setCamMode(if (camMode == "CAM_1_START") "CAM_2_FINISH" else "CAM_1_START")
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .background(SurfaceDark, CircleShape)
                            .testTag("cam_mode_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Tukar Mod Kamera",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // High Precision Digital Stopwatch
                Surface(
                    color = Color.Black.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                ) {
                    Text(
                        text = MainViewModel.formatMillisToStopwatch(elapsedMillis),
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag("clock_display"),
                        color = when (timingState) {
                            TimingState.RUNNING -> RacingRed
                            TimingState.FINISHED -> SprintGreen
                            TimingState.READY -> NeonCyan
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Galeri Bukti Button (Tersimpan di Telefon)
                    IconButton(
                        onClick = { showGalleryDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(SurfaceDark, CircleShape)
                            .border(1.dp, NeonCyan, CircleShape)
                            .testTag("gallery_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Galeri Bukti Telefon",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // QR Code Button for Cam 2 pairing
                    IconButton(
                        onClick = { showQrDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(NeonCyan, CircleShape)
                            .testTag("qr_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Imbas QR Cam 2",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Run Badge (e.g. LARIAN #1 (SEDIA))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = SurfaceDark.copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "LARIAN #$runIndex (${
                            when (timingState) {
                                TimingState.READY -> "SEDIA"
                                TimingState.RUNNING -> "SEDANG LARI"
                                TimingState.FINISHED -> "SELESAI"
                            }
                        })",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Lane Allocation Bar (L1, L2, L3...)
            Surface(
                color = SurfaceDark.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Konfigurasi Lorong",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Lane Count selector (1 to 4 lanes)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(1, 2, 3, 4).forEach { count ->
                                val selected = laneConfigs.size == count
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (selected) NeonCyan else Color.Black)
                                        .clickable { viewModel.setLaneCount(count) }
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                        .testTag("lane_count_select_$count")
                                ) {
                                    Text(
                                        text = "$count P",
                                        color = if (selected) Color.Black else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Lanes List
                    laneConfigs.forEach { lane ->
                        LaneRowItem(
                            lane = lane,
                            athletesList = athletes.map { it.name },
                            onNameSelected = { viewModel.updateLaneRunner(lane.lane, it) }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        // Bottom Controls Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))
                    )
                )
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Photo Finish scrub controls if in CAM_2_FINISH mode
            if (camMode == "CAM_2_FINISH") {
                Surface(
                    color = SurfaceDark.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { /* Step backward */ },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("-1 Frame", fontSize = 11.sp, color = TextPrimary)
                        }

                        Button(
                            onClick = {
                                val formattedTime = MainViewModel.formatMillisToStopwatch(elapsedMillis)
                                viewModel.savePhotoFinishEvidence(
                                    baseBitmap = null,
                                    runNumber = runIndex,
                                    timeFormatted = formattedTime
                                ) { path ->
                                    lastSavedMessage = "Foto Photo Finish ($formattedTime) berjaya disimpan ke telefon!"
                                    showSnapshotSavedToast = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SprintGreen),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simpan Bukti", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { /* Step forward */ },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("+1 Frame", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            }

            // Main Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flip Camera
                IconButton(
                    onClick = { isFrontCam = !isFrontCam },
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Pusing Kamera",
                        tint = Color.White
                    )
                }

                // Big Gun Start Action Button
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.White, CircleShape)
                        .background(
                            when (timingState) {
                                TimingState.READY -> Brush.linearGradient(listOf(NeonCyan, Color(0xFF0072FF)))
                                TimingState.RUNNING -> Brush.linearGradient(listOf(RacingRed, Color(0xFFB30006)))
                                TimingState.FINISHED -> Brush.linearGradient(listOf(SprintGreen, SprintGreenDark))
                            }
                        )
                        .clickable { viewModel.handleMainTimerAction() }
                        .testTag("main_action_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (timingState) {
                                TimingState.READY -> Icons.Default.VolumeUp
                                TimingState.RUNNING -> Icons.Default.Stop
                                TimingState.FINISHED -> Icons.Default.SkipNext
                            },
                            contentDescription = null,
                            tint = if (timingState == TimingState.FINISHED) Color.Black else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (timingState) {
                                TimingState.READY -> "FIRE!"
                                TimingState.RUNNING -> "STOP"
                                TimingState.FINISHED -> "SETERUSNYA"
                            },
                            color = if (timingState == TimingState.FINISHED) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // New Session
                IconButton(
                    onClick = { viewModel.startNewSession() },
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sesi Baru",
                        tint = Color.White
                    )
                }
            }
        }

        // QR Code Modal for Finish Cam connection
        if (showQrDialog) {
            AlertDialog(
                onDismissRequest = { showQrDialog = false },
                containerColor = SurfaceDark,
                title = {
                    Text("Sambung Cam 2 (Finish)", color = NeonCyan, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Buka kamera telefon kedua untuk imbas pautan Photo Finish Cam bagi Sesi: $sessionId",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(180.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode2,
                                        contentDescription = null,
                                        modifier = Modifier.size(130.dp),
                                        tint = Color.Black
                                    )
                                    Text(
                                        text = "https://psycotimexpro.my/et_finish.php?sid=$sessionId",
                                        fontSize = 8.sp,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showQrDialog = false }) {
                        Text("Tutup", color = NeonCyan)
                    }
                }
            )
        }

        // Galeri Bukti Dialog
        if (showGalleryDialog) {
            AlertDialog(
                onDismissRequest = { showGalleryDialog = false },
                containerColor = SurfaceDark,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("BUKTI FOTO FINISH TERSIMPAN (${photoProofs.size})", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                },
                text = {
                    if (photoProofs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("Belum ada bukti photo finish tersimpan di telefon. Beralih ke mod FINISH LINE CAM dan tekan butang 'Simpan Bukti' untuk merakam.", color = TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(photoProofs) { proof ->
                                Surface(
                                    color = Color.Black,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        val file = File(proof.filePath)
                                        if (file.exists()) {
                                            AsyncImage(
                                                model = file,
                                                contentDescription = "Bukti Photo Finish",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(130.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Sesi: ${proof.sessionId} (Larian #${proof.runNumber})", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text("Masa: ${proof.formattedTime} • ${proof.dateString}", color = SprintGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                Text("Lokasi Storan: ${proof.filePath}", color = TextMuted, fontSize = 8.sp, maxLines = 1)
                                            }

                                            IconButton(
                                                onClick = { viewModel.deletePhotoProof(proof) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Padam", tint = RacingRed, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showGalleryDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("Tutup", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Toast feedback
        if (showSnapshotSavedToast) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2500)
                showSnapshotSavedToast = false
            }
            Snackbar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 90.dp, start = 16.dp, end = 16.dp),
                containerColor = SprintGreen,
                contentColor = Color.Black
            ) {
                Text(
                    text = if (lastSavedMessage.isNotEmpty()) lastSavedMessage else "Gambar Photo Finish Berjaya Disimpan ke Storan Telefon!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun LaneRowItem(
    lane: LaneConfig,
    athletesList: List<String>,
    onNameSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isCustomInput by remember { mutableStateOf(false) }
    var customText by remember { mutableStateOf(lane.athleteName) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Lane Tag
        Surface(
            color = Color(0xFF0072FF),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.width(42.dp)
        ) {
            Text(
                text = "L${lane.lane}",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (isCustomInput) {
            OutlinedTextField(
                value = customText,
                onValueChange = {
                    customText = it
                    onNameSelected(it)
                },
                modifier = Modifier.weight(1f).height(46.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = TextPrimary),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = BorderDark
                )
            )
            IconButton(
                onClick = { isCustomInput = false },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = SprintGreen, modifier = Modifier.size(16.dp))
            }
        } else {
            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    color = Color.Black,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lane.athleteName,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(SurfaceDark)
                ) {
                    athletesList.forEach { name ->
                        DropdownMenuItem(
                            text = { Text(name, color = TextPrimary, fontSize = 12.sp) },
                            onClick = {
                                onNameSelected(name)
                                expanded = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("+ Taip Manual...", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        onClick = {
                            isCustomInput = true
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
