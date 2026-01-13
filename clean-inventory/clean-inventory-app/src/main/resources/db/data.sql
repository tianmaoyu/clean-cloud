INSERT INTO material (material_code, material_name, material_type, base_unit, is_batch, is_serial, description) VALUES
                                                                                                                    ('MAT001', '不锈钢螺丝', '原材料', '个', TRUE, FALSE, 'M6x20不锈钢螺丝'),
                                                                                                                    ('MAT002', 'LED灯泡', '半成品', '只', TRUE, FALSE, '5W暖白光LED灯泡'),
                                                                                                                    ('MAT003', '智能门锁', '成品', '套', FALSE, TRUE, '指纹识别智能门锁'),
                                                                                                                    ('MAT004', '包装纸箱', '包装材料', '个', FALSE, FALSE, '50x50x50cm瓦楞纸箱'),
                                                                                                                    ('MAT005', '锂电池', '原材料', '块', TRUE, TRUE, '3.7V 18650锂电池');



INSERT INTO material_attribute (material_id, spec_model, weight, volume, color, shelf_life, extended_attrs) VALUES
                                                                                                                (1, 'M6x20', 0.005, 0.0001, '银色', NULL, '{"thread_type":"细牙","standard":"GB/T 818"}'),
                                                                                                                (2, 'LED-5W-WW', 0.08, 0.0005, '白色', 3650, '{"lumen":"450lm","color_temp":"3000K","life_hour":"25000"}'),
                                                                                                                (3, 'SmartLock-X1', 1.2, 0.008, '黑色', 1825, '{"lock_type":"指纹+密码","battery_type":"AAx4","ip_rating":"IP54"}'),
                                                                                                                (4, '50x50x50', 0.5, 0.125, '棕色', NULL, '{"material":"瓦楞纸","max_load":"20kg"}'),
                                                                                                                (5, '18650-3.7V', 0.045, 0.0002, '蓝色', 1095, '{"capacity":"2600mAh","charge_cycles":"500"}');

INSERT INTO warehouse (warehouse_code, warehouse_name, warehouse_type, address, manager) VALUES
                                                                                             ('WH001', '原材料主仓库', '原材料仓', '园区A栋1楼', '张经理'),
                                                                                             ('WH002', '成品仓库', '成品仓', '园区B栋2楼', '李主管'),
                                                                                             ('WH003', '电子元件仓', '原材料仓', '园区C栋1楼', '王主任'),
                                                                                             ('WH004', '退货处理仓', '退货仓', '园区D栋1楼', '赵专员');
INSERT INTO location (warehouse_id, location_code, location_name, location_type, coordinates) VALUES
-- WH001的库位
(1, 'A-01', 'A区货架01', '货架', 'A-01'),
(1, 'A-02', 'A区货架02', '货架', 'A-02'),
(1, 'B-01', 'B区平面区', '平面区', 'B-01'),
-- WH002的库位
(2, 'F-01', '成品货架01', '货架', 'F-01'),
(2, 'F-02', '成品货架02', '货架', 'F-02'),
-- WH003的库位
(3, 'E-01', '电子元件区', '防静电区', 'E-01'),
(3, 'E-02', '电池专用区', '防爆区', 'E-02'),
-- WH004的库位
(4, 'R-01', '退货待检区', '平面区', 'R-01'),
(4, 'R-02', '退货处理区', '平面区', 'R-02');
INSERT INTO batch (material_id, batch_code, production_date, expiration_date, quality_status) VALUES
-- 不锈钢螺丝批次
(1, 'B20230101', '2023-01-01', NULL, '合格'),
(1, 'B20230201', '2023-02-01', NULL, '合格'),
-- LED灯泡批次
(2, 'L20230501', '2023-05-01', '2033-05-01', '合格'),
(2, 'L20230601', '2023-06-01', '2033-06-01', '合格'),
-- 锂电池批次
(5, 'BT20230401', '2023-04-01', '2026-04-01', '合格'),
(5, 'BT20230701', '2023-07-01', '2026-07-01', '待检');

-- 智能门锁序列号(每个产品唯一)
INSERT INTO batch (material_id, batch_code, production_date, quality_status) VALUES
                                                                                 (3, 'SL2023080001', '2023-08-01', '合格'),
                                                                                 (3, 'SL2023080002', '2023-08-01', '合格'),


-- 清空原有测试数据（如果存在）
TRUNCATE stock_transaction RESTART IDENTITY CASCADE;

-- 插入正确的库存流水数据
INSERT INTO stock_transaction (
    stock_id, material_id, warehouse_id, location_id, batch_id,
    transaction_type, quantity, before_qty, after_qty,
    document_type, document_no, operator_id, notes
) VALUES
-- 1. 不锈钢螺丝(MAT001)采购入库
(NULL, 1, 1, 1, 1, '采购入库', 5000, 0, 5000,
 'purchase', 'PO202301001', 101, '初始采购入库'),
(NULL, 1, 1, 1, 2, '采购入库', 3000, 0, 3000,
 'purchase', 'PO202302001', 101, '第二批采购'),

-- 2. LED灯泡(MAT002)生产入库
(NULL, 2, 2, 4, 3, '生产入库', 200, 0, 200,
 'production', 'PR202305001', 102, '5月生产批次'),
(NULL, 2, 2, 5, 4, '生产入库', 150, 0, 150,
 'production', 'PR202306001', 102, '6月生产批次'),

-- 3. 智能门锁(MAT003)生产入库
(5, 3, 2, 4, 5, '生产入库', 1, 0, 1,
 'production', 'PR202308001', 102, '序列号SL2023080001'),
(6, 3, 2, 4, 6, '生产入库', 1, 0, 1,
 'production', 'PR202308002', 102, '序列号SL2023080002'),
(7, 3, 2, 5, 7, '生产入库', 1, 0, 1,
 'production', 'PR202308003', 102, '序列号SL2023080003'),

-- 4. 包装纸箱(MAT004)采购入库
(8, 4, 1, 3, NULL, '采购入库', 100, 0, 100,
 'purchase', 'PO202307001', 101, '包装材料采购'),

-- 5. 锂电池(MAT005)采购入库
(9, 5, 3, 7, 8, '采购入库', 50, 0, 50,
 'purchase', 'PO202304001', 101, '4月电池采购'),
(10, 5, 3, 7, 9, '采购入库', 30, 0, 30,
 'purchase', 'PO202307002', 101, '7月电池采购'),

-- 6. 智能门锁出库
(5, 3, 2, 4, 5, '销售出库', -1, 1, 0,
 'sales', 'SO202308001', 103, '客户A订单'),

-- 7. 库存调整（锁定库存）
(7, 3, 2, 5, 7, '库存调整', 0, 1, 1,
 'adjustment', 'ADJ202308001', 104, '样品展示锁定'),
(10, 5, 3, 7, 9, '库存调整', 0, 30, 30,
 'adjustment', 'ADJ202308002', 104, '待检批次锁定'),

-- 8. 调拨操作示例
(2, 1, 1, 1, 2, '调拨出库', -1000, 3000, 2000,
 'transfer', 'TO202308001', 105, '调往WH002'),
(NULL, 1, 2, 4, 2, '调拨入库', 1000, 0, 1000,
 'transfer', 'TO202308001', 105, '从WH001调入'),

-- 9. 盘点调整
(8, 4, 1, 3, NULL, '盘盈', 5, 100, 105,
 'inventory', 'INV202308001', 106, '盘点差异调整');