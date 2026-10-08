package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.local.entity.InboxMessageEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun InboxDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val messages by viewModel.inboxMessages.collectAsState()
    var selectedMessage by remember { mutableStateOf<InboxMessageEntity?>(null) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = RacingRed.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Mail, contentDescription = null, tint = RacingRed, modifier = Modifier.size(20.dp))
                        }
                    }
                    Column {
                        Text(
                            "PETI MASUK & LAPORAN",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            "Laporan Bulanan 1hb & Dokumen Rasmi Admin",
                            fontSize = 10.sp,
                            color = SilverMetallic
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = SilverMetallic)
                }
            }
        },
        text = {
            if (selectedMessage != null) {
                val msg = selectedMessage!!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .background(SurfaceDarkVariant, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (msg.messageType == "MONTHLY_REPORT") GoldAccent.copy(alpha = 0.2f) else SprintGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (msg.messageType == "MONTHLY_REPORT") GoldAccent else SprintGreen)
                        ) {
                            Text(
                                text = if (msg.messageType == "MONTHLY_REPORT") "LAPORAN BULANAN 1HB" else "MEMO PENTADBIR",
                                color = if (msg.messageType == "MONTHLY_REPORT") GoldAccent else SprintGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(msg.dateString, color = TextMuted, fontSize = 10.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(msg.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Daripada: ${msg.senderName}", color = SilverMetallic, fontSize = 10.sp)

                    HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 8.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = msg.content,
                            color = TextPrimary,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { selectedMessage = null },
                            colors = ButtonDefaults.buttonColors(containerColor = BorderDark),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Kembali", color = Color.White, fontSize = 11.sp)
                        }

                        if (!msg.attachedDocumentTitle.isNullOrBlank()) {
                            Button(
                                onClick = {
                                    android.widget.Toast.makeText(context, "Dokumen '${msg.attachedDocumentTitle}' sedia untuk simpanan rasmi!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SprintGreen),
                                modifier = Modifier.weight(1.3f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cetak / Simpan", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else {
                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = TextMuted, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Peti masuk anda kosong.", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Laporan bulanan automatik akan dihantar setiap 1hb.", color = TextMuted, fontSize = 10.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { msg ->
                            Surface(
                                color = if (msg.isRead) SurfaceDarkVariant else SurfaceDark,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (!msg.isRead) RacingRed else BorderDark),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.markInboxMessageAsRead(msg.id)
                                        selectedMessage = msg
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        color = if (!msg.isRead) RacingRed else BorderDark,
                                        shape = CircleShape,
                                        modifier = Modifier.size(10.dp)
                                    ) {}

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (msg.messageType == "MONTHLY_REPORT") "📊 LAPORAN BULANAN" else "📩 MEMO PENTADBIR",
                                                color = if (msg.messageType == "MONTHLY_REPORT") GoldAccent else SprintGreen,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(msg.dateString, color = TextMuted, fontSize = 9.sp)
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = msg.title,
                                            color = Color.White,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (!msg.isRead) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Text(
                                            text = msg.content.take(65) + "...",
                                            color = SilverMetallic,
                                            fontSize = 10.sp,
                                            lineHeight = 13.sp
                                        )
                                    }

                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SilverMetallic, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(14.dp)
    )
}
