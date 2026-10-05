package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.FeePaymentEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val athletes by viewModel.athletes.collectAsState()
    val feePayments by viewModel.feePayments.collectAsState()

    var selectedAthleteIndex by remember { mutableStateOf(0) }
    var feeTitleInput by remember { mutableStateOf("Yuran Latihan Bulanan (Oktober)") }
    var amountInput by remember { mutableStateOf("60.00") }
    var statusInput by remember { mutableStateOf("LUNAS") }
    var showReceiptDialog by remember { mutableStateOf<FeePaymentEntity?>(null) }

    val totalCollected = remember(feePayments) {
        feePayments.filter { it.status == "LUNAS" }.sumOf { it.amount }
    }
    val totalPending = remember(feePayments) {
        feePayments.filter { it.status != "LUNAS" }.sumOf { it.amount }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "PENGURUSAN YURAN LATIHAN",
                color = NeonCyan,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Rekod yuran bulanan, pendaftaran dan penjanaan resit",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // Summary Revenue Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Jumlah Kutipan (Lunas)", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "RM ${String.format("%.2f", totalCollected)}",
                            color = SprintGreen,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Tertunggak", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "RM ${String.format("%.2f", totalPending)}",
                            color = RacingRed,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Add Payment Form
        item {
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("REKOD BAYARAN BARU", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (athletes.isNotEmpty()) {
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        val currentAthlete = athletes.getOrNull(selectedAthleteIndex) ?: athletes.first()

                        Box {
                            Surface(
                                color = Color.Black,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { dropdownExpanded = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Atlet: ${currentAthlete.name}", color = TextPrimary, fontSize = 13.sp)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                                }
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.background(SurfaceDark)
                            ) {
                                athletes.forEachIndexed { idx, a ->
                                    DropdownMenuItem(
                                        text = { Text(a.name, color = TextPrimary, fontSize = 12.sp) },
                                        onClick = {
                                            selectedAthleteIndex = idx
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = feeTitleInput,
                        onValueChange = { feeTitleInput = it },
                        label = { Text("Keterangan Yuran") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = BorderDark
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { amountInput = it },
                            label = { Text("Jumlah (RM)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = BorderDark
                            )
                        )

                        // Status Selector
                        Surface(
                            color = if (statusInput == "LUNAS") SprintGreen.copy(alpha = 0.2f) else RacingRed.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (statusInput == "LUNAS") SprintGreen else RacingRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clickable {
                                    statusInput = if (statusInput == "LUNAS") "TERTUNGGAK" else "LUNAS"
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = statusInput,
                                    color = if (statusInput == "LUNAS") SprintGreen else RacingRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val chosenAthlete = athletes.getOrNull(selectedAthleteIndex)
                            if (chosenAthlete != null) {
                                val amt = amountInput.toDoubleOrNull() ?: 50.0
                                viewModel.addFeePayment(chosenAthlete, feeTitleInput, amt, statusInput)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("fee_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SprintGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan & Jana Resit", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: Riwayat Yuran
        item {
            Text(
                text = "SENARAI REKOD YURAN (${feePayments.size})",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        if (feePayments.isEmpty()) {
            item {
                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada rekod bayaran yuran.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(feePayments) { fee ->
                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = fee.athleteName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${fee.title} • ${fee.dateString}", color = TextSecondary, fontSize = 11.sp)
                            Text(text = "No. Resit: ${fee.receiptNo}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "RM ${String.format("%.2f", fee.amount)}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                color = if (fee.status == "LUNAS") SprintGreen.copy(alpha = 0.2f) else RacingRed.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (fee.status == "LUNAS") SprintGreen else RacingRed),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.clickable {
                                    val nextStatus = if (fee.status == "LUNAS") "TERTUNGGAK" else "LUNAS"
                                    viewModel.updateFeeStatus(fee, nextStatus)
                                }
                            ) {
                                Text(
                                    text = fee.status,
                                    color = if (fee.status == "LUNAS") SprintGreen else RacingRed,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { showReceiptDialog = fee },
                            modifier = Modifier.size(28.dp).padding(start = 6.dp)
                        ) {
                            Icon(Icons.Default.RemoveRedEye, contentDescription = "Lihat Resit", tint = NeonCyan, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Receipt Dialog
    showReceiptDialog?.let { receipt ->
        AlertDialog(
            onDismissRequest = { showReceiptDialog = null },
            containerColor = SurfaceDark,
            title = {
                Text("RESIT PEMBAYARAN RASMI", color = NeonCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            },
            text = {
                Surface(
                    color = Color.Black,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("PSYCO TIME X PRO", color = NeonCyan, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("Kelab Olahraga & Latihan Pecut", color = TextSecondary, fontSize = 11.sp)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderDark)
                        Text("No. Resit: ${receipt.receiptNo}", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Text("Tarikh: ${receipt.dateString}", color = TextSecondary, fontSize = 11.sp)
                        Text("Diterima Daripada: ${receipt.athleteName}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Butiran: ${receipt.title}", color = TextSecondary, fontSize = 11.sp)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderDark)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("JUMLAH BESAR", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("RM ${String.format("%.2f", receipt.amount)}", color = SprintGreen, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Status: ${receipt.status}", color = if (receipt.status == "LUNAS") SprintGreen else RacingRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showReceiptDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Tutup", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
