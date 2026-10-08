package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PsycotimexproLogoBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isRegisterMode by remember { mutableStateOf(false) }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Register Form States (Detail Kelab & Coach Lengkap)
    var regClubName by remember { mutableStateOf("") }
    var regClubAddress by remember { mutableStateOf("") }
    var regTrainingSpecialty by remember { mutableStateOf("Balapan & Padang") }
    var regName by remember { mutableStateOf("") }
    var regNickname by remember { mutableStateOf("") }
    var regIcNumber by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }

    val loginError by viewModel.loginError.collectAsState()
    val approvalNotice by viewModel.approvalNotice.collectAsState()
    val passwordResetNotice by viewModel.passwordResetNotice.collectAsState()

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetCode by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var resetStep by remember { mutableStateOf(1) }

    var showGoogleSignInDialog by remember { mutableStateOf(false) }
    var googleEmailInput by remember { mutableStateOf("") }
    var googleNameInput by remember { mutableStateOf("") }
    var googleClubInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = SurfaceDark,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PsycotimexproLogoBadge(size = 58.dp)

                Text(
                    text = "PSYCO TIME X PRO",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Pusat Pengurusan Latihan & Olahraga",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                if (approvalNotice != null) {
                    Surface(
                        color = GoldAccent.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(approvalNotice!!, color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/60195326399?text=Salam%20Admin%20Roger,%20saya%20telah%20mendaftar%20Coach%20PsycoTimeXPro"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SprintGreen),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("WhatsApp Roger (+60195326399)", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (loginError != null) {
                    Surface(
                        color = RacingRed.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RacingRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(loginError!!, color = RacingRed, fontSize = 11.sp, modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold)
                    }
                }

                // Switcher (Log Masuk vs Daftar)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = if (!isRegisterMode) RacingRed else Color.Transparent,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isRegisterMode = false }
                    ) {
                        Text(
                            text = "LOG MASUK",
                            color = if (!isRegisterMode) Color.White else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Surface(
                        color = if (isRegisterMode) RacingRed else Color.Transparent,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isRegisterMode = true }
                    ) {
                        Text(
                            text = "DAFTAR COACH BARU",
                            color = if (isRegisterMode) Color.White else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                if (!isRegisterMode) {
                    // Google 1-Click Login (Tanpa Password)
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                googleEmailInput = ""
                                showGoogleSignInDialog = true
                            },
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF4285F4),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("G", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Log Masuk dengan Google",
                                color = Color(0xFF1F1F1F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderDark)
                        Text("  atau guna kata laluan  ", color = TextMuted, fontSize = 10.sp)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderDark)
                    }

                    // Manual Login Form
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Emel Google / Username Sub-Coach") },
                        placeholder = { Text("nama@gmail.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Kata Laluan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                    )

                    Button(
                        onClick = { viewModel.loginCoach(username, password) },
                        colors = ButtonDefaults.buttonColors(containerColor = RacingRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Log Masuk ke Sistem", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    TextButton(
                        onClick = {
                            resetStep = 1
                            resetEmail = username
                            showForgotPasswordDialog = true
                        }
                    ) {
                        Text("Lupa Kata Laluan? (Reset Password)", color = SilverMetallic, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    // Complete 1st Time Coach & Club Registration Form
                    Text(
                        text = "BUTIRAN KELAB & JURULATIH (1ST TIME REGISTER):",
                        color = SilverMetallic,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 1. Club Details
                    OutlinedTextField(
                        value = regClubName,
                        onValueChange = { regClubName = it },
                        label = { Text("Nama Kelab / Akademi Sukan *") },
                        placeholder = { Text("cth: Kelab Olahraga Sabah / Akademi Pecut") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                    )

                    OutlinedTextField(
                        value = regClubAddress,
                        onValueChange = { regClubAddress = it },
                        label = { Text("Alamat Kelab / Lokasi Latihan *") },
                        placeholder = { Text("cth: Stadium Likas / Kompleks Sukan Keningau") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                    )

                    OutlinedTextField(
                        value = regTrainingSpecialty,
                        onValueChange = { regTrainingSpecialty = it },
                        label = { Text("Latihan yang Diajar *") },
                        placeholder = { Text("cth: Balapan (100m, 200m) & Padang (Lompat)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                    )

                    HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 4.dp))

                    // 2. Coach Details
                    OutlinedTextField(
                        value = regName,
                        onValueChange = { regName = it },
                        label = { Text("Nama Penuh Jurulatih (Seperti Kad Pengenalan) *") },
                        placeholder = { Text("cth: Roger Salipar Jipun") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = regNickname,
                            onValueChange = { regNickname = it },
                            label = { Text("Nick Name / Nama Coach *") },
                            placeholder = { Text("cth: Coach Roger") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                        )

                        OutlinedTextField(
                            value = regIcNumber,
                            onValueChange = { regIcNumber = it },
                            label = { Text("No. Kad Pengenalan *") },
                            placeholder = { Text("850412-12-XXXX") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = regPhone,
                            onValueChange = { regPhone = it },
                            label = { Text("No. Telefon (WhatsApp) *") },
                            placeholder = { Text("019-XXXXXXX") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                        )

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Emel Google (Gmail) *") },
                            placeholder = { Text("coach@gmail.com") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                        )
                    }

                    OutlinedTextField(
                        value = regPassword,
                        onValueChange = { regPassword = it },
                        label = { Text("Kata Laluan Pilihan *") },
                        placeholder = { Text("••••••••") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                    )

                    Button(
                        onClick = {
                            if (regName.isNotBlank() && regEmail.isNotBlank() && regPassword.isNotBlank()) {
                                viewModel.registerNewCoach(
                                    name = regName,
                                    nickname = regNickname,
                                    icNumber = regIcNumber,
                                    phone = regPhone,
                                    email = regEmail,
                                    clubName = regClubName,
                                    clubAddress = regClubAddress,
                                    trainingSpecialty = regTrainingSpecialty,
                                    pass = regPassword
                                )
                                isRegisterMode = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RacingRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Hantar Pendaftaran Lengkap (7 Hari Percuma)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Text(
                    text = "Perlukan bantuan pendaftaran? WhatsApp Roger: +60195326399",
                    color = TextMuted,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Forgot Password Dialog Flow
        if (showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = {
                    showForgotPasswordDialog = false
                    viewModel.dismissPasswordResetNotice()
                },
                title = {
                    Text(
                        if (resetStep == 1) "LUPA KATA LALUAN" else "PENGESAHAN KOD & KATA LALUAN BARU",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (passwordResetNotice != null) {
                            Surface(
                                color = SprintGreen.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SprintGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    passwordResetNotice!!,
                                    color = SprintGreen,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(8.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (resetStep == 1) {
                            Text(
                                "Masukkan Emel Google yang telah anda daftarkan. Sistem akan menjana kod pengesahan reset:",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            OutlinedTextField(
                                value = resetEmail,
                                onValueChange = { resetEmail = it },
                                label = { Text("Emel Google (Gmail)") },
                                placeholder = { Text("coach@gmail.com") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                            )
                        } else {
                            Text(
                                "Masukkan kod 6-digit yang dihantar dan tetapkan kata laluan baharu anda:",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            OutlinedTextField(
                                value = resetCode,
                                onValueChange = { resetCode = it },
                                label = { Text("Kod Pengesahan (OTP / Reset Code)") },
                                placeholder = { Text("cth: 123456") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                            )
                            OutlinedTextField(
                                value = newPasswordInput,
                                onValueChange = { newPasswordInput = it },
                                label = { Text("Kata Laluan Baharu") },
                                placeholder = { Text("••••••••") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RacingRed, unfocusedBorderColor = BorderDark)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (resetStep == 1) {
                                if (resetEmail.isNotBlank()) {
                                    viewModel.requestPasswordReset(resetEmail)
                                    resetStep = 2
                                }
                            } else {
                                if (resetCode.isNotBlank() && newPasswordInput.isNotBlank()) {
                                    val ok = viewModel.submitNewPasswordWithCode(resetEmail, resetCode, newPasswordInput)
                                    if (ok) {
                                        showForgotPasswordDialog = false
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RacingRed)
                    ) {
                        Text(if (resetStep == 1) "Hantar Kod Pengesahan" else "Kemas Kini Kata Laluan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showForgotPasswordDialog = false
                        viewModel.dismissPasswordResetNotice()
                    }) {
                        Text("Batal", color = TextSecondary)
                    }
                },
                containerColor = SurfaceDark,
                shape = RoundedCornerShape(14.dp)
            )
        }

        // Google 1-Click Sign-In Dialog (Tanpa Password)
        if (showGoogleSignInDialog) {
            AlertDialog(
                onDismissRequest = { showGoogleSignInDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF4285F4),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("G", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                        }
                        Text(
                            "LOG MASUK DENGAN GOOGLE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Log masuk automatik dibenarkan menggunakan Emel Google anda tanpa memerlukan kata laluan.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Text("Masukkan Emel Google anda:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SilverMetallic)

                        OutlinedTextField(
                            value = googleEmailInput,
                            onValueChange = { googleEmailInput = it },
                            label = { Text("Emel Google (Gmail)") },
                            placeholder = { Text("nama.anda@gmail.com") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4285F4), unfocusedBorderColor = BorderDark)
                        )

                        OutlinedTextField(
                            value = googleNameInput,
                            onValueChange = { googleNameInput = it },
                            label = { Text("Nama Jurulatih / Nick Name (Jika Pengguna Baharu)") },
                            placeholder = { Text("cth: Coach Sam") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4285F4), unfocusedBorderColor = BorderDark)
                        )

                        OutlinedTextField(
                            value = googleClubInput,
                            onValueChange = { googleClubInput = it },
                            label = { Text("Nama Kelab Sukan (Jika Pengguna Baharu)") },
                            placeholder = { Text("cth: Kelab Olahraga Sabah") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4285F4), unfocusedBorderColor = BorderDark)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (googleEmailInput.isNotBlank()) {
                                viewModel.loginWithGoogle(
                                    googleEmail = googleEmailInput,
                                    displayName = googleNameInput,
                                    clubName = googleClubInput
                                )
                                showGoogleSignInDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                    ) {
                        Text("Log Masuk Google (Tanpa Password)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGoogleSignInDialog = false }) {
                        Text("Batal", color = TextSecondary)
                    }
                },
                containerColor = SurfaceDark,
                shape = RoundedCornerShape(14.dp)
            )
        }
    }
}
