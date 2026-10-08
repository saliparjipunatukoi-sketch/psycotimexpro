package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AthleteEntity
import com.example.ui.components.PsycotimexproLogoBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun RankingScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val athletes by viewModel.athletes.collectAsState()
    val currentCoach by viewModel.currentCoach.collectAsState()
    val context = LocalContext.current

    var selectedSportType by remember { mutableStateOf("SEMUA") } // SEMUA, Balapan, Padang
    var selectedCategoryFilter by remember { mutableStateOf("SEMUA") }
    var athleteToEditPb by remember { mutableStateOf<AthleteEntity?>(null) }

    // Filter and Sort Athletes
    val filteredAthletes = remember(athletes, selectedSportType, selectedCategoryFilter) {
        athletes.filter { athlete ->
            val matchSport = when (selectedSportType) {
                "BALAPAN" -> athlete.sportType.equals("Balapan", ignoreCase = true)
                "PADANG" -> athlete.sportType.equals("Padang", ignoreCase = true)
                else -> true
            }
            val matchCategory = if (selectedCategoryFilter == "SEMUA") true else athlete.category.contains(selectedCategoryFilter, ignoreCase = true)
            matchSport && matchCategory
        }.sortedWith { a, b ->
            if (a.sportType.equals("Padang", ignoreCase = true)) {
                // For field events: higher distance is better
                b.distanceOrScore.compareTo(a.distanceOrScore)
            } else {
                // For track events: lower time is better (exclude 0.0)
                val timeA = if (a.pbSeconds > 0) a.pbSeconds else 999.0
                val timeB = if (b.pbSeconds > 0) b.pbSeconds else 999.0
                timeA.compareTo(timeB)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Header with Official Logo & Web Sync Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PsycotimexproLogoBadge(size = 46.dp)
                    Column {
                        Text(
                            text = "CARTA RANKING ATLIT",
                            color = RacingRed,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Prestasi PB Terkini vs PB Lama (${filteredAthletes.size} Atlit)",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = {
                        val url = "https://www.psycotimexpro.my/training-management/ranking.php"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                ) {
                    Icon(Icons.Default.Public, contentDescription = "Buka Web Ranking", tint = SprintGreen)
                }
            }
        }

        // Sport Type Tabs (SEMUA, BALAPAN, PADANG)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black, RoundedCornerShape(8.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("SEMUA", "BALAPAN", "PADANG").forEach { type ->
                    val isSelected = selectedSportType == type
                    Surface(
                        color = if (isSelected) RacingRed else Color.Transparent,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedSportType = type
                                selectedCategoryFilter = "SEMUA"
                            }
                    ) {
                        Text(
                            text = type,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // Quick Category Filter Chips
        item {
            val categories = when (selectedSportType) {
                "BALAPAN" -> listOf("SEMUA", "100m", "200m", "400m", "800m")
                "PADANG" -> listOf("SEMUA", "Lompat Jauh", "Lompat Tinggi", "Lontar Peluru")
                else -> listOf("SEMUA", "100m", "200m", "Lompat Jauh")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    Surface(
                        color = if (isSelected) SurfaceDarkVariant else SurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GoldAccent else BorderDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable { selectedCategoryFilter = cat }
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) GoldAccent else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        if (filteredAthletes.isEmpty()) {
            item {
                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tiada pelatih dijumpai untuk kategori ini", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        } else {
            itemsIndexed(filteredAthletes) { index, athlete ->
                val rank = index + 1
                val isTrack = athlete.sportType.equals("Balapan", ignoreCase = true)
                val currentScoreStr = if (isTrack) String.format("%.2fs", athlete.pbSeconds) else String.format("%.2fm", athlete.distanceOrScore)
                val oldScoreStr = if (isTrack) {
                    if (athlete.previousPbSeconds > 0) String.format("%.2fs", athlete.previousPbSeconds) else "-"
                } else {
                    if (athlete.previousDistanceOrScore > 0) String.format("%.2fm", athlete.previousDistanceOrScore) else "-"
                }

                // Calculate Improvement
                val hasPrevious = if (isTrack) athlete.previousPbSeconds > 0 else athlete.previousDistanceOrScore > 0
                val delta = if (isTrack) {
                    athlete.previousPbSeconds - athlete.pbSeconds // positive means faster!
                } else {
                    athlete.distanceOrScore - athlete.previousDistanceOrScore // positive means further!
                }

                RankingItemCard(
                    rank = rank,
                    athlete = athlete,
                    isTrack = isTrack,
                    currentScore = currentScoreStr,
                    oldScore = oldScoreStr,
                    hasPrevious = hasPrevious,
                    delta = delta,
                    onEditPb = { athleteToEditPb = athlete }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Dialog Kemaskini PB Atlet
    if (athleteToEditPb != null) {
        val target = athleteToEditPb!!
        val isTrack = target.sportType.equals("Balapan", ignoreCase = true)
        var inputScore by remember { mutableStateOf(if (isTrack) target.pbSeconds.toString() else target.distanceOrScore.toString()) }

        AlertDialog(
            onDismissRequest = { athleteToEditPb = null },
            title = {
                Text(
                    "KEMAS KINI PB: ${target.name}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Rekod PB semasa akan dialihkan ke 'PB Lama' untuk perbandingan graf peningkatan prestasi.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = inputScore,
                        onValueChange = { inputScore = it },
                        label = { Text(if (isTrack) "PB Baharu (Saat, cth: 10.35)" else "Jarak Baharu (Meter, cth: 7.20)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = inputScore.toDoubleOrNull()
                        if (num != null && num > 0) {
                            viewModel.updateAthletePb(target, num, isFieldEvent = !isTrack)
                            athleteToEditPb = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RacingRed)
                ) {
                    Text("Simpan PB Baharu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { athleteToEditPb = null }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
fun RankingItemCard(
    rank: Int,
    athlete: AthleteEntity,
    isTrack: Boolean,
    currentScore: String,
    oldScore: String,
    hasPrevious: Boolean,
    delta: Double,
    onEditPb: () -> Unit
) {
    val rankBadgeColor = when (rank) {
        1 -> GoldAccent // Emas
        2 -> SilverMetallic // Perak
        3 -> BronzeAccent // Gangsa
        else -> BorderDark
    }

    val medalText = when (rank) {
        1 -> "🥇 #1"
        2 -> "🥈 #2"
        3 -> "🥉 #3"
        else -> "#$rank"
    }

    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (rank <= 3) rankBadgeColor.copy(alpha = 0.6f) else BorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = rankBadgeColor.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, rankBadgeColor),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = medalText,
                            color = if (rank <= 3) rankBadgeColor else Color.White,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (athlete.photoUri.isNotBlank()) {
                        coil.compose.AsyncImage(
                            model = athlete.photoUri,
                            contentDescription = athlete.name,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .border(1.dp, rankBadgeColor, CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }

                    Column {
                        Text(
                            text = athlete.name,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${athlete.category} • ${athlete.gender}, ${athlete.age} thn",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Surface(
                    color = if (isTrack) RacingRed.copy(alpha = 0.15f) else SprintGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = athlete.sportType.uppercase(),
                        color = if (isTrack) RacingRed else SprintGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Score Display: Current PB vs Previous PB
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDarkVariant, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("PB TERKINI", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = currentScore,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column {
                    Text("PB LAMA", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = oldScore,
                        color = SilverMetallic,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("PENINGKATAN", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    if (hasPrevious) {
                        val isImproved = delta > 0
                        val deltaSign = if (isTrack) (if (delta > 0) String.format("-%.2fs", delta) else String.format("+%.2fs", -delta))
                        else (if (delta > 0) String.format("+%.2fm", delta) else String.format("%.2fm", delta))

                        Text(
                            text = if (isImproved) "🚀 $deltaSign" else "⚠️ $deltaSign",
                            color = if (isImproved) SprintGreen else RacingRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Text("Catatan Awal", color = TextMuted, fontSize = 10.sp)
                    }
                }

                IconButton(
                    onClick = onEditPb,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Kemaskini PB", tint = GoldAccent, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
