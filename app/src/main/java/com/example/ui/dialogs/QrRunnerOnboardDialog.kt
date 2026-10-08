package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.PsycotimexproLogoBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrRunnerOnboardDialog(
    coachId: Long,
    coachName: String,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = QR Code, 1 = Manual Form
    val registrationUrl = "https://psycotimexpro.my/training-management/register_runner.php?coach_id=$coachId"

    // Manual Form States
    var name by remember { mutableStateOf("") }
    var icNumber by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var heightCm by remember { mutableStateOf("") }
    var weightKg by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Lelaki") }
    var sportType by remember { mutableStateOf("Balapan") } // Balapan, Padang
    var category by remember { mutableStateOf("100m Pecut") }
    var pb by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf("") }
    var monthlyFee by remember { mutableStateOf("60.00") }
    var feeDueDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    val trackEvents = listOf(
        "100m Pecut", "200m Pecut", "400m Pecut",
        "800m Jarak Sederhana", "1500m Jarak Jauh",
        "110m Lari Berpagar", "100m Lari Berpagar",
        "400m Lari Berpagar", "4x100m Berganti-ganti", "4x400m Berganti-ganti"
    )

    val fieldEvents = listOf(
        "Lompat Jauh (Long Jump)", "Lompat Kijang (Triple Jump)",
        "Lompat Tinggi (High Jump)", "Lompat Bergalah",
        "Lontar Peluru (Shot Put)", "Lempar Cakera (Discus)",
        "Rejam Lembing (Javelin)", "Baling Tukul Besi"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        modifier = Modifier.fillMaxWidth(0.95f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PsycotimexproLogoBadge(size = 32.dp)
                    Column {
                        Text("DAFTAR ATLIT BARU", color = RacingRed, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        Text("Jurulatih: $coachName", color = TextSecondary, fontSize = 11.sp)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Tab Selector (QR Code vs Manual Form)
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.Black, RoundedCornerShape(8.dp)).padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = if (selectedTab == 0) RacingRed else Color.Transparent,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).clickable { selectedTab = 0 }
                    ) {
                        Text(
                            text = "IMBAS QR CODE",
                            color = if (selectedTab == 0) Color.White else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Surface(
                        color = if (selectedTab == 1) RacingRed else Color.Transparent,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).clickable { selectedTab = 1 }
                    ) {
                        Text(
                            text = "DAFTAR MANUAL",
                            color = if (selectedTab == 1) Color.White else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // QR Code View
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ibu bapa atau atlet boleh imbas kod QR ini dengan telefon untuk mengisi butiran penuh:",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(190.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = "QR Code Atlit",
                                    tint = Color.Black,
                                    modifier = Modifier.size(150.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = Color.Black,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = registrationUrl,
                                color = SilverMetallic,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Link Pendaftaran", registrationUrl))
                                    Toast.makeText(context, "Pautan disalin ke papan klip!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Salin Link", color = TextPrimary, fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(registrationUrl))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RacingRed),
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Buka Borang", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Manual Registration Form
                    Column(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 440.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Mandatory Photo Section
                        Surface(
                            color = Color.Black,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (photoUri.isNotBlank()) SprintGreen else RacingRed),
                            modifier = Modifier.fillMaxWidth().clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (photoUri.isNotBlank()) {
                                    AsyncImage(
                                        model = photoUri,
                                        contentDescription = "Gambar Atlit",
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, SprintGreen, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Surface(
                                        color = RacingRed.copy(alpha = 0.2f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(54.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = RacingRed, modifier = Modifier.size(24.dp))
                                        }
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("GAMBAR ATLIT * (MANDATORI)", color = if (photoUri.isNotBlank()) SprintGreen else RacingRed, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                    }
                                    Text(
                                        text = if (photoUri.isNotBlank()) "✓ Gambar atlit berjaya dimuat naik (Sedia disimpan)" else "Tekan sini untuk pilih gambar profil/badan penuh atlit",
                                        color = if (photoUri.isNotBlank()) TextPrimary else TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (photoUri.isNotBlank()) SprintGreen else RacingRed),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(if (photoUri.isNotBlank()) "Tukar" else "Pilih", color = if (photoUri.isNotBlank()) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nama Penuh Atlit *", fontSize = 11.sp) },
                            placeholder = { Text("cth: Muhammad Danial") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                        )

                        OutlinedTextField(
                            value = icNumber,
                            onValueChange = { icNumber = it },
                            label = { Text("No. Kad Pengenalan / Surat Beranak *", fontSize = 11.sp) },
                            placeholder = { Text("090412-12-5567") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = dob,
                                onValueChange = { dob = it },
                                label = { Text("Tarikh Lahir", fontSize = 11.sp) },
                                placeholder = { Text("YYYY-MM-DD") },
                                modifier = Modifier.weight(1.2f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                            )
                            OutlinedTextField(
                                value = age,
                                onValueChange = { age = it },
                                label = { Text("Umur", fontSize = 11.sp) },
                                placeholder = { Text("16") },
                                modifier = Modifier.weight(0.8f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = heightCm,
                                onValueChange = { heightCm = it },
                                label = { Text("Tinggi (cm)", fontSize = 11.sp) },
                                placeholder = { Text("175") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                            )
                            OutlinedTextField(
                                value = weightKg,
                                onValueChange = { weightKg = it },
                                label = { Text("Berat (kg)", fontSize = 11.sp) },
                                placeholder = { Text("65") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                            )
                            // Gender
                            Surface(
                                color = SurfaceVariantDark,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                                modifier = Modifier.weight(1f).height(56.dp).clickable {
                                    gender = if (gender == "Lelaki") "Perempuan" else "Lelaki"
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(gender, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Sukan: Balapan vs Padang
                        Text("Pilihan Sukan & Acara Latihan:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val isBalapan = sportType == "Balapan"
                            Surface(
                                color = if (isBalapan) RacingRed.copy(alpha = 0.2f) else SurfaceVariantDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isBalapan) RacingRed else BorderDark),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).clickable {
                                    sportType = "Balapan"
                                    category = "100m Pecut"
                                }
                            ) {
                                Text(
                                    text = "Balapan (Track)",
                                    color = if (isBalapan) RacingRed else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }

                            val isPadang = sportType == "Padang"
                            Surface(
                                color = if (isPadang) SilverMetallic.copy(alpha = 0.2f) else SurfaceVariantDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isPadang) SilverMetallic else BorderDark),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).clickable {
                                    sportType = "Padang"
                                    category = "Lompat Jauh (Long Jump)"
                                }
                            ) {
                                Text(
                                    text = "Padang (Field)",
                                    color = if (isPadang) SilverMetallic else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }

                        // Event selector dropdown
                        var eventExpanded by remember { mutableStateOf(false) }
                        val eventOptions = if (sportType == "Balapan") trackEvents else fieldEvents
                        Box {
                            Surface(
                                color = Color.Black,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth().clickable { eventExpanded = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(category, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                                }
                            }
                            DropdownMenu(
                                expanded = eventExpanded,
                                onDismissRequest = { eventExpanded = false },
                                modifier = Modifier.background(SurfaceDark)
                            ) {
                                eventOptions.forEach { ev ->
                                    DropdownMenuItem(
                                        text = { Text(ev, color = TextPrimary, fontSize = 12.sp) },
                                        onClick = {
                                            category = ev
                                            eventExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = pb,
                                onValueChange = { pb = it },
                                label = { Text("PB (Saat)", fontSize = 11.sp) },
                                placeholder = { Text("10.50") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Telefon", fontSize = 11.sp) },
                                placeholder = { Text("019-XXXXXXX") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                            )
                        }

                        Button(
                            onClick = {
                                if (name.isBlank()) {
                                    Toast.makeText(context, "Sila masukkan Nama Penuh Atlit!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (photoUri.isBlank()) {
                                    Toast.makeText(context, "Gambar Atlit adalah MANDATORI! Sila tekan butang pilih gambar.", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                val calculatedAge = age.toIntOrNull() ?: 16
                                val calculatedPb = pb.toDoubleOrNull() ?: 10.50
                                val height = heightCm.toDoubleOrNull() ?: 0.0
                                val weight = weightKg.toDoubleOrNull() ?: 0.0
                                val fee = monthlyFee.toDoubleOrNull() ?: 60.0

                                viewModel.registerAthlete(
                                    name = name,
                                    age = calculatedAge,
                                    dob = dob,
                                    phone = phone,
                                    icNumber = icNumber,
                                    heightCm = height,
                                    weightKg = weight,
                                    gender = gender,
                                    sportType = sportType,
                                    category = category,
                                    pb = calculatedPb,
                                    photoUri = photoUri,
                                    monthlyFee = fee,
                                    feeDueDate = feeDueDate,
                                    notes = notes
                                )
                                Toast.makeText(context, "Atlit berjaya didaftarkan!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RacingRed),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Simpan Atlit ke Sistem", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}
