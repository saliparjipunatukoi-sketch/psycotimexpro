<?php
// hosting_infinityfree/training-management/admin.php
// Panel Kawalan Eksklusif Master Admin (Roger - Saliparjipun.atukoi@gmail.com)
session_start();
require_once __DIR__ . '/db_connect.php';
require_once __DIR__ . '/mailer.php';

// Pastikan hanya Master Admin Roger dibenarkan masuk
if (!isset($_SESSION['user_type']) || $_SESSION['user_type'] !== 'coach') {
    header("Location: login.php");
    exit;
}

$admin_id = $_SESSION['coach_id'];
$checkAdmin = $pdo->prepare("SELECT * FROM coaches WHERE id = ? AND email = 'Saliparjipun.atukoi@gmail.com'");
$checkAdmin->execute([$admin_id]);
$current_admin = $checkAdmin->fetch(PDO::FETCH_ASSOC);

if (!$current_admin) {
    header("Location: index.php?error=" . urlencode("Akses ditolak: Hanya Master Admin Roger dibenarkan ke halaman ini."));
    exit;
}

$alert_msg = "";
$error_msg = "";

// PROSES TINDAKAN ADMIN
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? '';

    // 1. Luluskan / Tambah Hari Langganan
    if ($action === 'extend_subscription') {
        $target_id = (int)$_POST['target_id'];
        $days = (int)$_POST['days'];
        
        $cStmt = $pdo->prepare("SELECT * FROM coaches WHERE id = ?");
        $cStmt->execute([$target_id]);
        $targetCoach = $cStmt->fetch(PDO::FETCH_ASSOC);

        if ($targetCoach) {
            $base_time = max(time() * 1000, $targetCoach['subscription_expires_at']);
            $new_expiry = $base_time + ($days * 24 * 3600 * 1000);
            
            $up = $pdo->prepare("UPDATE coaches SET is_approved = 1, subscription_status = 'ACTIVE', subscription_expires_at = ? WHERE id = ?");
            $up->execute([$new_expiry, $target_id]);

            // Cipta resit pembayaran jika langganan bulanan RM30
            if ($days >= 30) {
                $receipt_no = "SUB-PTXP-" . date('ym') . "-" . rand(100, 999);
                $p_date = date('d/m/Y');
                $next_due = date('d/m/Y', $new_expiry / 1000);
                
                $rIns = $pdo->prepare("INSERT INTO subscription_receipts (coach_id, receipt_no, amount, payment_date, next_payment_due, payment_method) VALUES (?, ?, 30.00, ?, ?, 'Tunai / Online Transfer Disahkan Admin')");
                $rIns->execute([$target_id, $receipt_no, $p_date, $next_due]);
            }

            $alert_msg = "Akaun {$targetCoach['name']} berjaya diluluskan / ditambah {$days} hari.";
        }
    }

    // 2. Tambah Slot Sub-Coach
    elseif ($action === 'update_slots') {
        $target_id = (int)$_POST['target_id'];
        $slots = (int)$_POST['slots'];
        $up = $pdo->prepare("UPDATE coaches SET sub_coach_slots = ? WHERE id = ?");
        $up->execute([$slots, $target_id]);
        $alert_msg = "Slot Sub-Coach berjaya dikemas kini kepada $slots slot.";
    }

    // 3. Edit Butiran Coach
    elseif ($action === 'edit_coach') {
        $target_id = (int)$_POST['target_id'];
        $name = trim($_POST['name']);
        $nickname = trim($_POST['nickname']);
        $club_name = trim($_POST['club_name']);
        $club_address = trim($_POST['club_address']);
        $training_specialty = trim($_POST['training_specialty']);
        $phone = trim($_POST['phone']);

        $up = $pdo->prepare("UPDATE coaches SET name = ?, nickname = ?, club_name = ?, club_address = ?, training_specialty = ?, phone = ? WHERE id = ?");
        $up->execute([$name, $nickname, $club_name, $club_address, $training_specialty, $phone, $target_id]);
        $alert_msg = "Maklumat profil jurulatih & kelab berjaya dikemas kini.";
    }

    // 4. Padam Coach (Delete Coach)
    elseif ($action === 'delete_coach') {
        $target_id = (int)$_POST['target_id'];
        if ($target_id === 1) {
            $error_msg = "Master Admin tidak boleh dipadam!";
        } else {
            $del = $pdo->prepare("DELETE FROM coaches WHERE id = ?");
            $del->execute([$target_id]);
            $alert_msg = "Akaun jurulatih berjaya dipadam dari sistem.";
        }
    }

    // 5. Hantar Emel Notifikasi Peringatan Tamat Langganan
    elseif ($action === 'send_expiry_email') {
        $target_id = (int)$_POST['target_id'];
        $cStmt = $pdo->prepare("SELECT * FROM coaches WHERE id = ?");
        $cStmt->execute([$target_id]);
        $targetCoach = $cStmt->fetch(PDO::FETCH_ASSOC);

        if ($targetCoach) {
            $days_left = max(0, ceil(($targetCoach['subscription_expires_at'] - (time() * 1000)) / (24 * 3600 * 1000)));
            $exp_str = date('d/m/Y', $targetCoach['subscription_expires_at'] / 1000);
            sendSubscriptionExpiryNotice($targetCoach['email'], $targetCoach['name'], $targetCoach['club_name'], $days_left, $exp_str);
            $alert_msg = "Emel notifikasi peringatan tamat langganan berjaya dihantar ke {$targetCoach['email']}.";
        }
    }
}

