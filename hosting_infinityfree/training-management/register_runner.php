<?php
// hosting_infinityfree/training-management/register_runner.php
// Borang Pendaftaran Atlit Baharu (Diimbas Melalui QR Code oleh Ibu Bapa / Atlet)
require_once __DIR__ . '/db_connect.php';

$coach_id = isset($_GET['coach_id']) ? intval($_GET['coach_id']) : 1;
$coach_name = "Jurulatih";

try {
    $stmt = $pdo->prepare("SELECT name, phone FROM coaches WHERE id = ?");
    $stmt->execute([$coach_id]);
    $coach = $stmt->fetch();
    if ($coach) {
        $coach_name = $coach['name'];
    }
} catch (Exception $e) {}

$success = false;
$error = "";

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $name = trim($_POST['name'] ?? '');
    $ic_number = trim($_POST['ic_number'] ?? '');
    $dob = trim($_POST['dob'] ?? '');
    $age = intval($_POST['age'] ?? 16);
    $phone = trim($_POST['phone'] ?? '');
    $height_cm = floatval($_POST['height_cm'] ?? 0.0);
    $weight_kg = floatval($_POST['weight_kg'] ?? 0.0);
    $gender = trim($_POST['gender'] ?? 'Lelaki');
    $sport_type = trim($_POST['sport_type'] ?? 'Balapan'); // Balapan atau Padang
    $category = trim($_POST['category'] ?? '100m Pecut');
    $notes = trim($_POST['notes'] ?? '');

    // Kendali Muat Naik Gambar Atlit (Mandatori)
    $photo_path = "";
    if (isset($_FILES['photo']) && $_FILES['photo']['error'] === UPLOAD_ERR_OK) {
        $upload_dir = __DIR__ . '/uploads/';
        if (!is_dir($upload_dir)) {
            mkdir($upload_dir, 0777, true);
        }
        $ext = strtolower(pathinfo($_FILES['photo']['name'], PATHINFO_EXTENSION));
        $new_filename = 'atlit_' . time() . '_' . rand(1000, 9999) . '.' . $ext;
        $target_file = $upload_dir . $new_filename;
        if (@move_uploaded_file($_FILES['photo']['tmp_name'], $target_file)) {
            $photo_path = 'uploads/' . $new_filename;
        } else {
            // Kebal kebenaran folder: Simpan sebagai Base64 Data URI jika pelayan sekat write permission
            $raw_bytes = @file_get_contents($_FILES['photo']['tmp_name']);
            if (!empty($raw_bytes)) {
                $photo_path = 'data:image/' . ($ext ?: 'jpeg') . ';base64,' . base64_encode($raw_bytes);
            }
        }
    }

    if (empty($name)) {
        $error = "Sila masukkan Nama Penuh Atlit.";
    } elseif (empty($ic_number)) {
        $error = "Sila masukkan No. Kad Pengenalan / Surat Beranak.";
    } elseif (empty($photo_path)) {
        $error = "Sila muat naik Gambar Atlit (Mandatori untuk rekod pengenalan kejohanan).";
    } else {
        try {
            $insert = $pdo->prepare("INSERT INTO athletes 
                (coach_id, name, ic_number, dob, age, phone, height_cm, weight_kg, gender, sport_type, category, pb_seconds, monthly_fee, notes, photo_uri)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 10.50, 60.00, ?, ?)");
            $insert->execute([$coach_id, $name, $ic_number, $dob, $age, $phone, $height_cm, $weight_kg, $gender, $sport_type, $category, $notes, $photo_path]);
            $success = true;
        } catch (Exception $e) {
            $error = "Gagal menyimpan pendaftaran: " . $e->getMessage();
        }
    }
}
?>
<!DOCTYPE html>
<html lang="ms">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pendaftaran Atlit Baharu - Psyco Time X Pro</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;800;900&family=JetBrains+Mono:wght@700&display=swap" rel="stylesheet">
    <style>
        :root {
            --racing-red: #E50914;
            --dark-red: #9E0B0F;
            --silver: #C0C0C0;
            --black: #0A0A0A;
            --surface: #161618;
            --surface-variant: #222226;
            --border: #333338;
            --text-primary: #F5F5F7;
            --text-secondary: #B0B0B5;
            --green: #00E676;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Montserrat', sans-serif; }
        body { background: #0F0F11; color: var(--text-primary); padding: 20px 12px; }
        .container { max-width: 580px; margin: 0 auto; background: var(--surface); border: 1px solid var(--border); border-radius: 16px; padding: 24px; box-shadow: 0 10px 30px rgba(0,0,0,0.7); }
        .header { text-align: center; margin-bottom: 20px; }
        .logo-badge { display: inline-flex; align-items: center; justify-content: center; width: 64px; height: 64px; background: #000; border: 2px solid var(--racing-red); border-radius: 50%; font-family: 'JetBrains Mono', monospace; font-size: 20px; font-weight: 900; color: #FFF; margin-bottom: 10px; }
        .logo-badge span { color: var(--racing-red); }
        h1 { font-size: 20px; font-weight: 900; letter-spacing: 1px; color: #FFF; font-family: 'JetBrains Mono', monospace; }
        .coach-tag { display: inline-block; background: var(--surface-variant); border: 1px solid var(--border); border-radius: 20px; padding: 4px 14px; font-size: 11px; color: var(--silver); margin-top: 6px; }
        .alert-success { background: rgba(0, 230, 118, 0.15); border: 1px solid var(--green); color: var(--green); padding: 16px; border-radius: 10px; margin-bottom: 20px; text-align: center; }
        .alert-error { background: rgba(229, 9, 20, 0.15); border: 1px solid var(--racing-red); color: var(--racing-red); padding: 12px; border-radius: 10px; margin-bottom: 20px; font-size: 13px; }
        .form-group { margin-bottom: 14px; }
        label { display: block; font-size: 11px; font-weight: 700; text-transform: uppercase; color: var(--silver); margin-bottom: 6px; letter-spacing: 0.5px; }
        input, select, textarea { width: 100%; padding: 12px; background: #0A0A0A; border: 1px solid var(--border); border-radius: 8px; color: #FFF; font-size: 13px; outline: none; }
        input:focus, select:focus, textarea:focus { border-color: var(--racing-red); }
        .row-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
        .row-3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 10px; }
        .sport-selector { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 14px; }
        .sport-card { background: var(--surface-variant); border: 2px solid var(--border); border-radius: 10px; padding: 12px; text-align: center; cursor: pointer; transition: all 0.2s; }
        .sport-card.active { border-color: var(--racing-red); background: rgba(229, 9, 20, 0.15); }
        .sport-card h3 { font-size: 13px; font-weight: 800; color: #FFF; }
        .sport-card p { font-size: 10px; color: var(--text-secondary); margin-top: 4px; }
        .btn-submit { width: 100%; padding: 14px; background: var(--racing-red); color: #FFF; border: none; border-radius: 10px; font-size: 14px; font-weight: 900; letter-spacing: 1px; text-transform: uppercase; cursor: pointer; transition: background 0.2s; font-family: 'JetBrains Mono', monospace; margin-top: 8px; }
        .btn-submit:hover { background: #b80710; }
        .footer-note { text-align: center; font-size: 10px; color: var(--text-secondary); margin-top: 18px; line-height: 1.5; }
        .footer-note a { color: var(--green); text-decoration: none; font-weight: bold; }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <div class="logo-badge">P<span>X</span>P</div>
        <h1>BORANG PENDAFTARAN ATLIT</h1>
        <div class="coach-tag">Jurulatih Bertanggungjawab: <strong><?= htmlspecialchars($coach_name) ?></strong></div>
    </div>

    <?php if ($success): ?>
        <div class="alert-success">
            <h3 style="font-size: 16px; margin-bottom: 6px;">Pendaftaran Atlit Berjaya Disimpan!</h3>
            <p style="font-size: 12px; color: #FFF;">Maklumat dan gambar atlit telah dihantar ke sistem jurulatih <strong><?= htmlspecialchars($coach_name) ?></strong>.</p>
            <p style="font-size: 11px; margin-top: 8px; color: var(--silver);">Sebarang pertanyaan, hubungi Admin Roger: <a href="https://wa.me/60195326399?text=Salam%20Roger,%20saya%20telah%20mendaftar%20atlit%20baru" style="color:var(--green);font-weight:bold;">+60195326399</a></p>
            <br>
            <a href="register_runner.php?coach_id=<?= $coach_id ?>" style="display:inline-block;padding:8px 16px;background:var(--surface-variant);color:#FFF;text-decoration:none;border-radius:6px;font-size:11px;font-weight:bold;">+ Daftar Atlit Lain</a>
        </div>
    <?php else: ?>

        <?php if (!empty($error)): ?>
            <div class="alert-error"><?= htmlspecialchars($error) ?></div>
        <?php endif; ?>

        <form method="POST" enctype="multipart/form-data">
            <div class="form-group">
                <label>Gambar Atlit (Mandatori *)</label>
                <input type="file" name="photo" accept="image/*" required style="padding:8px;background:var(--surface-variant);border:1px solid var(--racing-red);">
                <small style="color:var(--silver);font-size:10px;">Format JPG/PNG. Wajib untuk kad profil atlit & rekod kejohanan.</small>
            </div>

            <div class="form-group">
                <label>Nama Penuh Atlit *</label>
                <input type="text" name="name" required placeholder="cth: Muhammad Danial bin Rosli">
            </div>

            <div class="form-group">
                <label>No. Kad Pengenalan / Sijil Kelahiran *</label>
                <input type="text" name="ic_number" required placeholder="cth: 080512-12-5678">
            </div>

            <div class="row-2">
                <div class="form-group">
                    <label>Tarikh Lahir</label>
                    <input type="date" name="dob" id="dob_input" onchange="calculateAge()">
                </div>
                <div class="form-group">
                    <label>Umur (Tahun)</label>
                    <input type="number" name="age" id="age_input" value="16">
                </div>
            </div>

            <div class="row-3">
                <div class="form-group">
                    <label>Tinggi (cm)</label>
                    <input type="number" step="0.1" name="height_cm" placeholder="172">
                </div>
                <div class="form-group">
                    <label>Berat (kg)</label>
                    <input type="number" step="0.1" name="weight_kg" placeholder="62">
                </div>
                <div class="form-group">
                    <label>Jantina</label>
                    <select name="gender">
                        <option value="Lelaki">Lelaki</option>
                        <option value="Perempuan">Perempuan</option>
                    </select>
                </div>
            </div>

            <label>PILIHAN SUKAN DALAM LATIHAN:</label>
            <div class="sport-selector">
                <div class="sport-card active" id="card_balapan" onclick="selectSport('Balapan')">
                    <h3>🏃 BALAPAN (TRACK)</h3>
                    <p>Pecut, Berpagar, Jarak Jauh</p>
                </div>
                <div class="sport-card" id="card_padang" onclick="selectSport('Padang')">
                    <h3>🎯 PADANG (FIELD)</h3>
                    <p>Lompatan, Lontaran, Balingan</p>
                </div>
            </div>
            <input type="hidden" name="sport_type" id="sport_type_input" value="Balapan">

            <div class="form-group">
                <label>Acara Pilihan</label>
                <select name="category" id="category_select">
                    <!-- Default Track Events -->
                    <option value="100m Pecut">100m Pecut</option>
                    <option value="200m Pecut">200m Pecut</option>
                    <option value="400m Pecut">400m Pecut</option>
                    <option value="800m Jarak Sederhana">800m Jarak Sederhana</option>
                    <option value="1500m Jarak Jauh">1500m Jarak Jauh</option>
                    <option value="110m Lari Berpagar">110m Lari Berpagar</option>
                    <option value="400m Lari Berpagar">400m Lari Berpagar</option>
                    <option value="4x100m Berganti-ganti">4x100m Berganti-ganti</option>
                </select>
            </div>

            <div class="form-group">
                <label>No. Telefon (WhatsApp Ibu Bapa / Atlet)</label>
                <input type="tel" name="phone" placeholder="cth: 019-8765432">
            </div>

            <div class="form-group">
                <label>Catatan Kesihatan / Sejarah Sukan (Jika Ada)</label>
                <textarea name="notes" rows="2" placeholder="cth: Tiada alahan, pernah mewakili MSSD 2024"></textarea>
            </div>

            <button type="submit" class="btn-submit">HANTAR PENDAFTARAN</button>
        </form>

        <div class="footer-note">
            Sistem Pengurusan Sukan & Electronic Timing © Psyco Time X Pro<br>
            Bantuan / Pertanyaan Pentadbir: <a href="https://wa.me/60195326399">Roger (+60195326399)</a>
        </div>
    <?php endif; ?>
</div>

<script>
    const trackEvents = [
        "100m Pecut", "200m Pecut", "400m Pecut",
        "800m Jarak Sederhana", "1500m Jarak Jauh",
        "110m Lari Berpagar", "100m Lari Berpagar",
        "400m Lari Berpagar", "4x100m Berganti-ganti", "4x400m Berganti-ganti"
    ];

    const fieldEvents = [
        "Lompat Jauh (Long Jump)", "Lompat Kijang (Triple Jump)",
        "Lompat Tinggi (High Jump)", "Lompat Bergalah",
        "Lontar Peluru (Shot Put)", "Lempar Cakera (Discus)",
        "Rejam Lembing (Javelin)", "Baling Tukul Besi"
    ];

    function selectSport(type) {
        document.getElementById('sport_type_input').value = type;
        const balapanCard = document.getElementById('card_balapan');
        const padangCard = document.getElementById('card_padang');
        const select = document.getElementById('category_select');
        select.innerHTML = "";

        if (type === 'Balapan') {
            balapanCard.classList.add('active');
            padangCard.classList.remove('active');
            trackEvents.forEach(ev => {
                const opt = document.createElement('option');
                opt.value = ev;
                opt.textContent = ev;
                select.appendChild(opt);
            });
        } else {
            padangCard.classList.add('active');
            balapanCard.classList.remove('active');
            fieldEvents.forEach(ev => {
                const opt = document.createElement('option');
                opt.value = ev;
                opt.textContent = ev;
                select.appendChild(opt);
            });
        }
    }

    function calculateAge() {
        const dob = document.getElementById('dob_input').value;
        if (dob) {
            const birth = new Date(dob);
            const now = new Date();
            let age = now.getFullYear() - birth.getFullYear();
            const m = now.getMonth() - birth.getMonth();
            if (m < 0 || (m === 0 && now.getDate() < birth.getDate())) {
                age--;
            }
            if (age > 0) {
                document.getElementById('age_input').value = age;
            }
        }
    }
</script>
</body>
</html>
