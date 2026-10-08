-- hosting_infinityfree/training-management/schema.sql
-- Skrip Pangkalan Data MySQL untuk Sistem Latihan Psyco Time X Pro
-- Sila import skrip ini melalui phpMyAdmin di cPanel InfinityFree anda

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Jadual Akaun Jurulatih (Coaches)
CREATE TABLE IF NOT EXISTS `coaches` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `email` VARCHAR(150) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `name` VARCHAR(150) NOT NULL, -- Nama Penuh Seperti IC
  `nickname` VARCHAR(80) DEFAULT '', -- Nick Name cth: Coach Roger
  `ic_number` VARCHAR(30) DEFAULT '', -- No. Kad Pengenalan
  `phone` VARCHAR(30) DEFAULT '',
  `club_name` VARCHAR(150) DEFAULT '', -- Nama Kelab / Akademi
  `club_address` TEXT DEFAULT NULL, -- Alamat Kelab / Lokasi Latihan
  `training_specialty` VARCHAR(150) DEFAULT 'Balapan & Padang', -- Latihan yang Diajar
  `role` VARCHAR(20) DEFAULT 'COACH', -- 'ADMIN' atau 'COACH'
  `is_approved` TINYINT(1) DEFAULT 1, -- Permulaan: Terus aktif & boleh guna 7 hari percuma serta merta
  `is_verified` TINYINT(1) DEFAULT 1, -- Verify email / no. telefon
  `verification_code` VARCHAR(20) DEFAULT '',
  `reset_token` VARCHAR(100) DEFAULT '',
  `reset_token_expires` BIGINT DEFAULT 0,
  `subscription_status` VARCHAR(30) DEFAULT 'TRIAL_7_DAYS',
  `subscription_expires_at` BIGINT NOT NULL,
  `sub_coach_slots` INT DEFAULT 1,
  `club_logo_uri` LONGTEXT DEFAULT NULL,
  `profile_photo_uri` LONGTEXT DEFAULT NULL,
  `achievements` TEXT DEFAULT NULL, -- Pencapaian Jurulatih (MSSM, Sukma, Kebangsaan)
  `licenses` TEXT DEFAULT NULL, -- Lesen (Sport Science Level 1, Level 2, World Athletics Level 1)
  `bio` TEXT DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Jadual Penolong Jurulatih (Sub-Coaches)
-- Sub-member hanya boleh log masuk ke sistem jurulatih yang menjananya
CREATE TABLE IF NOT EXISTS `sub_coaches` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `coach_id` INT NOT NULL,
  `username` VARCHAR(80) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `full_name` VARCHAR(150) NOT NULL,
  `phone` VARCHAR(30) DEFAULT '',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`coach_id`) REFERENCES `coaches`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Jadual Pelatih / Atlet (Athletes - Balapan & Padang)
CREATE TABLE IF NOT EXISTS `athletes` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `coach_id` INT NOT NULL,
  `name` VARCHAR(150) NOT NULL,
  `ic_number` VARCHAR(30) DEFAULT '',
  `dob` VARCHAR(20) DEFAULT '',
  `age` INT DEFAULT 16,
  `phone` VARCHAR(30) DEFAULT '',
  `height_cm` DECIMAL(5,2) DEFAULT 0.00,
  `weight_kg` DECIMAL(5,2) DEFAULT 0.00,
  `gender` VARCHAR(20) DEFAULT 'Lelaki',
  `sport_type` VARCHAR(30) DEFAULT 'Balapan', -- 'Balapan' atau 'Padang'
  `category` VARCHAR(100) DEFAULT '100m Pecut',
  `pb_seconds` DECIMAL(6,2) DEFAULT 10.50,
  `previous_pb_seconds` DECIMAL(6,2) DEFAULT 10.75,
  `distance_or_score` DECIMAL(6,2) DEFAULT 0.00, -- Catatan Padang (meter)
  `previous_distance_or_score` DECIMAL(6,2) DEFAULT 0.00,
  `monthly_fee` DECIMAL(8,2) DEFAULT 60.00,
  `fee_due_date` VARCHAR(20) DEFAULT '',
  `photo_uri` LONGTEXT DEFAULT NULL,
  `notes` TEXT DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`coach_id`) REFERENCES `coaches`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Jadual Catatan Masa Larian (Timing Runs - Cam 1 Mula & Cam 2 Penamat)
