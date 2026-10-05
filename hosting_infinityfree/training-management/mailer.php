<?php
// hosting_infinityfree/training-management/mailer.php
// Modul Automasi Emel Rasmi Psyco Time X Pro
// Menghantar emel rasmi untuk: Pendaftaran Berjaya, Reset Password, dan Peringatan Tamat Langganan

function sendPsycoEmail($toEmail, $subject, $bodyHtml) {
    $fromName = "Psyco Time X Pro Admin";
    $fromEmail = "Saliparjipun.atukoi@gmail.com";
    $replyTo = "Saliparjipun.atukoi@gmail.com";

    // Header standard MIME HTML untuk sokongan pelayan PHP
    $headers  = "MIME-Version: 1.0\r\n";
    $headers .= "Content-type: text/html; charset=UTF-8\r\n";
    $headers .= "From: {$fromName} <{$fromEmail}>\r\n";
    $headers .= "Reply-To: {$replyTo}\r\n";
    $headers .= "X-Mailer: PHP/" . phpversion() . "\r\n";

    // Balut dalam template HTML rasmi Psyco Time X Pro (Merah Hitam Silver)
    $fullHtml = "
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset='UTF-8'>
        <style>
            body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #0F0F11; color: #FFFFFF; margin: 0; padding: 20px; }
            .email-container { max-width: 600px; margin: 0 auto; background: #161618; border: 1px solid #333338; border-radius: 14px; overflow: hidden; }
            .email-header { background: #000000; border-bottom: 2px solid #E50914; padding: 20px; text-align: center; }
            .badge { display: inline-block; width: 44px; height: 44px; line-height: 44px; background: #161618; border: 2px solid #E50914; border-radius: 50%; color: #FFFFFF; font-weight: 900; font-size: 16px; margin-bottom: 8px; }
            .badge span { color: #E50914; }
            .email-title { color: #FFFFFF; margin: 0; font-size: 18px; font-weight: 900; letter-spacing: 1px; }
            .email-body { padding: 24px; font-size: 14px; line-height: 1.6; color: #E5E5E5; }
            .email-btn { display: inline-block; background: #E50914; color: #FFFFFF !important; text-decoration: none; padding: 12px 24px; border-radius: 8px; font-weight: bold; margin: 16px 0; text-transform: uppercase; font-size: 13px; }
            .code-box { background: #0A0A0A; border: 1px solid #E50914; padding: 14px; text-align: center; font-size: 24px; font-weight: 900; letter-spacing: 4px; color: #FFD700; border-radius: 8px; margin: 16px 0; font-family: monospace; }
            .email-footer { background: #0A0A0A; border-top: 1px solid #333338; padding: 16px; text-align: center; font-size: 11px; color: #888888; }
            .email-footer a { color: #00E676; text-decoration: none; }
        </style>
    </head>
    <body>
        <div class='email-container'>
            <div class='email-header'>
                <div class='badge'>P<span>X</span>P</div>
                <h1 class='email-title'>PSYCO TIME X PRO</h1>
                <p style='color:#C0C0C0;margin:4px 0 0;font-size:11px;'>Sistem Pengurusan Latihan & Olahraga Rasmi</p>
            </div>
            <div class='email-body'>
                {$bodyHtml}
            </div>
            <div class='email-footer'>
                Emel ini dihantar secara automatik oleh Sistem Psyco Time X Pro.<br>
                Bantuan & Pertanyaan: <a href='https://wa.me/60195326399'>WhatsApp Roger (+60195326399)</a><br>
                Portal Rasmi: <a href='https://www.psycotimexpro.my/training-management'>www.psycotimexpro.my</a>
            </div>
        </div>
    </body>
    </html>
    ";

    // Hantar menggunakan fungsi mail() pelayan
    $mailSent = @mail($toEmail, $subject, $fullHtml, $headers);
    
    // Log aktiviti emel dalam fail log
    $logEntry = "[" . date('Y-m-d H:i:s') . "] To: $toEmail | Subject: $subject | Result: " . ($mailSent ? "SUCCESS" : "FAILED_OR_LOCAL") . "\n";
    @file_put_contents(__DIR__ . "/mail_log.txt", $logEntry, FILE_APPEND);

    return $mailSent;
}

// 1. Emel Pendaftaran Berjaya (Auto Response Welcome)
function sendWelcomeEmail($toEmail, $coachName, $nickname, $clubName, $verifyCode = "") {
    $subject = "Pendaftaran Berjaya - Selamat Datang ke Psyco Time X Pro!";
    $body = "
        <h2 style='color:#00E676;margin-top:0;'>Pendaftaran Anda Berjaya & Aktif Serta-Merta!</h2>
        <p>Salam <strong>{$nickname}</strong> ({$coachName}),</p>
        <p>Tahniah! Akaun anda untuk <strong>{$clubName}</strong> telah berjaya didaftarkan di dalam sistem <strong>Psyco Time X Pro</strong>.</p>
        <p>Akaun anda telah diaktifkan dengan <strong>Percuma 7 Hari (Full Access)</strong> serta-merta tanpa perlu menunggu kelulusan admin. Anda boleh terus log masuk ke portal web dan memuat turun aplikasi Android sekarang!</p>
        " . (!empty($verifyCode) ? "<p>Kod Pengesahan Anda:</p><div class='code-box'>{$verifyCode}</div>" : "") . "
        <div style='text-align:center;'>
            <a href='https://www.psycotimexpro.my/training-management/download.php' class='email-btn'>📥 Muat Turun Aplikasi Android (APK)</a>
        </div>
        <p><strong>Butiran Akses Anda:</strong><br>
        • Emel: {$toEmail}<br>
        • Kelab: {$clubName}<br>
        • Portal Web: <a href='https://www.psycotimexpro.my/training-management/login.php' style='color:#00E676;'>Buka Portal Web</a>
        </p>
        <p>Sekiranya anda memerlukan bantuan, sila hubungi Admin Roger melalui WhatsApp di talian +60195326399.</p>
    ";
    return sendPsycoEmail($toEmail, $subject, $body);
}

// 2. Emel Reset Kata Laluan (Forgot Password)
function sendPasswordResetEmail($toEmail, $coachName, $resetCode, $resetToken) {
    $subject = "Permintaan Tetapan Semula Kata Laluan - Psyco Time X Pro";
    $resetUrl = "https://www.psycotimexpro.my/training-management/reset_password.php?token=" . urlencode($resetToken) . "&email=" . urlencode($toEmail);
    $body = "
        <h2 style='color:#FFD700;margin-top:0;'>Permintaan Tetapan Semula Kata Laluan</h2>
        <p>Salam <strong>{$coachName}</strong>,</p>
        <p>Kami telah menerima permintaan untuk menetapkan semula kata laluan bagi akaun Psyco Time X Pro anda ({$toEmail}).</p>
        <p>Gunakan kod 6-digit berikut di aplikasi atau laman web:</p>
        <div class='code-box'>{$resetCode}</div>
        <p style='text-align:center;'>Atau klik pautan selamat di bawah untuk menukar kata laluan anda terus:</p>
        <div style='text-align:center;'>
            <a href='{$resetUrl}' class='email-btn'>Tukar Kata Laluan Sekarang</a>
        </div>
        <p style='font-size:12px;color:#888;'>Pautan dan kod ini sah untuk tempoh 24 jam. Jika anda tidak membuat permintaan ini, sila abaikan emel ini.</p>
    ";
    return sendPsycoEmail($toEmail, $subject, $body);
}

// 3. Emel Peringatan Tempoh Langganan Hampir Tamat
function sendSubscriptionExpiryNotice($toEmail, $coachName, $clubName, $daysLeft, $expiryDate) {
    $subject = "Peringatan: Langganan Psyco Time X Pro Tamat Dalam {$daysLeft} Hari";
    $waRenewUrl = "https://wa.me/60195326399?text=" . urlencode("Salam Admin Roger, saya ingin memperbaharui langganan RM30 akaun $toEmail ($clubName)");
    $body = "
        <h2 style='color:#E50914;margin-top:0;'>Pemberitahuan Tempoh Langganan</h2>
        <p>Salam <strong>{$coachName}</strong> ({$clubName}),</p>
        <p>Ini adalah peringatan mesra bahawa tempoh langganan Psyco Time X Pro anda berbaki <strong>{$daysLeft} hari</strong> lagi (Tamat pada <strong>{$expiryDate}</strong>).</p>
        <p>Untuk memastikan akses rakaman masa tanpa had (Unlimited Runner), bukti penamat photo finish, dan pangkalan data atlet tidak terganggu, sila perbaharui langganan anda:</p>
        <div style='background:#0A0A0A;border:1px solid #333338;padding:14px;border-radius:8px;margin:14px 0;'>
            <strong>Pakej:</strong> Coach Unlimited Runner<br>
            <strong>Kadar:</strong> RM30.00 / sebulan<br>
            <strong>Kaedah:</strong> Tunai / Pemindahan Bank Disahkan Admin Roger
        </div>
        <div style='text-align:center;'>
            <a href='{$waRenewUrl}' class='email-btn' style='background:#00E676;color:#000000 !important;'>Perbaharui Melalui WhatsApp (+60195326399)</a>
        </div>
    ";
    return sendPsycoEmail($toEmail, $subject, $body);
}
