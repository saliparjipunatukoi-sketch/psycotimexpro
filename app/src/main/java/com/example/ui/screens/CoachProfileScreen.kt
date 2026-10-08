package com.example.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.dialogs.InboxDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun CoachProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentCoach by viewModel.currentCoach.collectAsState()
    val unreadInboxCount by viewModel.unreadInboxCount.collectAsState()
    val context = LocalContext.current

    var showInboxDialog by remember { mutableStateOf(false) }

    // Form States
    var nickname by remember(currentCoach) { mutableStateOf(currentCoach?.nickname ?: "") }
    var clubName by remember(currentCoach) { mutableStateOf(currentCoach?.clubName ?: "") }
    var clubAddress by remember(currentCoach) { mutableStateOf(currentCoach?.clubAddress ?: "") }
    var trainingSpecialty by remember(currentCoach) { mutableStateOf(currentCoach?.trainingSpecialty ?: "") }
    var achievements by remember(currentCoach) { mutableStateOf(currentCoach?.achievements ?: "") }
    var licenses by remember(currentCoach) { mutableStateOf(currentCoach?.licenses ?: "") }
    var bio by remember(currentCoach) { mutableStateOf(currentCoach?.bio ?: "") }
    var profilePhotoUri by remember(currentCoach) { mutableStateOf(currentCoach?.profilePhotoUri ?: "") }
    var clubLogoUri by remember(currentCoach) { mutableStateOf(currentCoach?.clubLogoUri ?: "") }

    // Image Pickers
    val profilePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            profilePhotoUri = uri.toString()
        }
    }

    val clubLogoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            clubLogoUri = uri.toString()
        }
    }

    val needsCompletionReminder = currentCoach != null &&
            (!currentCoach!!.hasUpdatedProfileDetails && (achievements.isBlank() || licenses.isBlank() || profilePhotoUri.isBlank()))

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        // Top Action Bar with Inbox Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PROFIL PROFESIONAL JURULATIH",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = RacingRed,
                    fontSize = 15.sp
                )
                Text(
                    text = "Butiran Sijil, Lesen, Logo Kelab & Rekod",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }

            // Inbox Button with Unread Badge
            Button(
                onClick = { showInboxDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkVariant),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (unreadInboxCount > 0) RacingRed else BorderDark),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                BadgedBox(
                    badge = {
                        if (unreadInboxCount > 0) {
                            Badge(containerColor = RacingRed) {
                                Text("$unreadInboxCount", color = Color.White, fontSize = 9.sp)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Mail,
                        contentDescription = "Inbox",
                        tint = if (unreadInboxCount > 0) RacingRed else GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Peti Masuk (Inbox)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        // Reminder Banner (After first registration reminder)
        if (needsCompletionReminder) {
            Surface(
                color = GoldAccent.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PERINGATAN SISTEM: KEMASKINI PROFIL",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Sila lengkapkan maklumat Pencapaian Coach, Lesen (contoh: Sport Science Level 1/2, World Athletics), dan Gambar Profil anda di bawah untuk profil rasmi kelab.",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // Profile Photo & Club Logo Row
        Surface(
            color = SurfaceDark,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Gambar Profil Coach
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(SurfaceDarkVariant, CircleShape)
                            .border(2.dp, RacingRed, CircleShape)
                            .clickable {
                                profilePhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilePhotoUri.isNotBlank()) {
                            AsyncImage(
                                model = profilePhotoUri,
                                contentDescription = "Gambar Profil Jurulatih",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, tint = SilverMetallic, modifier = Modifier.size(44.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("GAMBAR JURULATIH", color = SilverMetallic, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    TextButton(
                        onClick = {
                            profilePhotoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(if (profilePhotoUri.isNotBlank()) "Tukar Foto" else "+ Muat Naik", fontSize = 10.sp, color = RacingRed)
                    }
                }

                Box(modifier = Modifier.width(1.dp).height(80.dp).background(BorderDark))

                // 2. Logo Kelab (100x100)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(SurfaceDarkVariant, RoundedCornerShape(10.dp))
                            .border(1.5.dp, GoldAccent, RoundedCornerShape(10.dp))
                            .clickable {
                                clubLogoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (clubLogoUri.isNotBlank()) {
                            AsyncImage(
                                model = clubLogoUri,
                                contentDescription = "Logo Kelab",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(38.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("LOGO KELAB (100x100)", color = SilverMetallic, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    TextButton(
                        onClick = {
                            clubLogoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(if (clubLogoUri.isNotBlank()) "Tukar Logo" else "+ Muat Naik", fontSize = 10.sp, color = GoldAccent)
                    }
                }
            }
        }

        // Read-only Essential Info Box
        Surface(
            color = SurfaceDark,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("MAKLUMAT PENGESAHAN AKAUN:", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Nama Penuh:", color = TextSecondary, fontSize = 11.sp)
                    Text(currentCoach?.name ?: "-", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Emel Google:", color = TextSecondary, fontSize = 11.sp)
                    Text(currentCoach?.email ?: "-", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("No. IC Jurulatih:", color = TextSecondary, fontSize = 11.sp)
                    Text(currentCoach?.icNumber?.ifEmpty { "-" } ?: "-", color = Color.White, fontSize = 11.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Peranan / Akses:", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        if (currentCoach?.role == "ADMIN") "MASTER ADMIN" else "KETUA JURULATIH (COACH)",
                        color = if (currentCoach?.role == "ADMIN") GoldAccent else SprintGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Editable Professional Details Form
        Surface(
            color = SurfaceDark,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("BUTIRAN KEJURULATIHAN & KELAB:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nama Panggilan / Gelaran (cth: Coach Sam)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                )

                OutlinedTextField(
                    value = clubName,
                    onValueChange = { clubName = it },
                    label = { Text("Nama Kelab / Akademi Sukan") },
                    placeholder = { Text("cth: Akademi Pecut Sabah") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                )

                OutlinedTextField(
                    value = clubAddress,
                    onValueChange = { clubAddress = it },
                    label = { Text("Lokasi / Venue Latihan Rasmi") },
                    placeholder = { Text("cth: Kompleks Sukan Keningau / Stadium Likas") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                )

                OutlinedTextField(
                    value = trainingSpecialty,
                    onValueChange = { trainingSpecialty = it },
                    label = { Text("Pengkhususan Latihan") },
                    placeholder = { Text("cth: Balapan (Pecut 100m/200m) & Lompat Jauh") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                )

                // 1. Pencapaian Coach
                OutlinedTextField(
                    value = achievements,
                    onValueChange = { achievements = it },
                    label = { Text("Pencapaian Kejurulatihan *") },
                    placeholder = { Text("cth: Jurulatih Emas MSSM 2024, Jurulatih Pecut Sukma, Rekod Terbuka") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                )

                // 2. Lesen & Pensijilan Kejurulatihan
                OutlinedTextField(
                    value = licenses,
                    onValueChange = { licenses = it },
                    label = { Text("Lesen & Sijil Kejurulatihan *") },
                    placeholder = { Text("cth: Sains Sukan ISN Tahap 1, Sains Sukan Tahap 2, World Athletics Level 1, Lesen Kebangsaan") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                )

                // 3. Falsafah / Biodata Ringkas
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Biodata / Falsafah Latihan (Pilihan)") },
                    placeholder = { Text("Ringkasan pendekatan latihan berasaskan sains sukan & timing elektronik") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                )

                Button(
                    onClick = {
                        viewModel.updateCoachProfile(
                            profilePhotoUri = profilePhotoUri,
                            clubLogoUri = clubLogoUri,
                            achievements = achievements,
                            licenses = licenses,
                            nickname = nickname,
                            clubName = clubName,
                            clubAddress = clubAddress,
                            trainingSpecialty = trainingSpecialty,
                            bio = bio
                        )
                        Toast.makeText(context, "Profil Jurulatih berjaya dikemas kini!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RacingRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simpan & Kemaskini Profil Jurulatih", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showInboxDialog) {
        InboxDialog(
            viewModel = viewModel,
            onDismiss = { showInboxDialog = false }
        )
    }
}
