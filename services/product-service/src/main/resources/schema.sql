-- ============================================================================
-- Product Service - MySQL Schema
-- Database: product_service_db
--
-- Run once on a fresh DB:
--   mysql -u root -p < schema.sql
--
-- Then optionally seed with: mysql -u root -p product_service_db < data.sql
-- ============================================================================

CREATE DATABASE IF NOT EXISTS product_service_db;
USE product_service_db;

-- Drop in reverse-dependency order (safe to re-run)
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS category;

-- ----------------------------------------------------------------------------
-- category
-- ----------------------------------------------------------------------------
CREATE TABLE category (
                          category_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
                          category_name  VARCHAR(100) NOT NULL UNIQUE,
                          created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- @CreatedDate
                          updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP   -- @LastModifiedDate
                              ON UPDATE CURRENT_TIMESTAMP,
                          created_by     VARCHAR(50),                          -- @CreatedBy
                          updated_by     VARCHAR(50)                           -- @LastModifiedBy
);

-- ----------------------------------------------------------------------------
-- products
-- ----------------------------------------------------------------------------
CREATE TABLE products (
                          product_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
                          product_name  VARCHAR(255) NOT NULL,
                          description   VARCHAR(500),
                          price         DOUBLE NOT NULL,
                          stock         INT DEFAULT 0,
                          category_id   BIGINT NOT NULL,
                          image_url     VARCHAR(500),
                          created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,   -- @CreatedDate
                          updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP    -- @LastModifiedDate
                              ON UPDATE CURRENT_TIMESTAMP,
                          created_by    VARCHAR(50),                           -- @CreatedBy
                          updated_by    VARCHAR(50),                           -- @LastModifiedBy
                          FOREIGN KEY (category_id)
                              REFERENCES category(category_id)
);
