<?php
// hosting_infinityfree/training-management/db_connect.php
// Konfigurasi Sambungan Pangkalan Data Kebal HTTP 500 (MySQL & SQLite Auto-Fallback)

// Dayakan pelaporan ralat untuk mengelakkan blank 500 error
ini_set('display_errors', 0); // Sembunyikan output ralat mentah daripada merosakkan paparan
error_reporting(E_ALL);

if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

date_default_timezone_set("Asia/Kuching"); // Zon Masa Malaysia (+08:00)

// 1. Muat turun tetapan konfigurasi jika wujud config.php berasingan
$db_host = "localhost"; // InfinityFree: gantikan dengan cth sql305.infinityfree.com (lihat cPanel MySQL Details)
$db_user = "if0_41886177";
$db_pass = "YOUR_MYSQL_PASSWORD";
$db_name = "if0_41886177_registry_psyco";

if (file_exists(__DIR__ . '/config.php')) {
    include __DIR__ . '/config.php';
}

$pdo = null;
$db_driver = "none";
$db_error_message = "";

// 2. Cuba sambung ke MySQL InfinityFree
if (!empty($db_pass) && $db_pass !== "YOUR_MYSQL_PASSWORD") {
    try {
        $pdo = new PDO("mysql:host=$db_host;dbname=$db_name;charset=utf8mb4", $db_user, $db_pass, [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false,
            PDO::ATTR_TIMEOUT => 4
        ]);
        $db_driver = "mysql";
    } catch (Exception $e) {
        $pdo = null;
        $db_error_message = $e->getMessage();
    }
}

// 3. Jika MySQL belum dikonfigurasi, gunakan fail SQLite setempat
if (!$pdo) {
    try {
        $sqlite_file = __DIR__ . '/database_psyco.sqlite';
        $pdo = new PDO("sqlite:" . $sqlite_file);
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
        $db_driver = "sqlite";
    } catch (Exception $e) {
        // Jika pelayan tidak membenarkan tulis fail, gunakan in-memory SQLite agar web TIDAK CRASH 500!
        try {
            $pdo = new PDO("sqlite::memory:");
            $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
            $db_driver = "sqlite_memory";
        } catch (Exception $e2) {
            $pdo = null;
            $db_driver = "failed";
        }
    }
}

