package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PsycotimexproLogoBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SubCoachScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentCoach by viewModel.currentCoach.collectAsState()
    val subCoaches by viewModel.subCoaches.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val maxFreeSlots = currentCoach?.subCoachSlots ?: 1

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PsycotimexproLogoBadge(size = 36.dp)
                Column {
                    Text(
                        text = "PENOLONG JURULATIH (SUB-COACH)",
                        color = RacingRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Daftar 1 Penolong Jurulatih Percuma ke sistem anda",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Slot Status Card
        item {
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Kouta Sub-Coach Percuma", color = TextSecondary, fontSize = 11.sp)
                        Text("${subCoaches.size} / $maxFreeSlots Slot Digunakan", color = if (subCoaches.size >= maxFreeSlots) GoldAccent else SprintGreen, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }

                    Button(
                        onClick = {
                            val url = "https://wa.me/60195326399?text=Salam%20Admin%20Roger,%20saya%20ingin%20tambah%203%20sub-coach%20(RM10/bulan)%20untuk%20akaun%20${currentCoach?.email}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+3 Slot (RM10)", color = RacingRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Add Sub-Coach Form
        item {
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("DAFTAR SUB-COACH BARU", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("Sistem akan automatik mengesan dan membenarkan penolong ini log masuk ke sistem anda tanpa perlu kelulusan admin.", color = TextSecondary, fontSize = 11.sp)

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nama Penuh Penolong Coach") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("No. Telefon Penolong") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username Login") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Kata Laluan") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                        )
                    }

                    Button(
                        onClick = {
                            if (fullName.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
                                if (subCoaches.size >= maxFreeSlots) {
                                    Toast.makeText(context, "Slot percuma telah penuh. Sila langgan tambahan RM10 untuk 3 orang.", Toast.LENGTH_LONG).show()
                                } else {
                                    viewModel.addSubCoach(username, password, fullName, phone)
                                    fullName = ""
                                    phone = ""
                                    username = ""
                                    password = ""
                                    Toast.makeText(context, "Sub-Coach berjaya didaftarkan!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RacingRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Sub-Coach", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Active Sub-Coaches List
        item {
            Text("SENARAI PENOLONG JURULATIH AKTIF (${subCoaches.size}):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }

        if (subCoaches.isEmpty()) {
            item {
                Surface(color = SurfaceDark, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada penolong jurulatih didaftarkan.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(subCoaches) { sc ->
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
                            Text(sc.fullName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Username: ${sc.username} • Tel: ${sc.phone.ifEmpty { "-" }}", color = TextSecondary, fontSize = 11.sp)
                            Text("Akses: Sistem Penuh Jurulatih (Automatik)", color = SprintGreen, fontSize = 10.sp)
                        }

                        IconButton(onClick = { viewModel.deleteSubCoach(sc) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Padam", tint = RacingRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
