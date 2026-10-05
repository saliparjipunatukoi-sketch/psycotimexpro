<?php
// hosting_infinityfree/training-management/forgot_password.php
// Permintaan Reset Kata Laluan (Forgot Password Request)
require_once __DIR__ . '/db_connect.php';
require_once __DIR__ . '/mailer.php';

$message = "";
$error = "";

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = trim($_POST['email'] ?? '');

    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $error = "Sila masukkan alamat emel Google yang sah.";
    } else {
        try {
            $stmt = $pdo->prepare("SELECT id, name, nickname FROM coaches WHERE email = ?");
            $stmt->execute([$email]);
            $coach = $stmt->fetch(PDO::FETCH_ASSOC);

            if ($coach) {
                $code = (string)rand(100000, 999999);
                $token = bin2hex(random_bytes(24));
                $expires_at = time() + (24 * 3600); // 24 jam

                // Kemaskini token dalam database
                $update = $pdo->prepare("UPDATE coaches SET verification_code = ?, reset_token = ?, reset_token_expires = ? WHERE id = ?");
                $update->execute([$code, $token, $expires_at, $coach['id']]);

                // Hantar Auto-response Emel dari Admin Roger
                sendPasswordResetEmail($email, $coach['name'], $code, $token);

                // WhatsApp option jika mahu minta terus daripada Roger
                $wa_url = "https://wa.me/60195326399?text=" . urlencode("Salam Admin Roger, saya telah membuat permintaan reset kata laluan bagi akaun: $email. Kod pengesahan saya ialah $code.");

                header("Location: reset_password.php?email=" . urlencode($email) . "&sent=1");
                exit;
            } else {
                $error = "Akaun dengan emel '$email' tidak dijumpai di dalam pangkalan data.";
            }
        } catch (Exception $e) {
            $error = "Ralat sistem: " . $e->getMessage();
        }
    }
}
?>
<!DOCTYPE html>
<html lang="ms">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lupa Kata Laluan - Psyco Time X Pro</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700;900&family=JetBrains+Mono:wght@700;900&display=swap" rel="stylesheet">
    <style>
        :root {
            --racing-red: #E50914;
            --silver: #C0C0C0;
            --surface: #161618;
            --border: #333338;
            --green: #00E676;
            --gold: #FFD700;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Montserrat', sans-serif; }
        body { background: #0F0F11; color: #FFF; display: flex; align-items: center; justify-content: center; min-height: 100vh; padding: 20px 12px; }
        .card { width: 100%; max-width: 440px; background: var(--surface); border: 1px solid var(--border); border-radius: 16px; padding: 28px; box-shadow: 0 12px 40px rgba(0,0,0,0.85); text-align: center; }
        .badge { display: inline-flex; align-items: center; justify-content: center; width: 60px; height: 60px; background: #000; border: 2px solid var(--racing-red); border-radius: 50%; font-family: 'JetBrains Mono', monospace; font-size: 20px; font-weight: 900; color: #FFF; margin-bottom: 12px; }
        .badge span { color: var(--racing-red); }
        h1 { font-family: 'JetBrains Mono', monospace; font-size: 18px; font-weight: 900; margin-bottom: 6px; }
        p.sub { font-size: 12px; color: var(--silver); margin-bottom: 20px; }
        .alert-error { background: rgba(229, 9, 20, 0.15); border: 1px solid var(--racing-red); color: var(--racing-red); padding: 10px; border-radius: 8px; font-size: 12px; margin-bottom: 16px; font-weight: 600; text-align: left; }
        .form-group { text-align: left; margin-bottom: 16px; }
        label { display: block; font-size: 11px; font-weight: 700; color: var(--silver); margin-bottom: 6px; text-transform: uppercase; }
        input { width: 100%; padding: 12px; background: #0A0A0A; border: 1px solid var(--border); border-radius: 8px; color: #FFF; font-size: 14px; outline: none; }
        input:focus { border-color: var(--racing-red); }
        .btn-submit { width: 100%; padding: 14px; background: var(--racing-red); color: #FFF; border: none; border-radius: 8px; font-size: 13px; font-weight: 900; text-transform: uppercase; cursor: pointer; font-family: 'JetBrains Mono', monospace; }
        .btn-submit:hover { background: #B80710; }
        .links { margin-top: 18px; font-size: 12px; }
        .links a { color: var(--silver); text-decoration: none; }
        .links a strong { color: var(--racing-red); }
    </style>
</head>
<body>
<div class="card">
    <div class="badge">P<span>X</span>P</div>
    <h1>LUPA KATA LALUAN</h1>
    <p class="sub">Masukkan Emel Google berdaftar anda untuk menerima kod pengesahan & pautan tetapan semula kata laluan.</p>

    <?php if (!empty($error)): ?>
        <div class="alert-error"><?= htmlspecialchars($error) ?></div>
    <?php endif; ?>

    <form method="POST">
        <div class="form-group">
            <label>Emel Google (Gmail) Akaun Anda</label>
            <input type="email" name="email" required placeholder="cth: coach@gmail.com">
        </div>

        <button type="submit" class="btn-submit">HANTAR KOD PENGESAHAN</button>
    </form>

    <div class="links">
        <a href="login.php">Kembali ke <strong>Log Masuk</strong></a>
    </div>
</div>
</body>
</html>
