<?php
// hosting_infinityfree/training-management/download.php
// Halaman Muat Turun Aplikasi Rasmi Android (APK) Psyco Time X Pro
$apk_filename = "psycotimexpro.apk";
$apk_path = __DIR__ . "/" . $apk_filename;
$apk_exists = file_exists($apk_path);
$apk_size_mb = $apk_exists ? round(filesize($apk_path) / (1024 * 1024), 1) : 25.0;

// Pautan Rasmi GitHub Release & Repo oleh Roger
$github_download_url = "https://github.com/saliparjipunatukoi-sketch/psycotimexpro/releases/download/v1.0.0/psycotimexpro.apk";
$github_commit_url = "https://github.com/saliparjipunatukoi-sketch/psycotimexpro/commits/v1.0.0";
$github_release_page = "https://github.com/saliparjipunatukoi-sketch/psycotimexpro/releases";

// URL muat turun terus pelayan web (Mirror)
$current_url = (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? "https" : "http") . "://$_SERVER[HTTP_HOST]$_SERVER[REQUEST_URI]";
$download_direct_url = dirname($current_url) . "/" . $apk_filename;

// Gunakan GitHub download URL untuk QR code kerana lebih pantas dan tiada had kuota bandwidth
$qr_target_url = $github_download_url;
?>
<!DOCTYPE html>
<html lang="ms">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Muat Turun Aplikasi Android - Psyco Time X Pro</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700;800;900&family=JetBrains+Mono:wght@700;900&display=swap" rel="stylesheet">
    <style>
        :root {
            --racing-red: #E50914;
            --dark-red: #9E0B0F;
            --silver: #C0C0C0;
            --silver-light: #E5E5E5;
            --dark-bg: #0F0F11;
            --surface: #161618;
            --surface-variant: #222226;
            --border: #333338;
            --green: #00E676;
            --gold: #FFD700;
            --github-dark: #24292e;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Montserrat', sans-serif; }
        body { background: var(--dark-bg); color: #FFF; line-height: 1.6; padding: 20px 12px 60px; }
        .container { max-width: 680px; margin: 0 auto; }
        
        .header { text-align: center; margin-bottom: 24px; padding-top: 10px; }
        .logo-badge { display: inline-flex; align-items: center; justify-content: center; width: 72px; height: 72px; background: #000; border: 2.5px solid var(--racing-red); border-radius: 50%; font-family: 'JetBrains Mono', monospace; font-size: 24px; font-weight: 900; color: #FFF; margin-bottom: 12px; box-shadow: 0 0 25px rgba(229, 9, 20, 0.4); }
        .logo-badge span { color: var(--racing-red); }
        h1 { font-family: 'JetBrains Mono', monospace; font-size: 22px; font-weight: 900; letter-spacing: 1px; color: #FFF; }
        p.sub { font-size: 13px; color: var(--silver); margin-top: 6px; }

        /* Main Download Hero Card */
        .card-download { background: var(--surface); border: 2px solid var(--racing-red); border-radius: 18px; padding: 28px 20px; text-align: center; margin-bottom: 24px; box-shadow: 0 14px 40px rgba(0,0,0,0.8); position: relative; overflow: hidden; }
        .badge-version { display: inline-block; background: rgba(229, 9, 20, 0.15); border: 1px solid var(--racing-red); color: var(--racing-red); padding: 4px 12px; border-radius: 20px; font-size: 11px; font-weight: 800; font-family: 'JetBrains Mono', monospace; margin-bottom: 14px; }
        
        /* Download Buttons */
        .btn-download-main { display: inline-flex; align-items: center; justify-content: center; gap: 10px; width: 100%; max-width: 440px; background: linear-gradient(135deg, #E50914, #B80710); color: #FFF; text-decoration: none; padding: 16px 24px; border-radius: 12px; font-size: 14px; font-weight: 900; font-family: 'JetBrains Mono', monospace; text-transform: uppercase; letter-spacing: 0.5px; box-shadow: 0 8px 24px rgba(229, 9, 20, 0.5); transition: transform 0.2s, box-shadow 0.2s; margin: 8px auto; }
        .btn-download-main:hover { transform: translateY(-2px); box-shadow: 0 12px 30px rgba(229, 9, 20, 0.7); }
        
        .btn-download-github { display: inline-flex; align-items: center; justify-content: center; gap: 10px; width: 100%; max-width: 440px; background: #24292e; border: 1px solid #444d56; color: #FFF; text-decoration: none; padding: 14px 24px; border-radius: 12px; font-size: 13px; font-weight: 800; font-family: 'JetBrains Mono', monospace; margin: 8px auto; transition: background 0.2s; }
        .btn-download-github:hover { background: #2f363d; border-color: var(--silver); }

        .apk-meta { font-size: 11px; color: var(--silver); margin-top: 14px; font-family: 'JetBrains Mono', monospace; }

        /* QR Scanner Section */
        .qr-section { background: var(--surface-variant); border: 1px solid var(--border); border-radius: 14px; padding: 20px; text-align: center; margin-bottom: 24px; }
        .qr-box { background: #FFF; padding: 12px; border-radius: 12px; display: inline-block; margin: 12px 0; }
        .qr-box img { display: block; width: 170px; height: 170px; }
        .qr-hint { font-size: 11px; color: var(--silver); }

        /* GitHub Box Info */
        .github-box { background: rgba(36, 41, 46, 0.6); border: 1px solid #444d56; border-radius: 12px; padding: 16px; margin-bottom: 24px; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 10px; font-size: 12px; }
        .github-box a { color: var(--green); text-decoration: none; font-weight: bold; }

        /* Feature list */
        .features-card { background: var(--surface); border: 1px solid var(--border); border-radius: 14px; padding: 22px; margin-bottom: 24px; }
        .features-card h2 { font-family: 'JetBrains Mono', monospace; font-size: 14px; font-weight: 900; color: #FFF; margin-bottom: 12px; border-bottom: 1px solid var(--border); padding-bottom: 8px; }
        .feature-item { display: flex; align-items: flex-start; gap: 10px; margin-bottom: 10px; font-size: 12px; color: var(--silver-light); }
        .feature-icon { color: var(--racing-red); font-weight: bold; font-size: 14px; }

        /* Step by step install guide */
        .guide-card { background: var(--surface); border: 1px solid var(--border); border-radius: 14px; padding: 22px; margin-bottom: 24px; }
        .guide-card h2 { font-family: 'JetBrains Mono', monospace; font-size: 14px; font-weight: 900; color: var(--green); margin-bottom: 14px; border-bottom: 1px solid var(--border); padding-bottom: 8px; }
        .step { display: flex; gap: 14px; margin-bottom: 16px; align-items: flex-start; }
        .step-num { width: 28px; height: 28px; background: var(--racing-red); color: #FFF; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 900; font-size: 12px; flex-shrink: 0; font-family: 'JetBrains Mono', monospace; }
        .step-content h3 { font-size: 13px; font-weight: 800; color: #FFF; margin-bottom: 2px; }
        .step-content p { font-size: 11px; color: var(--silver); }

        /* Action Buttons */
        .nav-links { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 24px; }
        .btn-sub { display: block; text-align: center; background: var(--surface-variant); border: 1px solid var(--border); color: #FFF; padding: 12px; border-radius: 8px; text-decoration: none; font-size: 12px; font-weight: 700; transition: border-color 0.2s; }
        .btn-sub:hover { border-color: var(--silver); }

        .footer { text-align: center; font-size: 11px; color: #777; border-top: 1px solid var(--border); padding-top: 18px; margin-top: 20px; }
        .footer a { color: var(--green); text-decoration: none; font-weight: bold; }
    </style>
</head>
<body>

<div class="container">
    <div class="header">
        <div class="logo-badge">P<span>X</span>P</div>
        <h1>PSYCO TIME X PRO</h1>
        <p class="sub">Muat Turun Aplikasi Android Rasmi (Versi Telefon Pintar)</p>
    </div>

    <!-- Main Hero Download Card -->
    <div class="card-download">
        <div class="badge-version">VERSI 1.0.0 PRO • ANDROID STAND-ALONE</div>
        <h2 style="font-size:18px;font-weight:900;margin-bottom:8px;">Aplikasi Electronic Timing & Pengurusan Latihan</h2>
        <p style="font-size:12px;color:var(--silver);margin-bottom:16px;">Pasang terus ke telefon pintar anda. Berfungsi 100% tanpa internet di trek larian dan padang sukan.</p>

        <!-- Butang 1: GitHub Cloud Release (Laju & Tiada Had Kuota Hosting) -->
        <a href="<?= htmlspecialchars($github_download_url) ?>" class="btn-download-main">
            📥 MUAT TURUN APK (SERVER GITHUB CLOUD)
        </a>

        <!-- Butang 2: Server Web Hosting (Cermin / Mirror) -->
        <a href="<?= htmlspecialchars($apk_filename) ?>" class="btn-download-github" download>
            🌐 MUAT TURUN DARI SERVER WEB (MIRROR)
        </a>

        <div class="apk-meta">
            Fail: <strong><?= htmlspecialchars($apk_filename) ?></strong> | Saiz: <strong><?= $apk_size_mb ?> MB</strong> | Versi: <strong>v1.0.0</strong>
        </div>
    </div>

    <!-- GitHub Verified Box -->
    <div class="github-box">
        <div>
            <strong>Repositori Rasmi GitHub:</strong><br>
            <span style="font-family:'JetBrains Mono',monospace;font-size:11px;color:var(--silver);">saliparjipunatukoi-sketch/psycotimexpro</span>
        </div>
        <div style="display:flex;gap:10px;">
            <a href="<?= htmlspecialchars($github_commit_url) ?>" target="_blank">Lihat Commits v1.0.0 ↗</a>
            <a href="<?= htmlspecialchars($github_release_page) ?>" target="_blank">Halaman Releases ↗</a>
        </div>
    </div>

    <!-- QR Code Scan & Install on Phone -->
    <div class="qr-section">
        <h3 style="font-family:'JetBrains Mono',monospace;font-size:14px;color:#FFF;">IMBAS DENGAN TELEFON UNTUK MUAT TURUN</h3>
        <p class="qr-hint" style="margin-top:4px;">Gunakan kamera telefon anda untuk mengimbas kod QR ini dan fail APK akan dimuat turun secara automatik:</p>
        
        <div class="qr-box">
            <img src="https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=<?= urlencode($github_download_url) ?>" alt="QR Code Download APK Psyco Time X Pro">
        </div>

        <p class="qr-hint">Pautan Terus GitHub Cloud: <br><span style="font-family:'JetBrains Mono',monospace;color:var(--green);word-break:break-all;"><?= htmlspecialchars($github_download_url) ?></span></p>
    </div>

    <!-- Features Overview -->
    <div class="features-card">
        <h2>CIRI-CIRI UTAMA DALAM APLIKASI TELEFON:</h2>
        
        <div class="feature-item">
            <div class="feature-icon">✓</div>
            <div><strong>Log Masuk Google (Tanpa Password):</strong> Masuk terus menggunakan akaun Google Gmail anda dengan 1-klik tanpa perlu taip kata laluan.</div>
        </div>
        <div class="feature-item">
            <div class="feature-icon">✓</div>
            <div><strong>Carta Ranking Atlit (Balapan & Padang):</strong> Semak rekod PB Terkini vs PB Lama atlet berserta peratusan peningkatan masa/jarak.</div>
        </div>
        <div class="feature-item">
            <div class="feature-icon">✓</div>
            <div><strong>Kamera Electronic Timing (Photo Finish):</strong> Rakaman masa pecutan tepat 1/1000 saat dengan garisan penamat digital dan paparan lorong.</div>
        </div>
        <div class="feature-item">
            <div class="feature-icon">✓</div>
            <div><strong>Pangkalan Data Penuh Atlet:</strong> Simpan rekod atlet, nombor kad pengenalan, ketinggian, berat badan, umur, dan kutipan yuran bulanan.</div>
        </div>
    </div>

    <!-- Installation Steps Guide -->
    <div class="guide-card">
        <h2>CARA PEMASANGAN FAIL APK PADA TELEFON ANDROID:</h2>
        
        <div class="step">
            <div class="step-num">1</div>
            <div class="step-content">
                <h3>Muat Turun Fail APK</h3>
                <p>Tekan butang merah di atas atau imbas kod QR. Jika pelayar memaparkan amaran <em>"File might be harmful / Fail mungkin berbahaya"</em>, tekan <strong>"Download anyway"</strong> (fail ini rasmi dibina sendiri & selamat).</p>
            </div>
        </div>

        <div class="step">
            <div class="step-num">2</div>
            <div class="step-content">
                <h3>Buka Fail Dari Notifikasi / Downloads</h3>
                <p>Setelah selesai muat turun, sentuh fail <strong>psycotimexpro.apk</strong> pada bar notifikasi telefon atau buka dari aplikasi <em>Files / Pengurus Fail</em>.</p>
            </div>
        </div>

        <div class="step">
            <div class="step-num">3</div>
            <div class="step-content">
                <h3>Benarkan Pemasangan (Unknown Sources)</h3>
                <p>Jika telefon meminta kebenaran, tekan <strong>"Settings" (Tetapan)</strong> ➔ aktifkan suis <strong>"Allow from this source" (Benarkan dari sumber ini)</strong> ➔ tekan kembali.</p>
            </div>
        </div>

        <div class="step">
            <div class="step-num">4</div>
            <div class="step-content">
                <h3>Pasang & Buka Aplikasi</h3>
                <p>Tekan <strong>"Install" (Pasang)</strong> dan tunggu beberapa saat. Tekan <strong>"Open" (Buka)</strong> untuk memulakan sistem latihan anda!</p>
            </div>
        </div>
    </div>

    <!-- Bottom Navigation Links -->
    <div class="nav-links">
        <a href="index.php" class="btn-sub">🖥️ Portal Latihan (Web)</a>
        <a href="login.php" class="btn-sub">🔑 Log Masuk Coach</a>
    </div>

    <div class="footer">
        Hak Cipta Terpelihara © <strong>Psyco Time X Pro</strong><br>
        Dibina untuk Pengurusan Kelab & Jurulatih Olahraga Malaysia.<br>
        Bantuan pemasangan: <a href="https://wa.me/60195326399">WhatsApp Admin Roger (+60195326399)</a>
    </div>
</div>

</body>
</html>
