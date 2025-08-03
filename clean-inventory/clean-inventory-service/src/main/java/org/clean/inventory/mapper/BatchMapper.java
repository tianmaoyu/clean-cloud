package org.clean.inventory.mapper;

import org.clean.inventory.entity.Batch;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author eric
* @description 针对表【batch(物料批次/序列号表)】的数据库操作Mapper
* @createDate 2025-06-22 14:18:06
* @Entity org.clean.inventory.entity.Batch
*/
public interface BatchMapper extends BaseMapper<Batch> {

   Integer insertBatchSomeColumn(List<Batch>  list);
}




