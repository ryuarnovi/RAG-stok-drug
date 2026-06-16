-- KEPO Seed Data (PostgreSQL)

-- Clean up
TRUNCATE TABLE audit_logs CASCADE;
TRUNCATE TABLE refugee_movements CASCADE;
TRUNCATE TABLE inventory_transactions CASCADE;
TRUNCATE TABLE distribution_details CASCADE;
TRUNCATE TABLE distributions CASCADE;
TRUNCATE TABLE shelter_stocks CASCADE;
TRUNCATE TABLE medicines CASCADE;
TRUNCATE TABLE refugees CASCADE;
TRUNCATE TABLE shelters CASCADE;
TRUNCATE TABLE events CASCADE;
TRUNCATE TABLE suppliers CASCADE;
TRUNCATE TABLE donors CASCADE;
TRUNCATE TABLE users CASCADE;

-- Demo Users (passwords are bcrypt hashes)
-- admin -> admin123
-- health -> pharma123
-- shelter -> staff123
INSERT INTO users (username, password_hash, full_name, role) VALUES
('admin', '$2a$12$Ctmg56/uUxhE0pIhD3lLq.RMA435rS3ce1nl.UDkmw2ZsVIpbSkXW', 'Dr. Aris Wijaya', 'ADMIN'),
('health', '$2a$12$jCpbRrTY9zMrEKiaidHjheX39/zr9DBEu8ePIStsWVbKDIhSPxD76', 'Apt. Sari Dewi', 'HEALTH_OFFICER'),
('shelter', '$2a$12$NyYrBjXt0HBAjtjACgmXJusscU6PywyeSaiz6a7tVaILr8DtpfSi.', 'Budi Santoso', 'SHELTER_OFFICER');

-- Events
INSERT INTO events (name, location, status, description, shelter_count) VALUES
('Kebakaran Hutan Riau', 'Kabupaten Bengkalis, Riau', 'ACTIVE', 'Kebakaran lahan gambut meluas akibat musim kemarau ekstrem.', 2),
('Kebakaran Depo Plumpang', 'Koja, Jakarta Utara', 'CLOSED', 'Insiden kebakaran area depo penampungan BBM.', 1),
('Kebakaran Pemukiman Padat Tambora', 'Tambora, Jakarta Barat', 'ACTIVE', 'Kebakaran pemukiman padat penduduk yang menghanguskan ratusan rumah.', 3);

-- Shelters
INSERT INTO shelters (name, location, capacity, current_occupancy, status, penanggung_jawab, event_id) VALUES
('GOR Grogol', 'Grogol Petamburan, Jakarta Barat', 200, 150, 'WASPADA', 'Budi Santoso', 3),
('Stadion Utama Riau (Shelter A)', 'Pekanbaru, Riau', 500, 120, 'AMAN', 'Hendra Kusuma', 1),
('Masjid Agung Baiturrahman', 'Tambora, Jakarta Barat', 100, 95, 'KRITIS', 'Ahmad Fauzi', 3),
('RPTRA Utama Jaya', 'Koja, Jakarta Utara', 80, 0, 'AMAN', 'Siti Aminah', 2),
('Kantor Kelurahan Bengkalis', 'Bengkalis, Riau', 150, 150, 'KRITIS', 'Dedi Hermawan', 1);

