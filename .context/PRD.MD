# PRODUCT REQUIREMENT DOCUMENT (PRD)

# KEPO

## Kendali Evakuasi dan Pengelolaan Operasional Bencana

Versi: 2.0

Status: Draft Final

Platform: Desktop Application

Teknologi UI: JavaFX + MaterialFX

Bahasa Pemrograman: Java 21

---

# 1. Ringkasan Eksekutif

KEPO (Kendali Evakuasi dan Pengelolaan Operasional Bencana) adalah sistem informasi desktop terintegrasi yang dirancang untuk mendukung operasi penanggulangan bencana kebakaran secara menyeluruh.

Sistem menyediakan kemampuan untuk:

* Mengelola data shelter
* Mengelola data pengungsi
* Mengelola inventaris obat
* Mengelola distribusi bantuan
* Memantau kapasitas shelter
* Menghasilkan laporan operasional
* Memberikan rekomendasi berbasis Artificial Intelligence

KEPO dirancang sebagai pusat kendali operasional (Command Center) yang memungkinkan petugas memonitor kondisi lapangan secara real-time dari satu platform.

---

# 2. Latar Belakang

Dalam operasi penanggulangan bencana kebakaran terdapat beberapa tantangan utama:

* Data pengungsi tersebar dan tidak terpusat
* Shelter mengalami over-capacity
* Distribusi bantuan tidak merata
* Inventaris obat tidak termonitor secara optimal
* Pelaporan membutuhkan waktu lama
* Tidak tersedia sistem pendukung keputusan yang membantu petugas lapangan

KEPO dibangun untuk mengintegrasikan seluruh proses tersebut dalam satu sistem terpadu.

---

# 3. Tujuan Sistem

## Tujuan Utama

Menyediakan platform terpadu untuk pengelolaan evakuasi, logistik kesehatan, dan distribusi bantuan selama operasi penanggulangan bencana.

## Tujuan Khusus

* Monitoring shelter secara real-time
* Monitoring pengungsi secara real-time
* Pengelolaan inventaris obat
* Distribusi bantuan yang tepat sasaran
* Pembuatan laporan otomatis
* Rekomendasi operasional berbasis AI

---

# 4. Pengguna Sistem

## 4.1 Administrator

Hak akses penuh terhadap seluruh sistem.

Tanggung Jawab:

* Mengelola pengguna
* Mengelola event bencana
* Melihat seluruh laporan
* Menyetujui distribusi bantuan

---

## 4.2 Petugas Kesehatan

Tanggung Jawab:

* Mengelola inventaris obat
* Melakukan distribusi obat
* Memantau stok
* Mengakses AI Recommendation

---

## 4.3 Petugas Shelter

Tanggung Jawab:

* Registrasi pengungsi
* Check-in pengungsi
* Check-out pengungsi
* Monitoring kapasitas shelter

---

# 5. Teknologi

## Bahasa Pemrograman

Java 21

---

## Desktop Framework

JavaFX

MaterialFX

Alasan:

* Modern User Interface
* CSS Styling
* Responsive Layout
* Dark Mode Support
* Animasi dan Transisi
* Lebih fleksibel dibanding Java Swing

---

## Database

PostgreSQL 16

---

## Koneksi Database

JDBC

HikariCP Connection Pool

---

## Build Tool

Maven

---

## Reporting

JasperReports

---

## Barcode

ZXing

---

## Visualisasi Data

ChartsFX

---

## JSON Processing

Jackson

---

## AI Integration

Java HttpClient

OpenAI API

Gemini API

Rule-Based Recommendation Engine

---

# 6. Arsitektur Sistem

## Arsitektur

Clean MVC Architecture

Presentation Layer

↓

Controller Layer

↓

Service Layer

↓

Repository Layer

↓

Database Layer

---

# 7. Struktur Modul

## Dashboard

Command Center utama.

---

## Event Bencana

Manajemen operasi bencana.

---

## Shelter

Manajemen lokasi evakuasi.

---

## Pengungsi

Pendataan dan monitoring pengungsi.

---

## Inventaris Obat

Manajemen stok dan distribusi obat.

---

## Distribusi Bantuan

Pengelolaan alokasi bantuan.

