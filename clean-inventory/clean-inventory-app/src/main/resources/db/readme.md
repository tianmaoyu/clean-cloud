表关系说明
一对多关系:

1个物料 → N个属性 (material ↔ material_attribute)

1个物料 → N个批次 (material ↔ batch)

1个物料 → N个库存记录 (material ↔ stock)

1个仓库 → N个库存记录 (warehouse ↔ stock)

1个批次 → N个库存记录 (batch ↔ stock)

1个库存记录 → N个流水记录 (stock ↔ stock_transaction)

设计特点:

库存主表(stock)是核心事实表，记录当前库存状态

库存流水表(stock_transaction)记录所有历史变动

冗余存储关键字段(material_id, warehouse_id等)以提高查询性能

支持批次/序列号管理(通过is_batch/is_serial控制)

支持多仓库多库位管理