<?php
session_start();
require_once __DIR__ . '/db_config.php';

// Handle Logout
if (isset($_GET['action']) && $_GET['action'] === 'logout') {
    session_destroy();
    header("Location: index.php");
    exit;
}

$login_error = '';
$register_success = '';
$register_error = '';

// Handle Login (Coach, Sub-Coach or Master Admin)
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['login_submit'])) {
    $email_or_user = trim($_POST['username'] ?? '');
    $password = trim($_POST['password'] ?? '');

    if ($email_or_user === ADMIN_EMAIL && $password === 'Abc@1234') {
        // Master Admin Direct Login
        $_SESSION['user_id'] = 1;
        $_SESSION['email'] = ADMIN_EMAIL;
        $_SESSION['name'] = 'Roger (Master Admin)';
        $_SESSION['role'] = 'ADMIN';
        header("Location: admin.php");
        exit;
    }

    // Check Sub-Coach first
    $stmt = $pdo->prepare("SELECT s.*, c.name as coach_name FROM sub_coaches s JOIN coaches c ON s.parent_coach_id = c.id WHERE s.username = ? LIMIT 1");
    $stmt->execute([$email_or_user]);
    $sub = $stmt->fetch();
    if ($sub && ($password === $sub['password_hash'] || password_verify($password, $sub['password_hash']))) {
        $_SESSION['user_id'] = $sub['id'];
        $_SESSION['parent_coach_id'] = $sub['parent_coach_id'];
        $_SESSION['email'] = $sub['username'];
        $_SESSION['name'] = $sub['full_name'];
        $_SESSION['role'] = 'SUB_COACH';
        header("Location: index.php");
        exit;
    }

    // Check Coach
    $stmt = $pdo->prepare("SELECT * FROM coaches WHERE email = ? LIMIT 1");
    $stmt->execute([$email_or_user]);
    $coach = $stmt->fetch();

    if ($coach && ($password === $coach['password_hash'] || password_verify($password, $coach['password_hash']))) {
        if (!$coach['is_approved'] && $coach['role'] !== 'ADMIN') {
            $login_error = "Akaun anda sedang menunggu kelulusan daripada Admin (Roger). Sila hubungi WhatsApp +60195326399 untuk kelulusan.";
        } else {
            $_SESSION['user_id'] = $coach['id'];
            $_SESSION['email'] = $coach['email'];
            $_SESSION['name'] = $coach['name'];
            $_SESSION['role'] = $coach['role'];
            header("Location: index.php");
            exit;
        }
    } else {
        $login_error = "Emel atau kata laluan tidak tepat.";
    }
}

// Handle Register Coach
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['register_submit'])) {
    $name = trim($_POST['reg_name'] ?? '');
    $email = trim($_POST['reg_email'] ?? '');
    $phone = trim($_POST['reg_phone'] ?? '');
    $password = trim($_POST['reg_password'] ?? '');

    if (empty($name) || empty($email) || empty($password)) {
        $register_error = "Sila lengkapkan semua maklumat pendaftaran.";
    } else {
        // Check existing
        $check = $pdo->prepare("SELECT id FROM coaches WHERE email = ?");
        $check->execute([$email]);
        if ($check->fetch()) {
            $register_error = "Emel '$email' telah berdaftar. Sila log masuk.";
        } else {
            $stmt = $pdo->prepare("
                INSERT INTO coaches (email, name, phone, password_hash, role, is_approved, subscription_status, trial_start_date, trial_end_date, subscription_expires_at, sub_coach_slots)
                VALUES (?, ?, ?, ?, 'COACH', 0, 'PENDING_APPROVAL', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 1)
            ");
            $stmt->execute([$email, $name, $phone, $password]);
            $register_success = "Pendaftaran berjaya! Akaun anda kini menunggu kelulusan Admin. Admin Roger telah dimaklumkan (+60195326399).";
        }
    }
}

// Logged in user context
$is_logged_in = isset($_SESSION['user_id']);
$user_id = $_SESSION['user_id'] ?? 0;
$user_role = $_SESSION['role'] ?? 'COACH';
$active_coach_id = ($user_role === 'SUB_COACH') ? $_SESSION['parent_coach_id'] : $user_id;

// Handle CRUD Runner on Dashboard
if ($is_logged_in && isset($_GET['delete_runner'])) {
    $del_id = intval($_GET['delete_runner']);
    $pdo->prepare("DELETE FROM runners WHERE id = ? AND coach_id = ?")->execute([$del_id, $active_coach_id]);
    header("Location: index.php?msg=deleted");
    exit;
}

// Handle Add Sub-Coach
if ($is_logged_in && $_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['add_sub_coach'])) {
    $sub_user = trim($_POST['sub_username'] ?? '');
    $sub_pass = trim($_POST['sub_password'] ?? '');
    $sub_name = trim($_POST['sub_fullname'] ?? '');
    $sub_phone = trim($_POST['sub_phone'] ?? '');

    if (!empty($sub_user) && !empty($sub_pass)) {
        $check = $pdo->prepare("SELECT id FROM sub_coaches WHERE username = ?");
        $check->execute([$sub_user]);
        if (!$check->fetch()) {
            $ins = $pdo->prepare("INSERT INTO sub_coaches (parent_coach_id, username, password_hash, full_name, phone) VALUES (?, ?, ?, ?, ?)");
            $ins->execute([$active_coach_id, $sub_user, $sub_pass, $sub_name, $sub_phone]);
            header("Location: index.php?msg=sub_added");
            exit;
        }
    }
}

