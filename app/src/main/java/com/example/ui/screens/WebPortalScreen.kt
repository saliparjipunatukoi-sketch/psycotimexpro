package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PsycotimexproLogoBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun WebPortalScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val syncMessage by viewModel.syncMessage.collectAsState()
    val totalAthletes by viewModel.totalAthletesCount.collectAsState()
    val totalRuns by viewModel.totalRunsCount.collectAsState()
    val currentCoach by viewModel.currentCoach.collectAsState()

    var apiEndpointStatus by remember { mutableStateOf("Tersedia untuk penyegerakan") }
    var showSetupGuideDialog by remember { mutableStateOf(false) }

    val portalUrl = "https://www.psycotimexpro.my/training-management"
    val downloadUrl = "https://www.psycotimexpro.my/training-management/download.php"
    val qrRegisterUrl = "https://www.psycotimexpro.my/training-management/register_runner.php?coach_id=${currentCoach?.id ?: 1}"

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
                PsycotimexproLogoBadge(size = 38.dp)
                Column {
                    Text(
                        text = "INTEGRASI WWW.PSYCOTIMEXPRO.MY",
                        color = RacingRed,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Pautan Dua Hala Aplikasi Telefon & Web Hosting InfinityFree",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Primary Training Management Hub Card
        item {
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RacingRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("PORTAL WEB PENGURUSAN LATIHAN", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                            Text("Sesuai untuk jurulatih pantau di Laptop & PC", color = SilverMetallic, fontSize = 11.sp)
                        }
                        Surface(color = RacingRed, shape = RoundedCornerShape(6.dp)) {
                            Text("AKTIF", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = Color.Black,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("URL Halaman Baru:", color = TextSecondary, fontSize = 10.sp)
                            Text(portalUrl, color = RacingRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Log Masuk Asing: Google Email Sahaja (Diapprove oleh Roger)", color = SilverMetallic, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(portalUrl))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RacingRed),
                            modifier = if (currentCoach?.role == "ADMIN") Modifier.weight(1.3f) else Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.LaptopMac, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buka Web di Laptop", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        // Hanya Master Admin Roger dibenarkan melihat Panduan FTP & Kod SQL
                        if (currentCoach?.role == "ADMIN") {
                            Button(
                                onClick = { showSetupGuideDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, tint = SilverMetallic, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Panduan FTP/SQL", color = SilverMetallic, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Web Synchronization Card
        item {
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("PENYEGERAKAN AWAN (CLOUD SYNC)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Data Tempatan sedia disegerak: $totalAthletes Atlit, $totalRuns Rekod Larian ET",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = Color.Black,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Portal Web Rasmi: www.psycotimexpro.my/training-management", color = SilverMetallic, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            if (currentCoach?.role == "ADMIN") {
                                Text("Path: /htdocs/training-management/api.php", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text("Database MySQL: if0_41886177_registry_psyco", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                            Text("Status Sambungan: $apiEndpointStatus", color = SprintGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.exportAndSyncToWebPortal()
                            apiEndpointStatus = "Pakej JSON sedia disegerak ke www.psycotimexpro.my/training-management/api.php"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RacingRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("sync_export_btn")
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Segerak Data ke Website", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Clarification Card (Stand Alone & Offline)
        item {
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SprintGreen.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SprintGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SISTEM STAND-ALONE & WEBSITE INTEGRATED", color = SprintGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "1. Boleh Muat Turun & Pasang Terus:\n" +
                                "Aplikasi ini merupakan Native Android App (APK/AAB). Anda boleh memasangnya terus pada telefon pintar.\n\n" +
                                "2. Berfungsi Secara 'Stand Alone' (Luar Talian / Padang):\n" +
                                "Semasa di padang atau trek larian (walaupun tiada WiFi atau liputan internet), Electronic Timing (Cam 1 & Cam 2 Photo Finish), pangkalan data atlet SQLite, dan catatan masa berfungsi sepenuhnya 100% pada telefon anda.\n\n" +
                                "3. Pautan Sambungan ke www.psycotimexpro.my/training-management:\n" +
                                "Sebaik sahaja telefon mendapat internet, anda boleh menyegerak rekod ke portal web agar jurulatih dapat memantau prestasi di Laptop.",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Direct Quick Web Links
        item {
            Text(
                text = "PAUTAN PANTAS SISTEM WEB:",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WebLinkRow(
                    title = "Halaman Muat Turun APK Telefon",
                    url = downloadUrl,
                    desc = "Pautan & QR Code muat turun fail APK untuk telefon pengguna",
                    icon = Icons.Default.Download
                ) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                    context.startActivity(intent)
                }

                WebLinkRow(
                    title = "Portal Pengurusan Latihan (Laptop)",
                    url = portalUrl,
                    desc = "Papan pemuka utama untuk pantauan jurulatih di laptop",
                    icon = Icons.Default.LaptopMac
                ) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(portalUrl))
                    context.startActivity(intent)
                }

                WebLinkRow(
                    title = "Borang Pendaftaran Atlit (QR Target)",
                    url = qrRegisterUrl,
                    desc = "Borang yang dibuka bila ibu bapa/atlit imbas QR Code",
                    icon = Icons.Default.QrCodeScanner
                ) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(qrRegisterUrl))
                    context.startActivity(intent)
                }

                WebLinkRow(
                    title = "WhatsApp Master Admin (Roger)",
                    url = "https://wa.me/60195326399",
                    desc = "Pemberitahuan pendaftaran, bantuan & sokongan teknikal",
                    icon = Icons.Default.Phone
                ) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/60195326399?text=Salam%20Roger,%20saya%20perlukan%20bantuan%20mengenai%20PsycoTimeXPro"))
                    context.startActivity(intent)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Sync notification banner
    syncMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissSyncMessage() },
            containerColor = SurfaceDark,
            title = {
                Text("Status Penyegerakan Web", color = RacingRed, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(msg, color = TextPrimary, fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissSyncMessage() },
                    colors = ButtonDefaults.buttonColors(containerColor = RacingRed)
                ) {
                    Text("Faham", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Dialog Panduan Setup InfinityFree
    if (showSetupGuideDialog) {
        val setupText = "Langkah Pemasangan di Hosting InfinityFree:\n\n" +
                "1. Database MySQL (phpMyAdmin):\n" +
                "Import fail 'schema.sql' yang telah dijana ke dalam database MySQL anda.\n\n" +
                "2. Fail Konfigurasi:\n" +
                "Buka 'db_connect.php' dan masukkan Hostname MySQL, Username & Kata Laluan cPanel anda.\n\n" +
                "3. Muat Naik via FTP:\n" +
                "Upload folder 'training-management' ke dalam direktori /htdocs/ di pelayan InfinityFree anda.\n\n" +
                "4. Akses di Pelayar Web:\n" +
                "Buka https://www.psycotimexpro.my/training-management di Laptop anda.\n\n" +
                "Master Admin Login:\n" +
                "Username: Saliparjipun.atukoi@gmail.com\n" +
                "Password: Abc@1234\n" +
                "WhatsApp Roger: +60195326399"

        AlertDialog(
            onDismissRequest = { showSetupGuideDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text("Panduan Pasang di InfinityFree", color = RacingRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp).verticalScroll(rememberScrollState())) {
                    Text(setupText, color = TextPrimary, fontSize = 11.sp, lineHeight = 16.sp, fontFamily = FontFamily.Monospace)
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Setup Guide", setupText))
                            Toast.makeText(context, "Panduan disalin!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark)
                    ) {
                        Text("Salin Panduan", color = SilverMetallic, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { showSetupGuideDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = RacingRed)
                    ) {
                        Text("Tutup", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}

@Composable
fun WebLinkRow(
    title: String,
    url: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = RacingRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = RacingRed, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(desc, color = TextSecondary, fontSize = 10.sp)
                    Text(url, color = SilverMetallic, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }

            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
        }
    }
}
