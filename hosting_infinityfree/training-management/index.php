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

$is_expired = false;
$days_left = 7;
$expiry_date_str = "7 Hari";
if ($current_coach) {
    if (!empty($current_coach['subscription_expires_at'])) {
        $expiry_ms = $current_coach['subscription_expires_at'];
        $now_ms = time() * 1000;
        if ($now_ms > $expiry_ms && !$is_admin) {
            $is_expired = true;
        }
        $days_left = max(0, ceil(($expiry_ms - $now_ms) / (24 * 3600 * 1000)));
        $expiry_date_str = date("d/m/Y", $expiry_ms / 1000);
    }
}

// Tindakan POST (Padam Pelatih, Kemaskini Pelatih, Tambah Sub-Coach, Kelulusan Admin, dll)
$alert_msg = "";
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? '';

    if ($action === 'delete_athlete') {
        if (!$is_admin) {
            $alert_msg = "Akses Terhad: Hanya Master Admin Roger dibenarkan memadam rekod atlit.";
        } else {
            $ath_id = intval($_POST['athlete_id'] ?? 0);
            $del = $pdo->prepare("DELETE FROM athletes WHERE id = ?");
            $del->execute([$ath_id]);
            $alert_msg = "Atlit berjaya dipadam daripada sistem oleh Master Admin.";
        }
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
    } elseif ($action === 'update_coach_profile') {
        $nickname = trim($_POST['nickname'] ?? '');
        $club_name = trim($_POST['club_name'] ?? '');
        $club_address = trim($_POST['club_address'] ?? '');
        $training_specialty = trim($_POST['training_specialty'] ?? '');
        $achievements = trim($_POST['achievements'] ?? '');
        $licenses = trim($_POST['licenses'] ?? '');
        $bio = trim($_POST['bio'] ?? '');
        $photo_uri = trim($_POST['profile_photo_uri'] ?? '');

        try {
            $up_prf = $pdo->prepare("UPDATE coaches SET nickname = ?, club_name = ?, club_address = ?, training_specialty = ?, achievements = ?, licenses = ?, bio = ?, profile_photo_uri = ? WHERE id = ?");
            $up_prf->execute([$nickname, $club_name, $club_address, $training_specialty, $achievements, $licenses, $bio, $photo_uri, $coach_id]);
            $alert_msg = "Profil Profesional Jurulatih berjaya dikemaskini!";
            // Refresh profil
            $stmt = $pdo->prepare("SELECT * FROM coaches WHERE id = ?");
            $stmt->execute([$coach_id]);
            $current_coach = $stmt->fetch();
        } catch (Exception $e) {
            $alert_msg = "Ralat kemaskini profil: " . $e->getMessage();
        }
    } elseif ($action === 'mark_inbox_read') {
        $msg_id = intval($_POST['message_id'] ?? 0);
        $pdo->prepare("UPDATE inbox_messages SET is_read = 1 WHERE id = ? AND coach_id = ?")->execute([$msg_id, $coach_id]);
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

// 7. Auto-Jana Laporan Bulanan 1hb untuk Coach jika belum ada bagi bulan semasa
$current_month_key = date("Y-m");
$current_month_name = date("F Y");
$check_report = $pdo->prepare("SELECT COUNT(*) FROM inbox_messages WHERE coach_id = ? AND message_type = 'MONTHLY_REPORT' AND title LIKE ?");
$check_report->execute([$coach_id, "%$current_month_name%"]);
if ($check_report->fetchColumn() == 0) {
    $report_title = "Laporan Bulanan Rasmi (1hb $current_month_name) - Pendaftaran & Yuran Atlit";
    $report_content = "Salam Coach " . (!empty($current_coach['nickname']) ? $current_coach['nickname'] : $current_coach['name']) . ",\n\n" .
        "Berikut ialah rumusan kemas kini automatik sistem bagi bulan $current_month_name:\n" .
        "• Jumlah Keseluruhan Atlit Berdaftar: $total_athletes orang\n" .
        "• Atlit Balapan: $balapan_count orang\n" .
        "• Atlit Padang: $padang_count orang\n" .
        "• Jumlah Penolong Jurulatih (Sub-Coach): " . count($sub_coaches) . " orang\n" .
        "• Status Langganan Sistem: " . ($days_left > 0 ? "AKTIF ($days_left hari lagi)" : "TAMAT TEMPOH") . "\n\n" .
        "Sila semak senarai bayaran yuran dan kehadiran atlit di portal atau aplikasi telefon untuk rekod audit kelab anda.";

    $ins_rep = $pdo->prepare("INSERT INTO inbox_messages (coach_id, sender_name, title, content, date_string, message_type, is_read, attached_document_title) VALUES (?, 'Sistem Psyco Time X Pro', ?, ?, ?, 'MONTHLY_REPORT', 0, ?)");
    $ins_rep->execute([$coach_id, $report_title, $report_content, date("01/m/Y"), "Laporan-Bulanan-$current_month_key.pdf"]);
}

// Dapatkan semua mesej inbox untuk coach ini
$stmt_inbox = $pdo->prepare("SELECT * FROM inbox_messages WHERE coach_id = ? ORDER BY id DESC");
$stmt_inbox->execute([$coach_id]);
$inbox_messages = $stmt_inbox->fetchAll();
$unread_inbox_count = 0;
foreach ($inbox_messages as $im) {
    if (!$im['is_read']) $unread_inbox_count++;
}

// Semak status kelengkapan profil coach
$needs_profile_reminder = (empty($current_coach['achievements']) || empty($current_coach['licenses']) || empty($current_coach['profile_photo_uri']));

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

        /* Modal QR & Windows */
        .modal { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.85); z-index: 200; align-items: center; justify-content: center; padding: 20px; overflow-y: auto; }
        .modal.active { display: flex; }
        .modal-content { background: var(--surface); border: 1px solid var(--border); border-radius: 16px; width: 100%; max-width: 600px; padding: 24px; text-align: center; max-height: 90vh; overflow-y: auto; }
        .qr-frame { background: #FFF; padding: 14px; border-radius: 12px; display: inline-block; margin: 16px 0; }
        .btn-close-modal { background: var(--surface-variant); color: #FFF; border: none; padding: 8px 16px; border-radius: 6px; font-size: 11px; font-weight: 700; cursor: pointer; margin-top: 12px; }

        /* Badge Count */
        .badge-count { background: var(--racing-red); color: #FFF; font-size: 10px; font-weight: 900; padding: 2px 7px; border-radius: 10px; margin-left: 4px; display: inline-block; }
        .reminder-banner { background: rgba(255, 215, 0, 0.12); border: 1px solid var(--gold); border-radius: 10px; padding: 14px 18px; margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 10px; }
        .reminder-banner h4 { color: var(--gold); font-size: 13px; font-weight: 800; font-family: 'JetBrains Mono', monospace; }
        .reminder-banner p { color: #FFF; font-size: 11px; margin-top: 3px; }

        /* Form Controls */
        .form-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 10px; margin-bottom: 10px; text-align: left; }
        input, select, textarea { width: 100%; padding: 10px; background: #0A0A0A; border: 1px solid var(--border); border-radius: 6px; color: #FFF; font-size: 12px; }
        textarea { resize: vertical; min-height: 70px; }
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
        <button onclick="openInboxModal()" class="btn-logout" style="border-color:var(--racing-red);color:#FFF;background:rgba(229,9,20,0.2);display:inline-flex;align-items:center;">
            📬 Peti Masuk (Inbox)<?php if ($unread_inbox_count > 0): ?><span class="badge-count"><?= $unread_inbox_count ?></span><?php endif; ?>
        </button>
        <button onclick="openProfileModal()" class="btn-logout" style="border-color:var(--silver);color:#FFF;background:rgba(192,192,192,0.15);">
            👤 Profil Coach
        </button>
        <div class="user-pill" onclick="openProfileModal()" style="cursor:pointer;" title="Klik untuk kemaskini profil">
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

    <?php if ($needs_profile_reminder): ?>
        <div class="reminder-banner">
            <div>
                <h4>⚠️ PERINGATAN: KEMASKINI PROFIL & SIJIL JURULATIH ANDA</h4>
                <p>Sila lengkapkan <strong>Pencapaian Coach</strong>, <strong>Lesen Kejurulatihan</strong> (cth: Sport Science Level 1, World Athletics Level 1), dan <strong>Gambar Profil</strong> agar akaun anda kelihatan profesional kepada atlit & ibu bapa.</p>
            </div>
            <button onclick="openProfileModal()" class="btn-action" style="padding:8px 16px;font-size:11px;background:var(--gold);color:#000;">Kemaskini Profil Sekarang</button>
        </div>
    <?php endif; ?>

    <?php if ($days_left <= 3 && !$is_admin && !$is_expired): ?>
        <div class="banner-warning">
            <div>
                <h4>PERINGATAN: LANGGANAN HAMPIR TAMAT</h4>
                <p>Baki masa anda: <strong><?= $days_left ?> hari</strong> (Tamat: <?= $expiry_date_str ?>). Kadar pembaharuan: RM30/bulan (Unlimited Atlit).</p>
            </div>
            <a href="https://wa.me/60195326399?text=Salam%20Admin%20Roger,%20saya%20ingin%20memperbaharui%20langganan%20RM30%20akaun%20<?= urlencode($current_coach['email']) ?>" class="btn-wa-renew">WhatsApp Roger (+60195326399)</a>
        </div>
    <?php endif; ?>

    <?php if ($is_expired): ?>
        <div style="background:var(--surface);border:2px solid var(--racing-red);border-radius:16px;padding:36px;text-align:center;max-width:600px;margin:30px auto;box-shadow:0 10px 40px rgba(0,0,0,0.8);">
            <div style="width:64px;height:64px;background:rgba(229,9,20,0.2);border:2px solid var(--racing-red);border-radius:50%;display:inline-flex;align-items:center;justify-content:center;font-size:28px;margin-bottom:16px;">🔒</div>
            <h2 style="color:var(--racing-red);font-family:'JetBrains Mono',monospace;font-size:18px;">AKSES SISTEM DIKUNCI</h2>
            <p style="color:#FFF;font-size:13px;font-weight:bold;margin:10px 0;">Tempoh Percubaan 7 Hari / Langganan Anda Telah Tamat</p>
            <p style="color:var(--silver);font-size:12px;margin-bottom:20px;">Sistem Psyco Time X Pro dikunci sehingga pembaharuan disahkan oleh Master Admin Roger. Sila hubungi Admin Roger untuk mengaktifkan semula akaun anda (RM30 / sebulan).</p>
            <a href="https://wa.me/60195326399?text=Salam%20Admin%20Roger,%20saya%20ingin%20memperbaharui%20langganan%20Psyco%20Time%20X%20Pro%20RM30%20akaun%20<?= urlencode($current_coach['email']) ?>" style="display:inline-block;background:var(--green);color:#000;padding:12px 24px;border-radius:8px;font-weight:900;text-decoration:none;font-size:13px;margin-bottom:12px;">
                WhatsApp Admin Roger (+60195326399)
            </a><br>
            <a href="login.php?logout=1" style="color:var(--silver);font-size:11px;text-decoration:none;">Log Keluar</a>
        </div>
    </main>
    </body>
    </html>
    <?php exit; endif; ?>

    <!-- Navigation Hub to All Pages / Options -->
    <div class="action-bar">
        <a href="ranking.php" class="action-btn" style="border-color:var(--gold);background:rgba(255,215,0,0.08);">
            <h3 style="color:var(--gold);">🏆 Carta Ranking Atlit</h3>
            <p>Lihat kedudukan PB Terkini vs PB Lama atlit</p>
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
            <h3>🏃 Senarai Atlit (<?= $total_athletes ?>)</h3>
            <p>Balapan (<?= $balapan_count ?>) • Padang (<?= $padang_count ?>)</p>
        </a>
        <a href="#section_sub_coaches" class="action-btn">
            <h3>👥 Sub-Coach (<?= count($sub_coaches) ?> / <?= $current_coach['sub_coach_slots'] ?>)</h3>
            <p>1 Slot Percuma • +3 Slot RM10/bln</p>
        </a>
        <div class="action-btn" onclick="openInboxModal()" style="border-color:var(--racing-red);background:rgba(229,9,20,0.08);">
            <h3 style="color:var(--racing-red);">📬 Peti Masuk (Inbox) <?php if ($unread_inbox_count > 0): ?><span class="badge-count"><?= $unread_inbox_count ?> Baru</span><?php endif; ?></h3>
            <p>Laporan Bulanan 1hb & Dokumen Rasmi Admin</p>
        </div>
        <div class="action-btn" onclick="openProfileModal()" style="border-color:var(--silver);background:rgba(192,192,192,0.08);">
            <h3 style="color:var(--silver);">👤 Profil Coach Profesional</h3>
            <p>Pencapaian, Lesen Sukan & Gambar Profil</p>
        </div>
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
            <div class="label">Jumlah Atlit</div>
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
            <h2>SENARAI ATLIT JURULATIH (BALAPAN & PADANG)</h2>
            <div class="filter-tabs">
                <a href="?sport=Semua" class="filter-tab <?= ($sport_filter === 'Semua') ? 'active' : '' ?>">Semua (<?= $total_athletes ?>)</a>
                <a href="?sport=Balapan" class="filter-tab <?= ($sport_filter === 'Balapan') ? 'active' : '' ?>">Balapan (<?= $balapan_count ?>)</a>
                <a href="?sport=Padang" class="filter-tab <?= ($sport_filter === 'Padang') ? 'active' : '' ?>">Padang (<?= $padang_count ?>)</a>
                <button type="button" onclick="printBulkSelectedAthletes()" class="btn-action" style="padding:6px 12px;font-size:11px;background:var(--gold);color:#000;">
                    🖨️ Cetak / PDF Pukal (<span id="selectedCountDisplay">0</span>)
                </button>
                <button onclick="openQrModal()" class="btn-action" style="padding:6px 12px;font-size:11px;">+ New Member (QR)</button>
            </div>
        </div>

        <?php if (empty($athletes)): ?>
            <p style="text-align:center;padding:30px;color:var(--silver);font-size:13px;">Belum ada atlit didaftarkan. Tekan butang 'New Member (QR)' di atas untuk mula mendaftar atlit baharu.</p>
        <?php else: ?>
            <div class="table-responsive">
                <table>
                    <thead>
                        <tr>
                            <th style="width:36px;text-align:center;">
                                <input type="checkbox" id="selectAllCheckbox" onchange="toggleSelectAllAthletes(this)" style="cursor:pointer;width:16px;height:16px;">
                            </th>
                            <th>Gambar</th>
                            <th>Nama Penuh</th>
                            <th>No. Kad Pengenalan</th>
                            <th>Umur / Lahir</th>
                            <th>Fizikal</th>
                            <th>Jantina</th>
                            <th>Sukan</th>
                            <th>Acara Latihan</th>
                            <th>PB (Personal Best)</th>
                            <th>Telefon</th>
                            <th>Cetak / PDF</th>
                            <?php if ($is_admin): ?><th>Tindakan (Admin)</th><?php endif; ?>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($athletes as $ath): ?>
                            <tr>
                                <td style="text-align:center;">
                                    <input type="checkbox" class="athlete-checkbox" value="<?= $ath['id'] ?>" onchange="updateSelectedAthleteCount()" style="cursor:pointer;width:16px;height:16px;">
                                </td>
                                <td>
                                    <?php if (!empty($ath['photo_uri'])): ?>
                                        <img src="<?= htmlspecialchars($ath['photo_uri']) ?>" alt="Atlit" style="width:36px;height:36px;border-radius:50%;object-fit:cover;border:1.5px solid var(--racing-red);">
                                    <?php else: ?>
                                        <div style="width:36px;height:36px;border-radius:50%;background:rgba(229,9,20,0.2);display:flex;align-items:center;justify-content:center;color:var(--racing-red);font-weight:bold;font-size:12px;">🏃</div>
                                    <?php endif; ?>
                                </td>
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
                                    <button type="button" onclick="printSingleAthlete(<?= $ath['id'] ?>)" class="btn-action" style="padding:4px 8px;font-size:10px;background:var(--surface-variant);border:1px solid var(--border);">
                                        📄 PDF
                                    </button>
                                </td>
                                <?php if ($is_admin): ?>
                                <td>
                                    <form method="POST" onsubmit="return confirm('Adakah anda pasti ingin memadam <?= htmlspecialchars(addslashes($ath['name'])) ?>?');">
                                        <input type="hidden" name="action" value="delete_athlete">
                                        <input type="hidden" name="athlete_id" value="<?= $ath['id'] ?>">
                                        <button type="submit" class="btn-del">Padam</button>
                                    </form>
                                </td>
                                <?php endif; ?>
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

<!-- Modal QR Code Atlit -->
<div class="modal" id="modal_qr">
    <div class="modal-content">
        <h3 style="font-family:'JetBrains Mono',monospace;color:#FFF;font-size:16px;">IMBAS QR CODE PENDAFTARAN ATLIT</h3>
        <p style="font-size:12px;color:var(--silver);margin-top:6px;">Ibu bapa atau atlit boleh imbas kod QR ini di telefon untuk mengisi borang pendaftaran lengkap.</p>
        
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
            <p><strong>Pakej:</strong> Coach Pro (Unlimited Atlit Balapan & Padang)</p>
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

<!-- Modal Peti Masuk (Inbox) Coach -->
<div class="modal" id="modal_inbox">
    <div class="modal-content" style="text-align:left;max-width:680px;">
        <div style="display:flex;justify-content:space-between;align-items:center;border-bottom:1px solid var(--border);padding-bottom:12px;margin-bottom:14px;">
            <div>
                <h3 style="font-family:'JetBrains Mono',monospace;color:#FFF;font-size:16px;">📬 PETI MASUK & LAPORAN BULANAN</h3>
                <p style="font-size:11px;color:var(--silver);">Laporan Auto 1hb Setiap Bulan & Memo Rasmi Admin Roger</p>
            </div>
            <button class="btn-close-modal" onclick="closeInboxModal()" style="margin:0;padding:6px 12px;">✕</button>
        </div>

        <?php if (empty($inbox_messages)): ?>
            <p style="text-align:center;padding:24px;color:var(--silver);font-size:12px;">Peti masuk anda kosong. Laporan auto 1hb akan muncul di sini setiap awal bulan.</p>
        <?php else: ?>
            <div style="display:flex;flex-direction:column;gap:12px;">
                <?php foreach ($inbox_messages as $msg): ?>
                    <div style="background:#000;border:1px solid <?= ($msg['message_type'] === 'MONTHLY_REPORT') ? 'var(--gold)' : 'var(--border)' ?>;border-radius:10px;padding:14px;">
                        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
                            <span style="font-size:10px;font-weight:bold;padding:2px 8px;border-radius:4px;background:<?= ($msg['message_type'] === 'MONTHLY_REPORT') ? 'rgba(255,215,0,0.15)' : 'rgba(0,230,118,0.15)' ?>;color:<?= ($msg['message_type'] === 'MONTHLY_REPORT') ? 'var(--gold)' : 'var(--green)' ?>;">
                                <?= ($msg['message_type'] === 'MONTHLY_REPORT') ? 'LAPORAN BULANAN 1HB' : 'MEMO PENTADBIR' ?>
                            </span>
                            <span style="font-size:11px;color:var(--silver);font-family:'JetBrains Mono',monospace;"><?= htmlspecialchars($msg['date_string']) ?></span>
                        </div>
                        <h4 style="font-size:13px;color:#FFF;font-weight:800;margin-bottom:4px;"><?= htmlspecialchars($msg['title']) ?></h4>
                        <p style="font-size:10px;color:var(--silver);margin-bottom:8px;">Daripada: <strong><?= htmlspecialchars($msg['sender_name']) ?></strong></p>
                        <div style="background:var(--surface);border:1px solid var(--border);border-radius:6px;padding:10px;font-size:11.5px;color:#DDD;white-space:pre-line;line-height:1.6;">
                            <?= htmlspecialchars($msg['content']) ?>
                        </div>
                        <?php if (!empty($msg['attached_document_title'])): ?>
                            <div style="margin-top:10px;display:flex;justify-content:space-between;align-items:center;background:rgba(0,230,118,0.08);border:1px dashed var(--green);border-radius:6px;padding:8px 12px;">
                                <span style="font-size:11px;color:var(--green);font-weight:bold;">📎 Dokumen: <?= htmlspecialchars($msg['attached_document_title']) ?></span>
                                <button type="button" onclick="printMonthlyReport('<?= htmlspecialchars(addslashes($msg['title'])) ?>', `<?= htmlspecialchars(addslashes($msg['content'])) ?>`, '<?= htmlspecialchars($msg['date_string']) ?>')" class="btn-action" style="padding:4px 10px;font-size:10px;background:var(--green);color:#000;">
                                    🖨️ Cetak / PDF
                                </button>
                            </div>
                        <?php endif; ?>
                    </div>
                <?php endforeach; ?>
            </div>
        <?php endif; ?>

        <div style="text-align:right;margin-top:14px;">
            <button class="btn-close-modal" onclick="closeInboxModal()">Tutup</button>
        </div>
    </div>
</div>

<!-- Modal Profil Coach Profesional -->
<div class="modal" id="modal_profile">
    <div class="modal-content" style="text-align:left;max-width:640px;">
        <div style="display:flex;justify-content:space-between;align-items:center;border-bottom:1px solid var(--border);padding-bottom:12px;margin-bottom:14px;">
            <div>
                <h3 style="font-family:'JetBrains Mono',monospace;color:var(--racing-red);font-size:16px;">👤 PROFIL PROFESIONAL JURULATIH</h3>
                <p style="font-size:11px;color:var(--silver);">Kemaskini Sijil, Lesen, Pencapaian & Gambar Rasmi</p>
            </div>
            <button class="btn-close-modal" onclick="closeProfileModal()" style="margin:0;padding:6px 12px;">✕</button>
        </div>

        <form method="POST">
            <input type="hidden" name="action" value="update_coach_profile">

            <div style="text-align:center;margin-bottom:16px;">
                <div style="width:72px;height:72px;border-radius:50%;border:2px solid var(--racing-red);margin:0 auto 8px;overflow:hidden;background:#000;display:flex;align-items:center;justify-content:center;">
                    <?php if (!empty($current_coach['profile_photo_uri'])): ?>
                        <img src="<?= htmlspecialchars($current_coach['profile_photo_uri']) ?>" alt="Coach" style="width:100%;height:100%;object-fit:cover;">
                    <?php else: ?>
                        <span style="font-size:28px;">🏃‍♂️</span>
                    <?php endif; ?>
                </div>
                <label style="font-size:11px;color:var(--silver);">URL Gambar Profil / Avatar:</label>
                <input type="text" name="profile_photo_uri" value="<?= htmlspecialchars($current_coach['profile_photo_uri'] ?? '') ?>" placeholder="https://... atau data:image/..." style="margin-top:4px;">
            </div>

            <div class="form-row">
                <div>
                    <label>Nama Penuh (Seperti IC):</label>
                    <input type="text" value="<?= htmlspecialchars($current_coach['name']) ?>" disabled style="opacity:0.7;">
                </div>
                <div>
                    <label>Gelaran / Nick Name Coach:</label>
                    <input type="text" name="nickname" value="<?= htmlspecialchars($current_coach['nickname'] ?? '') ?>" placeholder="cth: Coach Roger">
                </div>
            </div>

            <div class="form-row">
                <div>
                    <label>Nama Kelab / Akademi:</label>
                    <input type="text" name="club_name" value="<?= htmlspecialchars($current_coach['club_name'] ?? '') ?>" placeholder="cth: Kelab Olahraga Sabah">
                </div>
                <div>
                    <label>Pengkhususan Latihan:</label>
                    <input type="text" name="training_specialty" value="<?= htmlspecialchars($current_coach['training_specialty'] ?? 'Balapan & Padang') ?>" placeholder="cth: 100m, 200m, Lompat Jauh">
                </div>
            </div>

            <div style="margin-bottom:10px;">
                <label>Lokasi / Alamat Latihan:</label>
                <input type="text" name="club_address" value="<?= htmlspecialchars($current_coach['club_address'] ?? '') ?>" placeholder="cth: Kompleks Sukan Keningau / Stadium Likas">
            </div>

            <div style="margin-bottom:10px;">
                <label style="color:var(--gold);font-weight:bold;">🎖️ Pencapaian Jurulatih (Achievements):</label>
                <textarea name="achievements" placeholder="Contoh: Jurulatih Pecut MSSM Negeri, Melahirkan Pelari Emas 100m Sukma 2024, Johan Terbuka Malaysia"><?= htmlspecialchars($current_coach['achievements'] ?? '') ?></textarea>
            </div>

            <div style="margin-bottom:10px;">
                <label style="color:var(--green);font-weight:bold;">📜 Lesen & Sijil Kejurulatihan (Licenses):</label>
                <textarea name="licenses" placeholder="Contoh: Sport Science Level 1, Sport Science Level 2, World Athletics Level 1 Youth & Sprints, CPR First Aid"><?= htmlspecialchars($current_coach['licenses'] ?? '') ?></textarea>
            </div>

            <div style="margin-bottom:14px;">
                <label>Bio / Latar Belakang Kejurulatihan:</label>
                <textarea name="bio" placeholder="Penerangan ringkas mengenai falsafah latihan anda..."><?= htmlspecialchars($current_coach['bio'] ?? '') ?></textarea>
            </div>

            <div style="display:flex;justify-content:flex-end;gap:8px;">
                <button type="button" class="btn-close-modal" onclick="closeProfileModal()">Batal</button>
                <button type="submit" class="btn-action">Simpan Profil Profesional</button>
            </div>
        </form>
    </div>
</div>

<script>
    const athletesData = <?= json_encode($athletes) ?>;
    const coachInfo = {
        name: <?= json_encode($current_coach['name']) ?>,
        nickname: <?= json_encode($current_coach['nickname']) ?>,
        club: <?= json_encode($current_coach['club_name']) ?>,
        phone: <?= json_encode($current_coach['phone']) ?>,
        email: <?= json_encode($current_coach['email']) ?>
    };

    function openQrModal() { document.getElementById('modal_qr').classList.add('active'); }
    function closeQrModal() { document.getElementById('modal_qr').classList.remove('active'); }
    function openReceiptModal() { document.getElementById('modal_receipt').classList.add('active'); }
    function closeReceiptModal() { document.getElementById('modal_receipt').classList.remove('active'); }
    function openInboxModal() { document.getElementById('modal_inbox').classList.add('active'); }
    function closeInboxModal() { document.getElementById('modal_inbox').classList.remove('active'); }
    function openProfileModal() { document.getElementById('modal_profile').classList.add('active'); }
    function closeProfileModal() { document.getElementById('modal_profile').classList.remove('active'); }

    function toggleSelectAllAthletes(master) {
        const boxes = document.querySelectorAll('.athlete-checkbox');
        boxes.forEach(b => b.checked = master.checked);
        updateSelectedAthleteCount();
    }

    function updateSelectedAthleteCount() {
        const checked = document.querySelectorAll('.athlete-checkbox:checked');
        const display = document.getElementById('selectedCountDisplay');
        if (display) display.innerText = checked.length;
    }

    function printSingleAthlete(athId) {
        const ath = athletesData.find(a => a.id == athId);
        if (!ath) return;
        generateAthletePdfWindow([ath], "Profil Rasmi Atlit - " + ath.name);
    }

    function printBulkSelectedAthletes() {
        const checked = document.querySelectorAll('.athlete-checkbox:checked');
        if (checked.length === 0) {
            alert("Sila tanda (tick) sekurang-kurangnya seorang atlit di dalam jadual untuk muat turun / cetak PDF pukal!");
            return;
        }
        const selectedIds = Array.from(checked).map(c => parseInt(c.value));
        const selectedAthletes = athletesData.filter(a => selectedIds.includes(parseInt(a.id)));
        generateAthletePdfWindow(selectedAthletes, "Direktori Rasmi Atlit (Pukal " + selectedAthletes.length + " Orang)");
    }

    function generateAthletePdfWindow(athList, title) {
        const win = window.open('', '_blank');
        let html = `
        <!DOCTYPE html>
        <html>
        <head>
            <title>${title}</title>
            <style>
                body { font-family: 'Arial', sans-serif; color: #111; margin: 20px; font-size: 11pt; }
                .header { text-align: center; border-bottom: 2px solid #E50914; padding-bottom: 10px; margin-bottom: 20px; }
                .header h1 { margin: 0; color: #E50914; font-size: 18pt; letter-spacing: 1px; }
                .header p { margin: 4px 0 0; color: #555; font-size: 10pt; }
                .card { border: 1px solid #CCC; border-radius: 8px; padding: 16px; margin-bottom: 18px; page-break-inside: avoid; background: #FAFAFA; }
                .card-header { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #EEE; padding-bottom: 10px; margin-bottom: 12px; }
                .card-title { font-size: 14pt; font-weight: bold; color: #000; }
                .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px 16px; font-size: 10pt; }
                .grid strong { color: #333; }
                .footer { text-align: center; font-size: 9pt; color: #777; margin-top: 30px; border-top: 1px solid #DDD; padding-top: 10px; }
                @media print {
                    .no-print { display: none; }
                    body { margin: 0; }
                }
            </style>
        </head>
        <body>
            <div class="no-print" style="margin-bottom: 15px; text-align: right;">
                <button onclick="window.print()" style="padding: 10px 20px; background: #E50914; color: #FFF; border: none; border-radius: 6px; font-weight: bold; cursor: pointer; font-size: 12pt;">
                    🖨️ Cetak / Simpan sebagai PDF
                </button>
            </div>
            <div class="header">
                <h1>PSYCO TIME X PRO - SPORT TRAINING MANAGEMENT</h1>
                <p>Dokumen Laporan & Direktori Pendaftaran Atlit Rasmi</p>
                <p>Jurulatih: <strong>${coachInfo.nickname || coachInfo.name}</strong> • Kelab: <strong>${coachInfo.club || "Pusat Latihan"}</strong> • Tarikh: ${new Date().toLocaleDateString('ms-MY')}</p>
            </div>
        `;

        athList.forEach((ath, idx) => {
            html += `
            <div class="card">
                <div class="card-header">
                    <span class="card-title">${idx + 1}. ${ath.name}</span>
                    <span style="font-weight:bold;color:#E50914;border:1px solid #E50914;padding:3px 8px;border-radius:4px;font-size:9pt;">${ath.sport_type} • ${ath.category}</span>
                </div>
                <div class="grid">
                    <div><strong>No. Kad Pengenalan:</strong> ${ath.ic_number || '-'}</div>
                    <div><strong>Umur / Tarikh Lahir:</strong> ${ath.age} Tahun (${ath.dob || '-'})</div>
                    <div><strong>Jantina:</strong> ${ath.gender || 'Lelaki'}</div>
                    <div><strong>Tinggi & Berat:</strong> ${ath.height_cm > 0 ? ath.height_cm + 'cm' : '-'} / ${ath.weight_kg > 0 ? ath.weight_kg + 'kg' : '-'}</div>
                    <div><strong>Catatan Peribadi Terbaik (PB):</strong> <span style="font-weight:bold;color:green;">${parseFloat(ath.pb_seconds).toFixed(2)}s</span> (Lama: ${parseFloat(ath.previous_pb_seconds || ath.pb_seconds).toFixed(2)}s)</div>
                    <div><strong>No. Telefon / Penjaga:</strong> ${ath.phone || '-'}</div>
                    <div><strong>Yuran Bulanan:</strong> RM ${parseFloat(ath.monthly_fee || 60).toFixed(2)}</div>
                    <div><strong>Tarikh Matang Yuran:</strong> ${ath.fee_due_date || '-'}</div>
                </div>
                ${ath.notes ? `<div style="margin-top:10px;font-size:9.5pt;background:#FFF;padding:8px;border:1px solid #DDD;border-radius:4px;"><strong>Nota / Catatan Jurulatih:</strong> ${ath.notes}</div>` : ''}
            </div>
            `;
        });

        html += `
            <div class="footer">
                Dokumen ini dijana secara rasmi melalui Portal Pengurusan Latihan Psyco Time X Pro.<br>
                Sahkan sebarang perubahan bersama Master Admin Roger (+60195326399).
            </div>
        </body>
        </html>
        `;

        win.document.write(html);
        win.document.close();
    }

    function printMonthlyReport(title, content, dateStr) {
        const win = window.open('', '_blank');
        const html = `
        <!DOCTYPE html>
        <html>
        <head>
            <title>${title}</title>
            <style>
                body { font-family: 'Arial', sans-serif; color: #111; margin: 30px; line-height: 1.6; }
                .header { text-align: center; border-bottom: 2px solid #E50914; padding-bottom: 12px; margin-bottom: 20px; }
                .header h1 { margin: 0; color: #E50914; font-size: 18pt; }
                .box { border: 1px solid #DDD; border-radius: 8px; padding: 20px; background: #F9F9F9; white-space: pre-line; font-size: 11pt; }
                @media print { .no-print { display: none; } }
            </style>
        </head>
        <body>
            <div class="no-print" style="margin-bottom: 15px; text-align: right;">
                <button onclick="window.print()" style="padding: 10px 20px; background: #E50914; color: #FFF; border: none; border-radius: 6px; font-weight: bold; cursor: pointer;">
                    🖨️ Cetak / Simpan PDF
                </button>
            </div>
            <div class="header">
                <h1>PSYCO TIME X PRO - LAPORAN BULANAN RASMI</h1>
                <p>Jurulatih: <strong>${coachInfo.nickname || coachInfo.name}</strong> • Tarikh: <strong>${dateStr}</strong></p>
            </div>
            <div class="box">
                <h2>${title}</h2>
                <hr style="border:0;border-top:1px solid #DDD;margin:10px 0;">
                ${content}
            </div>
            <div style="text-align:center;font-size:9pt;color:#777;margin-top:30px;">
                Dijana secara automatik pada 1hb setiap bulan oleh Sistem Pengurusan Latihan Psyco Time X Pro.
            </div>
        </body>
        </html>
        `;
        win.document.write(html);
        win.document.close();
    }
</script>
</body>
</html>
