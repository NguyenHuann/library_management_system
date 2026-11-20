-- Schema/dữ liệu sách mẫu. Server áp dụng thêm advanced.sql khi khởi động lần đầu.
CREATE TABLE IF NOT EXISTS Sach (
    MaSach VARCHAR(10) PRIMARY KEY NOT NULL CHECK (length(trim(MaSach)) BETWEEN 1 AND 10),
    TenSach VARCHAR(100) NOT NULL CHECK (length(trim(TenSach)) BETWEEN 1 AND 100),
    TacGia VARCHAR(100) NOT NULL CHECK (length(trim(TacGia)) BETWEEN 1 AND 100),
    NamXB INT NOT NULL CHECK (NamXB BETWEEN 1 AND 9999),
    SoLuong INT NOT NULL CHECK (SoLuong >= 0),
    Version INTEGER NOT NULL DEFAULT 0 CHECK (Version >= 0)
);
INSERT OR IGNORE INTO Sach (MaSach, TenSach, TacGia, NamXB, SoLuong) VALUES
('S001', 'Lập trình Java căn bản', 'Phạm Văn Ất', 2022, 15),
('S002', 'Mạng máy tính', 'Nguyễn Thúc Hải', 2021, 12),
('S003', 'Cơ sở dữ liệu', 'Nguyễn Kim Anh', 2020, 8),
('S004', 'Cấu trúc dữ liệu và giải thuật', 'Đỗ Xuân Lôi', 2023, 10),
('S005', 'Clean Code', 'Robert C. Martin', 2008, 6),
('S006', 'Dế Mèn phiêu lưu ký', 'Tô Hoài', 2020, 20),
('S007', 'Tôi thấy hoa vàng trên cỏ xanh', 'Nguyễn Nhật Ánh', 2019, 14),
('S008', 'Nhập môn trí tuệ nhân tạo', 'Đinh Mạnh Tường', 2024, 9);