// 4. Auto-Cipta Jadual & Auto-Tambah Ruang (Mencegah Table/Column Not Found Error)
if ($pdo) {
    try {
        if ($db_driver === 'sqlite' || $db_driver === 'sqlite_memory') {
            $pdo->exec("
                CREATE TABLE IF NOT EXISTS coaches (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    nickname TEXT DEFAULT '',
                    email TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    ic_number TEXT DEFAULT '',
                    phone TEXT DEFAULT '',
                    club_name TEXT DEFAULT '',
                    club_address TEXT DEFAULT '',
                    training_specialty TEXT DEFAULT 'Balapan & Padang',
                    role TEXT DEFAULT 'COACH',
                    is_approved INTEGER DEFAULT 1,
                    is_verified INTEGER DEFAULT 1,
                    subscription_status TEXT DEFAULT 'TRIAL_7_DAYS',
                    subscription_expires_at INTEGER DEFAULT 0,
                    sub_coach_slots INTEGER DEFAULT 1,
                    club_logo_uri TEXT DEFAULT '',
                    created_at INTEGER DEFAULT 0
                );

                CREATE TABLE IF NOT EXISTS athletes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    coach_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    age INTEGER DEFAULT 16,
                    dob TEXT DEFAULT '',
                    phone TEXT DEFAULT '',
                    ic_number TEXT DEFAULT '',
                    height_cm REAL DEFAULT 0.0,
                    weight_kg REAL DEFAULT 0.0,
                    gender TEXT DEFAULT 'Lelaki',
                    sport_type TEXT DEFAULT 'Balapan',
                    category TEXT DEFAULT '100m Pecut',
                    pb_seconds REAL DEFAULT 10.50,
                    previous_pb_seconds REAL DEFAULT 10.75,
                    distance_or_score REAL DEFAULT 0.0,
                    previous_distance_or_score REAL DEFAULT 0.0,
                    photo_uri TEXT DEFAULT '',
                    monthly_fee REAL DEFAULT 60.00,
                    fee_due_date TEXT DEFAULT '',
                    notes TEXT DEFAULT '',
                    created_at INTEGER DEFAULT 0
                );

                CREATE TABLE IF NOT EXISTS timing_runs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    coach_id INTEGER DEFAULT 1,
                    session_id TEXT NOT NULL,
                    run_number INTEGER DEFAULT 1,
                    race_title TEXT DEFAULT 'Race 1',
                    formatted_time TEXT NOT NULL,
                    duration_millis INTEGER DEFAULT 0,
                    date_string TEXT NOT NULL,
                    athletes_json TEXT DEFAULT '[]',
                    cam_type TEXT DEFAULT 'CAM_1_START'
                );

                CREATE TABLE IF NOT EXISTS sub_coaches (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    coach_id INTEGER NOT NULL,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    phone TEXT DEFAULT '',
                    created_at INTEGER DEFAULT 0
                );
                CREATE TABLE IF NOT EXISTS inbox_messages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    coach_id INTEGER NOT NULL,
                    sender_name TEXT DEFAULT 'Sistem Psyco Time X Pro',
                    title TEXT NOT NULL,
                    content TEXT NOT NULL,
                    date_string TEXT NOT NULL,
                    message_type TEXT DEFAULT 'MONTHLY_REPORT',
                    is_read INTEGER DEFAULT 0,
                    attached_document_title TEXT DEFAULT NULL,
                    created_at INTEGER DEFAULT 0
                );

                CREATE TABLE IF NOT EXISTS testimonials (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    coach_id INTEGER DEFAULT NULL,
                    coach_name TEXT NOT NULL,
                    club_or_role TEXT DEFAULT 'Jurulatih Balapan & Padang',
                    rating INTEGER DEFAULT 5,
                    comment TEXT NOT NULL,
                    is_approved INTEGER DEFAULT 1,
                    created_at INTEGER DEFAULT 0
                );
            ");
        } elseif ($db_driver === 'mysql') {
            // Auto-tambah lajur photo_uri dan club_logo_uri jika pengguna import skrip lama
            try { $pdo->exec("ALTER TABLE athletes ADD COLUMN photo_uri LONGTEXT DEFAULT NULL"); } catch (Exception $e) {}
            try { $pdo->exec("ALTER TABLE coaches ADD COLUMN club_logo_uri LONGTEXT DEFAULT NULL"); } catch (Exception $e) {}
            try { $pdo->exec("ALTER TABLE coaches ADD COLUMN profile_photo_uri LONGTEXT DEFAULT NULL"); } catch (Exception $e) {}
            try { $pdo->exec("ALTER TABLE coaches ADD COLUMN achievements TEXT DEFAULT NULL"); } catch (Exception $e) {}
            try { $pdo->exec("ALTER TABLE coaches ADD COLUMN licenses TEXT DEFAULT NULL"); } catch (Exception $e) {}
            try { $pdo->exec("ALTER TABLE coaches ADD COLUMN bio TEXT DEFAULT NULL"); } catch (Exception $e) {}

            try {
                $pdo->exec("CREATE TABLE IF NOT EXISTS `inbox_messages` (
                    `id` INT AUTO_INCREMENT PRIMARY KEY,
                    `coach_id` INT NOT NULL,
                    `sender_name` VARCHAR(150) DEFAULT 'Sistem Psyco Time X Pro',
                    `title` VARCHAR(255) NOT NULL,
                    `content` TEXT NOT NULL,
                    `date_string` VARCHAR(30) NOT NULL,
                    `message_type` VARCHAR(50) DEFAULT 'MONTHLY_REPORT',
                    `is_read` TINYINT(1) DEFAULT 0,
                    `attached_document_title` VARCHAR(255) DEFAULT NULL,
                    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            } catch (Exception $e) {}

            try {
                $pdo->exec("CREATE TABLE IF NOT EXISTS `testimonials` (
                    `id` INT AUTO_INCREMENT PRIMARY KEY,
                    `coach_id` INT DEFAULT NULL,
                    `coach_name` VARCHAR(150) NOT NULL,
                    `club_or_role` VARCHAR(150) DEFAULT 'Jurulatih Balapan & Padang',
                    `rating` INT DEFAULT 5,
                    `comment` TEXT NOT NULL,
                    `is_approved` TINYINT(1) DEFAULT 1,
                    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            } catch (Exception $e) {}
        }

        // Pastikan Master Admin Roger wujud untuk membolehkan pengesahan
        $chk = $pdo->prepare("SELECT COUNT(*) FROM coaches WHERE email = ?");
        $chk->execute(['Saliparjipun.atukoi@gmail.com']);
        if ($chk->fetchColumn() == 0) {
            $ins = $pdo->prepare("INSERT INTO coaches 
                (name, nickname, email, password, club_name, training_specialty, role, is_approved, is_verified, subscription_status, subscription_expires_at)
                VALUES ('Roger (Master Admin)', 'Coach Roger', 'Saliparjipun.atukoi@gmail.com', 'Abc@1234', 'Kelab Olahraga Psyco Time X Pro', 'Balapan & Padang', 'ADMIN', 1, 1, 'ACTIVE', 9999999999000)");
            $ins->execute();
        }
    } catch (Exception $e) {
        // Elak sebarang ralat mengganggu operasi
    }
}