// Ambil semua senarai jurulatih
$all_coaches = $pdo->query("SELECT * FROM coaches ORDER BY id ASC")->fetchAll(PDO::FETCH_ASSOC);
$total_athletes = $pdo->query("SELECT COUNT(*) FROM athletes")->fetchColumn();
$total_receipts = $pdo->query("SELECT COUNT(*) FROM subscription_receipts")->fetchColumn();
?>
<!DOCTYPE html>
<html lang="ms">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Master Admin Portal - Psyco Time X Pro</title>
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
        body { background: #0F0F11; color: #FFF; line-height: 1.6; padding: 16px 20px 60px; }
        .container { max-width: 1240px; margin: 0 auto; }

        .topbar { display: flex; align-items: center; justify-content: space-between; border-bottom: 2px solid var(--gold); padding-bottom: 14px; margin-bottom: 20px; flex-wrap: wrap; gap: 10px; }
        .brand { display: flex; align-items: center; gap: 12px; }
        .logo-badge { width: 48px; height: 48px; background: #000; border: 2.5px solid var(--gold); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-family: 'JetBrains Mono', monospace; font-size: 18px; font-weight: 900; color: var(--gold); }
        h1 { font-family: 'JetBrains Mono', monospace; font-size: 19px; font-weight: 900; color: var(--gold); }
        .sub { font-size: 11px; color: var(--silver); }

        .alert-success { background: rgba(0, 230, 118, 0.15); border: 1px solid var(--green); color: var(--green); padding: 12px; border-radius: 8px; margin-bottom: 16px; font-weight: bold; font-size: 12px; }
        .alert-danger { background: rgba(229, 9, 20, 0.15); border: 1px solid var(--racing-red); color: var(--racing-red); padding: 12px; border-radius: 8px; margin-bottom: 16px; font-weight: bold; font-size: 12px; }

        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 12px; margin-bottom: 24px; }
        .stat-card { background: var(--surface); border: 1px solid var(--border); border-radius: 12px; padding: 16px; text-align: center; }
        .stat-card .num { font-family: 'JetBrains Mono', monospace; font-size: 26px; font-weight: 900; color: var(--gold); }
        .stat-card .label { font-size: 11px; color: var(--silver); margin-top: 4px; font-weight: 700; text-transform: uppercase; }

        .table-card { background: var(--surface); border: 1px solid var(--border); border-radius: 14px; overflow: hidden; margin-bottom: 24px; box-shadow: 0 10px 30px rgba(0,0,0,0.8); }
        .table-header { padding: 16px 20px; border-bottom: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center; }
        .table-header h2 { font-family: 'JetBrains Mono', monospace; font-size: 15px; color: #FFF; }
        .table-responsive { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; font-size: 12px; text-align: left; }
        th { background: #000; color: var(--silver); padding: 12px 14px; font-size: 10px; text-transform: uppercase; font-family: 'JetBrains Mono', monospace; border-bottom: 1px solid var(--border); }
        td { padding: 12px 14px; border-bottom: 1px solid var(--border); vertical-align: top; }
        tr:hover td { background: rgba(255,255,255,0.02); }

        .btn-action { background: var(--surface-variant); border: 1px solid var(--border); color: #FFF; padding: 5px 10px; border-radius: 5px; font-size: 10px; font-weight: 700; cursor: pointer; text-decoration: none; font-family: 'JetBrains Mono', monospace; }
        .btn-action:hover { border-color: var(--gold); }
        .btn-action.gold { background: var(--gold); color: #000; border-color: var(--gold); }
        .btn-action.green { background: var(--green); color: #000; border-color: var(--green); }
        .btn-action.red { background: var(--racing-red); color: #FFF; border-color: var(--racing-red); }

        .modal { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.85); z-index: 999; align-items: center; justify-content: center; padding: 20px; }
        .modal.active { display: flex; }
        .modal-content { background: var(--surface); border: 1px solid var(--border); border-radius: 14px; width: 100%; max-width: 500px; padding: 24px; }
        .form-group { margin-bottom: 12px; }
        label { display: block; font-size: 11px; font-weight: 700; color: var(--silver); margin-bottom: 4px; text-transform: uppercase; }
        input { width: 100%; padding: 10px; background: #0A0A0A; border: 1px solid var(--border); border-radius: 6px; color: #FFF; font-size: 12px; }
    </style>
</head>
<body>

<div class="container">
    <div class="topbar">
        <div class="brand">
            <div class="logo-badge">ROGER</div>
            <div>
                <h1>PANEL KAWALAN MASTER ADMIN</h1>
                <p class="sub">Log Masuk: <strong>Saliparjipun.atukoi@gmail.com</strong> (Master Admin Roger)</p>
            </div>
        </div>
        <div style="display:flex;gap:8px;">
            <a href="index.php" class="btn-action">🖥️ Papan Pemuka Latihan</a>
            <a href="ranking.php" class="btn-action">🏆 Carta Ranking</a>
            <a href="login.php?logout=1" class="btn-action red">Log Keluar</a>
        </div>
    </div>

    <?php if (!empty($alert_msg)): ?>
        <div class="alert-success"><?= htmlspecialchars($alert_msg) ?></div>
    <?php endif; ?>
    <?php if (!empty($error_msg)): ?>
        <div class="alert-danger"><?= htmlspecialchars($error_msg) ?></div>
    <?php endif; ?>

    <!-- Stats Overview -->
    <div class="stats-grid">
        <div class="stat-card">
            <div class="num"><?= count($all_coaches) ?></div>
            <div class="label">Jumlah Jurulatih</div>
        </div>
        <div class="stat-card">
            <div class="num"><?= $total_athletes ?></div>
            <div class="label">Jumlah Pelatih Berdaftar</div>
        </div>
        <div class="stat-card">
            <div class="num" style="color:var(--green);"><?= $total_receipts ?></div>
            <div class="label">Resit Langganan (RM30)</div>
        </div>
    </div>

    <!-- Coaches Management Table -->
    <div class="table-card">
        <div class="table-header">
            <h2>SENARAI JURULATIH & KELAB SUKAN</h2>
            <span style="font-size:11px;color:var(--silver);">Urus langganan, slot sub-coach, edit dan padam akaun</span>
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
                        <th>Status / Tamat</th>
                        <th>Slot Sub-Coach</th>
                        <th>Tindakan Admin Roger</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach ($all_coaches as $c): 
                        $days_left = max(0, ceil(($c['subscription_expires_at'] - (time() * 1000)) / (24 * 3600 * 1000)));
                        $exp_str = date('d/m/Y', $c['subscription_expires_at'] / 1000);
                        $is_admin = $c['role'] === 'ADMIN';
                    ?>
                        <tr>
                            <td>#<?= $c['id'] ?></td>
                            <td>
                                <strong style="color:#FFF;"><?= htmlspecialchars($c['name']) ?></strong><br>
                                <span style="color:var(--gold);font-size:10px;">Nick: <?= htmlspecialchars(!empty($c['nickname']) ? $c['nickname'] : '-') ?></span>
                            </td>
                            <td>
                                <strong><?= htmlspecialchars(!empty($c['club_name']) ? $c['club_name'] : '-') ?></strong><br>
                                <span style="color:var(--silver);font-size:10px;"><?= htmlspecialchars(!empty($c['training_specialty']) ? $c['training_specialty'] : '-') ?></span>
                            </td>
                            <td>
                                IC: <?= htmlspecialchars(!empty($c['ic_number']) ? $c['ic_number'] : '-') ?><br>
                                Tel: <a href="https://wa.me/<?= preg_replace('/[^0-9]/', '', $c['phone']) ?>" target="_blank" style="color:var(--green);"><?= htmlspecialchars($c['phone']) ?></a>
                            </td>
                            <td><?= htmlspecialchars($c['email']) ?></td>
                            <td>
                                <?php if ($is_admin): ?>
                                    <span style="color:var(--gold);font-weight:bold;">Kekal (Admin)</span>
                                <?php else: ?>
                                    <span style="color:<?= $days_left <= 3 ? 'var(--racing-red)' : 'var(--green)' ?>;font-weight:bold;">
                                        <?= $days_left ?> Hari Lagi
                                    </span><br>
                                    <span style="font-size:10px;color:var(--silver);">Tamat: <?= $exp_str ?></span>
                                <?php endif; ?>
                            </td>
                            <td>
                                <strong><?= $c['sub_coach_slots'] ?> Slot</strong>
                                <?php if (!$is_admin): ?>
                                    <form method="POST" style="margin-top:4px;">
                                        <input type="hidden" name="action" value="update_slots">
                                        <input type="hidden" name="target_id" value="<?= $c['id'] ?>">
                                        <select name="slots" onchange="this.form.submit()" style="background:#0A0A0A;color:#FFF;border:1px solid var(--border);padding:2px;border-radius:4px;font-size:10px;">
                                            <option value="1" <?= $c['sub_coach_slots'] == 1 ? 'selected' : '' ?>>1 Percuma</option>
                                            <option value="4" <?= $c['sub_coach_slots'] == 4 ? 'selected' : '' ?>>4 Slot (+3 RM10)</option>
                                            <option value="10" <?= $c['sub_coach_slots'] == 10 ? 'selected' : '' ?>>10 Slot Elit</option>
                                        </select>
                                    </form>
                                <?php endif; ?>
                            </td>
                            <td>
                                <?php if (!$is_admin): ?>
                                    <div style="display:flex;gap:4px;flex-wrap:wrap;">
                                        <!-- Luluskan / Tambah 30 Hari -->
                                        <form method="POST" style="display:inline;">
                                            <input type="hidden" name="action" value="extend_subscription">
                                            <input type="hidden" name="target_id" value="<?= $c['id'] ?>">
                                            <input type="hidden" name="days" value="30">
                                            <button type="submit" class="btn-action green" onclick="return confirm('Sahkan pembaharuan langganan RM30 (30 Hari) bagi <?= addslashes($c['name']) ?>?')">+30 Hari (RM30)</button>
                                        </form>

                                        <!-- Hantar Emel Notifikasi -->
                                        <form method="POST" style="display:inline;">
                                            <input type="hidden" name="action" value="send_expiry_email">
                                            <input type="hidden" name="target_id" value="<?= $c['id'] ?>">
                                            <button type="submit" class="btn-action" title="Hantar Emel Peringatan Tamat">📧 Emel Peringatan</button>
                                        </form>

                                        <!-- Edit Modal Button -->
                                        <button type="button" class="btn-action gold" onclick="openEditModal(<?= htmlspecialchars(json_encode($c)) ?>)">Edit</button>

                                        <!-- Padam Coach -->
                                        <form method="POST" style="display:inline;">
                                            <input type="hidden" name="action" value="delete_coach">
                                            <input type="hidden" name="target_id" value="<?= $c['id'] ?>">
                                            <button type="submit" class="btn-action red" onclick="return confirm('AMARAN: Anda pasti mahu memadam akaun <?= addslashes($c['name']) ?> dan semua pelatihnya?')">Padam</button>
                                        </form>
                                    </div>
                                <?php else: ?>
                                    <span style="color:var(--gold);font-weight:bold;">Akaun Utama</span>
                                <?php endif; ?>
                            </td>
                        </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Modal Edit Coach -->
<div class="modal" id="editModal">
    <div class="modal-content">
        <h3 style="font-family:'JetBrains Mono',monospace;color:var(--gold);margin-bottom:14px;">EDIT PROFIL JURULATIH & KELAB</h3>
        <form method="POST">
            <input type="hidden" name="action" value="edit_coach">
            <input type="hidden" name="target_id" id="edit_target_id">

            <div class="form-group">
                <label>Nama Penuh Jurulatih</label>
                <input type="text" name="name" id="edit_name" required>
            </div>

            <div class="form-group">
                <label>Nick Name / Gelaran</label>
                <input type="text" name="nickname" id="edit_nickname" required>
            </div>

            <div class="form-group">
                <label>Nama Kelab / Akademi</label>
                <input type="text" name="club_name" id="edit_club_name" required>
            </div>

            <div class="form-group">
                <label>Alamat / Lokasi Latihan</label>
                <input type="text" name="club_address" id="edit_club_address" required>
            </div>

            <div class="form-group">
                <label>Latihan yang Diajar</label>
                <input type="text" name="training_specialty" id="edit_training_specialty" required>
            </div>

            <div class="form-group">
                <label>No. Telefon (WhatsApp)</label>
                <input type="text" name="phone" id="edit_phone" required>
            </div>

            <div style="display:flex;gap:10px;margin-top:16px;">
                <button type="submit" class="btn-action green" style="flex:1;padding:10px;">Simpan Perubahan</button>
                <button type="button" class="btn-action" style="flex:1;padding:10px;" onclick="closeEditModal()">Batal</button>
            </div>
        </form>
    </div>
</div>

<script>
function openEditModal(coach) {
    document.getElementById('edit_target_id').value = coach.id;
    document.getElementById('edit_name').value = coach.name;
    document.getElementById('edit_nickname').value = coach.nickname || '';
    document.getElementById('edit_club_name').value = coach.club_name || '';
    document.getElementById('edit_club_address').value = coach.club_address || '';
    document.getElementById('edit_training_specialty').value = coach.training_specialty || '';
    document.getElementById('edit_phone').value = coach.phone || '';
    document.getElementById('editModal').classList.add('active');
}
function closeEditModal() {
    document.getElementById('editModal').classList.remove('active');
}
</script>

</body>
</html>
