package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AthleteEntity
import com.example.data.local.entity.TimingRunEntity
import com.example.ui.components.PsycotimexproLogoBadge
import com.example.ui.dialogs.AdminPanelDialog
import com.example.ui.dialogs.EditRunnerDialog
import com.example.ui.dialogs.QrRunnerOnboardDialog
import com.example.ui.dialogs.SubscriptionReceiptDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CoachDashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentCoach by viewModel.currentCoach.collectAsState()
    val athletes by viewModel.athletes.collectAsState()
    val totalAthletes by viewModel.totalAthletesCount.collectAsState()
    val bestPb by viewModel.bestPbTime.collectAsState()
    val totalRuns by viewModel.totalRunsCount.collectAsState()
    val timingRuns by viewModel.timingRuns.collectAsState()
    val attendances by viewModel.attendances.collectAsState()
    val fees by viewModel.feePayments.collectAsState()

    var showReportExportedDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showReceiptDialog by remember { mutableStateOf(false) }
    var showAdminDialog by remember { mutableStateOf(false) }
    var selectedRunnerForEdit by remember { mutableStateOf<AthleteEntity?>(null) }

    val balapanCount = athletes.count { it.sportType == "Balapan" }
    val padangCount = athletes.count { it.sportType == "Padang" }

    // Check fee due dates for alert notifications
    val runnersWithDueFees = remember(athletes) {
        athletes.filter { it.feeDueDate.isNotBlank() }
    }

    val expiresAt = currentCoach?.subscriptionExpiresAt ?: (System.currentTimeMillis() + (7L * 24 * 3600 * 1000))
    val expiryDateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(expiresAt))
    val daysLeft = ((expiresAt - System.currentTimeMillis()) / (24 * 3600 * 1000)).coerceAtLeast(0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Dashboard Header with Official Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PsycotimexproLogoBadge(size = 46.dp)
                    Column {
                        Text(
                            text = "PUSAT PRESTASI JURULATIH",
                            color = RacingRed,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        val coachTitle = currentCoach?.let {
                            if (it.nickname.isNotBlank()) it.nickname else it.name
                        } ?: "Coach Roger (Admin)"
                        val clubTitle = currentCoach?.clubName?.ifBlank { "Kelab Olahraga" } ?: "Kelab Olahraga"
                        Text(
                            text = "$coachTitle • $clubTitle",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!currentCoach?.trainingSpecialty.isNullOrBlank()) {
                            Text(
                                text = "Latihan: ${currentCoach?.trainingSpecialty}",
                                color = SilverMetallic,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (currentCoach?.role == "ADMIN") {
                        Button(
                            onClick = { showAdminDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("Admin Panel", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { viewModel.selectTab(AppTab.TIMING_CAM) },
                        colors = ButtonDefaults.buttonColors(containerColor = RacingRed),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mula ET", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Prominent Action Bar: "New Member (QR Code Pelatih)" & "Resit Langganan RM30"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showQrDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = RacingRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.3f).height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Member (QR Code)", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }

                Button(
                    onClick = { showReceiptDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = SilverMetallic, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Resit RM30", color = SilverMetallic, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        // Notification / Alert Banner: Subscription & Runner Fee Due Alerts
        if (daysLeft <= 3) {
            item {
                Surface(
                    color = RacingRed.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RacingRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("PERINGATAN LANGGANAN APLIKASI", color = RacingRed, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                            Text("Baki Masa: $daysLeft hari (Tamat: $expiryDateStr). Kadar: RM30/bulan.", color = TextPrimary, fontSize = 10.sp)
                        }
                        Button(
                            onClick = {
                                val url = "https://wa.me/60195326399?text=Salam%20Admin%20Roger,%20saya%20ingin%20memperbaharui%20langganan%20RM30%20(Coach:%20${currentCoach?.email})"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SprintGreen),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Bayar RM30", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (runnersWithDueFees.isNotEmpty()) {
            item {
                Surface(
                    color = GoldAccent.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("NOTIFIKASI: YURAN PELATIH HAMPIR MATANG", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                            Text("${runnersWithDueFees.size} pelatih mempunyai tarikh matang yuran bulanan dalam masa terdekat.", color = TextPrimary, fontSize = 10.sp)
                        }
                        TextButton(onClick = { viewModel.selectTab(AppTab.FEES) }) {
                            Text("Semak Yuran", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Stats Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Pelatih",
                    value = "$totalAthletes",
                    accentColor = Color.White,
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Balapan",
                    value = "$balapanCount",
                    accentColor = RacingRed,
                    icon = Icons.Default.DirectionsRun,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Padang",
                    value = "$padangCount",
                    accentColor = SilverMetallic,
                    icon = Icons.Default.FitnessCenter,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "PB Terbaik",
                    value = if (bestPb != null && bestPb!! > 0) "${String.format("%.2f", bestPb)}s" else "--:--",
                    accentColor = SprintGreen,
                    icon = Icons.Default.Bolt,
                    modifier = Modifier.weight(1.1f)
                )
            }
        }

        // Quick Navigation Hub (Same flow for app and website)
        item {
            Text("PILIHAN MENU & NAVIGASI CEPAT:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickNavTile(
                    title = "Pelatih",
                    subtitle = "Balapan/Padang",
                    icon = Icons.Default.People,
                    accentColor = RacingRed,
                    modifier = Modifier.weight(1f)
                ) { viewModel.selectTab(AppTab.ATHLETES) }

                QuickNavTile(
                    title = "Sub-Coach",
                    subtitle = "1 Free Slot",
                    icon = Icons.Default.GroupAdd,
                    accentColor = GoldAccent,
                    modifier = Modifier.weight(1f)
                ) { viewModel.selectTab(AppTab.SUB_COACHES) }

                QuickNavTile(
                    title = "Langganan",
                    subtitle = "RM30/Bulan",
                    icon = Icons.Default.CardMembership,
                    accentColor = SprintGreen,
                    modifier = Modifier.weight(1f)
                ) { viewModel.selectTab(AppTab.SUBSCRIPTION) }

                QuickNavTile(
                    title = "Web Portal",
                    subtitle = "psycotimexpro.my",
                    icon = Icons.Default.Language,
                    accentColor = SilverMetallic,
                    modifier = Modifier.weight(1.2f)
                ) { viewModel.selectTab(AppTab.WEB_PORTAL) }
            }
        }

        // Section: Senarai Pelatih Berdaftar (Balapan & Padang)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SENARAI PELATIH (${athletes.size})",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                TextButton(onClick = { showQrDialog = true }) {
                    Text("+ Tambah Pelatih", color = RacingRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (athletes.isEmpty()) {
            item {
                Surface(color = SurfaceDark, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada pelatih. Tekan butang 'New Member (QR Code)' untuk mula mendaftar pelatih.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(athletes) { runner ->
                AthleteListItemCard(
                    athlete = runner,
                    onEdit = { selectedRunnerForEdit = runner },
                    onDelete = { viewModel.deleteAthlete(runner) }
                )
            }
        }

        // Contact Admin Roger Banner
        item {
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Hubungi Admin Roger (WhatsApp Direct):", color = TextSecondary, fontSize = 10.sp)
                        Text("+60195326399 (Bantuan / Error / Penambahan)", color = SprintGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val url = "https://wa.me/60195326399?text=Salam%20Admin%20Roger,%20saya%20memerlukan%20bantuan%20mengenai%20PsycoTimeXPro"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SprintGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("WhatsApp", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialogs
    if (showQrDialog) {
        QrRunnerOnboardDialog(
            coachId = currentCoach?.id ?: 1L,
            coachName = currentCoach?.name ?: "Coach",
            viewModel = viewModel,
            onDismiss = { showQrDialog = false }
        )
    }

    if (showReceiptDialog) {
        SubscriptionReceiptDialog(
            coach = currentCoach,
            onDismiss = { showReceiptDialog = false }
        )
    }

    if (showAdminDialog) {
        AdminPanelDialog(
            viewModel = viewModel,
            onDismiss = { showAdminDialog = false }
        )
    }

    selectedRunnerForEdit?.let { runner ->
        EditRunnerDialog(
            athlete = runner,
            onSave = { updated -> viewModel.updateAthlete(updated) },
            onDelete = { del -> viewModel.deleteAthlete(del) },
            onDismiss = { selectedRunnerForEdit = null }
        )
    }
}

@Composable
fun QuickNavTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextMuted, fontSize = 8.sp, maxLines = 1)
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, maxLines = 1)
            Text(text = title, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun AthleteListItemCard(
    athlete: AthleteEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth().clickable { onEdit() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(athlete.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    if (athlete.sportType == "Balapan") {
                        Surface(color = RacingRed.copy(alpha = 0.15f), border = androidx.compose.foundation.BorderStroke(0.5.dp, RacingRed), shape = RoundedCornerShape(4.dp)) {
                            Text("Balapan", color = RacingRed, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    } else {
                        Surface(color = SilverMetallic.copy(alpha = 0.15f), border = androidx.compose.foundation.BorderStroke(0.5.dp, SilverMetallic), shape = RoundedCornerShape(4.dp)) {
                            Text("Padang", color = SilverMetallic, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${athlete.category} • IC: ${athlete.icNumber.ifEmpty { "-" }} • ${athlete.age} thn (${athlete.gender})",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                if (athlete.heightCm > 0 || athlete.weightKg > 0) {
                    Text("Tinggi: ${athlete.heightCm}cm | Berat: ${athlete.weightKg}kg", color = TextMuted, fontSize = 10.sp)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (athlete.pbSeconds > 0) {
                    Surface(
                        color = SprintGreen.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SprintGreen.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${athlete.pbSeconds}s",
                            color = SprintGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SilverMetallic, modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Padam", tint = RacingRed, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}
