# PANDUAN PEMASANGAN PORTAL WWW.PSYCOTIMEXPRO.MY/TRAINING-MANAGEMENT
## InfinityFree Hosting (FTP & MySQL Database)

Panduan ini disediakan khas untuk anda memasang portal pengurusan latihan **Psyco Time X Pro** ke hosting **infinityfree.com** dan domain aktif anda **psycotimexpro.my**.

---

### Langkah 1: Import Database SQL ke InfinityFree (phpMyAdmin)
1. Log masuk ke akaun **InfinityFree** anda (app.infinityfree.net).
2. Pergi ke **Control Panel (cPanel)** akaun hosting anda.
3. Klik ikon **MySQL Databases** dan pastikan pangkalan data `if0_41886177_registry_psyco` telah wujud.
4. Klik butang **phpMyAdmin** untuk membuka pengurusan pangkalan data.
5. Pilih pangkalan data `if0_41886177_registry_psyco` di sebelah kiri.
6. Klik tab **Import** di bahagian atas.
7. Pilih fail `schema.sql` (yang telah disediakan di dalam folder ini) dan klik butang **Go / Import**.
8. Pangkalan data anda kini telah lengkap dengan jadual:
   - `coaches` (lengkap dengan Master Admin `Saliparjipun.atukoi@gmail.com`)
   - `sub_coaches` (penolong jurulatih)
   - `runners` (pelatih dengan kategori Balapan & Padang, IC, tinggi, berat, umur)
   - `timing_runs` (catatan masa Electronic Timing)
   - `attendance` (kehadiran latihan)
   - `runner_fees` (yuran bulanan pelatih)
   - `subscription_payments` (resit rasmi langganan RM30/bulan)

---

### Langkah 2: Upload Fail ke Hosting melalui File Manager / FTP
1. Di cPanel InfinityFree, klik **Online File Manager** (atau gunakan FileZilla FTP).
2. Masuk ke dalam folder:
   ```
   htdocs/ (atau public_html/)
   ```
3. Cipta folder baru bernama:
   ```
   training-management
   ```
4. Upload semua fail dari folder `web/training-management/` ke dalam folder tersebut:
   - `index.php` (Papan Pemuka Jurulatih & Log Masuk Laptop)
   - `admin.php` (Panel Master Admin untuk Roger meluluskan coach)
   - `register_runner.php` (Halaman pendaftaran pelatih melalui imbasan QR Code)
   - `db_config.php` (Konfigurasi sambungan MySQL)
   - `api.php` (API penyegerakan telefon dengan laman web)

---

### Langkah 3: Semak & Uji Laman Web Anda
Buka pelayar web (Chrome/Safari) di Laptop atau Telefon anda:

1. **Papan Pemuka Jurulatih**:
   👉 `https://psycotimexpro.my/training-management/`
   - Log masuk sebagai Master Admin:
     * **Emel**: `Saliparjipun.atukoi@gmail.com`
     * **Password**: `Abc@1234`
   - Pendaftaran Coach Baru (Percuma 7 hari selepas kelulusan Admin).

2. **Panel Master Admin Roger**:
   👉 `https://psycotimexpro.my/training-management/admin.php`
   - Luluskan coach yang baru mendaftar dengan satu klik.
   - Ubah tempoh percuma atau lanjutkan langganan bulanan RM30.

3. **Pendaftaran Pelatih (QR Code)**:
   👉 `https://psycotimexpro.my/training-management/register_runner.php?coach_id=1`
   - Boleh diimbas oleh atlet atau ibu bapa untuk mendaftar details lengkap terus ke sistem coach!

---

### Sebarang Bantuan:
Hubungi Master Admin Roger: **+60195326399** (WhatsApp terus).
