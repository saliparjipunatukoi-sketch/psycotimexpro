package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CoachAccountEntity
import com.example.ui.components.PsycotimexproLogoBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SubscriptionExpiredLockScreen(
    coach: CoachAccountEntity,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val expiryDateStr = SimpleDateFormat("dd/MM/yyyy hh:mma", Locale.getDefault()).format(Date(coach.subscriptionExpiresAt))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(2.dp, RacingRed),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PsycotimexproLogoBadge(size = 54.dp)

                Surface(
                    color = RacingRed.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Terkunci",
                            tint = RacingRed,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Text(
                    text = "AKSES SISTEM DIKUNCI",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = RacingRed,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Tempoh Percubaan 7 Hari / Langganan Anda Telah Tamat",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Surface(
                    color = SurfaceDarkVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Kelab: ${coach.clubName.ifEmpty { "Kelab Sukan" }}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Jurulatih: ${coach.name} (${coach.email})", color = SilverMetallic, fontSize = 11.sp)
                        Text("Tarikh Tamat: $expiryDateStr", color = RacingRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Kadar Pembaharuan: RM30.00 / Sebulan (Unlimited Atlit & ET)", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Text(
                    text = "Semua ciri sistem telah dikunci buat sementara waktu sehingga pembaharuan disahkan oleh Master Admin. Sila hubungi Admin Roger untuk mengaktifkan semula akaun anda.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                // WhatsApp Contact Admin Button
                Button(
                    onClick = {
                        val message = "Salam Admin Roger, saya ingin memperbaharui langganan Psyco Time X Pro RM30 bagi akaun:\n\n• Nama: ${coach.name}\n• Kelab: ${coach.clubName}\n• Emel: ${coach.email}\n\nMohon semakan dan pengaktifan semula. Terima kasih!"
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://wa.me/60195326399?text=" + Uri.encode(message))
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SprintGreen),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WhatsApp Admin Roger (+60195326399)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onLogout,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = SilverMetallic, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Keluar", color = SilverMetallic, fontSize = 12.sp)
                }
            }
        }
    }
}
