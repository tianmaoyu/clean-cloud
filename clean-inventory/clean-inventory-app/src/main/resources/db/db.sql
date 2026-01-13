CREATE TABLE material (
                          material_id BIGSERIAL PRIMARY KEY,          -- 物料ID，自增主键
                          material_code VARCHAR(50) NOT NULL UNIQUE,  -- 物料编码，唯一
                          material_name VARCHAR(100) NOT NULL,        -- 物料名称
                          material_type VARCHAR(30) NOT NULL,         -- 物料类型(原材料/半成品/成品等)
                          base_unit VARCHAR(20) NOT NULL,             -- 基本单位
                          is_batch BOOLEAN DEFAULT FALSE,             -- 是否启用批次管理
                          is_serial BOOLEAN DEFAULT FALSE,            -- 是否启用序列号管理
                          is_active BOOLEAN DEFAULT TRUE,             -- 是否启用
                          description TEXT,                           -- 物料描述
                          created_at TIMESTAMPTZ DEFAULT NOW(),       -- 创建时间
                          updated_at TIMESTAMPTZ DEFAULT NOW(),       -- 更新时间
                          created_by BIGINT,                          -- 创建人
                          updated_by BIGINT                           -- 更新人
);

COMMENT ON TABLE material IS '物料主表';
COMMENT ON COLUMN material.material_id IS '物料ID，自增主键';
COMMENT ON COLUMN material.is_batch IS '是否启用批次管理(TRUE/FALSE)';
COMMENT ON COLUMN material.is_serial IS '是否启用序列号管理(TRUE/FALSE)';


CREATE TABLE material_attribute (
                                    attribute_id BIGSERIAL PRIMARY KEY,
                                    material_id BIGINT NOT NULL REFERENCES material(material_id), -- 关联物料
    -- 通用属性
                                    spec_model VARCHAR(100),      -- 规格型号
                                    weight DECIMAL(10,3),         -- 重量(kg)
                                    volume DECIMAL(10,3),         -- 体积(m³)
                                    color VARCHAR(30),            -- 颜色
                                    shelf_life INT,               -- 保质期(天)
                                    safety_stock DECIMAL(12,3),   -- 安全库存
                                    max_stock DECIMAL(12,3),      -- 最大库存
                                    min_stock DECIMAL(12,3),      -- 最小库存
    -- 动态属性
                                    extended_attrs JSONB,         -- 扩展属性(JSON格式)
                                    created_at TIMESTAMPTZ DEFAULT NOW(),
                                    updated_at TIMESTAMPTZ DEFAULT NOW()
);

COMMENT ON TABLE material_attribute IS '物料属性表';
COMMENT ON COLUMN material_attribute.extended_attrs IS '扩展属性，JSON格式存储动态属性';


CREATE TABLE warehouse (
                           warehouse_id BIGSERIAL PRIMARY KEY,
                           warehouse_code VARCHAR(30) NOT NULL UNIQUE,  -- 仓库编码
                           warehouse_name VARCHAR(100) NOT NULL,        -- 仓库名称
                           warehouse_type VARCHAR(30),                  -- 仓库类型(原料仓/成品仓等)
                           is_active BOOLEAN DEFAULT TRUE,              -- 是否启用
                           address TEXT,                                -- 仓库地址
                           area DECIMAL(10,2),                          -- 仓库面积(m²)
                           manager VARCHAR(50),                         -- 负责人
                           contact_phone VARCHAR(20),                   -- 联系电话
                           created_at TIMESTAMPTZ DEFAULT NOW(),
                           updated_at TIMESTAMPTZ DEFAULT NOW()
);

COMMENT ON TABLE warehouse IS '仓库主表';
COMMENT ON COLUMN warehouse.warehouse_type IS '仓库类型：原料仓/成品仓/半成品仓/退货仓/废品仓等';


CREATE TABLE location (
                          location_id BIGSERIAL PRIMARY KEY,
                          warehouse_id BIGINT NOT NULL REFERENCES warehouse(warehouse_id), -- 所属仓库
                          location_code VARCHAR(30) NOT NULL,          -- 库位编码
                          location_name VARCHAR(100) NOT NULL,         -- 库位名称
                          location_type VARCHAR(30),                   -- 库位类型(货架/平面区等)
                          storage_capacity DECIMAL(10,2),              -- 存储容量(kg/m³等)
                          is_default BOOLEAN DEFAULT FALSE,            -- 是否默认库位
                          is_active BOOLEAN DEFAULT TRUE,              -- 是否启用
                          barcode VARCHAR(50),                        -- 库位条码
                          coordinates VARCHAR(50),                     -- 坐标位置(A-01-02)
                          notes TEXT,                                 -- 备注
                          created_at TIMESTAMPTZ DEFAULT NOW(),
                          updated_at TIMESTAMPTZ DEFAULT NOW(),
                          CONSTRAINT uk_location_code UNIQUE (warehouse_id, location_code)
);

