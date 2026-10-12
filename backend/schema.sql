CREATE DATABASE IF NOT EXISTS monate_chicken CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE monate_chicken;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(254) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NULL,
  google_sub VARCHAR(255) NULL UNIQUE,
  auth_provider ENUM('password','google','mixed') NOT NULL DEFAULT 'password',
  loyalty_points INT UNSIGNED NOT NULL DEFAULT 0,
  notifications_enabled TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS api_tokens (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  token_hash CHAR(64) NOT NULL UNIQUE,
  expires_at DATETIME NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_api_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_api_tokens_expiry (expires_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS menu_items (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  category VARCHAR(80) NOT NULL,
  description TEXT NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  emoji VARCHAR(16) NOT NULL DEFAULT '🍽️',
  image_url VARCHAR(1000) NULL,
  UNIQUE KEY uq_menu_name_category (name, category),
  is_available TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS orders (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  status ENUM('RECEIVED','PREPARING','READY','DELIVERED','CANCELLED') NOT NULL DEFAULT 'RECEIVED',
  total DECIMAL(10,2) NOT NULL,
  collection_time VARCHAR(120) NOT NULL DEFAULT 'ASAP',
  payment_method VARCHAR(80) NOT NULL DEFAULT 'Cash on Collection',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_orders_user_created (user_id, created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS order_items (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT UNSIGNED NOT NULL,
  menu_item_id INT UNSIGNED NOT NULL,
  quantity SMALLINT UNSIGNED NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_order_items_menu FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reviews (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  menu_item_id INT UNSIGNED NULL,
  rating TINYINT UNSIGNED NOT NULL,
  comment TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5),
  CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_reviews_menu FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS loyalty_transactions (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  points INT NOT NULL,
  reason VARCHAR(120) NOT NULL,
  order_id BIGINT UNSIGNED NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_loyalty_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_loyalty_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL
) ENGINE=InnoDB;

INSERT INTO menu_items (name,category,description,price,emoji) VALUES
('Quarter Chicken','Chicken','Flame-grilled chicken with your choice of side.',75.00,'🍗'),
('Rump 300g','Steak','Juicy rump steak served with a side.',180.00,'🥩'),
('6 Wings','Wings','Peri-peri wings served with a side.',95.00,'🍖'),
('Cheese Burger','Burgers','Beef patty, cheese, lettuce, tomato & sauce.',55.00,'🍔'),
('Malva Pudding','Desserts','Traditional Malva pudding & custard.',28.00,'🍮'),
('Coca Cola 330ml','Drinks','Refreshing Coca Cola soft drink.',20.00,'🥤'),
('Sprite 330ml','Drinks','Refreshing Sprite soft drink.',20.00,'🥤'),
('Still Water 500ml','Drinks','Bottled still water.',15.00,'💧'),
('Pap & Gravy','Sides','Traditional pap with tasty gravy.',20.00,'🍲'),
('Chips','Sides','Crispy golden fried chips.',25.00,'🍟'),
('Margherita Pizza','Pizza','Cheese & tomato classic pizza.',85.00,'🍕'),
('Chicken Wrap','Wraps','Grilled chicken with lettuce & sauce.',65.00,'🌯')
ON DUPLICATE KEY UPDATE description=VALUES(description), price=VALUES(price), emoji=VALUES(emoji);
