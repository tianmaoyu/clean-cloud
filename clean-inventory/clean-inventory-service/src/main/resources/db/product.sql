CREATE TABLE categories (
                            category_id SERIAL PRIMARY KEY,
                            category_name VARCHAR(100) NOT NULL,
                            parent_id INTEGER REFERENCES categories(category_id),
                            category_type VARCHAR(50) NOT NULL,  -- 如 'electronics', 'clothing', 'food'
                            description TEXT,
                            is_active BOOLEAN DEFAULT TRUE,
                            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                            attribute_schema JSONB  -- 存储该类别的属性定义/模板
);


CREATE TABLE products (
                          product_id BIGSERIAL PRIMARY KEY,
                          product_code VARCHAR(50) NOT NULL UNIQUE,
                          product_name VARCHAR(100) NOT NULL,
                          category_id INTEGER NOT NULL REFERENCES categories(category_id),
                          brand_id INTEGER,
                          price DECIMAL(10, 2) NOT NULL,
                          cost DECIMAL(10, 2),
                          stock_quantity INTEGER DEFAULT 0,
                          weight DECIMAL(10, 2),
                          is_active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          description TEXT,
                          image_url VARCHAR(255),
                          tags VARCHAR(255)[],
                          attributes JSONB NOT NULL,  -- 存储商品具体属性值
                          FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

-- 创建索引
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_attributes ON products USING GIN(attributes jsonb_path_ops);