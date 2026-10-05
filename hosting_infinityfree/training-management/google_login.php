<?php
// hosting_infinityfree/training-management/google_login.php
// Log Masuk Google Tanpa Password (1-Click Google Email Sign-In)
session_start();
require_once __DIR__ . '/db_connect.php';
require_once __DIR__ . '/mailer.php';

$error = "";

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = trim($_POST['google_email'] ?? '');
    $coach_name = trim($_POST['google_name'] ?? '');
    $club_name = trim($_POST['google_club'] ?? '');

    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        header("Location: login.php?error=" . urlencode("Sila masukkan alamat Emel Google yang sah."));
        exit;
    }

    try {
        // Semak sama ada akaun dengan emel Google ini telah wujud
        $stmt = $pdo->prepare("SELECT * FROM coaches WHERE email = ?");
        $stmt->execute([$email]);
        $coach = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($coach) {
            // Akaun sedia ada - Log masuk serta-merta tanpa perlu password!
            $_SESSION['user_type'] = 'coach';
            $_SESSION['coach_id'] = $coach['id'];
            $_SESSION['coach_email'] = $coach['email'];
            $_SESSION['coach_name'] = $coach['name'];
            $_SESSION['coach_role'] = $coach['role'];

            $welcomeMsg = "Log masuk Google berjaya! Selamat datang " . (!empty($coach['nickname']) ? $coach['nickname'] : $coach['name']) . ".";
            header("Location: index.php?msg=" . urlencode($welcomeMsg));
            exit;
        } else {
            // Akaun Google baharu - Auto daftar dengan Percuma 7 Hari serta-merta tanpa perlu password!
            if (empty($coach_name)) {
                $parts = explode('@', $email);
                $coach_name = ucwords(str_replace(['.', '_', '-'], ' ', $parts[0]));
            }
            if (empty($club_name)) {
                $club_name = "Kelab Olahraga & Balapan";
            }
            $nickname = "Coach " . substr($coach_name, 0, 15);
            $expires_at = (time() + (7 * 24 * 3600)) * 1000;
            $verify_code = (string)rand(100000, 999999);

            $ins = $pdo->prepare("INSERT INTO coaches 
                (name, nickname, email, password, club_name, training_specialty, role, is_approved, is_verified, subscription_status, subscription_expires_at, sub_coach_slots, verification_code)
                VALUES (?, ?, ?, 'GOOGLE_AUTH_NOPASS', ?, 'Balapan & Padang', 'COACH', 1, 1, 'TRIAL_7_DAYS', ?, 1, ?)");
            $ins->execute([$coach_name, $nickname, $email, $club_name, $expires_at, $verify_code]);
            $new_id = $pdo->lastInsertId();

            // Hantar auto-response welcome email
            sendWelcomeEmail($email, $coach_name, $nickname, $club_name, $verify_code);

            // Log masuk serta-merta
            $_SESSION['user_type'] = 'coach';
            $_SESSION['coach_id'] = $new_id;
            $_SESSION['coach_email'] = $email;
            $_SESSION['coach_name'] = $coach_name;
            $_SESSION['coach_role'] = 'COACH';

            $welcomeMsg = "Akaun Google anda ($nickname) berjaya didaftarkan dengan Percuma 7 Hari serta-merta!";
            header("Location: index.php?msg=" . urlencode($welcomeMsg));
            exit;
        }
    } catch (Exception $e) {
        header("Location: login.php?error=" . urlencode("Ralat log masuk Google: " . $e->getMessage()));
        exit;
    }
} else {
    header("Location: login.php");
    exit;
}