-- Refugees
INSERT INTO refugees (name, nik, age, gender, status, medical_notes, priority_status, family_code, shelter_id) VALUES
('Slamet Raharjo', '3173010203640001', 60, 'Laki-laki', 'CHECKED_IN', 'Mengalami sesak napas ringan (ISPA).', 'LANSIA', 'FAM-001', 1),
('Siti Rahmawati', '3173014203680002', 56, 'Perempuan', 'CHECKED_IN', 'Riwayat diabetes melitus tipe 2, butuh insulin.', 'SICK', 'FAM-001', 1),
('Andi Wijaya', '3173011204950003', 29, 'Laki-laki', 'CHECKED_IN', 'Luka bakar derajat 1 di lengan kanan, sudah diobati.', 'REGULAR', 'FAM-002', 1),
('Rina Amelia', '3173015506980004', 26, 'Perempuan', 'CHECKED_IN', 'Hamil trimester ke-3, memerlukan suplemen vitamin.', 'IBU_HAMIL', 'FAM-003', 3),
('Budi Cahyono', '1403010507720001', 52, 'Laki-laki', 'CHECKED_IN', 'Mengeluh pusing dan demam tinggi.', 'SICK', 'FAM-004', 2),
('Dewi Lestari', '1403014608750002', 49, 'Perempuan', 'CHECKED_IN', 'Asma bronkial aktif, butuh inhaler salbutamol.', 'REGULAR', 'FAM-004', 2),
('Doni Pratama', '1403011809050003', 19, 'Laki-laki', 'CHECKED_OUT', 'Sudah kembali ke rumah kerabat.', 'REGULAR', 'FAM-005', 2),
('Eka Yulianti', '3172025210920005', 32, 'Perempuan', 'CHECKED_IN', 'Diare akut dan dehidrasi ringan.', 'REGULAR', 'FAM-006', 3),
('Feri Setiawan', '3172021511900006', 34, 'Laki-laki', 'CHECKED_IN', 'Luka lecet di kaki akibat reruntuhan.', 'REGULAR', 'FAM-006', 3),
('Gita Permata', '1403026212880007', 36, 'Perempuan', 'CHECKED_IN', 'Mengalami syok pasca bencana (trauma ringan).', 'REGULAR', 'FAM-007', 5);

-- Refugee Movements
INSERT INTO refugee_movements (refugee_id, from_shelter_id, to_shelter_id, moved_by, notes) VALUES
(1, NULL, 1, 'admin', 'Registrasi masuk awal ke GOR Grogol'),
(2, NULL, 1, 'admin', 'Registrasi masuk awal ke GOR Grogol');

-- Suppliers
INSERT INTO suppliers (supplier_name, contact_person, phone, email, address) VALUES
('PT Kimia Farma Tbk', 'Hendra Kusuma', '021-4208311', 'order@kimiafarma.co.id', 'Jl. Veteran No.9, Jakarta Pusat'),
('PT Kalbe Farma Tbk', 'Rina Marlina', '021-4287 3888', 'supply@kalbe.co.id', 'Gedung Kalbe, Jl. Let. Jend. Suprapto, Jakarta'),
('PT Sanbe Farma', 'Dedi Hermawan', '022-7806 777', 'info@sanbe.co.id', 'Jl. Tamansari No.10, Bandung'),
('PT Dexa Medica', 'Fitri Handayani', '0751-40444', 'order@dexa-medica.com', 'Jl. Letnan Jenderal Bambang Utoyo No.138, Palembang'),
('PT Tempo Scan Pacific', 'Ahmad Fauzi', '021-7531 0011', 'procurement@temposcan.co.id', 'Gedung Tempo Scan Tower, Jakarta Selatan');

-- Donors
INSERT INTO donors (donor_name, contact, phone, email, address) VALUES
('Yayasan Buddha Tzu Chi', 'Johan', '021-50555666', 'info@tzuchi.or.id', 'Pantai Indah Kapuk, Jakarta Utara'),
('Palang Merah Indonesia', 'Dr. Sudirman', '021-7992325', 'pmi@pmi.or.id', 'Jl. Jend. Gatot Subroto No.96, Jakarta'),
('Dompet Dhuafa', 'Rahmad', '021-7416050', 'donasi@dompetdhuafa.org', 'Jl. Mega Mendung No.19, Ciputat'),
('ACT (Aksi Cepat Tanggap)', 'Arie', '021-29406565', 'info@act.id', 'Menara 165 Lantai 11, Cilandak'),
('Warga Peduli Bencana', 'Lestari', '081234567890', 'peduli@kepo.org', 'Komunitas Online');

