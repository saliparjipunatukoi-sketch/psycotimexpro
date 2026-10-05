<?php
// hosting_infinityfree/training-management/register_coach.php
// Borang Pendaftaran Jurulatih Baharu (1st Time Register - Instant Access 7 Hari Percuma)
session_start();
require_once __DIR__ . '/db_connect.php';
require_once __DIR__ . '/mailer.php';

$error = "";

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $club_name = trim($_POST['club_name'] ?? '');
    $club_address = trim($_POST['club_address'] ?? '');
    $training_specialty = trim($_POST['training_specialty'] ?? 'Balapan & Padang');
    $name = trim($_POST['name'] ?? ''); // Nama Penuh
    $nickname = trim($_POST['nickname'] ?? ''); // Nick Name cth: Coach Roger
    $ic_number = trim($_POST['ic_number'] ?? '');
    $phone = trim($_POST['phone'] ?? '');
    $email = trim($_POST['email'] ?? '');
    $password = trim($_POST['password'] ?? '');

    if (empty($nickname)) {
        $nickname = "Coach " . substr($name, 0, 15);
    }

    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $error = "Sila masukkan format Emel Google yang sah.";
    } elseif (empty($name) || empty($password) || empty($club_name) || empty($ic_number)) {
        $error = "Sila lengkapkan semua medan wajib (*) termasuk Nama Kelab, No. Kad Pengenalan dan Kata Laluan.";
    } else {
        try {
            $check = $pdo->prepare("SELECT id FROM coaches WHERE email = ?");
            $check->execute([$email]);
            if ($check->fetch()) {
                $error = "Emel Google ini telah didaftarkan dalam sistem.";
            } else {
                // Percuma 7 hari penggunaan SERTA-MERTA (Tanpa perlu kelulusan admin untuk mula guna)
                $expires_at = (time() + (7 * 24 * 3600)) * 1000;
                $verify_code = (string)rand(100000, 999999);
                
                $insert = $pdo->prepare("INSERT INTO coaches 
                    (name, nickname, ic_number, phone, email, club_name, club_address, training_specialty, password, role, is_approved, subscription_status, subscription_expires_at, sub_coach_slots, verification_code)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'COACH', 1, 'TRIAL_7_DAYS', ?, 1, ?)");
                $insert->execute([$name, $nickname, $ic_number, $phone, $email, $club_name, $club_address, $training_specialty, $password, $expires_at, $verify_code]);
                $new_coach_id = $pdo->lastInsertId();

                // Hantar Auto-response Emel Pendaftaran Berjaya
                sendWelcomeEmail($email, $name, $nickname, $club_name, $verify_code);

                // Auto-login ke sistem serta-merta
                $_SESSION['user_type'] = 'coach';
                $_SESSION['coach_id'] = $new_coach_id;
                $_SESSION['coach_email'] = $email;
                $_SESSION['coach_name'] = $name;
                $_SESSION['coach_role'] = 'COACH';

                header("Location: index.php?msg=" . urlencode("Selamat datang $nickname! Akaun anda aktif serta-merta dengan Percuma 7 Hari."));
                exit;
            }
        } catch (Exception $e) {
            $error = "Gagal mendaftar: " . $e->getMessage();
        }
    }
}
?>
<!DOCTYPE html>
<html lang="ms">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pendaftaran Jurulatih & Kelab - Psyco Time X Pro</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700;800;900&family=JetBrains+Mono:wght@700;900&display=swap" rel="stylesheet">
    <style>
        :root {
            --racing-red: #E50914;
            --dark-red: #9E0B0F;
            --silver: #C0C0C0;
            --surface: #161618;
            --surface-variant: #222226;
            --border: #333338;
            --green: #00E676;
            --gold: #FFD700;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Montserrat', sans-serif; }
        body { background: #0F0F11; color: #FFF; display: flex; align-items: center; justify-content: center; min-height: 100vh; padding: 24px 12px; }
        .card { width: 100%; max-width: 580px; background: var(--surface); border: 1px solid var(--border); border-radius: 16px; padding: 28px; box-shadow: 0 14px 45px rgba(0,0,0,0.85); }
        .header { text-align: center; margin-bottom: 20px; }
        .badge { display: inline-flex; align-items: center; justify-content: center; width: 66px; height: 66px; background: #000; border: 2.5px solid var(--racing-red); border-radius: 50%; font-family: 'JetBrains Mono', monospace; font-size: 22px; font-weight: 900; color: #FFF; margin-bottom: 8px; }
        .badge span { color: var(--racing-red); }
        h1 { font-family: 'JetBrains Mono', monospace; font-size: 19px; font-weight: 900; color: #FFF; }
        .trial-badge { display: inline-block; background: rgba(0, 230, 118, 0.15); border: 1px solid var(--green); color: var(--green); border-radius: 20px; padding: 4px 14px; font-size: 11px; font-weight: 800; margin-top: 6px; }
        .section-title { font-family: 'JetBrains Mono', monospace; font-size: 11px; font-weight: 900; color: var(--gold); text-transform: uppercase; letter-spacing: 0.5px; margin: 16px 0 8px; border-bottom: 1px solid var(--border); padding-bottom: 4px; }
        .alert-error { background: rgba(229, 9, 20, 0.15); border: 1px solid var(--racing-red); color: var(--racing-red); padding: 12px; border-radius: 8px; font-size: 12px; margin-bottom: 16px; font-weight: 600; }
        .form-group { margin-bottom: 12px; }
        label { display: block; font-size: 11px; font-weight: 700; color: var(--silver); margin-bottom: 5px; text-transform: uppercase; }
        input, select, textarea { width: 100%; padding: 11px 12px; background: #0A0A0A; border: 1px solid var(--border); border-radius: 8px; color: #FFF; font-size: 13px; outline: none; }
        input:focus, select:focus, textarea:focus { border-color: var(--racing-red); }
        .row-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
        .btn-submit { width: 100%; padding: 14px; background: linear-gradient(135deg, var(--racing-red), var(--dark-red)); color: #FFF; border: none; border-radius: 8px; font-size: 13px; font-weight: 900; text-transform: uppercase; cursor: pointer; font-family: 'JetBrains Mono', monospace; margin-top: 14px; letter-spacing: 0.5px; }
        .btn-submit:hover { background: #b80710; }
        .back-link { display: block; text-align: center; margin-top: 14px; font-size: 12px; color: var(--silver); text-decoration: none; }
        .back-link strong { color: var(--racing-red); }
        .info-box { background: #0A0A0A; border: 1px dashed var(--border); border-radius: 8px; padding: 12px; font-size: 11px; color: var(--silver); line-height: 1.5; margin-top: 16px; }
    </style>
</head>
<body>
<div class="card">
    <div class="header">
        <div class="badge">P<span>X</span>P</div>
        <h1>DAFTAR JURULATIH & KELAB</h1>
        <div class="trial-badge">Percuma 7 Hari (RM30/Bulan Selepas Diluluskan)</div>
    </div>

    <?php if (!empty($error)): ?>
        <div class="alert-error"><?= htmlspecialchars($error) ?></div>
    <?php endif; ?>

    <form method="POST">
        <!-- BAHAGIAN 1: BUTIRAN KELAB / AKADEMI -->
        <div class="section-title">1. BUTIRAN KELAB / AKADEMI SUKAN</div>

        <div class="form-group">
            <label>Nama Kelab / Akademi *</label>
            <input type="text" name="club_name" required placeholder="cth: Kelab Olahraga Sabah / Akademi Pecut Kinabalu">
        </div>

        <div class="form-group">
            <label>Alamat Kelab / Lokasi Latihan *</label>
            <input type="text" name="club_address" required placeholder="cth: Kompleks Sukan Keningau / Stadium Likas">
        </div>

        <div class="form-group">
            <label>Latihan yang Diajar *</label>
            <input type="text" name="training_specialty" required placeholder="cth: Balapan (Pecut 100m, 200m) & Padang (Lompat Jauh)">
        </div>

        <!-- BAHAGIAN 2: BUTIRAN JURULATIH -->
        <div class="section-title">2. BUTIRAN JURULATIH (COACH DETAILS)</div>

        <div class="form-group">
            <label>Nama Penuh Jurulatih (Seperti Dalam Kad Pengenalan) *</label>
            <input type="text" name="name" required placeholder="cth: Roger Salipar Jipun">
        </div>

        <div class="row-2">
            <div class="form-group">
                <label>Nick Name / Gelaran Coach *</label>
                <input type="text" name="nickname" required placeholder="cth: Coach Roger">
            </div>
            <div class="form-group">
                <label>No. Kad Pengenalan *</label>
                <input type="text" name="ic_number" required placeholder="cth: 850412-12-5678">
            </div>
        </div>

        <div class="row-2">
            <div class="form-group">
                <label>No. Telefon (WhatsApp) *</label>
                <input type="tel" name="phone" required placeholder="cth: 019-8765432">
            </div>
            <div class="form-group">
                <label>Emel Google (Gmail) *</label>
                <input type="email" name="email" required placeholder="cth: coach@gmail.com">
            </div>
        </div>

        <div class="form-group">
            <label>Kata Laluan Pilihan *</label>
            <input type="password" name="password" required placeholder="••••••••">
        </div>

        <button type="submit" class="btn-submit">HANTAR & WHATSAPP ADMIN (+60195326399)</button>
    </form>

    <div class="info-box">
        <strong>Pemberitahuan Pendaftaran:</strong><br>
        Setelah menghantar borang, sistem akan automatik membuka WhatsApp terus ke <strong>Admin Roger (+60195326399)</strong> dengan butiran kelab & jurulatih lengkap untuk semakan dan kelulusan akaun percuma 7 hari anda.
    </div>

    <a href="login.php" class="back-link">Sudah mempunyai akaun? <strong>Log Masuk</strong></a>
</div>
</body>
</html>
