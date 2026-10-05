<?php
// hosting_infinityfree/training-management/index.php
// Papan Pemuka Web Pengurusan Latihan (Sesuai untuk Jurulatih Pantau di Laptop & PC)
session_start();
require_once __DIR__ . '/db_connect.php';

if (!isset($_SESSION['coach_id'])) {
    header("Location: login.php");
    exit;
}

$coach_id = $_SESSION['coach_id'];
$is_admin = ($_SESSION['coach_role'] === 'ADMIN');
$is_sub_coach = !empty($_SESSION['is_sub_coach']);

// Dapatkan profil Coach
$stmt = $pdo->prepare("SELECT * FROM coaches WHERE id = ?");
$stmt->execute([$coach_id]);
$current_coach = $stmt->fetch();

// Tindakan POST (Padam Pelatih, Kemaskini Pelatih, Tambah Sub-Coach, Kelulusan Admin, dll)
$alert_msg = "";
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? '';

    if ($action === 'delete_athlete') {
        $ath_id = intval($_POST['athlete_id'] ?? 0);
        $del = $pdo->prepare("DELETE FROM athletes WHERE id = ? AND coach_id = ?");
        $del->execute([$ath_id, $coach_id]);
        $alert_msg = "Pelatih berjaya dipadam daripada sistem.";
    } elseif ($action === 'add_sub_coach') {
        $sub_user = trim($_POST['sub_username'] ?? '');
        $sub_pass = trim($_POST['sub_password'] ?? '');
        $sub_name = trim($_POST['sub_name'] ?? '');
        $sub_phone = trim($_POST['sub_phone'] ?? '');

        // Semak had slot
        $count_sub = $pdo->prepare("SELECT COUNT(*) FROM sub_coaches WHERE coach_id = ?");
        $count_sub->execute([$coach_id]);
        $curr_subs = $count_sub->fetchColumn();

        if ($curr_subs >= $current_coach['sub_coach_slots']) {
            $alert_msg = "Had slot Sub-Coach telah penuh ($curr_subs/{$current_coach['sub_coach_slots']}). Sila langgan penambahan RM10/bulan untuk +3 slot.";
        } else {
            try {
                $ins_sub = $pdo->prepare("INSERT INTO sub_coaches (coach_id, username, password, full_name, phone) VALUES (?, ?, ?, ?, ?)");
                $ins_sub->execute([$coach_id, $sub_user, $sub_pass, $sub_name, $sub_phone]);
                $alert_msg = "Sub-Coach berjaya didaftarkan! Sub-coach ini boleh terus log masuk di halaman web tanpa kelulusan admin.";
            } catch (Exception $e) {
                $alert_msg = "Ralat pendaftaran Sub-Coach: " . $e->getMessage();
            }
        }
    } elseif ($action === 'admin_approve_coach' && $is_admin) {
        $target_id = intval($_POST['target_coach_id'] ?? 0);
        $days = intval($_POST['extend_days'] ?? 7);
        $new_expiry = (time() + ($days * 24 * 3600)) * 1000;
        $appr = $pdo->prepare("UPDATE coaches SET is_approved = 1, subscription_expires_at = ?, subscription_status = 'ACTIVE' WHERE id = ?");
        $appr->execute([$new_expiry, $target_id]);
        $alert_msg = "Akaun Jurulatih berjaya diluluskan dan dilanjutkan $days hari!";
    }
}

// Senarai Pelatih
$sport_filter = $_GET['sport'] ?? 'Semua';
if ($sport_filter === 'Balapan') {
    $stmt_ath = $pdo->prepare("SELECT * FROM athletes WHERE coach_id = ? AND sport_type = 'Balapan' ORDER BY name ASC");
} elseif ($sport_filter === 'Padang') {
    $stmt_ath = $pdo->prepare("SELECT * FROM athletes WHERE coach_id = ? AND sport_type = 'Padang' ORDER BY name ASC");
} else {
    $stmt_ath = $pdo->prepare("SELECT * FROM athletes WHERE coach_id = ? ORDER BY name ASC");
}
$stmt_ath->execute([$coach_id]);
$athletes = $stmt_ath->fetchAll();

