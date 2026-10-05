package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AthleteEntity
import com.example.ui.components.PsycotimexproLogoBadge
import com.example.ui.dialogs.EditRunnerDialog
import com.example.ui.dialogs.QrRunnerOnboardDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AthletesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentCoach by viewModel.currentCoach.collectAsState()
    val athletes by viewModel.athletes.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSportFilter by remember { mutableStateOf("Semua") } // "Semua", "Balapan", "Padang"
    var showQrDialog by remember { mutableStateOf(false) }
    var athleteToEdit by remember { mutableStateOf<AthleteEntity?>(null) }
    var athleteToDelete by remember { mutableStateOf<AthleteEntity?>(null) }

    val balapanCount = athletes.count { it.sportType == "Balapan" }
    val padangCount = athletes.count { it.sportType == "Padang" }

    val filteredAthletes = remember(athletes, searchQuery, selectedSportFilter) {
        athletes.filter { ath ->
            val matchesSport = when (selectedSportFilter) {
                "Balapan" -> ath.sportType == "Balapan"
                "Padang" -> ath.sportType == "Padang"
                else -> true
            }
            val matchesQuery = if (searchQuery.isBlank()) true else {
                ath.name.contains(searchQuery, ignoreCase = true) ||
                        ath.icNumber.contains(searchQuery, ignoreCase = true) ||
                        ath.category.contains(searchQuery, ignoreCase = true)
            }
            matchesSport && matchesQuery
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PsycotimexproLogoBadge(size = 38.dp)
                    Column {
                        Text(
                            text = "DIREKTORI & PENDAFTARAN PELATIH",
                            color = RacingRed,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Jurulatih: ${currentCoach?.name ?: "Pusat Latihan"}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = { showQrDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = RacingRed),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Member", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Sport Category Filter Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Triple("Semua", "Semua (${athletes.size})", Color.White),
                    Triple("Balapan", "Balapan ($balapanCount)", RacingRed),
                    Triple("Padang", "Padang ($padangCount)", SilverMetallic)
                ).forEach { (sport, label, color) ->
                    val isSelected = selectedSportFilter == sport
                    Surface(
                        color = if (isSelected) color.copy(alpha = 0.2f) else SurfaceDark,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) color else BorderDark),
                        modifier = Modifier.weight(1f).clickable { selectedSportFilter = sport }
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) color else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama pelatih, IC, atau acara sukan...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Kosongkan", tint = TextMuted)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RacingRed,
                    unfocusedBorderColor = BorderDark
                )
            )
        }

        // Header Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SENARAI PELATIH AKTIF (${filteredAthletes.size})",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Tekan kad untuk ubah",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }

        if (filteredAthletes.isEmpty()) {
            item {
                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(28.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (athletes.isEmpty()) "Belum ada pelatih didaftarkan.\nTekan butang '+ New Member' di atas untuk memaparkan QR code pendaftaran."
                            else "Tiada pelatih ditemui dalam carian ini.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredAthletes) { athlete ->
                FullAthleteCard(
                    athlete = athlete,
                    onEdit = { athleteToEdit = athlete },
                    onDelete = { athleteToDelete = athlete }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // QR Code Onboarding Dialog
    if (showQrDialog) {
        QrRunnerOnboardDialog(
            coachId = currentCoach?.id ?: 1L,
            coachName = currentCoach?.name ?: "Coach",
            viewModel = viewModel,
            onDismiss = { showQrDialog = false }
        )
    }

    // Edit Runner Dialog
    athleteToEdit?.let { athlete ->
        EditRunnerDialog(
            athlete = athlete,
            onSave = { updated ->
                viewModel.updateAthlete(updated)
                Toast.makeText(context, "Maklumat pelatih dikemas kini!", Toast.LENGTH_SHORT).show()
                athleteToEdit = null
            },
            onDelete = { toDel ->
                viewModel.deleteAthlete(toDel)
                Toast.makeText(context, "Pelatih berjaya dipadam.", Toast.LENGTH_SHORT).show()
                athleteToEdit = null
            },
            onDismiss = { athleteToEdit = null }
        )
    }

    // Delete Confirmation Dialog
    athleteToDelete?.let { delAth ->
        AlertDialog(
            onDismissRequest = { athleteToDelete = null },
            containerColor = SurfaceDark,
            title = { Text("Padam Pelatih", color = RacingRed, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Adakah anda pasti ingin memadam ${delAth.name} (${delAth.category})? Data pelatih akan dikeluarkan daripada sistem.",
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAthlete(delAth)
                        Toast.makeText(context, "Pelatih berjaya dipadam.", Toast.LENGTH_SHORT).show()
                        athleteToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RacingRed)
                ) {
                    Text("Ya, Padam", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { athleteToDelete = null }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun FullAthleteCard(
    athlete: AthleteEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isPadang = athlete.sportType == "Padang"

    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth().clickable { onEdit() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = athlete.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // Sport Badge
                    Surface(
                        color = if (isPadang) SilverMetallic.copy(alpha = 0.2f) else RacingRed.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isPadang) SilverMetallic else RacingRed),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = athlete.sportType,
                            color = if (isPadang) SilverMetallic else RacingRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    color = SprintGreen.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SprintGreen.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "PB: ${String.format("%.2f", athlete.pbSeconds)}s",
                        color = SprintGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Acara: ${athlete.category} • No. IC: ${athlete.icNumber.ifEmpty { "-" }}",
                color = SilverLight,
                fontSize = 11.sp
            )

            Text(
                text = "Umur: ${athlete.age} Thn • Jantina: ${athlete.gender} • Lahir: ${athlete.dob.ifEmpty { "-" }}",
                color = TextSecondary,
                fontSize = 10.sp
            )

            if (athlete.heightCm > 0 || athlete.weightKg > 0) {
                Text(
                    text = "Fizikal: Tinggi ${athlete.heightCm} cm • Berat ${athlete.weightKg} kg",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            if (athlete.phone.isNotBlank()) {
                Text(
                    text = "Telefon: ${athlete.phone}",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            if (athlete.feeDueDate.isNotBlank()) {
                Text(
                    text = "Tarikh Matang Yuran: ${athlete.feeDueDate} (RM ${String.format("%.2f", athlete.monthlyFee)}/bln)",
                    color = GoldAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = SilverMetallic, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ubah Detail", color = SilverMetallic, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(onClick = onDelete, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = RacingRed, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Padam", color = RacingRed, fontSize = 11.sp)
                }
            }
        }
    }
}
