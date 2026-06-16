# PRODUCT REQUIREMENTS DOCUMENT (PRD)

# KEPO

## Kendali Evakuasi dan Pengelolaan Operasional Bencana

Version: 2.0
Status: Final Draft
Platform: Desktop Application
Technology: Java 21 + JavaFX + PostgreSQL

---

# 1. Product Overview

KEPO (Kendali Evakuasi dan Pengelolaan Operasional Bencana) adalah sistem informasi terpadu yang digunakan untuk mengelola operasi evakuasi, pengungsi, shelter, logistik, obat-obatan, dan pelaporan selama situasi bencana.

Sistem berfungsi sebagai Command Center yang membantu petugas lapangan dan pengambil keputusan memperoleh informasi kondisi pengungsi, kapasitas shelter, serta kebutuhan logistik secara cepat dan akurat.

Target utama sistem adalah meningkatkan efektivitas respon bencana, mempercepat distribusi bantuan, dan meminimalkan kekurangan logistik pada shelter.

---

# 2. Problem Statement

Dalam penanganan bencana sering ditemukan permasalahan:

* Data pengungsi tersebar di berbagai lokasi.
* Shelter mengalami kelebihan kapasitas.
* Distribusi bantuan tidak merata.
* Ketersediaan obat sulit dipantau.
* Sulit mengetahui shelter mana yang membutuhkan bantuan prioritas.
* Proses pelaporan masih dilakukan secara manual.
* Tidak tersedia sistem rekomendasi berbasis data.

Akibatnya proses pengambilan keputusan menjadi lambat dan berpotensi menimbulkan ketidaktepatan distribusi bantuan.

---

# 3. Product Goals

## Primary Goals

* Memusatkan seluruh data operasional bencana dalam satu sistem.
* Memantau kondisi shelter secara real-time.
* Memantau persebaran pengungsi.
* Mengelola distribusi bantuan dan obat.
* Menyediakan laporan operasional otomatis.
* Memberikan rekomendasi prioritas berbasis AI.

## Success Metrics

* Waktu pencarian data pengungsi < 5 detik.
* Akurasi data shelter > 95%.
* Pengurangan shelter over-capacity minimal 50%.
* Pengurangan kejadian kekurangan logistik minimal 40%.
* Waktu pembuatan laporan < 30 detik.

---

# 4. User Roles

## Administrator

Hak akses penuh terhadap sistem.

Fitur:

* Manajemen pengguna
* Manajemen role
* Manajemen event bencana
* Monitoring seluruh data
* Approval distribusi bantuan

---

## Petugas Shelter

Fitur:

* Registrasi pengungsi
* Penempatan pengungsi ke shelter
* Check-in / Check-out
* Monitoring kapasitas shelter
* Pelaporan kondisi shelter

---

## Petugas Kesehatan

Fitur:

* Manajemen inventaris obat
* Distribusi obat ke shelter
* Monitoring stok
* Monitoring kedaluwarsa
* AI Recommendation

---

## Koordinator Lapangan

Fitur:

* Monitoring seluruh shelter
* Monitoring distribusi bantuan
* Dashboard operasional
* Pelaporan harian

---

# 5. Core Business Flow

Event Bencana Dibuat

↓

Shelter Dibuka

↓

Pengungsi Diregistrasi

↓

Pengungsi Ditempatkan ke Shelter

↓

Kebutuhan Shelter Dihitung

↓

Distribusi Logistik dan Obat

↓

Monitoring Shelter

↓

Laporan Operasional

↓

Penutupan Event

---

# 6. Functional Requirements

## FR-01 Event Management

Sistem harus mampu:

* Membuat event bencana
* Menentukan lokasi kejadian
* Menentukan status event
* Menutup event

Status:

* Active
* Monitoring
* Closed

---

## FR-02 Shelter Management

Sistem harus mampu:

* CRUD shelter
* Menentukan kapasitas shelter
* Menampilkan jumlah penghuni
* Menampilkan kapasitas tersisa
* Menampilkan status shelter

Status Shelter:

* Available
* Warning
* Full
* Closed

Rumus Kapasitas:

Occupancy Rate = Penghuni / Kapasitas × 100%

---

## FR-03 Refugee Management

