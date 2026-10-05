package com.example.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.example.data.local.entity.CoachAccountEntity
import com.example.ui.components.PsycotimexproLogoBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SubscriptionReceiptDialog(
    coach: CoachAccountEntity?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    val nextDue = coach?.subscriptionExpiresAt?.let {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
    } ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(System.currentTimeMillis() + (30L * 24 * 3600 * 1000)))

    val receiptNo = "SUB-PTXP-" + SimpleDateFormat("yyMM", Locale.getDefault()).format(Date()) + "-" + (coach?.id ?: 1)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PsycotimexproLogoBadge(size = 32.dp)
                    Column {
                        Text("RESIT LANGGANAN RASMI", color = RacingRed, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        Text("Psyco Time X Pro Sport Management", color = TextSecondary, fontSize = 10.sp)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
                }
            }
        },
        text = {
            Surface(
                color = Color.Black,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("No. Resit:", color = TextSecondary, fontSize = 11.sp)
                        Text(receiptNo, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Jurulatih:", color = TextSecondary, fontSize = 11.sp)
                        val coachLabel = if (!coach?.nickname.isNullOrBlank()) "${coach?.name} (${coach?.nickname})" else coach?.name ?: "Jurulatih"
                        Text(coachLabel, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    if (!coach?.clubName.isNullOrBlank()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Kelab / Akademi:", color = TextSecondary, fontSize = 11.sp)
                            Text(coach?.clubName ?: "", color = SilverMetallic, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Emel:", color = TextSecondary, fontSize = 11.sp)
                        Text(coach?.email ?: "-", color = SilverMetallic, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tarikh Bayaran:", color = TextSecondary, fontSize = 11.sp)
                        Text(today, color = Color.White, fontSize = 11.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pakej Langganan:", color = TextSecondary, fontSize = 11.sp)
                        Text("Coach Pro (Unlimited Runner)", color = RacingRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tarikh Bayaran Seterusnya:", color = TextSecondary, fontSize = 11.sp)
                        Text(nextDue, color = SprintGreen, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BorderDark)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("JUMLAH BESAR (TUNAI):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("RM 30.00 (LUNAS)", color = SprintGreen, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                    Text(
                        text = "Status: Disahkan oleh Admin Roger (+60195326399)",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val whatsappUrl = "https://wa.me/60195326399?text=Salam%20Admin%20Roger,%20saya%20telah%20melihat%20resit%20langganan%20RM30%20(No:%20$receiptNo)"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SprintGreen)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp Roger", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = RacingRed)
                ) {
                    Text("Tutup", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    )
}
