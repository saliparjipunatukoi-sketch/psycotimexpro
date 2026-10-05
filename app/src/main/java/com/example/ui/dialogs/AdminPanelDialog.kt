package com.example.ui.dialogs

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
import com.example.data.local.entity.CoachAccountEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AdminPanelDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val allCoaches by viewModel.allCoaches.collectAsState()

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
                Column {
                    Text("MASTER ADMIN PANEL (ROGER)", color = RacingRed, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    Text("Kelulusan Jurulatih & Kawalan Langganan", color = TextSecondary, fontSize = 11.sp)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = Color.Black,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Admin: Saliparjipun.atukoi@gmail.com", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("WhatsApp Rasmi: +60195326399 (Roger)", color = SprintGreen, fontSize = 10.sp)
                        }
                        Surface(
                            color = RacingRed.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("SUPERUSER", color = RacingRed, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(4.dp))
                        }
                    }
                }

                Text("Senarai Jurulatih Mendaftar (${allCoaches.size}):", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                if (allCoaches.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Tiada coach lain mendaftar setakat ini.", color = TextMuted, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allCoaches.filter { it.role != "ADMIN" }) { coach ->
                            Surface(
                                color = Color.Black,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            val nick = if (coach.nickname.isNotBlank()) " (${coach.nickname})" else ""
                                            Text(coach.name + nick, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            if (coach.clubName.isNotBlank()) {
                                                Text("Kelab: ${coach.clubName} • IC: ${coach.icNumber.ifEmpty { "-" }}", color = GoldAccent, fontSize = 9.sp)
                                            }
                                            Text("${coach.email} • Tel: ${coach.phone}", color = TextSecondary, fontSize = 10.sp)
                                        }

                                        if (coach.isApprovedByAdmin) {
                                            Surface(color = SprintGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                                Text("Diluluskan", color = SprintGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                                            }
                                        } else {
                                            Button(
                                                onClick = { viewModel.approveCoach(coach, 7) },
                                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text("Luluskan (+7 Hari)", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Status: ${coach.subscriptionStatus}", color = SilverMetallic, fontSize = 10.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Button(
                                                onClick = { viewModel.extendCoachSubscription(coach, 30) },
                                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text("+30 Hari (RM30)", color = TextPrimary, fontSize = 9.sp)
                                            }

                                            if (coach.phone.isNotEmpty()) {
                                                IconButton(
                                                    onClick = {
                                                        val cleanPhone = coach.phone.replace("[^0-9]".toRegex(), "")
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanPhone"))
                                                        context.startActivity(intent)
                                                    },
                                                    modifier = Modifier.size(26.dp)
                                                ) {
                                                    Icon(Icons.Default.Phone, contentDescription = "WhatsApp", tint = SprintGreen, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = RacingRed)
            ) {
                Text("Tutup", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    )
}