// Kira statistik
$total_athletes = count($athletes);
$balapan_count = 0;
$padang_count = 0;
foreach ($athletes as $a) {
    if ($a['sport_type'] === 'Padang') $padang_count++;
    else $balapan_count++;
}

// Senarai Sub-Coaches
$stmt_sc = $pdo->prepare("SELECT * FROM sub_coaches WHERE coach_id = ?");
$stmt_sc->execute([$coach_id]);
$sub_coaches = $stmt_sc->fetchAll();

// Senarai Larian ET
$stmt_runs = $pdo->prepare("SELECT * FROM timing_runs WHERE coach_id = ? ORDER BY id DESC LIMIT 10");
$stmt_runs->execute([$coach_id]);
$recent_runs = $stmt_runs->fetchAll();

// Senarai Jurulatih untuk Admin
$all_coaches = [];
if ($is_admin) {
    $stmt_all = $pdo->query("SELECT * FROM coaches WHERE id != 1 ORDER BY id DESC");
    $all_coaches = $stmt_all->fetchAll();
}

$now_ms = time() * 1000;
$expiry_ms = $current_coach['subscription_expires_at'];
$days_left = max(0, ceil(($expiry_ms - $now_ms) / (24 * 3600 * 1000)));
$expiry_date_str = date('d/m/Y', $expiry_ms / 1000);
$qr_url = "https://" . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . "/register_runner.php?coach_id=" . $coach_id;
?>
<!DOCTYPE html>
<html lang="ms">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pusat Pengurusan Latihan - Psyco Time X Pro</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700;800;900&family=JetBrains+Mono:wght@500;700;900&display=swap" rel="stylesheet">
    <style>
        :root {
            --racing-red: #E50914;
            --dark-red: #9E0B0F;
            --silver: #C0C0C0;
            --silver-light: #E5E5E5;
            --dark-bg: #0F0F11;
            --surface: #161618;
            --surface-variant: #222226;
            --border: #333338;
            --green: #00E676;
            --gold: #FFD700;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Montserrat', sans-serif; }
        body { background: var(--dark-bg); color: #FFF; line-height: 1.5; padding-bottom: 40px; }
        
        /* Top Navigation Header */
        .topbar { background: var(--surface); border-bottom: 1px solid var(--border); padding: 12px 24px; display: flex; align-items: center; justify-content: space-between; position: sticky; top: 0; z-index: 100; }
        .brand { display: flex; align-items: center; gap: 12px; }
        .logo-badge { width: 44px; height: 44px; background: #000; border: 2px solid var(--racing-red); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-family: 'JetBrains Mono', monospace; font-size: 16px; font-weight: 900; color: #FFF; }
        .logo-badge span { color: var(--racing-red); }
        .brand-text h1 { font-family: 'JetBrains Mono', monospace; font-size: 15px; font-weight: 900; letter-spacing: 1px; color: #FFF; }
        .brand-text p { font-size: 11px; color: var(--silver); }
        .user-nav { display: flex; align-items: center; gap: 12px; }
        .user-pill { background: var(--surface-variant); border: 1px solid var(--border); padding: 6px 14px; border-radius: 20px; font-size: 11px; color: var(--silver); }
        .user-pill strong { color: #FFF; }
        .btn-logout { background: transparent; border: 1px solid var(--racing-red); color: var(--racing-red); padding: 6px 12px; border-radius: 6px; font-size: 11px; font-weight: 700; cursor: pointer; text-decoration: none; }
        .btn-logout:hover { background: var(--racing-red); color: #FFF; }

        /* Container */
        .container { max-width: 1200px; margin: 20px auto; padding: 0 16px; }

        /* Alerts */
        .alert-box { background: rgba(0, 230, 118, 0.15); border: 1px solid var(--green); color: var(--green); padding: 12px 18px; border-radius: 8px; margin-bottom: 16px; font-size: 13px; font-weight: 600; }
        .banner-warning { background: rgba(229, 9, 20, 0.15); border: 1px solid var(--racing-red); color: var(--racing-red); padding: 14px 20px; border-radius: 10px; margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between; }
        .banner-warning h4 { font-family: 'JetBrains Mono', monospace; font-size: 13px; font-weight: 900; }
        .banner-warning p { font-size: 12px; color: #FFF; margin-top: 2px; }
        .btn-wa-renew { background: var(--green); color: #000; text-decoration: none; font-weight: 800; font-size: 11px; padding: 8px 14px; border-radius: 6px; white-space: nowrap; }

        /* Quick Action Bar */
        .action-bar { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px; margin-bottom: 20px; }
        .action-btn { background: var(--surface); border: 1px solid var(--border); border-radius: 12px; padding: 16px; text-align: left; cursor: pointer; text-decoration: none; color: #FFF; transition: all 0.2s; display: block; }
        .action-btn:hover { border-color: var(--racing-red); transform: translateY(-2px); }
        .action-btn.primary { background: linear-gradient(135deg, var(--racing-red), var(--dark-red)); border-color: var(--racing-red); }
        .action-btn h3 { font-size: 13px; font-weight: 800; margin-bottom: 4px; display: flex; align-items: center; gap: 6px; }
        .action-btn p { font-size: 11px; color: var(--silver); }
        .action-btn.primary p { color: #FFF; opacity: 0.9; }

        /* Stats Grid */
        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 10px; margin-bottom: 24px; }
        .stat-card { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; padding: 14px; text-align: center; }
        .stat-card .num { font-family: 'JetBrains Mono', monospace; font-size: 24px; font-weight: 900; color: #FFF; }
        .stat-card .label { font-size: 10px; font-weight: 700; color: var(--silver); text-transform: uppercase; margin-top: 4px; }

        /* Main Section / Card */
        .card { background: var(--surface); border: 1px solid var(--border); border-radius: 14px; padding: 20px; margin-bottom: 24px; }
        .card-header { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--border); padding-bottom: 12px; margin-bottom: 16px; }
        .card-header h2 { font-family: 'JetBrains Mono', monospace; font-size: 15px; font-weight: 900; color: #FFF; }
        
        /* Filter tabs */
        .filter-tabs { display: flex; gap: 8px; }
        .filter-tab { background: var(--surface-variant); border: 1px solid var(--border); color: var(--silver); padding: 6px 14px; border-radius: 6px; font-size: 11px; font-weight: 700; text-decoration: none; }
        .filter-tab.active { background: var(--racing-red); color: #FFF; border-color: var(--racing-red); }

        /* Table */
        .table-responsive { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; font-size: 12px; }
        th { background: #0A0A0A; color: var(--silver); text-align: left; padding: 10px 12px; font-size: 10px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid var(--border); }
        td { padding: 12px; border-bottom: 1px solid var(--border); color: var(--silver-light); }
        tr:hover td { background: var(--surface-variant); }
        .tag-sport { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 10px; font-weight: 800; }
        .tag-balapan { background: rgba(229, 9, 20, 0.2); color: var(--racing-red); border: 1px solid var(--racing-red); }
        .tag-padang { background: rgba(192, 192, 192, 0.2); color: var(--silver); border: 1px solid var(--silver); }
        .btn-del { background: transparent; border: 1px solid var(--racing-red); color: var(--racing-red); padding: 4px 8px; border-radius: 4px; font-size: 10px; font-weight: 700; cursor: pointer; }
        .btn-del:hover { background: var(--racing-red); color: #FFF; }

        /* Modal QR */
        .modal { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.85); z-index: 200; align-items: center; justify-content: center; padding: 20px; }
        .modal.active { display: flex; }
        .modal-content { background: var(--surface); border: 1px solid var(--border); border-radius: 16px; width: 100%; max-width: 480px; padding: 24px; text-align: center; }
        .qr-frame { background: #FFF; padding: 14px; border-radius: 12px; display: inline-block; margin: 16px 0; }
        .btn-close-modal { background: var(--surface-variant); color: #FFF; border: none; padding: 8px 16px; border-radius: 6px; font-size: 11px; font-weight: 700; cursor: pointer; margin-top: 12px; }

        /* Form Controls */
        .form-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 10px; margin-bottom: 10px; }
        input, select { width: 100%; padding: 10px; background: #0A0A0A; border: 1px solid var(--border); border-radius: 6px; color: #FFF; font-size: 12px; }
        .btn-action { background: var(--racing-red); color: #FFF; border: none; padding: 10px 16px; border-radius: 6px; font-size: 11px; font-weight: 800; cursor: pointer; text-transform: uppercase; }

        /* Footer */
        .footer { text-align: center; font-size: 11px; color: #777; margin-top: 40px; border-top: 1px solid var(--border); padding-top: 20px; }
        .footer a { color: var(--green); text-decoration: none; font-weight: bold; }
    </style>
</head>
<body>

<header class="topbar">
    <div class="brand">
        <div class="logo-badge">P<span>X</span>P</div>
        <div class="brand-text">
            <h1>PSYCO TIME X PRO</h1>
            <p>Portal Pengurusan Latihan & Pemantauan (Laptop / PC)</p>
        </div>
    </div>
    <div class="user-nav">
        <?php if ($is_admin): ?>
            <a href="admin.php" class="btn-logout" style="border-color:var(--gold);color:var(--gold);font-weight:800;">⚙️ Admin Dashboard</a>
        <?php endif; ?>
        <a href="ranking.php" class="btn-logout" style="border-color:var(--gold);color:var(--gold);font-weight:800;">🏆 Ranking</a>
        <a href="download.php" class="btn-logout" style="border-color:var(--green);color:var(--green);font-weight:800;">📥 Muat Turun APK</a>
        <div class="user-pill">
            <strong><?= htmlspecialchars(!empty($current_coach['nickname']) ? $current_coach['nickname'] : $current_coach['name']) ?></strong> 
            <?php if (!empty($current_coach['club_name'])): ?>
                • <span style="color:var(--gold);"><?= htmlspecialchars($current_coach['club_name']) ?></span>
            <?php endif; ?>
        </div>
        <a href="login.php?logout=1" class="btn-logout">Log Keluar</a>
    </div>
</header>

<main class="container">
    <?php if (!empty($alert_msg)): ?>
        <div class="alert-box"><?= htmlspecialchars($alert_msg) ?></div>
    <?php endif; ?>

    <?php if ($days_left <= 3 && !$is_admin): ?>
        <div class="banner-warning">
            <div>
                <h4>PERINGATAN: LANGGANAN HAMPIR TAMAT</h4>
                <p>Baki masa anda: <strong><?= $days_left ?> hari</strong> (Tamat: <?= $expiry_date_str ?>). Kadar pembaharuan: RM30/bulan (Unlimited Runner).</p>
            </div>
            <a href="https://wa.me/60195326399?text=Salam%20Admin%20Roger,%20saya%20ingin%20memperbaharui%20langganan%20RM30%20akaun%20<?= urlencode($current_coach['email']) ?>" class="btn-wa-renew">WhatsApp Roger (+60195326399)</a>
        </div>
    <?php endif; ?>

    <!-- Navigation Hub to All Pages / Options -->
    <div class="action-bar">
        <a href="ranking.php" class="action-btn" style="border-color:var(--gold);background:rgba(255,215,0,0.08);">
            <h3 style="color:var(--gold);">🏆 Carta Ranking Pelatih</h3>
            <p>Lihat kedudukan PB Terkini vs PB Lama atlet</p>
        </a>
        <a href="download.php" class="action-btn" style="border-color:var(--green);background:rgba(0,230,118,0.1);">
            <h3 style="color:var(--green);">📲 Muat Turun APK Telefon</h3>
            <p>Pasang fail psycotimexpro.apk ke telefon Android</p>
        </a>
        <div class="action-btn primary" onclick="openQrModal()">
            <h3>📱 New Member (QR Code)</h3>
            <p>Papar QR Code untuk ibu bapa/atlet daftar sendiri</p>
        </div>
        <a href="#section_athletes" class="action-btn">
            <h3>🏃 Senarai Pelatih (<?= $total_athletes ?>)</h3>
            <p>Balapan (<?= $balapan_count ?>) • Padang (<?= $padang_count ?>)</p>
        </a>
        <a href="#section_sub_coaches" class="action-btn">
            <h3>👥 Sub-Coach (<?= count($sub_coaches) ?> / <?= $current_coach['sub_coach_slots'] ?>)</h3>
            <p>1 Slot Percuma • +3 Slot RM10/bln</p>
        </a>
        <div class="action-btn" onclick="openReceiptModal()">
            <h3>📄 Resit Langganan RM30</h3>
            <p>Status: Lunas Tunai • Tarikh Matang</p>
        </div>
        <a href="https://wa.me/60195326399?text=Salam%20Roger,%20saya%20perlukan%20bantuan%20mengenai%20PsycoTimeXPro" class="action-btn">
            <h3>💬 Hubungi Admin Roger</h3>
            <p>WhatsApp Direct: +60195326399</p>
        </a>
    </div>

    <!-- Quick Stats -->
    <div class="stats-grid">
        <div class="stat-card">
            <div class="num"><?= $total_athletes ?></div>
            <div class="label">Jumlah Pelatih</div>
        </div>
        <div class="stat-card">
            <div class="num" style="color:var(--racing-red);"><?= $balapan_count ?></div>
            <div class="label">Acara Balapan</div>
        </div>
        <div class="stat-card">
            <div class="num" style="color:var(--silver);"><?= $padang_count ?></div>
            <div class="label">Acara Padang</div>
        </div>
        <div class="stat-card">
            <div class="num" style="color:var(--green);"><?= count($recent_runs) ?></div>
            <div class="label">Sesi Larian ET</div>
        </div>
        <div class="stat-card">
            <div class="num" style="color:var(--gold);"><?= $days_left ?> Hari</div>
            <div class="label">Baki Langganan</div>
        </div>
    </div>

    <!-- Master Admin Section (Only Roger: Saliparjipun.atukoi@gmail.com) -->
    <?php if ($is_admin): ?>
        <section class="card" style="border-color: var(--gold);">
            <div class="card-header">
                <h2 style="color: var(--gold);">MASTER ADMIN PANEL (ROGER - SALIPARJIPUN.ATUKOI@GMAIL.COM)</h2>
                <span style="font-size:11px;color:var(--silver);">Pengurusan Kelulusan & Tarikh Luput Jurulatih</span>
            </div>
            <div class="table-responsive">
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Jurulatih & Gelaran</th>
                            <th>Kelab & Latihan</th>
                            <th>No. IC & Telefon</th>
                            <th>Emel Google</th>
                            <th>Status Kelulusan</th>
                            <th>Tamat Pada</th>
                            <th>Tindakan Admin</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($all_coaches as $ac): ?>
                            <tr>
                                <td>#<?= $ac['id'] ?></td>
                                <td>
                                    <strong><?= htmlspecialchars($ac['name']) ?></strong><br>
                                    <span style="color:var(--gold);font-size:10px;">Nick: <?= htmlspecialchars(!empty($ac['nickname']) ? $ac['nickname'] : '-') ?></span>
                                </td>
                                <td>
                                    <strong><?= htmlspecialchars(!empty($ac['club_name']) ? $ac['club_name'] : '-') ?></strong><br>
                                    <span style="color:var(--silver);font-size:10px;"><?= htmlspecialchars(!empty($ac['training_specialty']) ? $ac['training_specialty'] : '-') ?></span>
                                </td>
                                <td>
                                    IC: <?= htmlspecialchars(!empty($ac['ic_number']) ? $ac['ic_number'] : '-') ?><br>
                                    Tel: <?= htmlspecialchars($ac['phone']) ?>
                                </td>
                                <td><?= htmlspecialchars($ac['email']) ?></td>
                                <td>
                                    <?php if ($ac['is_approved']): ?>
                                        <span style="color:var(--green);font-weight:bold;">Diluluskan</span>
                                    <?php else: ?>
                                        <span style="color:var(--gold);font-weight:bold;">Menunggu Kelulusan</span>
                                    <?php endif; ?>
                                </td>
                                <td><?= date('d/m/Y', $ac['subscription_expires_at'] / 1000) ?></td>
                                <td>
                                    <form method="POST" style="display:inline-flex; gap:6px;">
                                        <input type="hidden" name="action" value="admin_approve_coach">
                                        <input type="hidden" name="target_coach_id" value="<?= $ac['id'] ?>">
                                        <button type="submit" name="extend_days" value="7" class="btn-action" style="padding:4px 8px;font-size:10px;background:var(--gold);color:#000;">Lulus 7 Hari</button>
                                        <button type="submit" name="extend_days" value="30" class="btn-action" style="padding:4px 8px;font-size:10px;">+30 Hari (RM30)</button>
                                        <?php if (!empty($ac['phone'])): ?>
                                            <a href="https://wa.me/<?= preg_replace('/[^0-9]/', '', $ac['phone']) ?>" target="_blank" style="padding:4px 8px;background:var(--green);color:#000;border-radius:4px;font-size:10px;text-decoration:none;font-weight:bold;">WhatsApp</a>
                                        <?php endif; ?>
                                    </form>
                                </td>
                            </tr>
                        <?php endforeach; ?>
                    </tbody>
                </table>
            </div>
        </section>
    <?php endif; ?>

    <!-- Athletes Section -->
    <section class="card" id="section_athletes">
        <div class="card-header">
            <h2>SENARAI PELATIH JURULATIH (BALAPAN & PADANG)</h2>
            <div class="filter-tabs">
                <a href="?sport=Semua" class="filter-tab <?= ($sport_filter === 'Semua') ? 'active' : '' ?>">Semua (<?= $total_athletes ?>)</a>
                <a href="?sport=Balapan" class="filter-tab <?= ($sport_filter === 'Balapan') ? 'active' : '' ?>">Balapan (<?= $balapan_count ?>)</a>
                <a href="?sport=Padang" class="filter-tab <?= ($sport_filter === 'Padang') ? 'active' : '' ?>">Padang (<?= $padang_count ?>)</a>
                <button onclick="openQrModal()" class="btn-action" style="padding:6px 12px;font-size:11px;">+ New Member (QR)</button>
            </div>
        </div>

        <?php if (empty($athletes)): ?>
            <p style="text-align:center;padding:30px;color:var(--silver);font-size:13px;">Belum ada pelatih didaftarkan. Tekan butang 'New Member (QR)' di atas untuk mula mendaftar pelatih baharu.</p>
        <?php else: ?>
            <div class="table-responsive">
                <table>
                    <thead>
                        <tr>
                            <th>Nama Penuh</th>
                            <th>No. Kad Pengenalan</th>
                            <th>Umur / Lahir</th>
                            <th>Fizikal</th>
                            <th>Jantina</th>
                            <th>Sukan</th>
                            <th>Acara Latihan</th>
                            <th>PB (Personal Best)</th>
                            <th>Telefon</th>
                            <th>Tindakan</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($athletes as $ath): ?>
                            <tr>
                                <td><strong><?= htmlspecialchars($ath['name']) ?></strong></td>
                                <td><?= htmlspecialchars($ath['ic_number']) ?></td>
                                <td><?= $ath['age'] ?> thn (<?= htmlspecialchars($ath['dob']) ?>)</td>
                                <td><?= ($ath['height_cm'] > 0) ? $ath['height_cm'] . 'cm' : '-' ?> / <?= ($ath['weight_kg'] > 0) ? $ath['weight_kg'] . 'kg' : '-' ?></td>
                                <td><?= htmlspecialchars($ath['gender']) ?></td>
                                <td>
                                    <span class="tag-sport <?= ($ath['sport_type'] === 'Padang') ? 'tag-padang' : 'tag-balapan' ?>">
                                        <?= htmlspecialchars($ath['sport_type']) ?>
                                    </span>
                                </td>
                                <td><?= htmlspecialchars($ath['category']) ?></td>
                                <td style="font-family:'JetBrains Mono',monospace;color:var(--green);font-weight:bold;"><?= number_format($ath['pb_seconds'], 2) ?>s</td>
                                <td><?= htmlspecialchars($ath['phone']) ?></td>
                                <td>
                                    <form method="POST" onsubmit="return confirm('Adakah anda pasti ingin memadam <?= htmlspecialchars(addslashes($ath['name'])) ?>?');">
                                        <input type="hidden" name="action" value="delete_athlete">
                                        <input type="hidden" name="athlete_id" value="<?= $ath['id'] ?>">
                                        <button type="submit" class="btn-del">Padam</button>
                                    </form>
                                </td>
                            </tr>
                        <?php endforeach; ?>
                    </tbody>
                </table>
            </div>
        <?php endif; ?>
    </section>

    <!-- Sub-Coach Management Section -->
    <section class="card" id="section_sub_coaches">
        <div class="card-header">
            <h2>PENGURUSAN SUB-COACH (PENOLONG JURULATIH)</h2>
            <span style="font-size:11px;color:var(--silver);">1 Orang Percuma • Sub-member hanya boleh log masuk ke sistem anda</span>
        </div>

        <div style="background:#0A0A0A;border:1px solid var(--border);border-radius:10px;padding:16px;margin-bottom:16px;">
            <h4 style="font-size:12px;margin-bottom:8px;color:#FFF;">Daftar Penolong Jurulatih Baru (Automatik Log Masuk Tanpa Kelulusan Admin):</h4>
            <form method="POST">
                <input type="hidden" name="action" value="add_sub_coach">
                <div class="form-row">
                    <input type="text" name="sub_name" required placeholder="Nama Penuh Penolong">
                    <input type="text" name="sub_username" required placeholder="Username Login">
                    <input type="password" name="sub_password" required placeholder="Kata Laluan">
                    <input type="tel" name="sub_phone" placeholder="No. Telefon">
                </div>
                <div style="display:flex;justify-content:space-between;align-items:center;margin-top:8px;">
                    <span style="font-size:11px;color:var(--silver);">Penggunaan Slot: <strong><?= count($sub_coaches) ?> / <?= $current_coach['sub_coach_slots'] ?></strong></span>
                    <button type="submit" class="btn-action">+ Tambah Penolong Jurulatih</button>
                </div>
            </form>
        </div>

        <?php if (!empty($sub_coaches)): ?>
            <div class="table-responsive">
                <table>
                    <thead>
                        <tr>
                            <th>Nama Penolong</th>
                            <th>Username</th>
                            <th>No. Telefon</th>
                            <th>Akses Sistem</th>
                            <th>Tarikh Didaftarkan</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($sub_coaches as $sc): ?>
                            <tr>
                                <td><strong><?= htmlspecialchars($sc['full_name']) ?></strong></td>
                                <td style="font-family:'JetBrains Mono',monospace;color:var(--silver);"><?= htmlspecialchars($sc['username']) ?></td>
                                <td><?= htmlspecialchars($sc['phone']) ?></td>
                                <td><span style="color:var(--green);font-weight:bold;">Sistem Penuh Coach (Automatik)</span></td>
                                <td><?= htmlspecialchars($sc['created_at']) ?></td>
                            </tr>
                        <?php endforeach; ?>
                    </tbody>
                </table>
            </div>
        <?php endif; ?>
    </section>

    <!-- Recent Electronic Timing Records -->
    <section class="card">
        <div class="card-header">
            <h2>REKOD TERKINI ELECTRONIC TIMING (KAMERA MULA & PENAMAT)</h2>
            <span style="font-size:11px;color:var(--silver);">Segerak secara automatik dari Aplikasi Telefon</span>
        </div>
        <?php if (empty($recent_runs)): ?>
            <p style="text-align:center;padding:20px;color:var(--silver);font-size:12px;">Belum ada rekod larian disegerakkan. Buka Aplikasi Android dan tekan 'Segerak Data' di tab Portal Web.</p>
        <?php else: ?>
            <div class="table-responsive">
                <table>
                    <thead>
                        <tr>
                            <th>ID Sesi</th>
                            <th>Larian</th>
                            <th>Masa Rasmi</th>
                            <th>Kamera</th>
                            <th>Tarikh</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($recent_runs as $r): ?>
                            <tr>
                                <td style="font-family:'JetBrains Mono',monospace;"><?= htmlspecialchars($r['session_id']) ?></td>
                                <td>#<?= $r['run_number'] ?></td>
                                <td style="font-family:'JetBrains Mono',monospace;font-size:14px;color:var(--green);font-weight:bold;"><?= htmlspecialchars($r['formatted_time']) ?></td>
                                <td><?= htmlspecialchars($r['cam_type']) ?></td>
                                <td><?= htmlspecialchars($r['date_string']) ?></td>
                            </tr>
                        <?php endforeach; ?>
                    </tbody>
                </table>
            </div>
        <?php endif; ?>
    </section>

    <footer class="footer">
        Sistem Pengurusan Latihan & Olahraga © <strong>Psyco Time X Pro</strong><br>
        Sebarang pertanyaan, ralat atau penambahan: <a href="https://wa.me/60195326399">WhatsApp Admin Roger (+60195326399)</a>
    </footer>
</main>

<!-- Modal QR Code Pelatih -->
<div class="modal" id="modal_qr">
    <div class="modal-content">
        <h3 style="font-family:'JetBrains Mono',monospace;color:#FFF;font-size:16px;">IMBAS QR CODE PENDAFTARAN PELATIH</h3>
        <p style="font-size:12px;color:var(--silver);margin-top:6px;">Ibu bapa atau atlet boleh imbas kod QR ini di telefon untuk mengisi borang pendaftaran lengkap.</p>
        
        <div class="qr-frame">
            <!-- QR Code generated via quickchart / google chart api -->
            <img src="https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=<?= urlencode($qr_url) ?>" alt="QR Code Pendaftaran" style="display:block;width:180px;height:180px;">
        </div>

        <div style="background:#0A0A0A;border:1px solid var(--border);border-radius:6px;padding:8px;font-size:10px;font-family:'JetBrains Mono',monospace;color:var(--silver);word-break:break-all;">
            <?= htmlspecialchars($qr_url) ?>
        </div>

        <br>
        <button class="btn-close-modal" onclick="closeQrModal()">Tutup</button>
    </div>
</div>

<!-- Modal Resit Langganan -->
<div class="modal" id="modal_receipt">
    <div class="modal-content" style="text-align:left;">
        <h3 style="font-family:'JetBrains Mono',monospace;color:var(--racing-red);font-size:15px;text-align:center;">RESIT LANGGANAN RASMI</h3>
        <p style="font-size:11px;color:var(--silver);text-align:center;margin-bottom:14px;">Psyco Time X Pro Sport Management</p>
        
        <div style="background:#000;border:1px solid var(--border);border-radius:8px;padding:14px;font-size:11px;line-height:1.8;">
            <p><strong>No. Resit:</strong> <span style="font-family:'JetBrains Mono',monospace;">SUB-PTXP-<?= date('ym') ?>-<?= $coach_id ?></span></p>
            <p><strong>Jurulatih:</strong> <?= htmlspecialchars($current_coach['name']) ?></p>
            <p><strong>Emel Google:</strong> <?= htmlspecialchars($current_coach['email']) ?></p>
            <p><strong>Pakej:</strong> Coach Pro (Unlimited Pelatih Balapan & Padang)</p>
            <p><strong>Tarikh Bayaran:</strong> <?= date('d/m/Y') ?></p>
            <p><strong>Tarikh Matang Seterusnya:</strong> <span style="color:var(--green);font-weight:bold;"><?= $expiry_date_str ?></span></p>
            <hr style="border:0;border-top:1px solid var(--border);margin:8px 0;">
            <p style="display:flex;justify-content:space-between;font-size:13px;font-weight:bold;">
                <span>JUMLAH BESAR (TUNAI):</span>
                <span style="color:var(--green);">RM 30.00 (LUNAS)</span>
            </p>
            <p style="font-size:10px;color:#777;margin-top:6px;">Status: Disahkan oleh Admin Roger (+60195326399)</p>
        </div>

        <div style="text-align:center;margin-top:14px;">
            <a href="https://wa.me/60195326399?text=Salam%20Admin%20Roger,%20saya%20telah%20melihat%20resit%20langganan%20RM30" style="display:inline-block;padding:8px 16px;background:var(--green);color:#000;border-radius:6px;font-size:11px;font-weight:bold;text-decoration:none;">WhatsApp Roger</a>
            <button class="btn-close-modal" onclick="closeReceiptModal()" style="margin-left:8px;">Tutup</button>
        </div>
    </div>
</div>

<script>
    function openQrModal() { document.getElementById('modal_qr').classList.add('active'); }
    function closeQrModal() { document.getElementById('modal_qr').classList.remove('active'); }
    function openReceiptModal() { document.getElementById('modal_receipt').classList.add('active'); }
    function closeReceiptModal() { document.getElementById('modal_receipt').classList.remove('active'); }
</script>
</body>
</html>
