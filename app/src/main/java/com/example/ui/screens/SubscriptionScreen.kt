package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import com.example.ui.dialogs.SubscriptionReceiptDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SubscriptionScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentCoach by viewModel.currentCoach.collectAsState()
    val subPayments by viewModel.subscriptionPayments.collectAsState()
    var showReceiptDialog by remember { mutableStateOf(false) }

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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PsycotimexproLogoBadge(size = 36.dp)
                Column {
                    Text(
                        text = "STATUS & RESIT LANGGANAN",
                        color = RacingRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Pelan Jurulatih Olahraga Pro (RM30 / Bulan)",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Expiry Notification Banner
        item {
            Surface(
                color = if (daysLeft <= 3) RacingRed.copy(alpha = 0.15f) else SprintGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (daysLeft <= 3) RacingRed else SprintGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (daysLeft <= 3) "PERINGATAN: LANGGANAN HAMPIR TAMAT" else "LANGGANAN AKTIF",
                            color = if (daysLeft <= 3) RacingRed else SprintGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Baki Masa: $daysLeft Hari lagi (Tarikh Matang: $expiryDateStr)",
                            color = TextPrimary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            val url = "https://wa.me/60195326399?text=Salam%20Admin%20Roger,%20saya%20ingin%20memperbaharui%20langganan%20RM30%20bagi%20akaun%20${currentCoach?.email}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SprintGreen)
                    ) {
                        Text("Perbaharui", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Subscription Features Card
        item {
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("PAKEJ JURULATIH (RM30 / BULAN)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("• Tambah pelatih tanpa had (Balapan & Padang)", color = TextSecondary, fontSize = 11.sp)
                    Text("• Pendaftaran pelatih baru automatik melalui QR Code", color = TextSecondary, fontSize = 11.sp)
                    Text("• Mod Electronic Timing Kamera 1 (Mula) & Kamera 2 (Penamat)", color = TextSecondary, fontSize = 11.sp)
                    Text("• 1 Slot Penolong Jurulatih (Sub-Coach) Percuma", color = TextSecondary, fontSize = 11.sp)
                    Text("• Resit pembayaran rasmi sistem", color = TextSecondary, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showReceiptDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = RacingRed),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buka Resit Rasmi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                val url = "https://wa.me/60195326399?text=Salam%20Admin%20Roger,%20saya%20ingin%20bantuan%20mengenai%20langganan"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp Roger", color = TextPrimary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Subscription Receipts History
        item {
            Text("SEJARAH PEMBAYARAN RESIT RASMI:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }

        if (subPayments.isEmpty()) {
            item {
                Surface(color = SurfaceDark, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada rekod pembayaran langganan terdahulu.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(subPayments) { p ->
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
                            Text(p.receiptNo, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("Tarikh: ${p.paymentDate} • Matang: ${p.nextPaymentDue}", color = TextSecondary, fontSize = 10.sp)
                            Text(p.paymentMethod, color = TextMuted, fontSize = 9.sp)
                        }

                        Text("RM ${String.format("%.2f", p.amount)}", color = SprintGreen, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showReceiptDialog) {
        SubscriptionReceiptDialog(
            coach = currentCoach,
            onDismiss = { showReceiptDialog = false }
        )
    }
}
