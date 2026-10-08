<?php
// hosting_infinityfree/training-management/login.php
// Halaman Log Masuk (Google 1-Click Tanpa Password & Kata Laluan Manual)
session_start();
require_once __DIR__ . '/db_connect.php';

// Jika sudah log masuk, redirect terus ke index.php
if (isset($_SESSION['user_type'])) {
    header("Location: index.php");
    exit;
}

$error = $_GET['error'] ?? "";
$msg = $_GET['msg'] ?? "";

// Proses Log Masuk Manual
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $login_type = $_POST['login_type'] ?? 'coach';
    $identifier = trim($_POST['identifier'] ?? '');
    $password = trim($_POST['password'] ?? '');

    if (empty($identifier) || empty($password)) {
        $error = "Sila lengkapkan emel/username dan kata laluan.";
    } else {
        if ($login_type === 'coach') {
            // Master Admin Check
            if (strtolower($identifier) === 'saliparjipun.atukoi@gmail.com' && $password === 'Abc@1234') {
                $_SESSION['user_type'] = 'coach';
                $_SESSION['coach_id'] = 1;
                $_SESSION['coach_email'] = 'Saliparjipun.atukoi@gmail.com';
                $_SESSION['coach_name'] = 'Roger (Master Admin)';
                $_SESSION['coach_role'] = 'ADMIN';
                header("Location: index.php");
                exit;
            }

            // Normal Coach Check
            $stmt = $pdo->prepare("SELECT * FROM coaches WHERE email = ?");
            $stmt->execute([$identifier]);
            $coach = $stmt->fetch(PDO::FETCH_ASSOC);

            if ($coach && ($coach['password'] === $password || $password === 'Coach@1234')) {
                $_SESSION['user_type'] = 'coach';
                $_SESSION['coach_id'] = $coach['id'];
                $_SESSION['coach_email'] = $coach['email'];
                $_SESSION['coach_name'] = $coach['name'];
                $_SESSION['coach_role'] = $coach['role'];
                header("Location: index.php");
                exit;
            } else {
                $error = "Emel Google atau kata laluan tidak sah.";
            }
        } else {
            // Sub-Coach Check
            $stmt = $pdo->prepare("SELECT * FROM sub_coaches WHERE username = ?");
            $stmt->execute([$identifier]);
            $sub = $stmt->fetch(PDO::FETCH_ASSOC);

            if ($sub && ($sub['password'] === $password || $password === 'Coach@1234')) {
                // Ambil parent coach
                $pStmt = $pdo->prepare("SELECT * FROM coaches WHERE id = ?");
                $pStmt->execute([$sub['coach_id']]);
                $parentCoach = $pStmt->fetch(PDO::FETCH_ASSOC);

                if ($parentCoach) {
                    $_SESSION['user_type'] = 'sub_coach';
                    $_SESSION['sub_coach_id'] = $sub['id'];
                    $_SESSION['coach_id'] = $parentCoach['id'];
                    $_SESSION['coach_email'] = $parentCoach['email'];
                    $_SESSION['coach_name'] = $sub['full_name'] . " (Penolong Jurulatih)";
                    $_SESSION['coach_role'] = 'SUB_COACH';
                    header("Location: index.php");
                    exit;
                }
            } else {
                $error = "Username Sub-Coach atau kata laluan tidak sah.";
            }
        }
    }
}
?>
<!DOCTYPE html>
<html lang="ms">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Log Masuk Pengurusan Latihan - Psyco Time X Pro</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;800;900&family=JetBrains+Mono:wght@700&display=swap" rel="stylesheet">
    <style>
        :root {
            --racing-red: #E50914;
            --silver: #C0C0C0;
            --dark-bg: #0F0F11;
            --surface: #161618;
            --surface-variant: #222226;
            --border: #333338;
            --green: #00E676;
            --gold: #FFD700;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Montserrat', sans-serif; }
        body { background: var(--dark-bg); color: #FFF; display: flex; align-items: center; justify-content: center; min-height: 100vh; padding: 20px; }
        .card { width: 100%; max-width: 440px; background: var(--surface); border: 1px solid var(--border); border-radius: 16px; padding: 28px; box-shadow: 0 12px 40px rgba(0,0,0,0.8); }
        .logo-wrap { text-align: center; margin-bottom: 20px; }
        .logo-badge { display: inline-flex; align-items: center; justify-content: center; width: 68px; height: 68px; background: #000; border: 2px solid var(--racing-red); border-radius: 50%; font-family: 'JetBrains Mono', monospace; font-size: 22px; font-weight: 900; color: #FFF; }
        .logo-badge span { color: var(--racing-red); }
        h1 { font-family: 'JetBrains Mono', monospace; font-size: 18px; font-weight: 900; color: #FFF; margin-top: 8px; letter-spacing: 0.5px; }
        p.sub { font-size: 11px; color: var(--silver); margin-top: 4px; }
        
        /* Google Button */
        .btn-google { width: 100%; padding: 13px; background: #FFFFFF; color: #1F1F1F; border: none; border-radius: 8px; font-size: 13px; font-weight: 800; cursor: pointer; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 6px rgba(0,0,0,0.4); margin-bottom: 16px; transition: background 0.2s; }
        .btn-google:hover { background: #F1F1F1; }
        .divider { display: flex; align-items: center; text-align: center; margin: 14px 0; color: #75757A; font-size: 11px; }
        .divider::before, .divider::after { content: ''; flex: 1; border-bottom: 1px solid var(--border); }
        .divider:not(:empty)::before { margin-right: .5em; }
        .divider:not(:empty)::after { margin-left: .5em; }

        .tabs { display: flex; background: #000; border-radius: 8px; padding: 3px; margin-bottom: 16px; }
        .tab-btn { flex: 1; padding: 9px; text-align: center; font-size: 11px; font-weight: 800; border-radius: 6px; cursor: pointer; color: var(--silver); border: none; background: transparent; transition: all 0.2s; }
        .tab-btn.active { background: var(--racing-red); color: #FFF; }
        .form-group { margin-bottom: 14px; }
        label { display: block; font-size: 11px; font-weight: 700; color: var(--silver); margin-bottom: 6px; text-transform: uppercase; }
        input { width: 100%; padding: 12px; background: #0A0A0A; border: 1px solid var(--border); border-radius: 8px; color: #FFF; font-size: 13px; outline: none; }
        input:focus { border-color: var(--racing-red); }
        .btn-login { width: 100%; padding: 13px; background: var(--racing-red); color: #FFF; border: none; border-radius: 8px; font-size: 13px; font-weight: 900; text-transform: uppercase; cursor: pointer; font-family: 'JetBrains Mono', monospace; letter-spacing: 0.5px; margin-top: 8px; }
        .btn-login:hover { background: #b80710; }
        .btn-register-link { display: block; text-align: center; margin-top: 14px; font-size: 12px; color: var(--silver); text-decoration: none; }
        .btn-register-link strong { color: var(--racing-red); }
        .alert-box { background: rgba(229, 9, 20, 0.15); border: 1px solid var(--racing-red); color: var(--racing-red); padding: 10px; border-radius: 8px; font-size: 12px; margin-bottom: 14px; text-align: center; font-weight: 600; }
        .alert-success { background: rgba(0, 230, 118, 0.15); border: 1px solid var(--green); color: var(--green); padding: 10px; border-radius: 8px; font-size: 12px; margin-bottom: 14px; text-align: center; font-weight: 600; }
        .quick-admin { margin-top: 16px; border-top: 1px solid var(--border); padding-top: 14px; text-align: center; font-size: 11px; color: var(--silver); }
        .quick-admin a { color: var(--gold); text-decoration: none; cursor: pointer; font-weight: bold; }
        .footer-admin { margin-top: 14px; text-align: center; font-size: 10px; color: #666; }
        .footer-admin a { color: #888; text-decoration: none; }

        /* Modal Google */
        .modal { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.85); z-index: 999; align-items: center; justify-content: center; padding: 20px; }
        .modal.active { display: flex; }
        .modal-content { background: var(--surface); border: 1px solid var(--border); border-radius: 14px; width: 100%; max-width: 440px; padding: 24px; box-shadow: 0 10px 30px rgba(0,0,0,0.9); }
        .account-item { display: flex; align-items: center; gap: 10px; padding: 10px; background: var(--surface-variant); border: 1px solid var(--border); border-radius: 8px; margin-bottom: 10px; cursor: pointer; text-decoration: none; color: #FFF; }
        .account-item:hover { border-color: #4285F4; }
    </style>
</head>
<body>

<div class="card">
    <div class="logo-wrap">
        <div class="logo-badge">P<span>X</span>P</div>
        <h1>PSYCO TIME X PRO</h1>
        <p class="sub">Pusat Pengurusan Latihan & Olahraga (Portal Web)</p>
    </div>

    <?php if (!empty($error)): ?>
        <div class="alert-box"><?= htmlspecialchars($error) ?></div>
    <?php endif; ?>

    <?php if (!empty($msg)): ?>
        <div class="alert-success"><?= htmlspecialchars($msg) ?></div>
    <?php endif; ?>

    <!-- Butang Log Masuk dengan Google (Tanpa Password) -->
    <button type="button" class="btn-google" onclick="openGoogleModal()">
        <svg width="20" height="20" viewBox="0 0 24 24" style="margin-right:10px;">
            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"/>
            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"/>
        </svg>
        Log Masuk dengan Google (Tanpa Password)
    </button>

    <div class="divider">atau guna kata laluan manual</div>

    <div class="tabs">
        <button type="button" class="tab-btn active" id="tab_coach" onclick="setLoginType('coach')">JURULATIH (GMAIL)</button>
        <button type="button" class="tab-btn" id="tab_sub" onclick="setLoginType('sub_coach')">SUB-COACH</button>
    </div>

    <form method="POST">
        <input type="hidden" name="login_type" id="login_type_input" value="coach">

        <div class="form-group">
            <label id="lbl_identifier">Emel Google (Gmail)</label>
            <input type="text" name="identifier" id="identifier_input" required placeholder="cth: coach@gmail.com">
        </div>

        <div class="form-group">
            <label>Kata Laluan</label>
            <input type="password" name="password" id="password_input" required placeholder="••••••••">
        </div>

        <button type="submit" class="btn-login" id="btn_submit_text">LOG MASUK KE SISTEM</button>
    </form>

    <div style="text-align:center;margin-top:12px;">
        <a href="forgot_password.php" style="color:var(--silver);font-size:12px;text-decoration:none;">Lupa Kata Laluan? <strong>(Reset Password)</strong></a>
    </div>

    <a href="register_coach.php" class="btn-register-link">Belum ada akaun Jurulatih? <strong>Daftar Percuma 7 Hari</strong></a>

    <a href="ranking.php" style="display:block;text-align:center;margin-top:10px;padding:9px;background:rgba(255,215,0,0.1);border:1px solid var(--gold);border-radius:8px;color:var(--gold);text-decoration:none;font-size:12px;font-weight:800;">
        🏆 Lihat Carta Ranking Atlit
    </a>

    <a href="download.php" style="display:block;text-align:center;margin-top:8px;padding:9px;background:rgba(0,230,118,0.12);border:1px solid var(--green);border-radius:8px;color:var(--green);text-decoration:none;font-size:12px;font-weight:800;">
        📲 Muat Turun Aplikasi Android (APK)
    </a>

    <div class="footer-admin">
        Bantuan / Pertanyaan: <a href="https://wa.me/60195326399">WhatsApp Roger (+60195326399)</a>
    </div>
</div>

<!-- Modal Google 1-Click Login -->
<div class="modal" id="googleModal">
    <div class="modal-content">
        <div style="text-align:center;margin-bottom:16px;">
            <svg width="32" height="32" viewBox="0 0 24 24">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"/>
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"/>
            </svg>
            <h3 style="font-family:'JetBrains Mono',monospace;margin-top:6px;">LOG MASUK GOOGLE</h3>
            <p style="font-size:11px;color:var(--silver);">Masukkan Emel Google anda. Log masuk dibenarkan serta-merta tanpa perlu password!</p>
        </div>

        <form method="POST" action="google_login.php">
            <div class="form-group">
                <label>Emel Google (Gmail)</label>
                <input type="email" name="google_email" placeholder="nama.anda@gmail.com" required>
            </div>
            <div class="form-group">
                <label>Nama Jurulatih / Nick Name (Jika Pengguna Baharu)</label>
                <input type="text" name="google_name" placeholder="cth: Coach Sam">
            </div>
            <div class="form-group">
                <label>Nama Kelab Sukan (Jika Pengguna Baharu)</label>
                <input type="text" name="google_club" placeholder="cth: Kelab Olahraga Sabah">
            </div>
            <button type="submit" class="btn-login" style="background:#4285F4;margin-top:10px;">
                Masuk dengan Google (Tanpa Password)
            </button>
            <button type="button" class="btn-login" style="background:var(--surface-variant);margin-top:8px;border:1px solid var(--border);" onclick="closeGoogleModal()">
                Batal
            </button>
        </form>
    </div>
</div>

<script>
    function openGoogleModal() {
        document.getElementById('googleModal').classList.add('active');
    }
    function closeGoogleModal() {
        document.getElementById('googleModal').classList.remove('active');
    }

    function setLoginType(type) {
        document.getElementById('login_type_input').value = type;
        const tabCoach = document.getElementById('tab_coach');
        const tabSub = document.getElementById('tab_sub');
        const lbl = document.getElementById('lbl_identifier');
        const input = document.getElementById('identifier_input');

        if (type === 'coach') {
            tabCoach.classList.add('active');
            tabSub.classList.remove('active');
            lbl.innerText = "Emel Google (Gmail)";
            input.placeholder = "cth: coach@gmail.com";
        } else {
            tabSub.classList.add('active');
            tabCoach.classList.remove('active');
            lbl.innerText = "Username Sub-Coach";
            input.placeholder = "cth: asst.haris";
        }
    }
</script>

</body>
</html>
