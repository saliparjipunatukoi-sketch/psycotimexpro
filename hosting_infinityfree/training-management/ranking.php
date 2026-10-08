<?php
// hosting_infinityfree/training-management/ranking.php
// Carta Ranking & Prestasi Atlit (Balapan & Padang)
session_start();
require_once __DIR__ . '/db_connect.php';

$filter_sport = $_GET['sport'] ?? 'SEMUA';
$filter_category = $_GET['cat'] ?? 'SEMUA';

// Ambil senarai atlet beserta maklumat kelab dan jurulatih
$sql = "SELECT a.*, c.name AS coach_name, c.nickname AS coach_nickname, c.club_name 
        FROM athletes a 
        LEFT JOIN coaches c ON a.coach_id = c.id 
        WHERE 1=1";
$params = [];

if ($filter_sport !== 'SEMUA') {
    $sql .= " AND a.sport_type = ?";
    $params[] = $filter_sport;
}

if ($filter_category !== 'SEMUA') {
    $sql .= " AND a.category LIKE ?";
    $params[] = "%" . $filter_category . "%";
}

// Susun mengikut prestasi terbaik (Balapan: masa terendah, Padang: jarak tertinggi)
if ($filter_sport === 'Padang') {
    $sql .= " ORDER BY a.distance_or_score DESC, a.name ASC";
} else {
    $sql .= " ORDER BY (CASE WHEN a.pb_seconds > 0 THEN a.pb_seconds ELSE 999 END) ASC, a.name ASC";
}

