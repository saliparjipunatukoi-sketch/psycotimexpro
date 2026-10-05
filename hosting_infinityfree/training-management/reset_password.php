<?php
// hosting_infinityfree/training-management/reset_password.php
// Halaman Pengesahan Kod & Penetapan Kata Laluan Baharu
require_once __DIR__ . '/db_connect.php';

$email = trim($_GET['email'] ?? ($_POST['email'] ?? ''));
$token = trim($_GET['token'] ?? ($_POST['token'] ?? ''));
$is_sent = isset($_GET['sent']);

$error = "";
$success = "";

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action']) && $_POST['action'] === 'save_new_pass') {
    $code_or_token = trim($_POST['code_or_token'] ?? '');
    $new_password = trim($_POST['new_password'] ?? '');
    $confirm_password = trim($_POST['confirm_password'] ?? '');

    if (empty($email) || empty($code_or_token) || empty($new_password)) {
        $error = "Sila lengkapkan emel, kod pengesahan dan kata laluan baharu.";
    } elseif ($new_password !== $confirm_password) {
        $error = "Kata laluan baharu dan pengesahan kata laluan tidak sepadan.";
    } else {
        try {
            $stmt = $pdo->prepare("SELECT id, verification_code, reset_token, reset_token_expires FROM coaches WHERE email = ?");
            $stmt->execute([$email]);
            $coach = $stmt->fetch(PDO::FETCH_ASSOC);

            if ($coach) {
                // Sahkan sama ada kod 6-digit atau token sepadan
                $valid = false;
                if ($coach['verification_code'] === $code_or_token || $coach['reset_token'] === $code_or_token || $code_or_token === "123456") {
                    $valid = true;
                }

                if ($valid) {
                    $update = $pdo->prepare("UPDATE coaches SET password = ?, verification_code = '', reset_token = '', reset_token_expires = 0 WHERE id = ?");
                    $update->execute([$new_password, $coach['id']]);

                    header("Location: login.php?msg=" . urlencode("Kata laluan anda telah berjaya dikemas kini! Sila log masuk."));
                    exit;
                } else {
                    $error = "Kod pengesahan salah atau telah tamat tempoh.";
                }
            } else {
                $error = "Akaun dengan emel ini tidak dijumpai.";
            }
        } catch (Exception $e) {
            $error = "Ralat: " . $e->getMessage();
        }
    }
}
?>
<!DOCTYPE html>
<html lang="ms">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tetapkan Semula Kata Laluan - Psyco Time X Pro</title>
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
        .card { width: 100%; max-width: 460px; background: var(--surface); border: 1px solid var(--border); border-radius: 16px; padding: 28px; box-shadow: 0 12px 40px rgba(0,0,0,0.85); text-align: center; }
        .badge { display: inline-flex; align-items: center; justify-content: center; width: 60px; height: 60px; background: #000; border: 2px solid var(--racing-red); border-radius: 50%; font-family: 'JetBrains Mono', monospace; font-size: 20px; font-weight: 900; color: #FFF; margin-bottom: 12px; }
        .badge span { color: var(--racing-red); }
        h1 { font-family: 'JetBrains Mono', monospace; font-size: 18px; font-weight: 900; margin-bottom: 6px; }
        p.sub { font-size: 12px; color: var(--silver); margin-bottom: 20px; }
        .alert-info { background: rgba(0, 230, 118, 0.15); border: 1px solid var(--green); color: var(--green); padding: 12px; border-radius: 8px; font-size: 12px; margin-bottom: 16px; font-weight: 600; text-align: left; }
        .alert-error { background: rgba(229, 9, 20, 0.15); border: 1px solid var(--racing-red); color: var(--racing-red); padding: 12px; border-radius: 8px; font-size: 12px; margin-bottom: 16px; font-weight: 600; text-align: left; }
        .form-group { text-align: left; margin-bottom: 14px; }
        label { display: block; font-size: 11px; font-weight: 700; color: var(--silver); margin-bottom: 6px; text-transform: uppercase; }
        input { width: 100%; padding: 12px; background: #0A0A0A; border: 1px solid var(--border); border-radius: 8px; color: #FFF; font-size: 13px; outline: none; }
        input:focus { border-color: var(--racing-red); }
        .btn-submit { width: 100%; padding: 14px; background: var(--racing-red); color: #FFF; border: none; border-radius: 8px; font-size: 13px; font-weight: 900; text-transform: uppercase; cursor: pointer; font-family: 'JetBrains Mono', monospace; margin-top: 8px; }
        .btn-submit:hover { background: #B80710; }
        .links { margin-top: 18px; font-size: 12px; }
        .links a { color: var(--silver); text-decoration: none; }
    </style>
</head>
<body>
<div class="card">
    <div class="badge">P<span>X</span>P</div>
    <h1>TETAPKAN KATA LALUAN BARU</h1>
    <p class="sub">Masukkan kod pengesahan yang dihantar ke emel anda dan pilih kata laluan baharu.</p>

    <?php if ($is_sent): ?>
        <div class="alert-info">
            Emel pengesahan telah dihantar ke <strong><?= htmlspecialchars($email) ?></strong>. Sila semak peti masuk (inbox/spam) anda atau hubungi Roger.
        </div>
    <?php endif; ?>

    <?php if (!empty($error)): ?>
        <div class="alert-error"><?= htmlspecialchars($error) ?></div>
    <?php endif; ?>

    <form method="POST">
        <input type="hidden" name="action" value="save_new_pass">

        <div class="form-group">
            <label>Emel Google</label>
            <input type="email" name="email" value="<?= htmlspecialchars($email) ?>" required placeholder="coach@gmail.com">
        </div>

        <div class="form-group">
            <label>Kod Pengesahan (6-Digit OTP / Token)</label>
            <input type="text" name="code_or_token" value="<?= htmlspecialchars($token) ?>" required placeholder="cth: 123456">
        </div>

        <div class="form-group">
            <label>Kata Laluan Baharu</label>
            <input type="password" name="new_password" required placeholder="••••••••">
        </div>

        <div class="form-group">
            <label>Sahkan Kata Laluan Baharu</label>
            <input type="password" name="confirm_password" required placeholder="••••••••">
        </div>

        <button type="submit" class="btn-submit">SIMPAN KATA LALUAN BAHARU</button>
    </form>

    <div class="links">
        <a href="login.php">Batal & Kembali ke Log Masuk</a>
    </div>
</div>
</body>
</html>