Sistem harus mampu:

* Registrasi pengungsi
* Pendataan keluarga
* Penempatan shelter
* Riwayat perpindahan shelter
* Check-in
* Check-out

Data Pengungsi:

* NIK
* Nama
* Jenis Kelamin
* Tanggal Lahir
* Nomor KK
* Status Kesehatan
* Kategori Rentan

Kategori Rentan:

* Anak-anak
* Lansia
* Ibu Hamil
* Disabilitas
* Penyakit Kronis

---

## FR-04 Shelter Placement Management

Sistem harus mampu:

* Menempatkan pengungsi ke shelter.
* Menampilkan daftar penghuni shelter.
* Menampilkan riwayat perpindahan.
* Menolak penempatan jika kapasitas penuh.

Dashboard Shelter menampilkan:

* Total penghuni
* Kapasitas
* Persentase okupansi
* Kategori pengungsi

---

## FR-05 Medical Inventory Management

Sistem harus mampu:

* CRUD obat
* Monitoring stok
* Monitoring batch
* Monitoring kedaluwarsa
* Barcode dan QR Code

Status Stok:

* Aman
* Rendah
* Kritis
* Habis

---

## FR-06 Logistics Distribution

Sistem harus mampu:

* Distribusi logistik ke shelter
* Distribusi obat ke shelter
* Monitoring stok shelter
* Tracking distribusi

Status Distribusi:

* Draft
* Approved
* Sent
* Received

---

## FR-07 Shelter Logistics Monitoring

Setiap shelter memiliki stok logistik sendiri.

Sistem harus menampilkan:

* Stok saat ini
* Kebutuhan minimum
* Persentase ketersediaan
* Estimasi hari cakupan

Rumus:

Availability (%) =
(Stok Saat Ini / Kebutuhan Shelter) × 100%

Status:

* ≥100% = Aman
* 70–99% = Waspada
* <70% = Kritis

---

## FR-08 Dashboard Command Center

Dashboard menampilkan:

### KPI

* Event Aktif
* Shelter Aktif
* Total Pengungsi
* Shelter Penuh
* Shelter Kritis
* Distribusi Hari Ini

### Monitoring

* Shelter Occupancy
* Distribusi Logistik
* Distribusi Obat
* Pengungsi Rentan
* Alert Operasional

### AI Insights

* Prioritas distribusi
* Shelter yang membutuhkan bantuan
* Prediksi kebutuhan logistik 7 hari
* Prediksi kebutuhan obat

---

## FR-09 AI Assistant

AI dapat menjawab:

* Shelter mana yang penuh?
* Shelter mana yang kekurangan obat?
* Berapa jumlah pengungsi lansia?
* Distribusi apa yang harus diprioritaskan?

AI juga menghasilkan rekomendasi otomatis.

---

## FR-10 Reporting

Format:

* PDF
* Excel

Jenis Laporan:

* Shelter
* Pengungsi
* Distribusi
* Logistik
* Inventaris Obat
* Event Bencana

---

# 7. Non Functional Requirements

## Performance

* Dashboard ≤ 3 detik
* Pencarian ≤ 2 detik
* Laporan ≤ 30 detik

## Security

* BCrypt Password Hash
* RBAC
* Audit Log
* Prepared Statement
* Input Validation

## Reliability

* Backup Harian
* Recovery Database
* Transaction Logging

## Scalability

Minimal mendukung:

* 100 Event Aktif
* 500 Shelter
* 50.000 Pengungsi
* 500.000 Transaksi

---

# 8. MVP Scope (Versi 2.0)

Termasuk:

* Authentication
* Event Management
* Shelter Management
* Refugee Management
* Medical Inventory
* Distribution Management
* Dashboard
* Reporting
* AI Recommendation

Tidak termasuk:

* GIS Map
* Mobile App
* SMS Gateway
* WhatsApp Gateway
* IoT Monitoring
* OCR Document

---

# 9. Future Roadmap

Phase 3

* GIS Shelter Mapping
* Mobile Companion App
* Offline Synchronization

Phase 4

* WhatsApp Notification
* SMS Gateway
* OCR Document Scanner

Phase 5

* Machine Learning Forecasting
* Disaster Prediction Analytics
* Resource Optimization Engine
