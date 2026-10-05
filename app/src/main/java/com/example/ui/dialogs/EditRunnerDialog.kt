package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AthleteEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRunnerDialog(
    athlete: AthleteEntity,
    onSave: (AthleteEntity) -> Unit,
    onDelete: (AthleteEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(athlete.name) }
    var icNumber by remember { mutableStateOf(athlete.icNumber) }
    var dob by remember { mutableStateOf(athlete.dob) }
    var age by remember { mutableStateOf(athlete.age.toString()) }
    var phone by remember { mutableStateOf(athlete.phone) }
    var heightCm by remember { mutableStateOf(if (athlete.heightCm > 0) athlete.heightCm.toString() else "") }
    var weightKg by remember { mutableStateOf(if (athlete.weightKg > 0) athlete.weightKg.toString() else "") }
    var gender by remember { mutableStateOf(athlete.gender) }
    var sportType by remember { mutableStateOf(athlete.sportType) }
    var category by remember { mutableStateOf(athlete.category) }
    var pb by remember { mutableStateOf(athlete.pbSeconds.toString()) }
    var monthlyFee by remember { mutableStateOf(athlete.monthlyFee.toString()) }
    var feeDueDate by remember { mutableStateOf(athlete.feeDueDate) }
    var notes by remember { mutableStateOf(athlete.notes) }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("KEMAS KINI MAKLUMAT PELATIH", color = RacingRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Penuh Pelatih") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                )

                OutlinedTextField(
                    value = icNumber,
                    onValueChange = { icNumber = it },
                    label = { Text("No. Kad Pengenalan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = { Text("Tarikh Lahir") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                    )
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Umur") },
                        modifier = Modifier.weight(0.7f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = heightCm,
                        onValueChange = { heightCm = it },
                        label = { Text("Tinggi (cm)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                    )
                    OutlinedTextField(
                        value = weightKg,
                        onValueChange = { weightKg = it },
                        label = { Text("Berat (kg)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Sukan Type
                    Surface(
                        color = if (sportType == "Balapan") RacingRed.copy(alpha = 0.2f) else SurfaceVariantDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (sportType == "Balapan") RacingRed else BorderDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(46.dp).clickable { sportType = "Balapan" }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Balapan", color = if (sportType == "Balapan") RacingRed else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(
                        color = if (sportType == "Padang") SilverMetallic.copy(alpha = 0.2f) else SurfaceVariantDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (sportType == "Padang") SilverMetallic else BorderDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(46.dp).clickable { sportType = "Padang" }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Padang", color = if (sportType == "Padang") SilverMetallic else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Acara Sukan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pb,
                        onValueChange = { pb = it },
                        label = { Text("PB (Saat)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                    )
                    OutlinedTextField(
                        value = monthlyFee,
                        onValueChange = { monthlyFee = it },
                        label = { Text("Yuran (RM)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                    )
                }

                OutlinedTextField(
                    value = feeDueDate,
                    onValueChange = { feeDueDate = it },
                    label = { Text("Tarikh Matang Yuran (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Nota Jurulatih") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = RacingRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Padam", color = RacingRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val updated = athlete.copy(
                            name = name.trim(),
                            icNumber = icNumber.trim(),
                            dob = dob.trim(),
                            age = age.toIntOrNull() ?: athlete.age,
                            phone = phone.trim(),
                            heightCm = heightCm.toDoubleOrNull() ?: athlete.heightCm,
                            weightKg = weightKg.toDoubleOrNull() ?: athlete.weightKg,
                            gender = gender,
                            sportType = sportType,
                            category = category.trim(),
                            pbSeconds = pb.toDoubleOrNull() ?: athlete.pbSeconds,
                            monthlyFee = monthlyFee.toDoubleOrNull() ?: athlete.monthlyFee,
                            feeDueDate = feeDueDate.trim(),
                            notes = notes.trim()
                        )
                        onSave(updated)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RacingRed)
                ) {
                    Text("Simpan Perubahan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = SurfaceDark,
            title = { Text("Sahkan Padam Pelatih", color = RacingRed, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Adakah anda pasti ingin memadam ${athlete.name}? Pelatih ini akan dikeluarkan daripada senarai latihan dan rekod.",
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(athlete)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RacingRed)
                ) {
                    Text("Ya, Padam Pelatih", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }
}
