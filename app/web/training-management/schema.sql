-- ==============================================================
-- PSYCO TIME X PRO - TRAINING MANAGEMENT DATABASE SCHEMA
-- Target Database: if0_41886177_registry_psyco (InfinityFree MySQL)
-- Web Portal Path: /training-management/
-- ==============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. COACHES TABLE (Pengurusan Jurulatih & Langganan)
CREATE TABLE IF NOT EXISTS `coaches` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `email` VARCHAR(191) NOT NULL UNIQUE,
  `name` VARCHAR(191) NOT NULL,
  `phone` VARCHAR(50) DEFAULT '',
  `password_hash` VARCHAR(255) NOT NULL,
  `role` ENUM('ADMIN', 'COACH', 'SUB_COACH') DEFAULT 'COACH',
  `is_approved` TINYINT(1) DEFAULT 0,
  `subscription_status` ENUM('PENDING_APPROVAL', 'FREE_TRIAL', 'ACTIVE', 'EXPIRED') DEFAULT 'PENDING_APPROVAL',
  `trial_start_date` DATETIME NULL,
  `trial_end_date` DATETIME NULL,
  `subscription_expires_at` DATETIME NULL,
  `sub_coach_slots` INT DEFAULT 1,
  `parent_coach_id` INT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX(`email`),
  INDEX(`is_approved`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. SUB-COACHES TABLE (Penolong Jurulatih Didaftar oleh Coach)
CREATE TABLE IF NOT EXISTS `sub_coaches` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `parent_coach_id` INT NOT NULL,
  `username` VARCHAR(100) NOT NULL UNIQUE,
  `password_hash` VARCHAR(255) NOT NULL,
  `full_name` VARCHAR(191) NOT NULL,
  `phone` VARCHAR(50) DEFAULT '',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX(`parent_coach_id`),
  INDEX(`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. RUNNERS TABLE (Pelatih / Atlet Di bawah Jurulatih)
CREATE TABLE IF NOT EXISTS `runners` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `coach_id` INT NOT NULL,
  `name` VARCHAR(191) NOT NULL,
  `ic_number` VARCHAR(30) DEFAULT '',
  `dob` DATE NULL,
  `age` INT DEFAULT 16,
  `phone` VARCHAR(50) DEFAULT '',
  `height_cm` DECIMAL(5,2) DEFAULT 0.00,
  `weight_kg` DECIMAL(5,2) DEFAULT 0.00,
  `gender` ENUM('Lelaki', 'Perempuan') DEFAULT 'Lelaki',
  `sport_type` ENUM('Balapan', 'Padang') DEFAULT 'Balapan',
  `category` VARCHAR(100) DEFAULT '100m Pecut',
  `pb_seconds` DECIMAL(6,3) DEFAULT 0.000,
  `monthly_fee` DECIMAL(8,2) DEFAULT 50.00,
  `fee_due_date` VARCHAR(30) DEFAULT '',
  `notes` TEXT NULL,
  `registered_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX(`coach_id`),
  INDEX(`sport_type`),
  INDEX(`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. TIMING RUNS TABLE (Catatan Masa Electronic Timing)
CREATE TABLE IF NOT EXISTS `timing_runs` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `coach_id` INT NOT NULL,
  `session_id` VARCHAR(100) NOT NULL,
  `run_number` INT NOT NULL,
  `formatted_time` VARCHAR(30) NOT NULL,
  `duration_millis` BIGINT NOT NULL,
  `date_string` VARCHAR(50) NOT NULL,
  `athletes_json` TEXT NULL,
  `cam_type` VARCHAR(50) DEFAULT 'CAM_1_START',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX(`coach_id`),
  INDEX(`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. ATTENDANCE TABLE (Kehadiran Latihan)
CREATE TABLE IF NOT EXISTS `attendance` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `coach_id` INT NOT NULL,
  `runner_id` INT NOT NULL,
  `runner_name` VARCHAR(191) NOT NULL,
  `date_string` VARCHAR(30) NOT NULL,
  `session_title` VARCHAR(191) DEFAULT 'Latihan Pecut Harian',
  `status` ENUM('HADIR', 'LEWAT', 'TIDAK_HADIR', 'SAKIT') DEFAULT 'HADIR',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX(`coach_id`),
  INDEX(`date_string`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. RUNNER FEES TABLE (Yuran Bulanan Pelatih)
CREATE TABLE IF NOT EXISTS `runner_fees` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `coach_id` INT NOT NULL,
  `runner_id` INT NOT NULL,
  `runner_name` VARCHAR(191) NOT NULL,
  `title` VARCHAR(191) NOT NULL,
  `amount` DECIMAL(8,2) NOT NULL,
  `date_string` VARCHAR(30) NOT NULL,
  `status` ENUM('LUNAS', 'TERTUNGGAK') DEFAULT 'LUNAS',
  `receipt_no` VARCHAR(50) NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX(`coach_id`),
  INDEX(`runner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. SUBSCRIPTION PAYMENTS TABLE (Resit Langganan RM30/Bulan Coach)
CREATE TABLE IF NOT EXISTS `subscription_payments` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `coach_id` INT NOT NULL,
  `coach_email` VARCHAR(191) NOT NULL,
  `receipt_no` VARCHAR(50) NOT NULL,
  `amount` DECIMAL(8,2) DEFAULT 30.00,
  `payment_date` DATE NOT NULL,
  `next_payment_due` DATE NOT NULL,
  `plan_type` VARCHAR(100) DEFAULT 'COACH_MONTHLY_RM30',
  `status` ENUM('PAID', 'PENDING', 'REJECTED') DEFAULT 'PAID',
  `payment_method` VARCHAR(100) DEFAULT 'TUNAI / ONLINE TRANSFER',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX(`coach_id`),
  INDEX(`receipt_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- SEED MASTER ADMIN (Saliparjipun.atukoi@gmail.com / Abc@1234)
INSERT INTO `coaches` (
  `id`, `email`, `name`, `phone`, `password_hash`, `role`, `is_approved`, `subscription_status`, `trial_start_date`, `trial_end_date`, `subscription_expires_at`, `sub_coach_slots`
) VALUES (
  1,
  'Saliparjipun.atukoi@gmail.com',
  'Roger (Master Admin)',
  '+60195326399',
  'Abc@1234', -- Plain for initial access, can be updated in admin
  'ADMIN',
  1,
  'ACTIVE',
  NOW(),
  DATE_ADD(NOW(), INTERVAL 365 DAY),
  DATE_ADD(NOW(), INTERVAL 365 DAY),
  10
) ON DUPLICATE KEY UPDATE `is_approved` = 1, `role` = 'ADMIN', `subscription_status` = 'ACTIVE';

SET FOREIGN_KEY_CHECKS = 1;
