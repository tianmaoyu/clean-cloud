package org.clean.inventory.service;

import org.clean.inventory.entity.Batch;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;
import java.util.List;

/**
* @author eric
* @description 针对表【batch(物料批次/序列号表)】的数据库操作Service
* @createDate 2025-06-22 14:18:06
*/
public interface BatchService extends IService<Batch> {

    boolean saveBatch(Collection<Batch> entityList, int batchSize);

    Integer insertBatchSomeColumn(List<Batch> list, int batchSize);
}
