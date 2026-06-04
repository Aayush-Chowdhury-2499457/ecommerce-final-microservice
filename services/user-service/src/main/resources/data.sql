USE user_service;
# ADMIN

-- SELECT * FROM users;
-- -- admin password - "admin123456"; (admin and password)
USE user_service;

INSERT INTO users (
    name,
    username,
    email,
    phone_number,
    date_of_birth,
    role,
    created_at,
    updated_at,
    created_by,
    updated_by
) VALUES (
             'SYS_ADMIN',
             'ADMIN',
             'admin@ecommerce.com',
             '1234567890',
             '1990-01-01',
             'ADMIN',
             NOW(),
             NOW(),
             'ADMIN',
             'ADMIN'
         );

-- Truncate Users;
# USER
