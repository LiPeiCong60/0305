INSERT INTO user_info (user_id, real_name, phone, address)
SELECT 1, 'admin profile', '13800138000', 'Shanghai Pudong'
WHERE EXISTS (SELECT 1 FROM sys_user WHERE id = 1)
  AND NOT EXISTS (SELECT 1 FROM user_info WHERE user_id = 1);

INSERT INTO user_info (user_id, real_name, phone, address)
SELECT 2, 'demo user 2', '13900139000', 'Beijing Haidian'
WHERE EXISTS (SELECT 1 FROM sys_user WHERE id = 2)
  AND NOT EXISTS (SELECT 1 FROM user_info WHERE user_id = 2);

INSERT INTO user_info (user_id, real_name, phone, address)
SELECT 3, 'demo user 3', '13700137000', 'Guangzhou Tianhe'
WHERE EXISTS (SELECT 1 FROM sys_user WHERE id = 3)
  AND NOT EXISTS (SELECT 1 FROM user_info WHERE user_id = 3);
