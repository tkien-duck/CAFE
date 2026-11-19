USE master;
GO
IF DB_ID(N'QuanLyQuanCafe') IS NOT NULL
BEGIN
    ALTER DATABASE QuanLyQuanCafe SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QuanLyQuanCafe;
END
GO

CREATE DATABASE QuanLyQuanCafe;
GO
USE QuanLyQuanCafe;
GO

-- 1. Bảng danh mục
CREATE TABLE categories (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE
);

-- 2. Bảng sản phẩm / thực đơn
CREATE TABLE products (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE,
    price FLOAT NOT NULL,
    category_id INT FOREIGN KEY REFERENCES categories(id),
    status NVARCHAR(50) DEFAULT N'Còn món'
);

-- 3. Bảng người dùng
CREATE TABLE users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(50) NOT NULL,
    full_name NVARCHAR(100) NOT NULL
);

-- 4. Bảng hóa đơn
CREATE TABLE orders (
    id INT IDENTITY(1,1) PRIMARY KEY,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    staff NVARCHAR(100) NOT NULL,
    total FLOAT NOT NULL
);

-- 5. Bảng chi tiết hóa đơn
CREATE TABLE order_details (
    id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL FOREIGN KEY REFERENCES orders(id),
    product_id INT NOT NULL FOREIGN KEY REFERENCES products(id),
    quantity INT NOT NULL,
    price FLOAT NOT NULL
);
GO

INSERT INTO categories (name) VALUES
(N'Cà phê'),      -- id 1
(N'Trà sữa'),     -- id 2
(N'Đồ ăn vặt'),   -- id 3
(N'Topping');     -- id 4

-- Mã danh mục: 1 = Cà phê, 2 = Trà sữa, 3 = Đồ ăn vặt, 4 = Topping
INSERT INTO products (name, price, category_id, status) VALUES
(N'Cà phê đen đá',     35000, 1, N'Còn món'),
(N'Cà phê sữa đá',     40000, 1, N'Còn món'),
(N'Bạc xỉu',           32000, 1, N'Còn món'),
(N'Trà sữa trân châu', 50000, 2, N'Còn món'),
(N'Trà sữa matcha',    45000, 2, N'Còn món'),
(N'Trà sữa socola',    42000, 2, N'Còn món'),
(N'Khoai tây chiên',   35000, 3, N'Còn món'),
(N'Bánh tráng trộn',   25000, 3, N'Còn món'),
(N'Trân châu trắng',    5000, 4, N'Còn món'),
(N'Hạt nổ củ năng',     7000, 4, N'Còn món');

INSERT INTO users (username, password, full_name) VALUES
(N'admin',    N'123456', N'Quản lý bán hàng'),
(N'nhanvien', N'123123', N'Nhân viên thu ngân');
GO

-- =====================================================================
-- KIỂM TRA: 4 danh mục, 10 món, 2 tài khoản
-- =====================================================================
SELECT * FROM categories ORDER BY id;
SELECT p.id, p.name, p.price, p.status, c.name AS danhmuc
FROM products p LEFT JOIN categories c ON p.category_id = c.id
ORDER BY p.id;
SELECT * FROM users ORDER BY id;
USE QuanLyQuanCafe;
INSERT INTO products (name, price, category_id, status)
SELECT N'Hạt nổ củ năng', 5000, id, N'Còn món'
FROM categories
WHERE name = N'Topping';
GO