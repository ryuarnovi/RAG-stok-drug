-- KEPO Database Schema (PostgreSQL)

DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS refugee_movements CASCADE;
DROP TABLE IF EXISTS inventory_transactions CASCADE;
DROP TABLE IF EXISTS distribution_details CASCADE;
DROP TABLE IF EXISTS distributions CASCADE;
DROP TABLE IF EXISTS shelter_stocks CASCADE;
DROP TABLE IF EXISTS medicines CASCADE;
DROP TABLE IF EXISTS refugees CASCADE;
DROP TABLE IF EXISTS shelters CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS suppliers CASCADE;
DROP TABLE IF EXISTS donors CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Users table
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL DEFAULT '',
    role VARCHAR(30) NOT NULL DEFAULT 'SHELTER_OFFICER', -- ADMIN, HEALTH_OFFICER, SHELTER_OFFICER
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Events table
CREATE TABLE events (
    event_id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    location VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, CLOSED
    description TEXT,
    shelter_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Shelters table
CREATE TABLE shelters (
    shelter_id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    location VARCHAR(255) NOT NULL,
    capacity INT NOT NULL DEFAULT 0,
    current_occupancy INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'AMAN', -- AMAN, WASPADA, KRITIS
    penanggung_jawab VARCHAR(100) NOT NULL DEFAULT '',
    event_id INT REFERENCES events(event_id) ON DELETE SET NULL ON UPDATE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Refugees table
CREATE TABLE refugees (
    refugee_id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    nik VARCHAR(30) NOT NULL UNIQUE,
    age INT NOT NULL DEFAULT 0,
    gender VARCHAR(20) NOT NULL, -- Laki-laki, Perempuan
    status VARCHAR(20) NOT NULL DEFAULT 'CHECKED_IN', -- CHECKED_IN, CHECKED_OUT
    medical_notes TEXT,
    priority_status VARCHAR(50) DEFAULT 'REGULAR', -- REGULAR, BALITA, LANSIA, IBU_HAMIL, DISABILITAS, SICK
    family_code VARCHAR(50),
    shelter_id INT REFERENCES shelters(shelter_id) ON DELETE SET NULL ON UPDATE CASCADE,
    check_in_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    check_out_time TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Refugee Movements table
CREATE TABLE refugee_movements (
    movement_id SERIAL PRIMARY KEY,
    refugee_id INT NOT NULL REFERENCES refugees(refugee_id) ON DELETE CASCADE,
    from_shelter_id INT REFERENCES shelters(shelter_id) ON DELETE SET NULL ON UPDATE CASCADE,
    to_shelter_id INT REFERENCES shelters(shelter_id) ON DELETE SET NULL ON UPDATE CASCADE,
    moved_by VARCHAR(50) REFERENCES users(username) ON DELETE SET NULL ON UPDATE CASCADE,
    notes TEXT,
    moved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Suppliers table
CREATE TABLE suppliers (
    supplier_id SERIAL PRIMARY KEY,
    supplier_name VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Donors table
CREATE TABLE donors (
    donor_id SERIAL PRIMARY KEY,
    donor_name VARCHAR(150) NOT NULL,
    contact VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Medicines table
CREATE TABLE medicines (
    medicine_id SERIAL PRIMARY KEY,
    medicine_code VARCHAR(30) NOT NULL UNIQUE,
    medicine_name VARCHAR(200) NOT NULL,
    category VARCHAR(50) NOT NULL,
    batch_number VARCHAR(50),
    unit VARCHAR(30) NOT NULL DEFAULT 'Tablet',
    stock_quantity INT NOT NULL DEFAULT 0,
    minimum_stock INT NOT NULL DEFAULT 10,
    purchase_price DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    selling_price DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    expiry_date DATE,
    supplier_id INT REFERENCES suppliers(supplier_id) ON DELETE SET NULL ON UPDATE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Distributions table
CREATE TABLE distributions (
    distribution_id SERIAL PRIMARY KEY,
    doc_num VARCHAR(50) NOT NULL UNIQUE,
    shelter_id INT REFERENCES shelters(shelter_id) ON DELETE CASCADE ON UPDATE CASCADE,
    item_type VARCHAR(50) NOT NULL, -- OBAT, LOGISTIK, MAKANAN, PAKAIAN
    quantity INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, APPROVED, SHIPPED, RECEIVED
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Distribution Details table (detailed medicine/item distribution)
CREATE TABLE distribution_details (
    detail_id SERIAL PRIMARY KEY,
    distribution_id INT NOT NULL REFERENCES distributions(distribution_id) ON DELETE CASCADE,
    medicine_id INT NOT NULL REFERENCES medicines(medicine_id) ON DELETE CASCADE,
    quantity INT NOT NULL CHECK (quantity > 0)
);

-- Shelter Stocks table (inventaris per posko)
CREATE TABLE shelter_stocks (
    shelter_stock_id SERIAL PRIMARY KEY,
    shelter_id INT NOT NULL REFERENCES shelters(shelter_id) ON DELETE CASCADE ON UPDATE CASCADE,
    medicine_id INT NOT NULL REFERENCES medicines(medicine_id) ON DELETE CASCADE ON UPDATE CASCADE,
    quantity INT NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    minimum_stock INT NOT NULL DEFAULT 10 CHECK (minimum_stock >= 0),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(shelter_id, medicine_id)
);

-- Inventory Transactions table (mainly for medicine stock logs)
CREATE TABLE inventory_transactions (
    transaction_id SERIAL PRIMARY KEY,
    medicine_id INT NOT NULL REFERENCES medicines(medicine_id) ON DELETE CASCADE ON UPDATE CASCADE,
    transaction_type VARCHAR(20) NOT NULL, -- IN, OUT, ADJUSTMENT
    quantity INT NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

-- Audit Logs table
CREATE TABLE audit_logs (
    log_id SERIAL PRIMARY KEY,
    username VARCHAR(50),
    action VARCHAR(255) NOT NULL,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refugee_nik ON refugees(nik);
CREATE INDEX idx_refugee_shelter ON refugees(shelter_id);
CREATE INDEX idx_medicine_code ON medicines(medicine_code);
CREATE INDEX idx_medicine_expiry ON medicines(expiry_date);
CREATE INDEX idx_distribution_doc ON distributions(doc_num);
CREATE INDEX idx_it_medicine ON inventory_transactions(medicine_id);