$stmt = $pdo->prepare($sql);
$stmt->execute($params);
$athletes = $stmt->fetchAll(PDO::FETCH_ASSOC);
?>
<!DOCTYPE html>
<html lang="ms">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Carta Ranking Atlit - Psyco Time X Pro</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700;800;900&family=JetBrains+Mono:wght@700;900&display=swap" rel="stylesheet">
    <style>
        :root {
            --racing-red: #E50914;
            --silver: #C0C0C0;
            --surface: #161618;
            --surface-variant: #222226;
            --border: #333338;
            --green: #00E676;
            --gold: #FFD700;
            --bronze: #CD7F32;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Montserrat', sans-serif; }
        body { background: #0F0F11; color: #FFF; line-height: 1.6; padding: 20px 16px 60px; }
        .container { max-width: 1100px; margin: 0 auto; }

        .top-bar { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--border); padding-bottom: 16px; margin-bottom: 24px; flex-wrap: wrap; gap: 12px; }
        .brand { display: flex; align-items: center; gap: 12px; }
        .logo-badge { width: 48px; height: 48px; background: #000; border: 2.5px solid var(--racing-red); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-family: 'JetBrains Mono', monospace; font-size: 18px; font-weight: 900; color: #FFF; }
        .logo-badge span { color: var(--racing-red); }
        .brand-text h1 { font-family: 'JetBrains Mono', monospace; font-size: 18px; font-weight: 900; color: #FFF; }
        .brand-text p { font-size: 11px; color: var(--silver); }

        .nav-btns { display: flex; gap: 8px; }
        .btn-nav { background: var(--surface); border: 1px solid var(--border); color: #FFF; padding: 8px 14px; border-radius: 6px; text-decoration: none; font-size: 11px; font-weight: 700; transition: border-color 0.2s; }
        .btn-nav:hover { border-color: var(--racing-red); }
        .btn-nav.primary { background: var(--racing-red); border-color: var(--racing-red); }

        /* Filter Controls */
        .filter-card { background: var(--surface); border: 1px solid var(--border); border-radius: 12px; padding: 16px 20px; margin-bottom: 24px; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 14px; }
        .tabs { display: flex; background: #0A0A0A; padding: 4px; border-radius: 8px; gap: 4px; }
        .tab-link { color: var(--silver); text-decoration: none; padding: 6px 16px; border-radius: 6px; font-size: 12px; font-weight: 800; font-family: 'JetBrains Mono', monospace; }
        .tab-link.active { background: var(--racing-red); color: #FFF; }

        .quick-chips { display: flex; gap: 6px; flex-wrap: wrap; }
        .chip { background: var(--surface-variant); border: 1px solid var(--border); color: var(--silver); text-decoration: none; padding: 4px 10px; border-radius: 20px; font-size: 11px; font-weight: 600; }
        .chip.active { border-color: var(--gold); color: var(--gold); }

        /* Table */
        .table-card { background: var(--surface); border: 1px solid var(--border); border-radius: 14px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.7); }
        .table-responsive { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; font-size: 12px; text-align: left; }
        th { background: #000; color: var(--silver); padding: 12px 14px; font-size: 10px; text-transform: uppercase; font-family: 'JetBrains Mono', monospace; border-bottom: 1px solid var(--border); }
        td { padding: 12px 14px; border-bottom: 1px solid var(--border); }
        tr:hover td { background: rgba(255,255,255,0.02); }

        .rank-badge { display: inline-flex; align-items: center; justify-content: center; width: 32px; height: 32px; border-radius: 6px; font-family: 'JetBrains Mono', monospace; font-size: 13px; font-weight: 900; }
        .rank-1 { background: rgba(255, 215, 0, 0.2); border: 1px solid var(--gold); color: var(--gold); }
        .rank-2 { background: rgba(192, 192, 192, 0.2); border: 1px solid var(--silver); color: #FFF; }
        .rank-3 { background: rgba(205, 127, 50, 0.2); border: 1px solid var(--bronze); color: var(--bronze); }
        .rank-other { background: var(--surface-variant); border: 1px solid var(--border); color: var(--silver); }

        .badge-type { display: inline-block; padding: 3px 8px; border-radius: 4px; font-size: 10px; font-weight: 800; font-family: 'JetBrains Mono', monospace; }
        .badge-balapan { background: rgba(229, 9, 20, 0.15); color: var(--racing-red); border: 1px solid var(--racing-red); }
        .badge-padang { background: rgba(0, 230, 118, 0.15); color: var(--green); border: 1px solid var(--green); }

        .score-box { font-family: 'JetBrains Mono', monospace; font-size: 15px; font-weight: 900; color: #FFF; }
        .score-old { font-family: 'JetBrains Mono', monospace; font-size: 12px; color: var(--silver); }
        .delta-improved { color: var(--green); font-weight: bold; font-family: 'JetBrains Mono', monospace; font-size: 11px; }
        .delta-worse { color: var(--racing-red); font-weight: bold; font-family: 'JetBrains Mono', monospace; font-size: 11px; }

        .footer { text-align: center; font-size: 11px; color: #777; margin-top: 30px; }
    </style>
</head>
<body>

<div class="container">
    <div class="top-bar">
        <div class="brand">
            <div class="logo-badge">P<span>X</span>P</div>
            <div class="brand-text">
                <h1>PSYCO TIME X PRO</h1>
                <p>Carta Ranking & Rekod Prestasi Atlit (Balapan & Padang)</p>
            </div>
        </div>
        <div class="nav-btns">
            <a href="index.php" class="btn-nav">🖥️ Portal Jurulatih</a>
            <a href="download.php" class="btn-nav primary">📲 Muat Turun APK</a>
        </div>
    </div>

    <!-- Filters -->
    <div class="filter-card">
        <div class="tabs">
            <a href="ranking.php?sport=SEMUA&cat=<?= urlencode($filter_category) ?>" class="tab-link <?= $filter_sport === 'SEMUA' ? 'active' : '' ?>">SEMUA</a>
            <a href="ranking.php?sport=Balapan&cat=<?= urlencode($filter_category) ?>" class="tab-link <?= $filter_sport === 'Balapan' ? 'active' : '' ?>">BALAPAN</a>
            <a href="ranking.php?sport=Padang&cat=<?= urlencode($filter_category) ?>" class="tab-link <?= $filter_sport === 'Padang' ? 'active' : '' ?>">PADANG</a>
        </div>

        <div class="quick-chips">
            <span style="font-size:11px;color:var(--silver);margin-right:4px;align-self:center;">PILIH ACARA:</span>
            <a href="ranking.php?sport=<?= urlencode($filter_sport) ?>&cat=SEMUA" class="chip <?= $filter_category === 'SEMUA' ? 'active' : '' ?>">Semua</a>
            <a href="ranking.php?sport=<?= urlencode($filter_sport) ?>&cat=100m" class="chip <?= $filter_category === '100m' ? 'active' : '' ?>">100m</a>
            <a href="ranking.php?sport=<?= urlencode($filter_sport) ?>&cat=200m" class="chip <?= $filter_category === '200m' ? 'active' : '' ?>">200m</a>
            <a href="ranking.php?sport=<?= urlencode($filter_sport) ?>&cat=400m" class="chip <?= $filter_category === '400m' ? 'active' : '' ?>">400m</a>
            <a href="ranking.php?sport=<?= urlencode($filter_sport) ?>&cat=Lompat" class="chip <?= $filter_category === 'Lompat' ? 'active' : '' ?>">Lompat Jauh</a>
        </div>
    </div>

    <!-- Table -->
    <div class="table-card">
        <div class="table-responsive">
            <table>
                <thead>
                    <tr>
                        <th style="width: 60px; text-align: center;">Kedudukan</th>
                        <th>Atlit</th>
                        <th>Kelab / Akademi</th>
                        <th>Jurulatih</th>
                        <th>Jenis & Acara</th>
                        <th>PB Terkini</th>
                        <th>PB Lama</th>
                        <th>Kemajuan Prestasi</th>
                    </tr>
                </thead>
                <tbody>
                    <?php if (empty($athletes)): ?>
                        <tr>
                            <td colspan="8" style="text-align: center; padding: 40px; color: var(--silver);">
                                Tiada rekod atlit untuk tapisan ini.
                            </td>
                        </tr>
                    <?php else: ?>
                        <?php foreach ($athletes as $idx => $at): 
                            $rank = $idx + 1;
                            $rank_cls = $rank === 1 ? 'rank-1' : ($rank === 2 ? 'rank-2' : ($rank === 3 ? 'rank-3' : 'rank-other'));
                            $medal = $rank === 1 ? '🥇' : ($rank === 2 ? '🥈' : ($rank === 3 ? '🥉' : '#' . $rank));
                            
                            $is_track = strtolower($at['sport_type']) === 'balapan';
                            $pb_curr = $is_track ? number_format($at['pb_seconds'], 2) . 's' : (isset($at['distance_or_score']) ? number_format($at['distance_or_score'], 2) . 'm' : '-');
                            $pb_old = $is_track ? (isset($at['previous_pb_seconds']) && $at['previous_pb_seconds'] > 0 ? number_format($at['previous_pb_seconds'], 2) . 's' : '-') : (isset($at['previous_distance_or_score']) && $at['previous_distance_or_score'] > 0 ? number_format($at['previous_distance_or_score'], 2) . 'm' : '-');
                            
                            // Delta
                            $has_improvement = false;
                            $delta_str = "Catatan Awal";
                            if ($is_track && isset($at['previous_pb_seconds']) && $at['previous_pb_seconds'] > 0) {
                                $diff = $at['previous_pb_seconds'] - $at['pb_seconds'];
                                if ($diff > 0) {
                                    $delta_str = "🚀 -" . number_format($diff, 2) . "s (Lebih Pantas)";
                                    $has_improvement = true;
                                } elseif ($diff < 0) {
                                    $delta_str = "⚠️ +" . number_format(abs($diff), 2) . "s";
                                } else {
                                    $delta_str = "Sama";
                                }
                            } elseif (!$is_track && isset($at['previous_distance_or_score']) && $at['previous_distance_or_score'] > 0) {
                                $diff = $at['distance_or_score'] - $at['previous_distance_or_score'];
                                if ($diff > 0) {
                                    $delta_str = "📈 +" . number_format($diff, 2) . "m (Lebih Jauh)";
                                    $has_improvement = true;
                                } else {
                                    $delta_str = number_format($diff, 2) . "m";
                                }
                            }
                        ?>
                            <tr>
                                <td style="text-align: center;">
                                    <div class="rank-badge <?= $rank_cls ?>"><?= $medal ?></div>
                                </td>
                                <td>
                                    <div style="display:flex;align-items:center;gap:10px;">
                                        <?php if (!empty($at['photo_uri'])): ?>
                                            <img src="<?= htmlspecialchars($at['photo_uri']) ?>" style="width:36px;height:36px;border-radius:50%;object-fit:cover;border:1.5px solid var(--gold);">
                                        <?php else: ?>
                                            <div style="width:36px;height:36px;border-radius:50%;background:var(--surface-variant);display:flex;align-items:center;justify-content:center;color:var(--silver);font-weight:bold;font-size:12px;">👤</div>
                                        <?php endif; ?>
                                        <div>
                                            <strong style="color:#FFF;"><?= htmlspecialchars($at['name']) ?></strong><br>
                                            <span style="color:var(--silver);font-size:10px;"><?= htmlspecialchars($at['gender']) ?>, <?= $at['age'] ?> Tahun</span>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <strong><?= htmlspecialchars(!empty($at['club_name']) ? $at['club_name'] : 'Akademi Sukan') ?></strong>
                                </td>
                                <td>
                                    <span style="color:var(--gold);"><?= htmlspecialchars(!empty($at['coach_nickname']) ? $at['coach_nickname'] : $at['coach_name']) ?></span>
                                </td>
                                <td>
                                    <span class="badge-type <?= $is_track ? 'badge-balapan' : 'badge-padang' ?>">
                                        <?= strtoupper($at['sport_type']) ?>
                                    </span>
                                    <div style="color:var(--silver);font-size:11px;margin-top:2px;"><?= htmlspecialchars($at['category']) ?></div>
                                </td>
                                <td>
                                    <div class="score-box"><?= $pb_curr ?></div>
                                </td>
                                <td>
                                    <div class="score-old"><?= $pb_old ?></div>
                                </td>
                                <td>
                                    <span class="<?= $has_improvement ? 'delta-improved' : 'score-old' ?>">
                                        <?= $delta_str ?>
                                    </span>
                                </td>
                            </tr>
                        <?php endforeach; ?>
                    <?php endif; ?>
                </tbody>
            </table>
        </div>
    </div>

    <div class="footer">
        Sistem Pengurusan & Analisis Sukan © <strong>Psyco Time X Pro</strong><br>
        Rekod masa rasmi dijana oleh Electronic Timing Cam & AI Biomechanics Analysis.
    </div>
</div>

</body>
</html>
