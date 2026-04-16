CREATE TABLE IF NOT EXISTS user_info (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    real_name VARCHAR(64) DEFAULT NULL,
    phone VARCHAR(32) DEFAULT NULL,
    address VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_info_user_id (user_id),
    CONSTRAINT fk_user_info_user_id FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
);
