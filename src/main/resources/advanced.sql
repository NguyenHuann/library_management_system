-- Áp dụng sau database.sql. Server tạo tài khoản mẫu bằng mật khẩu băm khi tạo bảng lần đầu.
CREATE TABLE IF NOT EXISTS TaiKhoan (
    TenDangNhap TEXT PRIMARY KEY COLLATE NOCASE NOT NULL,
    Salt BLOB NOT NULL CHECK (length(Salt) = 16),
    MatKhauBam BLOB NOT NULL CHECK (length(MatKhauBam) = 32),
    SoVongLap INTEGER NOT NULL CHECK (SoVongLap BETWEEN 100000 AND 2000000),
    Role TEXT NOT NULL DEFAULT 'READER' CHECK (Role IN ('ADMIN', 'READER'))
);

CREATE TABLE IF NOT EXISTS PhieuMuon (
    Id INTEGER PRIMARY KEY AUTOINCREMENT,
    TenDangNhap TEXT NOT NULL REFERENCES TaiKhoan(TenDangNhap),
    MaSach TEXT NOT NULL,
    TenSach TEXT NOT NULL,
    MuonLuc TEXT NOT NULL,
    TraLuc TEXT
);

CREATE INDEX IF NOT EXISTS PhieuMuon_TaiKhoan ON PhieuMuon (TenDangNhap, Id DESC);
CREATE INDEX IF NOT EXISTS PhieuMuon_SachDangMuon ON PhieuMuon (MaSach) WHERE TraLuc IS NULL;