CREATE TABLE IF NOT EXISTS `timing_runs` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `coach_id` INT NOT NULL,
  `session_id` VARCHAR(50) NOT NULL,
  `run_number` INT NOT NULL,
  `formatted_time` VARCHAR(20) NOT NULL,
  `duration_millis` BIGINT NOT NULL,
  `date_string` VARCHAR(30) NOT NULL,
  `athletes_json` TEXT DEFAULT NULL,
  `cam_type` VARCHAR(30) DEFAULT 'CAM_2_FINISH',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`coach_id`) REFERENCES `coaches`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Jadual Kehadiran Latihan Harian (Attendance)
CREATE TABLE IF NOT EXISTS `attendance` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `coach_id` INT NOT NULL,
  `athlete_id` INT DEFAULT 0,
  `athlete_name` VARCHAR(150) NOT NULL,
  `date_string` VARCHAR(20) NOT NULL,
  `session_type` VARCHAR(50) DEFAULT 'Pagi (07:30)',
  `status` VARCHAR(20) DEFAULT 'PRESENT', -- 'PRESENT' atau 'ABSENT'
  `notes` VARCHAR(255) DEFAULT '',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`coach_id`) REFERENCES `coaches`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Jadual Pembayaran Yuran Pelatih (Fee Payments)
CREATE TABLE IF NOT EXISTS `fee_payments` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `coach_id` INT NOT NULL,
  `athlete_name` VARCHAR(150) NOT NULL,
  `amount` DECIMAL(8,2) NOT NULL,
  `payment_date` VARCHAR(20) NOT NULL,
  `receipt_no` VARCHAR(50) NOT NULL,
  `fee_month` VARCHAR(30) NOT NULL,
  `payment_method` VARCHAR(50) DEFAULT 'Tunai (Cash)',
  `status` VARCHAR(20) DEFAULT 'PAID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`coach_id`) REFERENCES `coaches`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Jadual Resit Langganan Aplikasi Jurulatih (RM30/Bulan)
CREATE TABLE IF NOT EXISTS `subscription_receipts` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `coach_id` INT NOT NULL,
  `receipt_no` VARCHAR(50) NOT NULL UNIQUE,
  `amount` DECIMAL(8,2) DEFAULT 30.00,
  `payment_date` VARCHAR(20) NOT NULL,
  `next_payment_due` VARCHAR(20) NOT NULL,
  `payment_method` VARCHAR(50) DEFAULT 'Tunai / Pemindahan Bank Disahkan Admin',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`coach_id`) REFERENCES `coaches`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Jadual Peti Masuk & Laporan Bulanan Jurulatih (Inbox Messages)
CREATE TABLE IF NOT EXISTS `inbox_messages` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `coach_id` INT NOT NULL,
  `sender_name` VARCHAR(150) DEFAULT 'Sistem Psyco Time X Pro',
  `title` VARCHAR(255) NOT NULL,
  `content` TEXT NOT NULL,
  `date_string` VARCHAR(30) NOT NULL,
  `message_type` VARCHAR(50) DEFAULT 'MONTHLY_REPORT', -- 'MONTHLY_REPORT', 'ADMIN_MEMO', 'DOCUMENT_NOTICE'
  `is_read` TINYINT(1) DEFAULT 0,
  `attached_document_title` VARCHAR(255) DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`coach_id`) REFERENCES `coaches`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Jadual Testimonial Pengguna / Jurulatih
CREATE TABLE IF NOT EXISTS `testimonials` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `coach_id` INT DEFAULT NULL,
  `coach_name` VARCHAR(150) NOT NULL,
  `club_or_role` VARCHAR(150) DEFAULT 'Jurulatih Balapan & Padang',
  `rating` INT DEFAULT 5,
  `comment` TEXT NOT NULL,
  `is_approved` TINYINT(1) DEFAULT 1,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- MASUKKAN TESTIMONIAL CONTOH AWAL
