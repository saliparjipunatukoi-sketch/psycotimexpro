<?php
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/db_config.php';

// Rakam data masuk untuk debugging sync dari APK
$raw_input = file_get_contents('php://input');
if (!empty($raw_input)) {
    file_put_contents('debug_api.txt', date('Y-m-d H:i:s') . " - " . $raw_input . PHP_EOL, FILE_APPEND);
}

$action = $_GET['action'] ?? '';

// 1. LOGIN API
if ($action === 'login' && $_SERVER['REQUEST_METHOD'] === 'POST') {
    $input = json_decode($raw_input, true);
    $username = trim($input['username'] ?? '');
    $password = trim($input['password'] ?? '');

    if ($username === ADMIN_EMAIL && $password === 'Abc@1234') {
        echo json_encode([
            'status' => 'success',
            'user' => [
                'id' => 1,
                'email' => ADMIN_EMAIL,
                'name' => 'Roger (Master Admin)',
                'role' => 'ADMIN',
                'is_approved' => 1,
                'subscription_status' => 'ACTIVE'
            ]
        ]);
        exit;
    }

    // Check Coach
    $stmt = $pdo->prepare("SELECT * FROM coaches WHERE email = ? LIMIT 1");
    $stmt->execute([$username]);
    $coach = $stmt->fetch();

    if ($coach && ($password === $coach['password_hash'] || password_verify($password, $coach['password_hash']))) {
        echo json_encode([
            'status' => 'success',
            'user' => [
                'id' => $coach['id'],
                'email' => $coach['email'],
                'name' => $coach['name'],
                'role' => $coach['role'],
                'is_approved' => intval($coach['is_approved']),
                'subscription_status' => $coach['subscription_status'],
                'expires_at' => $coach['subscription_expires_at']
            ]
        ]);
        exit;
    }

    echo json_encode(['status' => 'error', 'message' => 'Emel atau kata laluan tidak tepat.']);
    exit;
}

// 2. GET RUNNERS FOR COACH
if ($action === 'get_runners') {
    $coach_id = intval($_GET['coach_id'] ?? 1);
    $stmt = $pdo->prepare("SELECT * FROM runners WHERE coach_id = ? ORDER BY sport_type ASC, id DESC");
    $stmt->execute([$coach_id]);
    echo json_encode(['status' => 'success', 'runners' => $stmt->fetchAll()]);
    exit;
}

// 3. SYNC RUNNERS FROM APP[cite: 15]
if ($action === 'sync_runners' && $_SERVER['REQUEST_METHOD'] === 'POST') {
    $input = json_decode($raw_input, true);
    $coach_id = intval($input['coach_id'] ?? 1);
    $runners = $input['runners'] ?? [];

    $count = 0;
    foreach ($runners as $r) {
        $stmt = $pdo->prepare("
            INSERT INTO runners (coach_id, name, ic_number, dob, age, phone, height_cm, weight_kg, gender, sport_type, category, pb_seconds, monthly_fee, fee_due_date, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE pb_seconds = VALUES(pb_seconds), height_cm = VALUES(height_cm), weight_kg = VALUES(weight_kg)
        ");
        $stmt->execute([
            $coach_id,
            $r['name'] ?? '',
            $r['ic_number'] ?? '',
            $r['dob'] ?? '',
            intval($r['age'] ?? 16),
            $r['phone'] ?? '',
            floatval($r['height_cm'] ?? 0),
            floatval($r['weight_kg'] ?? 0),
            $r['gender'] ?? 'Lelaki',
            $r['sport_type'] ?? 'Balapan',
            $r['category'] ?? '100m Pecut',
            floatval($r['pb'] ?? 0),
            floatval($r['monthly_fee'] ?? 60),
            $r['fee_due_date'] ?? '',
            $r['notes'] ?? ''
        ]);
        $count++;
    }
    echo json_encode(['status' => 'success', 'synced_count' => $count]);
    exit;
}

// Default Fallback
echo json_encode([
    'status' => 'online',
    'app' => 'Psyco Time X Pro Training Management API',
    'version' => '2.0',
    'admin' => ADMIN_NAME . ' (+' . ADMIN_WHATSAPP . ')'
]);
?>