CREATE INDEX idx_location_warehouse ON location(warehouse_id);
CREATE INDEX idx_location_code ON location(location_code);

COMMENT ON TABLE location IS '仓库库位表';
COMMENT ON COLUMN location.coordinates IS '库位坐标，如A区1排2号可存储为A-01-02';


CREATE TABLE batch (
                       batch_id BIGSERIAL PRIMARY KEY,
                       material_id BIGINT NOT NULL REFERENCES material(material_id), -- 关联物料
                       batch_code VARCHAR(50) NOT NULL,            -- 批次号/序列号
                       production_date DATE,                       -- 生产日期
                       expiration_date DATE,                       -- 过期日期
                       supplier_id BIGINT,                         -- 供应商ID
                       supplier_batch VARCHAR(50),                 -- 供应商批次号
                       quality_status VARCHAR(20)                  -- 质量状态
                           CHECK (quality_status IN ('合格','不合格','待检','已过期')),
                       notes TEXT,                                 -- 备注
                       created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_batch_material ON batch(material_id);
CREATE INDEX idx_batch_code ON batch(batch_code);
CREATE INDEX idx_batch_dates ON batch(production_date, expiration_date);

COMMENT ON TABLE batch IS '物料批次/序列号表';
COMMENT ON COLUMN batch.batch_code IS '批次号或序列号，根据物料设置决定';


CREATE TABLE stock (
                       stock_id BIGSERIAL PRIMARY KEY,
                       material_id BIGINT NOT NULL REFERENCES material(material_id), -- 物料
                       warehouse_id BIGINT NOT NULL REFERENCES warehouse(warehouse_id), -- 仓库
                       batch_id BIGINT REFERENCES batch(batch_id),    -- 批次(可为空)
    -- 库存数量
                       quantity DECIMAL(16,4) NOT NULL DEFAULT 0      -- 总数量
                           CHECK (quantity >= 0),
                       available_qty DECIMAL(16,4) NOT NULL DEFAULT 0 -- 可用数量
                           CHECK (available_qty >= 0 AND available_qty <= quantity),
                       locked_qty DECIMAL(16,4) DEFAULT 0             -- 锁定数量
                           CHECK (locked_qty >= 0),
    -- 状态
                       status VARCHAR(20) DEFAULT '正常'              -- 库存状态
                           CHECK (status IN ('正常','冻结','报废')),
                       last_check_time TIMESTAMPTZ,                   -- 最后盘点时间
                       created_at TIMESTAMPTZ DEFAULT NOW(),
                       updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_stock_material ON stock(material_id);
CREATE INDEX idx_stock_warehouse ON stock(warehouse_id);
CREATE INDEX idx_stock_batch ON stock(batch_id);
CREATE INDEX idx_stock_status ON stock(status);

COMMENT ON TABLE stock IS '库存主表';
COMMENT ON COLUMN stock.available_qty IS '可用数量 = 总数量 - 锁定数量';


CREATE TABLE stock_transaction (
                                   transaction_id BIGSERIAL PRIMARY KEY,
                                   stock_id BIGINT REFERENCES stock(stock_id),    -- 关联库存
                                   material_id BIGINT NOT NULL,                   -- 物料(冗余设计)
                                   warehouse_id BIGINT NOT NULL,                  -- 仓库(冗余设计)
                                   batch_id BIGINT,                               -- 批次(冗余设计)
    -- 交易信息
                                   transaction_type VARCHAR(20) NOT NULL          -- 交易类型
                                       CHECK (transaction_type IN (
                                                                   '采购入库','生产入库','调拨入库','退货入库',
                                                                   '销售出库','生产领料','调拨出库','盘盈','盘亏','库存调整'
                                           )),
                                   quantity DECIMAL(16,4) NOT NULL,               -- 变动数量(正数表示入库)
                                   before_qty DECIMAL(16,4) NOT NULL,             -- 变动前数量
                                   after_qty DECIMAL(16,4) NOT NULL,              -- 变动后数量
    -- 关联单据
                                   document_type VARCHAR(30),                     -- 关联单据类型
                                   document_id BIGINT,                            -- 关联单据ID
                                   document_no VARCHAR(50),                       -- 关联单据编号
    -- 操作信息
                                   operator_id BIGINT NOT NULL,                   -- 操作人
                                   operation_time TIMESTAMPTZ DEFAULT NOW(),      -- 操作时间
                                   notes TEXT                                    -- 备注
);

CREATE INDEX idx_stock_trans_material ON stock_transaction(material_id);
CREATE INDEX idx_stock_trans_warehouse ON stock_transaction(warehouse_id);
CREATE INDEX idx_stock_trans_batch ON stock_transaction(batch_id);
CREATE INDEX idx_stock_trans_document ON stock_transaction(document_type, document_id);
CREATE INDEX idx_stock_trans_time ON stock_transaction(operation_time);

COMMENT ON TABLE stock_transaction IS '库存流水表';
COMMENT ON COLUMN stock_transaction.quantity IS '变动数量，正数表示入库，负数表示出库';