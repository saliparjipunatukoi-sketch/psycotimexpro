package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.local.entity.AttendanceEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AttendanceScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val athletes by viewModel.athletes.collectAsState()
    val attendances by viewModel.attendances.collectAsState()

    var selectedDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var sessionTitle by remember { mutableStateOf("Latihan Pecut Pagi") }
    var showSuccessToast by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "PENGURUSAN KEHADIRAN",
                color = NeonCyan,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Semakan dan rekod kehadiran harian sesi latihan atlet",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // Session Selector Box
        item {
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
                        Text("SESI HARI INI", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(selectedDate, color = NeonCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Latihan Pecut Pagi", "Latihan Petang", "Ujian Masa / Time Trial").forEach { title ->
                            val isSel = sessionTitle == title
                            Surface(
                                color = if (isSel) NeonCyan.copy(alpha = 0.2f) else SurfaceVariantDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) NeonCyan else BorderDark),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { sessionTitle = title }
                            ) {
                                Text(
                                    text = title,
                                    color = if (isSel) NeonCyan else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Senarai Atlet untuk Ditanda
        item {
            Text(
                text = "TANDA KEHADIRAN ATLET",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        if (athletes.isEmpty()) {
            item {
                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Sila daftarkan atlet terlebih dahulu.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(athletes) { athlete ->
                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = athlete.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = athlete.category, color = TextSecondary, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status Options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AttendanceStatusButton(label = "Hadir", color = SprintGreen, modifier = Modifier.weight(1f)) {
                                viewModel.markAttendance(athlete, "HADIR", sessionTitle)
                                showSuccessToast = true
                            }
                            AttendanceStatusButton(label = "Lewat", color = GoldAccent, modifier = Modifier.weight(1f)) {
                                viewModel.markAttendance(athlete, "LEWAT", sessionTitle)
                                showSuccessToast = true
                            }
                            AttendanceStatusButton(label = "Tidak Hadir", color = RacingRed, modifier = Modifier.weight(1.2f)) {
                                viewModel.markAttendance(athlete, "TIDAK_HADIR", sessionTitle)
                                showSuccessToast = true
                            }
                            AttendanceStatusButton(label = "Sakit", color = Color(0xFFB388FF), modifier = Modifier.weight(1f)) {
                                viewModel.markAttendance(athlete, "SAKIT", sessionTitle)
                                showSuccessToast = true
                            }
                        }
                    }
                }
            }
        }

        // Section: Riwayat Kehadiran Terkini
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "LOG KEHADIRAN TERKINI (${attendances.size})",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        if (attendances.isEmpty()) {
            item {
                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada log kehadiran yang dicatat hari ini.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(attendances.take(8)) { record ->
                AttendanceRecordRow(
                    record = record,
                    onDelete = { viewModel.deleteAttendance(record) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSuccessToast) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1500)
            showSuccessToast = false
        }
        Snackbar(
            containerColor = SprintGreen,
            contentColor = Color.Black,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Kehadiran Berjaya Ditanda!", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AttendanceStatusButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}

@Composable
fun AttendanceRecordRow(
    record: AttendanceEntity,
    onDelete: () -> Unit
) {
    Surface(
        color = SurfaceDark,
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
                Text(text = record.athleteName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = "${record.dateString} • ${record.sessionTitle}", color = TextSecondary, fontSize = 10.sp)
            }

            val (badgeColor, label) = when (record.status) {
                "HADIR" -> SprintGreen to "HADIR"
                "LEWAT" -> GoldAccent to "LEWAT"
                "SAKIT" -> Color(0xFFB388FF) to "SAKIT"
                else -> RacingRed to "TIDAK HADIR"
            }

            Surface(
                color = badgeColor.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = label,
                    color = badgeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp).padding(start = 4.dp)) {
                Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
            }
        }
    }
}
