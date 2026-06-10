-- PharmaStock Database Schema
-- MySQL 8+

CREATE DATABASE IF NOT EXISTS pharmastock
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pharmastock;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL DEFAULT '',
    role ENUM('ADMIN', 'PHARMACIST', 'STAFF') NOT NULL DEFAULT 'STAFF',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Suppliers table
CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_name VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Medicines table
CREATE TABLE IF NOT EXISTS medicines (
    medicine_id INT AUTO_INCREMENT PRIMARY KEY,
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
    supplier_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_medicine_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_medicine_code ON medicines(medicine_code);
CREATE INDEX idx_medicine_category ON medicines(category);
CREATE INDEX idx_medicine_expiry ON medicines(expiry_date);
CREATE INDEX idx_medicine_supplier ON medicines(supplier_id);

-- Inventory Transactions table
CREATE TABLE IF NOT EXISTS inventory_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id INT NOT NULL,
    transaction_type ENUM('IN', 'OUT', 'ADJUSTMENT') NOT NULL,
    quantity INT NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    CONSTRAINT fk_it_medicine FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_it_medicine ON inventory_transactions(medicine_id);
CREATE INDEX idx_it_date ON inventory_transactions(transaction_date);
CREATE INDEX idx_it_type ON inventory_transactions(transaction_type);
