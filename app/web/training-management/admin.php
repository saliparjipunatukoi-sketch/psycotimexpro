<?php
session_start();
require_once __DIR__ . '/db_config.php';

// Check Admin Access
if (!isset($_SESSION['user_id']) || $_SESSION['role'] !== 'ADMIN' || $_SESSION['email'] !== ADMIN_EMAIL) {
    header("Location: index.php");
    exit;
}

$notice = '';

// Approve Coach
if (isset($_GET['approve'])) {
    $c_id = intval($_GET['approve']);
    $pdo->prepare("UPDATE coaches SET is_approved = 1, subscription_status = 'FREE_TRIAL', trial_end_date = DATE_ADD(NOW(), INTERVAL 7 DAY), subscription_expires_at = DATE_ADD(NOW(), INTERVAL 7 DAY) WHERE id = ?")->execute([$c_id]);
    $notice = "Coach ID $c_id berjaya diluluskan dengan percuma 7 hari penggunaan.";
}

// Extend Subscription
if (isset($_POST['extend_days'])) {
    $c_id = intval($_POST['coach_id']);
    $days = intval($_POST['days']);
    $pdo->prepare("UPDATE coaches SET subscription_status = 'ACTIVE', subscription_expires_at = DATE_ADD(IF(subscription_expires_at > NOW(), subscription_expires_at, NOW()), INTERVAL ? DAY) WHERE id = ?")->execute([$days, $c_id]);
    $notice = "Langganan Coach ID $c_id dilanjutkan sebanyak $days hari.";
}

// Delete Coach
if (isset($_GET['delete_coach'])) {
    $c_id = intval($_GET['delete_coach']);
    if ($c_id !== 1) {
        $pdo->prepare("DELETE FROM coaches WHERE id = ?")->execute([$c_id]);
        $notice = "Akaun coach dipadam.";
    }
}

// Fetch all coaches
$coaches = $pdo->query("SELECT * FROM coaches WHERE role != 'ADMIN' ORDER BY id DESC")->fetchAll();
?>
<!DOCTYPE html>
<html lang="ms">
<head>
  <meta charset="UTF-8">
  <title>Master Admin Panel | Psyco Time X Pro</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <style>
    body { background-color: #0A0A0A; color: #F5F5F7; font-family: sans-serif; }
    .card-dark { background-color: #141416; border: 1px solid #2B2B30; }
  </style>
</head>
<body class="p-6">
  <div class="max-w-6xl mx-auto">
    <div class="flex items-center justify-between card-dark p-6 rounded-2xl mb-6">
      <div>
        <span class="px-2.5 py-0.5 rounded-full text-xs font-bold bg-red-950 text-red-400 border border-red-600">MASTER ADMIN</span>
        <h1 class="text-2xl font-black text-white mt-1">PENGURUSAN JURULATIH & LANGGANAN</h1>
        <p class="text-xs text-gray-400">Admin: <span class="text-red-500 font-bold"><?= ADMIN_EMAIL ?></span> (+<?= ADMIN_WHATSAPP ?>)</p>
      </div>
      <div class="flex space-x-2">
        <a href="index.php" class="px-4 py-2 bg-gray-800 hover:bg-gray-700 text-white font-bold rounded-lg text-xs">Papan Pemuka Coach</a>
        <a href="index.php?action=logout" class="px-4 py-2 bg-red-600 hover:bg-red-700 text-white font-bold rounded-lg text-xs">Log Keluar</a>
      </div>
    </div>

    <?php if ($notice): ?>
      <div class="bg-green-950 border border-green-500 text-green-300 p-3 rounded-lg mb-6 text-sm">
        <?= htmlspecialchars($notice) ?>
      </div>
    <?php endif; ?>

    <div class="card-dark rounded-2xl p-6">
      <h2 class="text-lg font-black text-white uppercase mb-4">SENARAI SEMUA JURULATIH MENDAFTAR (<?= count($coaches) ?>)</h2>
      <div class="overflow-x-auto">
        <table class="w-full text-left text-sm">
          <thead class="bg-black text-xs uppercase text-gray-400 border-b border-gray-800">
            <tr>
              <th class="p-3">Nama Coach</th>
              <th class="p-3">Emel & Telefon</th>
              <th class="p-3">Status Kelulusan</th>
              <th class="p-3">Status Langganan</th>
              <th class="p-3">Tarikh Luput</th>
              <th class="p-3 text-right">Tindakan Admin</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-800">
            <?php foreach ($coaches as $c): ?>
              <tr class="hover:bg-black/40">
                <td class="p-3 font-bold text-white"><?= htmlspecialchars($c['name']) ?></td>
                <td class="p-3 text-xs">
                  <?= htmlspecialchars($c['email']) ?>
                  <span class="block text-gray-400"><?= htmlspecialchars($c['phone']) ?></span>
                </td>
                <td class="p-3">
                  <?php if ($c['is_approved']): ?>
                    <span class="px-2 py-0.5 bg-green-950 text-green-400 border border-green-600 rounded text-xs font-bold">Diluluskan</span>
                  <?php else: ?>
                    <a href="?approve=<?= $c['id'] ?>" class="px-2.5 py-1 bg-yellow-600 hover:bg-yellow-700 text-black font-extrabold rounded text-xs">Luluskan Sekarang</a>
                  <?php endif; ?>
                </td>
                <td class="p-3 text-xs font-semibold">
                  <span class="text-red-400"><?= htmlspecialchars($c['subscription_status']) ?></span>
                </td>
                <td class="p-3 text-xs font-mono text-gray-300">
                  <?= htmlspecialchars($c['subscription_expires_at'] ?: '-') ?>
                </td>
                <td class="p-3 text-right space-x-2">
                  <form method="POST" class="inline-flex items-center space-x-1">
                    <input type="hidden" name="coach_id" value="<?= $c['id'] ?>">
                    <select name="days" class="bg-black border border-gray-700 text-xs text-white rounded p-1">
                      <option value="7">+7 Hari Percuma</option>
                      <option value="30">+30 Hari (RM30)</option>
                      <option value="90">+90 Hari</option>
                    </select>
                    <button type="submit" name="extend_days" class="px-2 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded text-xs font-bold">Tambah</button>
                  </form>
                  <a href="https://wa.me/<?= preg_replace('/[^0-9]/', '', $c['phone']) ?>?text=Salam%20Coach,%20akaun%20PsycoTimeXPro%20anda" target="_blank" class="text-xs text-green-400 font-bold hover:underline">WhatsApp</a>
                  <a href="?delete_coach=<?= $c['id'] ?>" onclick="return confirm('Padam coach ini?')" class="text-xs text-red-500 hover:underline">Padam</a>
                </td>
              </tr>
            <?php endforeach; ?>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</body>
</html>
