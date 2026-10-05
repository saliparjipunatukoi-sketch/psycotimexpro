<?php
// hosting_infinityfree/training-management/db_connect.php
// Konfigurasi Sambungan Pangkalan Data MySQL InfinityFree

// Sila kemas kini butiran MySQL di bawah mengikut cPanel InfinityFree anda:
$db_host = "localhost"; // cth: sql305.infinityfree.com (lihat dalam cPanel InfinityFree)
$db_user = "if0_41886177"; // Username MySQL InfinityFree anda
$db_pass = "YOUR_MYSQL_PASSWORD"; // Kata laluan MySQL InfinityFree anda
$db_name = "if0_41886177_registry_psyco"; // Nama pangkalan data MySQL anda

date_default_timezone_set("Asia/Kuching"); // Zon Masa Malaysia (Sabah/Sarawak/KL +08:00)

try {
    $pdo = new PDO("mysql:host=$db_host;dbname=$db_name;charset=utf8mb4", $db_user, $db_pass, [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false,
    ]);
} catch (PDOException $e) {
    // Sekiranya belum configure DB, kembalikan ralat mesra pengguna
    if (basename($_SERVER['PHP_SELF']) == 'api.php') {
        header('Content-Type: application/json');
        echo json_encode([
            'status' => 'error',
            'message' => 'Ralat sambungan database MySQL InfinityFree: ' . $e->getMessage(),
            'hint' => 'Sila semak $db_host, $db_user, $db_pass, dan $db_name dalam db_connect.php'
        ]);
        exit;
    } else {
        die("<div style='background:#161618;color:#E50914;font-family:monospace;padding:20px;margin:20px;border:1px solid #E50914;border-radius:8px;'>
            <h3>Ralat Sambungan MySQL InfinityFree</h3>
            <p>Sila pastikan butiran pangkalan data dalam <code>db_connect.php</code> adalah betul mengikut maklumat MySQL di cPanel InfinityFree anda.</p>
            <p><strong>Mesej Ralat:</strong> " . htmlspecialchars($e->getMessage()) . "</p>
        </div>");
    }
}
?>
