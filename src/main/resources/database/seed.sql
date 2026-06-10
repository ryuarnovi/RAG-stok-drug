-- PharmaStock Seed Data
USE pharmastock;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE inventory_transactions;
TRUNCATE TABLE medicines;
TRUNCATE TABLE suppliers;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- Users (passwords are bcrypt hashes)
-- admin123, pharma123, staff123
INSERT INTO users (username, password_hash, full_name, role) VALUES
('admin', '$2a$12$Ctmg56/uUxhE0pIhD3lLq.RMA435rS3ce1nl.UDkmw2ZsVIpbSkXW', 'Dr. Aris Wijaya', 'ADMIN'),
('pharmacist', '$2a$12$jCpbRrTY9zMrEKiaidHjheX39/zr9DBEu8ePIStsWVbKDIhSPxD76', 'Apt. Sari Dewi', 'PHARMACIST'),
('staff', '$2a$12$NyYrBjXt0HBAjtjACgmXJusscU6PywyeSaiz6a7tVaILr8DtpfSi.', 'Budi Santoso', 'STAFF');

-- Suppliers
INSERT INTO suppliers (supplier_name, contact_person, phone, email, address) VALUES
('PT Kimia Farma Tbk', 'Hendra Kusuma', '021-4208311', 'order@kimiafarma.co.id', 'Jl. Veteran No.9, Jakarta Pusat'),
('PT Kalbe Farma Tbk', 'Rina Marlina', '021-4287 3888', 'supply@kalbe.co.id', 'Gedung Kalbe, Jl. Let. Jend. Suprapto, Jakarta'),
('PT Sanbe Farma', 'Dedi Hermawan', '022-7806 777', 'info@sanbe.co.id', 'Jl. Tamansari No.10, Bandung'),
('PT Dexa Medica', 'Fitri Handayani', '0751-40444', 'order@dexa-medica.com', 'Jl. Letnan Jenderal Bambang Utoyo No.138, Palembang'),
('PT Tempo Scan Pacific', 'Ahmad Fauzi', '021-7531 0011', 'procurement@temposcan.co.id', 'Gedung Tempo Scan Tower, Jakarta Selatan');

-- Medicines
INSERT INTO medicines (medicine_code, medicine_name, category, batch_number, unit, stock_quantity, minimum_stock, purchase_price, selling_price, expiry_date, supplier_id) VALUES
('MED-001', 'Amoxicillin 500mg', 'Antibiotik', 'BTH-2024-001', 'Tablet', 1240, 100, 1500.00, 2500.00, '2025-12-15', 1),
('MED-002', 'Paracetamol 500mg', 'Analgesik', 'BTH-2024-002', 'Tablet', 45, 100, 800.00, 1500.00, '2025-08-20', 2),
('MED-003', 'Cetirizine Syrup', 'Antihistamin', 'BTH-2023-010', 'Botol', 12, 30, 12000.00, 18000.00, '2024-01-10', 1),
('MED-004', 'Ibuprofen 400mg', 'Anti-inflamasi', 'BTH-2024-004', 'Kapsul', 850, 50, 2000.00, 3500.00, '2026-03-01', 3),
('MED-005', 'Metformin HCl', 'Antidiabetes', 'BTH-2024-005', 'Tablet', 18, 50, 3000.00, 5000.00, '2025-11-30', 2),
('MED-006', 'Insulin Glargine', 'Antidiabetes', 'BTH-2024-006', 'Vial', 4, 10, 250000.00, 350000.00, '2025-06-15', 4),
('MED-007', 'Amoxicillin Syrup', 'Antibiotik', 'BTH-2024-007', 'Botol', 65, 20, 15000.00, 22000.00, '2025-07-12', 1),
('MED-008', 'Vitamin C 500mg', 'Vitamin', 'BTH-2024-008', 'Tablet', 320, 50, 500.00, 1000.00, '2025-07-15', 5),
('MED-009', 'Omeprazole 20mg', 'Gastrointestinal', 'BTH-2024-009', 'Kapsul', 95, 30, 4000.00, 6500.00, '2026-02-28', 3),
('MED-010', 'Amlodipine 5mg', 'Antihipertensi', 'BTH-2024-010', 'Tablet', 200, 40, 2500.00, 4000.00, '2026-05-10', 4),
('MED-011', 'Ciprofloxacin 500mg', 'Antibiotik', 'BTH-2024-011', 'Tablet', 150, 30, 3500.00, 5500.00, '2026-01-20', 1),
('MED-012', 'Dexamethasone 0.5mg', 'Kortikosteroid', 'BTH-2024-012', 'Tablet', 500, 60, 1200.00, 2000.00, '2026-04-15', 2),
('MED-013', 'Salbutamol Inhaler', 'Respiratori', 'BTH-2024-013', 'Unit', 25, 15, 45000.00, 65000.00, '2025-09-01', 3),
('MED-014', 'Diazepam 5mg', 'Sedatif', 'BTH-2024-014', 'Tablet', 80, 20, 5000.00, 8000.00, '2026-06-30', 5),
('MED-015', 'Captopril 25mg', 'Antihipertensi', 'BTH-2024-015', 'Tablet', 180, 40, 1800.00, 3000.00, '2026-07-20', 4);

-- Inventory Transactions
INSERT INTO inventory_transactions (medicine_id, transaction_type, quantity, transaction_date, notes) VALUES
(1, 'IN', 500, '2024-06-01 09:00:00', 'Restok dari PO-20240601-001'),
(2, 'OUT', 30, '2024-06-02 10:30:00', 'Penjualan harian'),
(3, 'OUT', 5, '2024-06-02 11:00:00', 'Penjualan harian'),
(1, 'OUT', 20, '2024-06-03 09:15:00', 'Penjualan harian'),
(4, 'IN', 200, '2024-06-03 14:00:00', 'Restok manual'),
(5, 'OUT', 12, '2024-06-04 08:45:00', 'Penjualan harian'),
(2, 'IN', 50, '2024-06-05 10:00:00', 'Restok dari supplier'),
(8, 'OUT', 45, '2024-06-05 13:30:00', 'Penjualan harian'),
(6, 'OUT', 2, '2024-06-06 09:00:00', 'Penjualan resep'),
(9, 'IN', 30, '2024-06-07 11:00:00', 'Restok manual'),
(10, 'OUT', 15, '2024-06-08 10:00:00', 'Penjualan harian'),
(7, 'OUT', 10, '2024-06-08 14:30:00', 'Penjualan harian'),
(11, 'IN', 50, '2024-06-09 09:00:00', 'Restok dari PO'),
(12, 'OUT', 25, '2024-06-09 11:00:00', 'Penjualan harian'),
(1, 'OUT', 35, '2024-06-10 08:00:00', 'Penjualan harian'),
(13, 'OUT', 3, '2024-06-10 10:00:00', 'Penjualan resep'),
(2, 'ADJUSTMENT', -5, '2024-06-10 15:00:00', 'Stok opname - selisih'),
(14, 'OUT', 8, '2024-06-10 16:00:00', 'Penjualan resep'),
(15, 'IN', 100, '2024-06-11 09:00:00', 'Restok manual'),
(4, 'OUT', 50, '2024-06-11 13:00:00', 'Penjualan harian');
