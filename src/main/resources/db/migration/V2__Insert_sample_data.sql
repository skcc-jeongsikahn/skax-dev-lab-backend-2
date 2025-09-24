-- 샘플 사용자 데이터 삽입
INSERT INTO users (username, email, password, name, phone, role, enabled) VALUES
('admin', 'admin@carcenter.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '관리자', '010-1234-5678', 'ADMIN', true),
('user1', 'user1@carcenter.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '사용자1', '010-2345-6789', 'USER', true),
('user2', 'user2@carcenter.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '사용자2', '010-3456-7890', 'USER', true);

-- 비밀번호는 'password123'으로 암호화된 값입니다.