-- Medicines
INSERT INTO medicines (medicine_code, medicine_name, category, batch_number, unit, stock_quantity, minimum_stock, purchase_price, selling_price, expiry_date, supplier_id) VALUES
('MED-001', 'Amoxicillin 500mg', 'Antibiotik', 'BTH-2024-001', 'Tablet', 1240, 100, 1500.00, 2500.00, '2027-12-15', 1),
('MED-002', 'Paracetamol 500mg', 'Analgesik', 'BTH-2024-002', 'Tablet', 45, 100, 800.00, 1500.00, '2026-08-20', 2),
('MED-003', 'Cetirizine Syrup', 'Antihistamin', 'BTH-2023-010', 'Botol', 12, 30, 12000.00, 18000.00, '2024-01-10', 1),
('MED-004', 'Ibuprofen 400mg', 'Anti-inflamasi', 'BTH-2024-004', 'Kapsul', 850, 50, 2000.00, 3500.00, '2027-03-01', 3),
('MED-005', 'Metformin HCl', 'Antidiabetes', 'BTH-2024-005', 'Tablet', 18, 50, 3000.00, 5000.00, '2026-11-30', 2),
('MED-006', 'Insulin Glargine', 'Antidiabetes', 'BTH-2024-006', 'Vial', 4, 10, 250000.00, 350000.00, '2026-06-15', 4),
('MED-007', 'Amoxicillin Syrup', 'Antibiotik', 'BTH-2024-007', 'Botol', 65, 20, 15000.00, 22000.00, '2026-07-12', 1),
('MED-008', 'Vitamin C 500mg', 'Vitamin', 'BTH-2024-008', 'Tablet', 320, 50, 500.00, 1000.00, '2026-07-15', 5),
('MED-009', 'Omeprazole 20mg', 'Gastrointestinal', 'BTH-2024-009', 'Kapsul', 95, 30, 4000.00, 6500.00, '2027-02-28', 3),
('MED-010', 'Amlodipine 5mg', 'Antihipertensi', 'BTH-2024-010', 'Tablet', 200, 40, 2500.00, 4000.00, '2027-05-10', 4);

-- Distributions
INSERT INTO distributions (doc_num, shelter_id, item_type, quantity, status, notes) VALUES
('DIST-20260614-001', 1, 'OBAT', 100, 'RECEIVED', 'Distribusi Paracetamol dan Ibuprofen untuk pengungsi demam.'),
('DIST-20260614-002', 3, 'MAKANAN', 200, 'SHIPPED', 'Ransum makanan kaleng dan mie instan untuk 2 hari.'),
('DIST-20260614-003', 5, 'OBAT', 50, 'APPROVED', 'Pengiriman masker dan tabung oksigen kecil.');

-- Inventory Transactions (mainly for medicine logs)
INSERT INTO inventory_transactions (medicine_id, transaction_type, quantity, notes) VALUES
(1, 'IN', 1500, 'Penerimaan stok donasi awal'),
(2, 'IN', 100, 'Restok dari Kalbe'),
(3, 'IN', 50, 'Stok awal penanggulangan'),
(1, 'OUT', 260, 'Distribusi darurat ke Shelter Masjid GOR Grogol'),
(2, 'OUT', 55, 'Distribusi darurat ke Shelter RPTRA'),
(5, 'OUT', 5, 'Pemberian resep darurat');

-- Distribution Details
INSERT INTO distribution_details (distribution_id, medicine_id, quantity) VALUES
(1, 2, 50),
(1, 4, 50),
(3, 3, 20),
(3, 7, 30);

-- Shelter Stocks
INSERT INTO shelter_stocks (shelter_id, medicine_id, quantity, minimum_stock) VALUES
(1, 1, 150, 100),
(1, 2, 80, 50),
(1, 4, 100, 50),
(2, 2, 40, 50),
(2, 5, 25, 30),
(3, 1, 200, 100),
(3, 8, 150, 50),
(5, 3, 30, 20),
(5, 7, 35, 20);
