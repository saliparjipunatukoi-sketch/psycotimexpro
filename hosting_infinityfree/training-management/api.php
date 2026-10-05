<?php
// hosting_infinityfree/training-management/api.php
// REST API Antara Aplikasi Android & Pangkalan Data MySQL InfinityFree
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    exit;
}

require_once __DIR__ . '/db_connect.php';

$raw_input = file_get_contents('php://input');
$data = json_decode($raw_input, true) ?: $_POST;
$action = $_GET['action'] ?? $data['action'] ?? 'ping';

// 1. PING / TEST CONNECTION
if ($action === 'ping') {
    echo json_encode([
        'status' => 'success',
        'message' => 'API Psyco Time X Pro berfungsi dengan baik!',
        'server_time' => date('Y-m-d H:i:s'),
        'target_db' => $db_name
    ]);
    exit;
}

// 2. SYNC PUSH (Dari Telefon Android ke MySQL InfinityFree)
if ($action === 'sync_push') {
    $coach_id = intval($data['coach_id'] ?? 1);
    $athletes = $data['athletes'] ?? [];
    $timing_runs = $data['timing_runs'] ?? [];
    $attendances = $data['attendances'] ?? [];
    $fees = $data['fee_payments'] ?? [];

    $synced_athletes = 0;
    $synced_runs = 0;

    try {
        $pdo->beginTransaction();

        // Sync Athletes
        $ath_stmt = $pdo->prepare("INSERT INTO athletes 
            (coach_id, name, ic_number, dob, age, phone, height_cm, weight_kg, gender, sport_type, category, pb_seconds, monthly_fee, fee_due_date, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE 
            age = VALUES(age), height_cm = VALUES(height_cm), weight_kg = VALUES(weight_kg), pb_seconds = VALUES(pb_seconds)");

        foreach ($athletes as $a) {
            $ath_stmt->execute([
                $coach_id,
                $a['name'] ?? 'Atlet',
                $a['ic_number'] ?? '',
                $a['dob'] ?? '',
                intval($a['age'] ?? 16),
                $a['phone'] ?? '',
                floatval($a['height_cm'] ?? 0.0),
                floatval($a['weight_kg'] ?? 0.0),
                $a['gender'] ?? 'Lelaki',
                $a['sport_type'] ?? 'Balapan',
                $a['category'] ?? '100m Pecut',
                floatval($a['pb_seconds'] ?? 10.50),
                floatval($a['monthly_fee'] ?? 60.00),
                $a['fee_due_date'] ?? '',
                $a['notes'] ?? ''
            ]);
            $synced_athletes++;
        }

        // Sync Timing Runs
        $run_stmt = $pdo->prepare("INSERT INTO timing_runs 
            (coach_id, session_id, run_number, formatted_time, duration_millis, date_string, athletes_json, cam_type)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

        foreach ($timing_runs as $r) {
            $run_stmt->execute([
                $coach_id,
                $r['session_id'] ?? 'sess_1',
                intval($r['run_number'] ?? 1),
                $r['formatted_time'] ?? '00:00.000',
                intval($r['duration_millis'] ?? 0),
                $r['date_string'] ?? date('d/m/Y'),
                $r['athletes_json'] ?? '[]',
                $r['cam_type'] ?? 'CAM_2_FINISH'
            ]);
            $synced_runs++;
        }

        $pdo->commit();

        echo json_encode([
            'status' => 'success',
            'message' => "Penyegerakan berjaya: $synced_athletes Atlet & $synced_runs Larian telah disimpan ke pangkalan data web.",
            'synced_athletes' => $synced_athletes,
            'synced_runs' => $synced_runs
        ]);
        exit;
    } catch (Exception $e) {
        if ($pdo->inTransaction()) $pdo->rollBack();
        echo json_encode([
            'status' => 'error',
            'message' => 'Ralat penyegerakan: ' . $e->getMessage()
        ]);
        exit;
    }
}

// 3. SYNC PULL (Ambil data dari MySQL InfinityFree untuk dimuat turun ke Telefon Android)
if ($action === 'sync_pull') {
    $coach_id = intval($data['coach_id'] ?? $_GET['coach_id'] ?? 1);

    try {
        $stmt_a = $pdo->prepare("SELECT * FROM athletes WHERE coach_id = ? ORDER BY id DESC");
        $stmt_a->execute([$coach_id]);
        $athletes = $stmt_a->fetchAll();

        $stmt_r = $pdo->prepare("SELECT * FROM timing_runs WHERE coach_id = ? ORDER BY id DESC LIMIT 50");
        $stmt_r->execute([$coach_id]);
        $runs = $stmt_r->fetchAll();

        echo json_encode([
            'status' => 'success',
            'athletes' => $athletes,
            'timing_runs' => $runs
        ]);
        exit;
    } catch (Exception $e) {
        echo json_encode(['status' => 'error', 'message' => $e->getMessage()]);
        exit;
    }
}

// 4. ACTION TIDAK DIKETAHUI
echo json_encode([
    'status' => 'error',
    'message' => 'Tindakan API tidak sah: ' . htmlspecialchars($action)
]);
exit;
?>