// Fetch Coach Data & Runners
$coach_info = null;
$runners = [];
$sub_coaches = [];
if ($is_logged_in) {
    $c_stmt = $pdo->prepare("SELECT * FROM coaches WHERE id = ?");
    $c_stmt->execute([$active_coach_id]);
    $coach_info = $c_stmt->fetch();

    $r_stmt = $pdo->prepare("SELECT * FROM runners WHERE coach_id = ? ORDER BY sport_type ASC, id DESC");
    $r_stmt->execute([$active_coach_id]);
    $runners = $r_stmt->fetchAll();

    $s_stmt = $pdo->prepare("SELECT * FROM sub_coaches WHERE parent_coach_id = ?");
    $s_stmt->execute([$active_coach_id]);
    $sub_coaches = $s_stmt->fetchAll();
}
?>
<!DOCTYPE html>
<html lang="ms">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Papan Pemuka Jurulatih | Psyco Time X Pro Training Management</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <!-- QRCode.js for instant dynamic QR generation -->
  <script src="https://cdnjs.cloudflare.com/ajax/libs/qrcodejs/1.0.0/qrcode.min.js"></script>
  <style>
    body { background-color: #0A0A0A; color: #F5F5F7; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
    .card-dark { background-color: #141416; border: 1px solid #28282E; }
    .card-silver { background-color: #1E1E22; border: 1px solid #44444A; }
    .accent-red { color: #E50914; }
    .bg-red-brand { background-color: #E50914; }
  </style>
</head>
<body class="min-h-screen">

<!-- Top Navigation Bar (Merah, Hitam, Silver) -->
<nav class="bg-black/90 border-b border-gray-800 sticky top-0 z-50 px-4 py-3">
  <div class="max-w-7xl mx-auto flex items-center justify-between">
    <div class="flex items-center space-x-3">
      <div class="w-10 h-10 rounded-full bg-black border-2 border-red-600 flex items-center justify-center font-black text-red-600 text-base shadow-lg shadow-red-900/40">
        X
      </div>
      <div>
        <span class="text-lg font-black text-white tracking-wider">PSYCO TIME <span class="text-red-600">X</span> PRO</span>
        <span class="block text-xs text-gray-400">Pusat Pengurusan Latihan & Olahraga</span>
      </div>
    </div>

    <div class="flex items-center space-x-3">
      <?php if ($is_logged_in): ?>
        <span class="hidden md:inline-block text-xs font-semibold text-gray-300">
          Hi, <span class="text-red-500 font-bold"><?= htmlspecialchars($_SESSION['name']) ?></span> (<?= htmlspecialchars($user_role) ?>)
        </span>
        <a href="?action=logout" class="px-3 py-1.5 bg-gray-800 hover:bg-gray-700 text-gray-200 text-xs font-bold rounded-lg border border-gray-700">Log Keluar</a>
      <?php else: ?>
        <a href="https://wa.me/60195326399?text=Salam%20Admin%20Roger,%20saya%20ingin%20pertanyaan%20tentang%20Psyco%20Time%20X%20Pro" target="_blank" class="px-3 py-1.5 bg-green-700 hover:bg-green-600 text-white text-xs font-bold rounded-lg flex items-center space-x-1">
          <span>WhatsApp Admin (+60195326399)</span>
        </a>
      <?php endif; ?>
    </div>
  </div>
</nav>

<div class="max-w-7xl mx-auto p-4 sm:p-6">

<?php if (!$is_logged_in): ?>
  <!-- LOGIN & REGISTER TABS (SEPARATED FROM MAIN WEBSITE) -->
  <div class="max-w-md mx-auto card-dark rounded-2xl p-6 sm:p-8 shadow-2xl mt-8">
    <div class="text-center mb-6">
      <h2 class="text-xl font-black text-white uppercase tracking-wider">LOG MASUK JURULATIH</h2>
      <p class="text-xs text-gray-400 mt-1">Sistem Pemantauan Latihan Laptop & Aplikasi Android</p>
    </div>

    <?php if ($login_error): ?>
      <div class="bg-red-950 border border-red-600 text-red-300 p-3 rounded-lg mb-4 text-xs font-semibold leading-relaxed">
        <?= $login_error ?>
      </div>
    <?php endif; ?>

    <?php if ($register_success): ?>
      <div class="bg-green-950 border border-green-600 text-green-300 p-3 rounded-lg mb-4 text-xs font-semibold leading-relaxed">
        <?= $register_success ?>
      </div>
    <?php endif; ?>

    <?php if ($register_error): ?>
      <div class="bg-red-950 border border-red-600 text-red-300 p-3 rounded-lg mb-4 text-xs font-semibold leading-relaxed">
        <?= $register_error ?>
      </div>
    <?php endif; ?>

    <!-- Form Tab Switcher -->
    <div class="flex border-b border-gray-800 mb-6">
      <button onclick="showTab('login')" id="tabBtnLogin" class="flex-1 py-2 text-center text-sm font-bold border-b-2 border-red-600 text-white">Log Masuk</button>
      <button onclick="showTab('register')" id="tabBtnRegister" class="flex-1 py-2 text-center text-sm font-bold border-b-2 border-transparent text-gray-400 hover:text-white">Daftar Coach Baru</button>
    </div>

    <!-- Login Form -->
    <form method="POST" id="formLogin" class="space-y-4">
      <input type="hidden" name="login_submit" value="1">
      <div>
        <label class="block text-xs font-semibold uppercase text-gray-400 mb-1">Emel Google / Username</label>
        <input type="text" name="username" required placeholder="cth: Saliparjipun.atukoi@gmail.com" class="w-full bg-black border border-gray-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-red-600">
      </div>
      <div>
        <label class="block text-xs font-semibold uppercase text-gray-400 mb-1">Kata Laluan</label>
        <input type="password" name="password" required placeholder="••••••••" class="w-full bg-black border border-gray-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-red-600">
      </div>
      <button type="submit" class="w-full py-2.5 bg-red-brand hover:bg-red-700 text-white font-bold rounded-lg text-sm uppercase tracking-wider transition">
        Log Masuk ke Papan Pemuka
      </button>
      <div class="text-center pt-2">
        <span class="text-xs text-gray-500">Percuma 7 hari penggunaan selepas pendaftaran diluluskan Admin.</span>
      </div>
    </form>

    <!-- Register Form -->
    <form method="POST" id="formRegister" class="space-y-4 hidden">
      <input type="hidden" name="register_submit" value="1">
      <div>
        <label class="block text-xs font-semibold uppercase text-gray-400 mb-1">Nama Penuh Jurulatih</label>
        <input type="text" name="reg_name" required placeholder="cth: Coach Ahmad Razif" class="w-full bg-black border border-gray-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-red-600">
      </div>
      <div>
        <label class="block text-xs font-semibold uppercase text-gray-400 mb-1">Emel Google (Gmail)</label>
        <input type="email" name="reg_email" required placeholder="nama@gmail.com" class="w-full bg-black border border-gray-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-red-600">
      </div>
      <div>
        <label class="block text-xs font-semibold uppercase text-gray-400 mb-1">No. Telefon (WhatsApp)</label>
        <input type="tel" name="reg_phone" required placeholder="019-XXXXXXX" class="w-full bg-black border border-gray-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-red-600">
      </div>
      <div>
        <label class="block text-xs font-semibold uppercase text-gray-400 mb-1">Kata Laluan Baru</label>
        <input type="password" name="reg_password" required placeholder="Minimal 6 aksara" class="w-full bg-black border border-gray-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-red-600">
      </div>
      <button type="submit" class="w-full py-2.5 bg-red-brand hover:bg-red-700 text-white font-bold rounded-lg text-sm uppercase tracking-wider transition">
        Hantar Pendaftaran (Tunggu Kelulusan Admin)
      </button>
      <div class="text-center pt-2">
        <a href="https://wa.me/60195326399?text=Admin%20Roger,%20saya%20baru%20mendaftar%20Coach%20PsycoTimeXPro" target="_blank" class="text-xs text-green-400 hover:underline font-semibold">
          Hubungi Admin Roger (+60195326399) untuk Fast Approval
        </a>
      </div>
    </form>
  </div>

<?php else: ?>
  <!-- COACH LAPTOP DASHBOARD (RED, BLACK & SILVER) -->

  <!-- Top Hero & Action Banner -->
  <div class="card-dark rounded-2xl p-6 mb-6">
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <div class="flex items-center space-x-2">
          <span class="px-2.5 py-0.5 rounded-full text-xs font-black uppercase tracking-wider bg-red-950 text-red-400 border border-red-600">Pusat Prestasi Jurulatih</span>
          <span class="px-2.5 py-0.5 rounded-full text-xs font-bold uppercase bg-gray-800 text-gray-300 border border-gray-700">Status: <?= htmlspecialchars($coach_info['subscription_status'] ?? 'ACTIVE') ?></span>
        </div>
        <h1 class="text-2xl font-black text-white mt-2"><?= htmlspecialchars($_SESSION['name']) ?></h1>
        <p class="text-xs text-gray-400">Pangkalan Data Pelatih, Ujian Masa & Resit Langganan (RM30/Bulan)</p>
      </div>

      <div class="flex flex-wrap gap-2">
        <!-- New Member QR Button -->
        <button onclick="openQrModal()" class="px-4 py-2.5 bg-red-brand hover:bg-red-700 text-white font-extrabold rounded-xl shadow-lg shadow-red-900/50 flex items-center space-x-2 text-sm">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
          <span>New Member (QR Code Pelatih)</span>
        </button>

        <!-- Official Receipt Button -->
        <button onclick="openReceiptModal()" class="px-3.5 py-2.5 bg-gray-800 hover:bg-gray-700 text-gray-200 border border-gray-600 font-bold rounded-xl text-sm flex items-center space-x-1">
          <svg class="w-4 h-4 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
          <span>Resit Langganan Rasmi</span>
        </button>
      </div>
    </div>
  </div>

  <!-- Key Statistics Grid -->
  <?php
    $total_runners = count($runners);
    $balapan_count = count(array_filter($runners, fn($r) => $r['sport_type'] === 'Balapan'));
    $padang_count = count(array_filter($runners, fn($r) => $r['sport_type'] === 'Padang'));
  ?>
  <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
    <div class="card-dark p-4 rounded-xl">
      <span class="text-xs text-gray-400 font-bold uppercase">Jumlah Pelatih</span>
      <p class="text-2xl font-black text-white mt-1"><?= $total_runners ?> Orang</p>
      <span class="text-xs text-green-400">Unlimited Runner</span>
    </div>
    <div class="card-dark p-4 rounded-xl">
      <span class="text-xs text-gray-400 font-bold uppercase">Acara Balapan (Track)</span>
      <p class="text-2xl font-black text-red-500 mt-1"><?= $balapan_count ?> Pelari</p>
      <span class="text-xs text-gray-400">100m, 200m, 400m, Pagar</span>
    </div>
    <div class="card-dark p-4 rounded-xl">
      <span class="text-xs text-gray-400 font-bold uppercase">Acara Padang (Field)</span>
      <p class="text-2xl font-black text-gray-300 mt-1"><?= $padang_count ?> Atlet</p>
      <span class="text-xs text-gray-400">Lompat Jauh, Lontar, dll</span>
    </div>
    <div class="card-dark p-4 rounded-xl">
      <span class="text-xs text-gray-400 font-bold uppercase">Sub-Coach (Penolong)</span>
      <p class="text-2xl font-black text-yellow-500 mt-1"><?= count($sub_coaches) ?> / <?= $coach_info['sub_coach_slots'] ?? 1 ?></p>
      <span class="text-xs text-gray-400">1 Percuma (+3 RM10)</span>
    </div>
  </div>

  <!-- RUNNERS MANAGEMENT TABLE (BALAPAN & PADANG) -->
  <div class="card-dark rounded-2xl p-6 mb-8">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
      <div>
        <h2 class="text-lg font-black text-white uppercase tracking-wider">SENARAI PELATIH & ATLET BERDAFTAR</h2>
        <p class="text-xs text-gray-400">Pelatih yang mendaftar melalui imbasan QR Code atau ditambah manual</p>
      </div>
      <div class="text-xs text-gray-400">
        Disusun automatik: <span class="text-red-500 font-bold">Balapan</span> & <span class="text-gray-300 font-bold">Padang</span>
      </div>
    </div>

    <?php if (empty($runners)): ?>
      <div class="text-center py-12 text-gray-500 text-sm">
        Belum ada pelatih berdaftar. Tekan butang <strong class="text-red-500">"New Member"</strong> di atas untuk memaparkan QR Code pendaftaran kepada atlet/ibu bapa.
      </div>
    <?php else: ?>
      <div class="overflow-x-auto">
        <table class="w-full text-left text-sm">
          <thead class="bg-black text-xs uppercase text-gray-400 border-b border-gray-800">
            <tr>
              <th class="p-3">Nama Penuh</th>
              <th class="p-3">No. IC / Lahir</th>
              <th class="p-3">Fizikal (Tinggi/Berat)</th>
              <th class="p-3">Kategori Sukan</th>
              <th class="p-3">Acara Khusus</th>
              <th class="p-3">PB Masa / Rekod</th>
              <th class="p-3">Yuran Bulanan</th>
              <th class="p-3 text-right">Tindakan</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-800 text-gray-300">
            <?php foreach ($runners as $r): ?>
              <tr class="hover:bg-black/40 transition">
                <td class="p-3 font-bold text-white">
                  <?= htmlspecialchars($r['name']) ?>
                  <span class="block text-xs text-gray-400 font-normal"><?= htmlspecialchars($r['gender']) ?> • Tel: <?= htmlspecialchars($r['phone'] ?: '-') ?></span>
                </td>
                <td class="p-3 text-xs font-mono">
                  <?= htmlspecialchars($r['ic_number'] ?: '-') ?>
                  <span class="block text-gray-400"><?= htmlspecialchars($r['dob'] ?: '-') ?> (<?= $r['age'] ?> thn)</span>
                </td>
                <td class="p-3 text-xs">
                  <?= $r['height_cm'] > 0 ? $r['height_cm'] . ' cm' : '-' ?> /
                  <?= $r['weight_kg'] > 0 ? $r['weight_kg'] . ' kg' : '-' ?>
                </td>
                <td class="p-3">
                  <?php if ($r['sport_type'] === 'Balapan'): ?>
                    <span class="px-2 py-0.5 bg-red-950 text-red-400 border border-red-600 rounded text-xs font-bold">Balapan</span>
                  <?php else: ?>
                    <span class="px-2 py-0.5 bg-gray-800 text-gray-300 border border-gray-600 rounded text-xs font-bold">Padang</span>
                  <?php endif; ?>
                </td>
                <td class="p-3 font-semibold text-white"><?= htmlspecialchars($r['category']) ?></td>
                <td class="p-3 font-mono font-bold text-green-400"><?= $r['pb_seconds'] > 0 ? $r['pb_seconds'] . 's' : '-' ?></td>
                <td class="p-3 text-xs">
                  RM <?= number_format($r['monthly_fee'], 2) ?>
                  <span class="block text-yellow-500 font-mono text-[10px]">Tamat: <?= htmlspecialchars($r['fee_due_date'] ?: 'Setiap Bulan') ?></span>
                </td>
                <td class="p-3 text-right space-x-2">
                  <a href="?delete_runner=<?= $r['id'] ?>" onclick="return confirm('Adakah anda pasti ingin memadam pelatih ini?')" class="text-xs text-red-500 hover:text-red-400 font-bold">Padam</a>
                </td>
              </tr>
            <?php endforeach; ?>
          </tbody>
        </table>
      </div>
    <?php endif; ?>
  </div>

  <!-- SUB-COACH SECTION (1 FREE, ADD 3 FOR RM10) -->
  <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
    <div class="card-dark rounded-2xl p-6">
      <div class="flex items-center justify-between mb-4">
        <div>
          <h3 class="text-base font-black text-white uppercase tracking-wider">PENOLONG JURULATIH (SUB-COACH)</h3>
          <p class="text-xs text-gray-400">1 Slot Percuma. Akses sistem yang sama tanpa perlu approval admin.</p>
        </div>
        <span class="px-2.5 py-1 bg-yellow-950 text-yellow-400 border border-yellow-600 rounded-lg text-xs font-bold">1 Free Slot</span>
      </div>

      <form method="POST" class="space-y-3 mb-4">
        <input type="hidden" name="add_sub_coach" value="1">
        <div class="grid grid-cols-2 gap-2">
          <input type="text" name="sub_fullname" required placeholder="Nama Penolong Jurulatih" class="bg-black border border-gray-700 rounded-lg px-3 py-2 text-white text-xs">
          <input type="tel" name="sub_phone" placeholder="No. Telefon" class="bg-black border border-gray-700 rounded-lg px-3 py-2 text-white text-xs">
        </div>
        <div class="grid grid-cols-2 gap-2">
          <input type="text" name="sub_username" required placeholder="Username Login" class="bg-black border border-gray-700 rounded-lg px-3 py-2 text-white text-xs">
          <input type="password" name="sub_password" required placeholder="Password Login" class="bg-black border border-gray-700 rounded-lg px-3 py-2 text-white text-xs">
        </div>
        <button type="submit" class="w-full py-2 bg-gray-800 hover:bg-gray-700 border border-gray-600 text-white font-bold rounded-lg text-xs transition">
          + Daftar Sub-Coach Baru
        </button>
      </form>

      <?php if (!empty($sub_coaches)): ?>
        <div class="divide-y divide-gray-800 border-t border-gray-800 pt-2">
          <?php foreach ($sub_coaches as $sc): ?>
            <div class="py-2 flex items-center justify-between text-xs">
              <div>
                <span class="font-bold text-white"><?= htmlspecialchars($sc['full_name']) ?></span>
                <span class="block text-gray-400">Username: <code class="text-yellow-400"><?= htmlspecialchars($sc['username']) ?></code></span>
              </div>
              <span class="text-green-400 font-bold">Aktif</span>
            </div>
          <?php endforeach; ?>
        </div>
      <?php endif; ?>

      <div class="mt-4 p-3 rounded-lg bg-black/60 border border-gray-800 flex items-center justify-between text-xs">
        <span class="text-gray-400">Ingin tambah lagi sub-coach?</span>
        <a href="https://wa.me/60195326399?text=Admin%20Roger,%20saya%20ingin%20tambah%203%20sub-coach%20RM10" target="_blank" class="text-red-500 font-bold hover:underline">+3 Orang (RM10/Bulan)</a>
      </div>
    </div>

    <!-- SUBSCRIPTION STATUS & OFFICIAL RECEIPT CARD -->
    <div class="card-dark rounded-2xl p-6 flex flex-col justify-between">
      <div>
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-base font-black text-white uppercase tracking-wider">LANGGANAN APLIKASI (RM30/BULAN)</h3>
          <span class="px-2.5 py-1 bg-green-950 text-green-400 border border-green-600 rounded-lg text-xs font-bold">Aktif</span>
        </div>
        <p class="text-xs text-gray-400 leading-relaxed">
          Pakej Jurulatih Olahraga Pro merangkumi penambahan pelatih tanpa had, modul Electronic Timing Cam 1 & Cam 2 Photo Finish, dan analisis biomekanik AI.
        </p>

        <div class="mt-4 space-y-2 text-xs">
          <div class="flex justify-between py-1 border-b border-gray-800">
            <span class="text-gray-400">Kadar Langganan:</span>
            <span class="font-bold text-white">RM 30.00 / Bulan</span>
          </div>
          <div class="flex justify-between py-1 border-b border-gray-800">
            <span class="text-gray-400">Tarikh Tamat / Pembaharuan:</span>
            <span class="font-bold text-red-500 font-mono"><?= htmlspecialchars($coach_info['subscription_expires_at'] ?? 'Aktif') ?></span>
          </div>
          <div class="flex justify-between py-1 border-b border-gray-800">
            <span class="text-gray-400">Kaedah Pembayaran:</span>
            <span class="font-bold text-gray-300">Tunai / DuitNow QR Admin</span>
          </div>
        </div>
      </div>

      <div class="mt-6 pt-4 border-t border-gray-800 flex items-center justify-between">
        <button onclick="openReceiptModal()" class="px-4 py-2 bg-green-600 hover:bg-green-700 text-black font-extrabold rounded-lg text-xs">
          Cetak Resit Pembayaran
        </button>
        <a href="https://wa.me/60195326399?text=Admin%20Roger,%20saya%20ingin%20membuat%20pembaharuan%20langganan%20RM30" target="_blank" class="text-xs text-gray-400 hover:text-white flex items-center space-x-1">
          <span>Hubungi Admin Roger</span>
        </a>
      </div>
    </div>
  </div>

<?php endif; ?>

</div>

<!-- QR CODE MODAL FOR NEW RUNNER ONBOARDING -->
<div id="qrModal" class="fixed inset-0 bg-black/80 z-50 hidden flex items-center justify-center p-4">
  <div class="card-dark rounded-2xl p-6 sm:p-8 max-w-sm w-full text-center relative shadow-2xl border border-red-600">
    <button onclick="closeQrModal()" class="absolute top-4 right-4 text-gray-400 hover:text-white text-xl font-bold">&times;</button>
    <div class="inline-block p-2 rounded-full bg-black border border-red-600 mb-2">
      <span class="text-sm font-black text-red-600">NEW MEMBER ONBOARDING</span>
    </div>
    <h3 class="text-lg font-black text-white">IMBAS UNTUK DAFTAR PELATIH</h3>
    <p class="text-xs text-gray-400 mt-1 mb-4">Atlet atau ibu bapa imbas kod ini dengan kamera telefon untuk mengisi butiran penuh</p>

    <div class="bg-white p-4 rounded-xl inline-block shadow-inner mb-4" id="qrcode"></div>

    <div class="text-xs text-gray-400 break-all mb-4" id="qrLinkText"></div>

    <button onclick="copyQrLink()" class="w-full py-2 bg-gray-800 hover:bg-gray-700 border border-gray-600 text-white font-bold rounded-lg text-xs">
      Salin Pautan Pendaftaran
    </button>
  </div>
</div>

<!-- OFFICIAL SUBSCRIPTION PAYMENT RECEIPT MODAL -->
<div id="receiptModal" class="fixed inset-0 bg-black/80 z-50 hidden flex items-center justify-center p-4">
  <div class="card-dark rounded-2xl p-6 sm:p-8 max-w-md w-full relative shadow-2xl border border-gray-700">
    <button onclick="closeReceiptModal()" class="absolute top-4 right-4 text-gray-400 hover:text-white text-xl font-bold">&times;</button>
    
    <div class="text-center pb-4 border-b border-gray-800">
      <div class="w-12 h-12 mx-auto rounded-full bg-black border-2 border-red-600 flex items-center justify-center font-black text-red-600 text-lg mb-2">
        X
      </div>
      <h3 class="text-lg font-black text-white">RESIT RASMI LANGGANAN SISTEM</h3>
      <p class="text-xs text-gray-400">PSYCO TIME X PRO SPORT MANAGEMENT</p>
    </div>

    <div class="py-4 space-y-2 text-xs">
      <div class="flex justify-between">
        <span class="text-gray-400">No. Resit Rasmi:</span>
        <span class="font-mono font-bold text-white">SUB-PTXP-<?= date('ym') ?>-<?= str_pad($active_coach_id, 3, '0', STR_PAD_LEFT) ?></span>
      </div>
      <div class="flex justify-between">
        <span class="text-gray-400">Dikeluarkan Kepada:</span>
        <span class="font-bold text-white"><?= htmlspecialchars($_SESSION['name'] ?? 'Coach') ?></span>
      </div>
      <div class="flex justify-between">
        <span class="text-gray-400">Emel:</span>
        <span class="font-mono text-gray-300"><?= htmlspecialchars($_SESSION['email'] ?? '-') ?></span>
      </div>
      <div class="flex justify-between">
        <span class="text-gray-400">Tarikh Bayaran:</span>
        <span class="font-mono text-gray-300"><?= date('d/m/Y') ?></span>
      </div>
      <div class="flex justify-between">
        <span class="text-gray-400">Tempoh Langganan:</span>
        <span class="font-bold text-green-400">1 Bulan (Unlimited Runner)</span>
      </div>
      <div class="flex justify-between">
        <span class="text-gray-400">Tarikh Luput / Bayaran Seterusnya:</span>
        <span class="font-bold text-red-500 font-mono"><?= date('d/m/Y', strtotime('+1 month')) ?></span>
      </div>
      <div class="flex justify-between pt-2 border-t border-gray-800 text-sm font-bold">
        <span class="text-white">JUMLAH DIBAYAR:</span>
        <span class="text-green-400 font-black">RM 30.00 (LUNAS)</span>
      </div>
    </div>

    <div class="pt-4 border-t border-gray-800 flex space-x-2">
      <button onclick="window.print()" class="flex-1 py-2 bg-red-brand hover:bg-red-700 text-white font-bold rounded-lg text-xs">Cetak Resit</button>
      <button onclick="closeReceiptModal()" class="px-4 py-2 bg-gray-800 hover:bg-gray-700 text-gray-300 font-bold rounded-lg text-xs">Tutup</button>
    </div>
  </div>
</div>

<script>
  function showTab(tab) {
    if (tab === 'login') {
      document.getElementById('formLogin').classList.remove('hidden');
      document.getElementById('formRegister').classList.add('hidden');
      document.getElementById('tabBtnLogin').classList.add('border-red-600', 'text-white');
      document.getElementById('tabBtnRegister').classList.remove('border-red-600', 'text-white');
      document.getElementById('tabBtnRegister').classList.add('text-gray-400');
    } else {
      document.getElementById('formLogin').classList.add('hidden');
      document.getElementById('formRegister').classList.remove('hidden');
      document.getElementById('tabBtnRegister').classList.add('border-red-600', 'text-white');
      document.getElementById('tabBtnLogin').classList.remove('border-red-600', 'text-white');
      document.getElementById('tabBtnLogin').classList.add('text-gray-400');
    }
  }

  // QR Modal Logic
  let qrGenerated = false;
  const runnerRegisterUrl = "<?= BASE_URL ?>/register_runner.php?coach_id=<?= $active_coach_id ?>";

  function openQrModal() {
    document.getElementById('qrModal').classList.remove('hidden');
    document.getElementById('qrLinkText').innerText = runnerRegisterUrl;
    if (!qrGenerated) {
      new QRCode(document.getElementById("qrcode"), {
        text: runnerRegisterUrl,
        width: 180,
        height: 180,
        colorDark : "#000000",
        colorLight : "#ffffff",
        correctLevel : QRCode.CorrectLevel.H
      });
      qrGenerated = true;
    }
  }

  function closeQrModal() {
    document.getElementById('qrModal').classList.add('hidden');
  }

  function copyQrLink() {
    navigator.clipboard.writeText(runnerRegisterUrl).then(() => {
      alert("Pautan pendaftaran pelatih disalin: " + runnerRegisterUrl);
    });
  }

  // Receipt Modal
  function openReceiptModal() {
    document.getElementById('receiptModal').classList.remove('hidden');
  }

  function closeReceiptModal() {
    document.getElementById('receiptModal').classList.add('hidden');
  }
</script>

</body>
</html>