INSERT INTO `testimonials` (`coach_name`, `club_or_role`, `rating`, `comment`)
VALUES 
  ('Coach Haris Ridzuan', 'Akademi Pecut Elit Selangor', 5, 'Sistem Electronic Timing dengan video penamat dan AI posture analysis sangat membantu atlit kami tingkatkan catatan PB pecut 100m. Laporan bulanan 1hb siap automatik!'),
  ('Coach Maznah Kassim', 'Kelab Olahraga Gemilang Sabah', 5, 'Sangat mudah urus pendaftaran atlit melalui QR code dan cetak kad profile PDF secara pukal. Berbaloi hanya RM30 sebulan!'),
  ('Coach Tan Wei Lun', 'Penang Track & Field Club', 5, 'Web portal senang dipantau dari laptop. Sub-coach boleh bantu ambil masa dan kehadiran terus dari telefon.')
ON DUPLICATE KEY UPDATE `comment` = VALUES(`comment`);

-- MASUKKAN DATA MASTER ADMIN (ROGER) SECARA AUTOMATIK
-- Username: Saliparjipun.atukoi@gmail.com
-- Password: Abc@1234
INSERT INTO `coaches` (`id`, `email`, `password`, `name`, `nickname`, `ic_number`, `phone`, `club_name`, `club_address`, `training_specialty`, `role`, `is_approved`, `subscription_status`, `subscription_expires_at`, `sub_coach_slots`)
VALUES (
  1,
  'Saliparjipun.atukoi@gmail.com',
  'Abc@1234',
  'Roger (Master Admin)',
  'Coach Roger',
  '850412-12-5678',
  '+60195326399',
  'Kelab Olahraga Psyco Time X Pro',
  'Kompleks Sukan Keningau / Stadium Likas, Sabah',
  'Balapan & Padang (Pecut, Jarak Jauh & Padang)',
  'ADMIN',
  1,
  'ACTIVE_PERMANENT',
  2524608000000, -- Tahun 2050
  99
)
ON DUPLICATE KEY UPDATE `role` = 'ADMIN', `is_approved` = 1, `nickname` = 'Coach Roger';

-- MASUKKAN DATA ATLET CONTOH UNTUK CARTA RANKING
INSERT INTO `athletes` (`id`, `coach_id`, `name`, `ic_number`, `dob`, `age`, `phone`, `height_cm`, `weight_kg`, `gender`, `sport_type`, `category`, `pb_seconds`, `previous_pb_seconds`, `distance_or_score`, `previous_distance_or_score`, `monthly_fee`, `fee_due_date`, `notes`)
VALUES 
  (1, 1, 'Muhammad Danial', '090412-12-5567', '2009-04-12', 17, '011-2345678', 178.00, 68.50, 'Lelaki', 'Balapan', '100m Pecut', 10.45, 10.72, 0.00, 0.00, 60.00, '2026-10-10', 'Atlet Sukma, pecutan 30m cemerlang'),
  (2, 1, 'Ahmad Harith', '100725-12-8871', '2010-07-25', 16, '012-9876543', 172.50, 62.00, 'Lelaki', 'Balapan', '100m Pecut', 10.68, 10.85, 0.00, 0.00, 60.00, '2026-10-08', 'Fokus fasa drive'),
  (3, 1, 'Nur Aisyah Binti Zulkifli', '101103-12-6632', '2010-11-03', 16, '019-8765432', 165.00, 54.00, 'Perempuan', 'Balapan', '100m Pecut', 11.85, 12.10, 0.00, 0.00, 60.00, '2026-10-15', 'Pelari baton 4x100m'),
  (4, 1, 'Farhan Hakimi', '080214-12-3321', '2008-02-14', 18, '013-4455667', 182.00, 72.00, 'Lelaki', 'Padang', 'Lompat Jauh', 0.00, 0.00, 7.15, 6.85, 60.00, '2026-10-12', 'Pemenang Emas MSSM Lompat Jauh 7.15m'),
  (5, 1, 'Siti Sarah Binti Osman', '090819-12-4412', '2009-08-19', 17, '017-3322114', 168.00, 58.00, 'Perempuan', 'Padang', 'Lompat Jauh', 0.00, 0.00, 5.42, 5.20, 60.00, '2026-10-20', 'Peningkatan lonjakan pelepasan')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

SET FOREIGN_KEY_CHECKS = 1;
