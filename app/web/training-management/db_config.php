<?php
/**
 * PSYCO TIME X PRO - DATABASE CONFIGURATION
 * infinityfree.com MySQL Settings
 */

// Sila ubah nilai ini mengikut maklumat dalam panel InfinityFree (Control Panel -> MySQL Databases):
$db_host = 'sql109.infinityfree.com'; // cth: sql109.infinityfree.com atau localhost
$db_name = 'if0_41886177_registry_psyco';
$db_user = 'if0_41886177';
$db_pass = 'Abc@1234'; // Masukkan password vPanel InfinityFree anda

// Pilihan jika menggunakan server local / testing
if ($_SERVER['SERVER_NAME'] === 'localhost' || $_SERVER['SERVER_NAME'] === '127.0.0.1') {
    $db_host = 'localhost';
    $db_user = 'root';
    $db_pass = '';
    $db_name = 'if0_41886177_registry_psyco';
}

try {
    $pdo = new PDO(
        "mysql:host={$db_host};dbname={$db_name};charset=utf8mb4",
        $db_user,
        $db_pass,
        [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false
        ]
    );
} catch (PDOException $e) {
    // Graceful error display
    http_response_code(500);
    die(json_encode([
        'status' => 'error',
        'message' => 'Penyambungan ke Pangkalan Data MySQL InfinityFree Gagal: ' . $e->getMessage()
    ]));
}

// Master Admin Contact
define('ADMIN_EMAIL', 'Saliparjipun.atukoi@gmail.com');
define('ADMIN_WHATSAPP', '60195326399');
define('ADMIN_NAME', 'Roger');
define('BASE_URL', 'https://psycotimexpro.my/training-management');