---

## Supplier dan Donatur

Pengelolaan sumber bantuan.

---

## AI Assistant

Analisis dan rekomendasi.

---

## Laporan

Pelaporan operasional.

---

## Pengaturan

Manajemen pengguna dan konfigurasi sistem.

---

# 8. Desain UI/UX

## Konsep Desain

Emergency Command Center

Tampilan modern dengan pendekatan dashboard operasional.

Inspirasi:

* Linear
* Notion
* Jira
* Supabase Dashboard

---

## Struktur Layout

Top Navigation Bar

Sidebar Navigation

Main Content Area

Right Notification Panel

---

## Sidebar Menu

Dashboard

Event Bencana

Shelter

Pengungsi

Inventaris Obat

Distribusi Bantuan

Supplier dan Donatur

AI Assistant

Laporan

Pengaturan

---

## Dashboard Utama

### KPI Cards

Menampilkan:

* Shelter Aktif
* Total Pengungsi
* Shelter Penuh
* Obat Kritis
* Distribusi Hari Ini
* Kasus Medis

---

### Emergency Alert Center

Menampilkan:

* Shelter Over Capacity
* Obat Hampir Habis
* Distribusi Tertunda
* Kasus Medis Prioritas

---

### Shelter Monitoring

Progress Bar kapasitas shelter.

Warna:

Hijau = Aman

Kuning = Waspada

Merah = Kritis

---

### AI Recommendation Panel

Contoh:

* Shelter A membutuhkan tambahan obat
* Shelter B mendekati kapasitas maksimum
* Prediksi kebutuhan stok 7 hari ke depan

---

# 9. Modul Event Bencana

Fitur:

* Membuat event baru
* Mengubah status event
* Menutup event
* Statistik event

---

# 10. Modul Shelter

Fitur:

* CRUD Shelter
* Monitoring Kapasitas
* Riwayat Shelter
* Status Shelter

Data:

* Nama Shelter
* Lokasi
* Kapasitas
* Penanggung Jawab
* Status

---

# 11. Modul Pengungsi

Fitur:

* Registrasi Pengungsi
* Check In
* Check Out
* Riwayat Pengungsi
* Catatan Medis

---

# 12. Modul Inventaris Obat

Fitur:

* CRUD Obat
* Monitoring Stok
* Monitoring Kedaluwarsa
* Barcode Management
* Riwayat Transaksi

---

# 13. Modul Distribusi Bantuan

Fitur:

* Pembuatan Dokumen Distribusi
* Approval Workflow
* Tracking Distribusi
* Riwayat Distribusi

Status:

Draft

Disetujui

Dikirim

Diterima

---

# 14. Modul AI Assistant

## AI Recommendation

Memberikan rekomendasi:

* Prioritas distribusi
* Shelter prioritas
* Prediksi stok
* Analisis kebutuhan

---

## AI Chat

Petugas dapat bertanya:

"Obat apa yang hampir habis?"

"Shelter mana yang penuh?"

"Bantuan apa yang harus diprioritaskan?"

---

# 15. Laporan

Jenis laporan:

* Laporan Shelter
* Laporan Pengungsi
* Laporan Inventaris
* Laporan Distribusi
* Laporan Donatur
* Laporan Event Bencana

Format:

* PDF
* Excel

---

# 16. Keamanan

Autentikasi:

* BCrypt Password Hash

Otorisasi:

* Role Based Access Control

Audit:

* Audit Log

Keamanan Database:

* Prepared Statement
* Input Validation

---

# 17. Deployment

Output:

* Windows (.exe)
* Windows (.msi)
* Linux (.AppImage)
* Linux (.deb)
* macOS (.dmg)

Menggunakan:

jpackage

---

# 18. Target Kinerja

Dashboard Load Time < 3 detik

Pembuatan Laporan < 30 detik

Mendukung:

* 100 Shelter
* 50.000 Pengungsi
* 100.000 Transaksi

---

# 19. Future Development

* Mobile Companion App
* GIS Map Integration
* SMS Gateway
* WhatsApp Notification
* Offline Synchronization
* IoT Shelter Monitoring
* OCR Dokumen Korban
* Machine Learning Forecasting
