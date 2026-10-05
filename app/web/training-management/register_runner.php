<?php
require_once __DIR__ . '/db_config.php';

$coach_id = isset($_GET['coach_id']) ? intval($_GET['coach_id']) : 1;

// Fetch Coach Details
$stmt = $pdo->prepare("SELECT name, phone FROM coaches WHERE id = ?");
$stmt->execute([$coach_id]);
$coach = $stmt->fetch();
$coach_name = $coach ? $coach['name'] : 'Jurulatih Psyco Time X Pro';

$success_message = '';
$error_message = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $name = trim($_POST['name'] ?? '');
    $ic_number = trim($_POST['ic_number'] ?? '');
    $dob = trim($_POST['dob'] ?? '');
    $age = intval($_POST['age'] ?? 0);
    $phone = trim($_POST['phone'] ?? '');
    $height_cm = floatval($_POST['height_cm'] ?? 0);
    $weight_kg = floatval($_POST['weight_kg'] ?? 0);
    $gender = trim($_POST['gender'] ?? 'Lelaki');
    $sport_type = trim($_POST['sport_type'] ?? 'Balapan');
    $category = trim($_POST['category'] ?? '100m Pecut');
    $pb_seconds = floatval($_POST['pb_seconds'] ?? 0.0);
    $notes = trim($_POST['notes'] ?? '');

    if (empty($name) || empty($ic_number) || empty($dob)) {
        $error_message = 'Sila lengkapkan Nama Penuh, No. Kad Pengenalan dan Tarikh Lahir.';
    } else {
        try {
            $insert = $pdo->prepare("
                INSERT INTO runners (coach_id, name, ic_number, dob, age, phone, height_cm, weight_kg, gender, sport_type, category, pb_seconds, monthly_fee, fee_due_date, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 60.00, DATE_FORMAT(DATE_ADD(NOW(), INTERVAL 1 MONTH), '%Y-%m-%d'), ?)
            ");
            $insert->execute([$coach_id, $name, $ic_number, $dob, $age, $phone, $height_cm, $weight_kg, $gender, $sport_type, $category, $pb_seconds, $notes]);
            $success_message = "Tahniah! Pendaftaran pelatih '$name' telah berjaya dihantar ke jurulatih $coach_name.";
        } catch (Exception $e) {
            $error_message = 'Ralat pendaftaran: ' . $e->getMessage();
        }
    }
}
?>
<!DOCTYPE html>
<html lang="ms">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Pendaftaran Pelatih Baru | Psyco Time X Pro</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <style>
    body { background-color: #0A0A0A; color: #F5F5F7; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
    .accent-red { color: #E50914; }
    .bg-accent-red { background-color: #E50914; }
    .border-silver { border-color: #444448; }
    .card-dark { background-color: #141416; border: 1px solid #2B2B30; }
  </style>
</head>
<body class="min-h-screen flex items-center justify-center p-4">

<div class="w-full max-w-xl card-dark rounded-2xl shadow-2xl p-6 sm:p-8">
  <!-- Brand Header -->
  <div class="text-center mb-6">
    <div class="inline-block p-3 rounded-full bg-black border-2 border-red-600 mb-3">
      <span class="text-2xl font-black text-red-600 tracking-wider">PSYCO TIME <span class="text-white">X</span> PRO</span>
    </div>
    <h1 class="text-xl font-extrabold text-white">BORANG PENDAFTARAN PELATIH BARU</h1>
    <p class="text-sm text-gray-400 mt-1">Jurulatih: <span class="text-red-500 font-semibold"><?= htmlspecialchars($coach_name) ?></span></p>
  </div>

  <?php if ($success_message): ?>
    <div class="bg-green-950 border border-green-500 text-green-300 p-4 rounded-xl mb-6 text-center">
      <p class="font-bold text-base"><?= $success_message ?></p>
      <p class="text-xs text-green-400 mt-2">Data anda telah direkodkan dalam sistem pemantauan jurulatih.</p>
      <a href="register_runner.php?coach_id=<?= $coach_id ?>" class="inline-block mt-4 px-4 py-2 bg-green-600 hover:bg-green-700 text-black font-bold rounded-lg text-sm">Daftar Pelatih Lain</a>
    </div>
  <?php else: ?>

    <?php if ($error_message): ?>
      <div class="bg-red-950 border border-red-600 text-red-300 p-3 rounded-lg mb-4 text-sm text-center">
        <?= $error_message ?>
      </div>
    <?php endif; ?>

    <form method="POST" class="space-y-4" id="runnerForm">
      <!-- Full Name -->
      <div>
        <label class="block text-xs font-semibold uppercase text-gray-300 mb-1">Nama Penuh Pelatih *</label>
        <input type="text" name="name" required placeholder="cth: Muhammad Danial Bin Azman" class="w-full bg-black border border-gray-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-red-600 text-sm">
      </div>

      <!-- IC Number -->
      <div>
        <label class="block text-xs font-semibold uppercase text-gray-300 mb-1">No. Kad Pengenalan / Surat Beranak *</label>
        <input type="text" name="ic_number" required placeholder="cth: 090412-12-5567" class="w-full bg-black border border-gray-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-red-600 text-sm">
      </div>

      <!-- DOB & Age Grid -->
      <div class="grid grid-cols-2 gap-3">
        <div>
          <label class="block text-xs font-semibold uppercase text-gray-300 mb-1">Tarikh Lahir *</label>
          <input type="date" name="dob" id="dobInput" required class="w-full bg-black border border-gray-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-red-600 text-sm">
        </div>
        <div>
          <label class="block text-xs font-semibold uppercase text-gray-300 mb-1">Umur (Tahun)</label>
          <input type="number" name="age" id="ageInput" readonly class="w-full bg-gray-900 border border-gray-700 rounded-lg px-3 py-2 text-red-500 font-bold text-sm">
        </div>
      </div>

      <!-- Height, Weight & Gender Grid -->
      <div class="grid grid-cols-3 gap-3">
        <div>
          <label class="block text-xs font-semibold uppercase text-gray-300 mb-1">Tinggi (cm)</label>
          <input type="number" step="0.1" name="height_cm" placeholder="175" class="w-full bg-black border border-gray-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-red-600 text-sm">
        </div>
        <div>
          <label class="block text-xs font-semibold uppercase text-gray-300 mb-1">Berat (kg)</label>
          <input type="number" step="0.1" name="weight_kg" placeholder="65" class="w-full bg-black border border-gray-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-red-600 text-sm">
        </div>
        <div>
          <label class="block text-xs font-semibold uppercase text-gray-300 mb-1">Jantina</label>
          <select name="gender" class="w-full bg-black border border-gray-700 rounded-lg px-2 py-2 text-white focus:outline-none focus:border-red-600 text-sm">
            <option value="Lelaki">Lelaki</option>
            <option value="Perempuan">Perempuan</option>
          </select>
        </div>
      </div>

      <!-- Phone -->
      <div>
        <label class="block text-xs font-semibold uppercase text-gray-300 mb-1">No. Telefon (Atlet / Penjaga)</label>
        <input type="tel" name="phone" placeholder="cth: 019-8765432" class="w-full bg-black border border-gray-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-red-600 text-sm">
      </div>

      <!-- Sport Selection: Balapan vs Padang (Automatic Categorization) -->
      <div class="p-3 bg-black/60 rounded-xl border border-gray-800">
        <label class="block text-xs font-bold uppercase text-red-500 mb-2">PILIHAN SUKAN & ACARA LATIHAN *</label>
        <div class="grid grid-cols-2 gap-3 mb-3">
          <label class="flex items-center space-x-2 p-2 rounded-lg border border-gray-700 cursor-pointer hover:border-red-600">
            <input type="radio" name="sport_type" value="Balapan" checked id="radioBalapan" onchange="updateEventOptions()">
            <span class="text-sm font-bold text-white">Acara Balapan (Track)</span>
          </label>
          <label class="flex items-center space-x-2 p-2 rounded-lg border border-gray-700 cursor-pointer hover:border-red-600">
            <input type="radio" name="sport_type" value="Padang" id="radioPadang" onchange="updateEventOptions()">
            <span class="text-sm font-bold text-white">Acara Padang (Field)</span>
          </label>
        </div>

        <div>
          <label class="block text-xs font-semibold text-gray-300 mb-1">Acara Khusus:</label>
          <select name="category" id="eventSelect" class="w-full bg-black border border-gray-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-red-600 text-sm">
            <!-- Dynamically populated via JS -->
          </select>
        </div>
      </div>

      <!-- Personal Best (PB) Time/Distance -->
      <div class="grid grid-cols-2 gap-3">
        <div>
          <label class="block text-xs font-semibold uppercase text-gray-300 mb-1">Catatan Masa Terbaik (PB Saat)</label>
          <input type="number" step="0.01" name="pb_seconds" placeholder="cth: 10.55" class="w-full bg-black border border-gray-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-red-600 text-sm">
        </div>
        <div>
          <label class="block text-xs font-semibold uppercase text-gray-300 mb-1">Nota Kesihatan / Pengalaman</label>
          <input type="text" name="notes" placeholder="cth: Tiada alahan, bekas wakil sekolah" class="w-full bg-black border border-gray-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-red-600 text-sm">
        </div>
      </div>

      <button type="submit" class="w-full py-3 bg-red-600 hover:bg-red-700 text-white font-extrabold rounded-xl transition duration-150 uppercase tracking-wider text-sm shadow-lg shadow-red-900/50">
        Hantar Pendaftaran Pelatih
      </button>
    </form>
  <?php endif; ?>

  <div class="mt-6 text-center text-xs text-gray-500">
    <p>Sistem Pemantauan Sukan & Latihan &copy; <?= date('Y') ?> Psyco Time X Pro</p>
    <p class="mt-1">Sebarang bantuan hubungi Admin: <a href="https://wa.me/60195326399" class="text-red-500 hover:underline font-bold">+60195326399 (Roger)</a></p>
  </div>
</div>

<script>
  // Auto Age Calculator
  const dobInput = document.getElementById('dobInput');
  const ageInput = document.getElementById('ageInput');
  if (dobInput) {
    dobInput.addEventListener('change', function() {
      if (!this.value) return;
      const dob = new Date(this.value);
      const diff = Date.now() - dob.getTime();
      const ageDate = new Date(diff);
      const calculatedAge = Math.abs(ageDate.getUTCFullYear() - 1970);
      ageInput.value = calculatedAge;
    });
  }

  // Dynamic Event Options (Balapan vs Padang)
  const trackEvents = [
    "100m Pecut",
    "200m Pecut",
    "400m Pecut",
    "800m Jarak Sederhana",
    "1500m Jarak Jauh",
    "110m Lari Berpagar",
    "100m Lari Berpagar (Wanita)",
    "400m Lari Berpagar",
    "4x100m Berganti-ganti",
    "4x400m Berganti-ganti"
  ];

  const fieldEvents = [
    "Lompat Jauh (Long Jump)",
    "Lompat Kijang (Triple Jump)",
    "Lompat Tinggi (High Jump)",
    "Lompat Bergalah (Pole Vault)",
    "Lontar Peluru (Shot Put)",
    "Lempar Cakera (Discus Throw)",
    "Rejam Lembing (Javelin Throw)",
    "Baling Tukul Besi (Hammer Throw)"
  ];

  function updateEventOptions() {
    const isBalapan = document.getElementById('radioBalapan').checked;
    const select = document.getElementById('eventSelect');
    select.innerHTML = '';
    const events = isBalapan ? trackEvents : fieldEvents;
    events.forEach(ev => {
      const opt = document.createElement('option');
      opt.value = ev;
      opt.textContent = ev;
      select.appendChild(opt);
    });
  }

  updateEventOptions();
</script>

</body>
</html>
