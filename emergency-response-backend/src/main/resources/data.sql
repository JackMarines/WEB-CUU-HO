-- Admin local accounts (password: admin123)
INSERT IGNORE INTO users (name, email, phone, password, provider, role, is_active, created_at) VALUES
('Quản trị viên', 'admin@emergency.vn', '0900000000', '$2a$10$8eKxi.QC0XJDWtlq2ZaCnOGBf7/iWj0OCLsUzY1mWNdG5YZUOSpEq', 'local', 'admin', true, NOW()),
('Super Admin', 'superadmin@emergency.vn', '0900000001', '$2a$10$8eKxi.QC0XJDWtlq2ZaCnOGBf7/iWj0OCLsUzY1mWNdG5YZUOSpEq', 'local', 'superadmin', true, NOW());

-- Disaster types
INSERT IGNORE INTO disaster_types (name, slug, icon, base_urgency_score, created_at) VALUES
('Lũ lụt', 'flood', '🌊', 80, NOW()),
('Cháy', 'fire', '🔥', 90, NOW()),
('Sạt lở đất', 'landslide', '⛰️', 85, NOW()),
('Bão', 'storm', '🌀', 70, NOW()),
('Khác', 'other', '⚠️', 50, NOW());

-- Rescue centers
INSERT IGNORE INTO rescue_centers (name, type, lat, lng, address, phone, supplies, capacity, created_at) VALUES
('Điểm cứu trợ Quận 1', 'supply_distribution', 10.7768899, 106.7005228, '123 Nguyễn Huệ, Quận 1, TP. HCM', '0901234567', '{"water":500,"rice":200,"noodles":300,"life_jackets":100}', 500, NOW()),
('Nhà tạm trú Đống Đa', 'shelter', 21.0277644, 105.8341598, '456 Tây Sơn, Đống Đa, Hà Nội', '0902345678', '{"water":300,"rice":150,"blankets":200}', 300, NOW()),
('Đội cứu hộ Sơn Trà', 'rescue_team', 16.0544068, 108.2021665, '789 Ngô Quyền, Sơn Trà, Đà Nẵng', '0903456789', '{"boats":5,"life_jackets":50,"first_aid_kits":20}', 100, NOW()),
('Trạm cứu trợ Huế', 'supply_distribution', 16.4637130, 107.5908666, '12 Trần Hưng Đạo, TP. Huế', '0904567890', '{"water":400,"rice":250,"noodles":200}', 400, NOW()),
('Điểm sơ tán Cầu Giấy', 'shelter', 21.0285110, 105.7882440, '78 Cầu Giấy, Hà Nội', '0905678901', '{"water":200,"blankets":150,"mats":100}', 200, NOW()),
('Đội cứu hộ Liên Chiểu', 'rescue_team', 16.0750000, 108.1500000, '456 Nguyễn Lương Bằng, Đà Nẵng', '0906789012', '{"boats":3,"life_jackets":30,"first_aid_kits":15}', 60, NOW()),
('Điểm phát gạo Thủ Đức', 'supply_distribution', 10.8500000, 106.7700000, '789 Kha Vạn Cân, Thủ Đức, TP. HCM', '0907890123', '{"rice":500,"noodles":400,"water":600}', 800, NOW()),
('Nhà tạm trú Hai Bà Trưng', 'shelter', 21.0130000, 105.8500000, '123 Bạch Mai, Hai Bà Trưng, Hà Nội', '0908901234', '{"water":250,"blankets":180,"food":300}', 250, NOW());
