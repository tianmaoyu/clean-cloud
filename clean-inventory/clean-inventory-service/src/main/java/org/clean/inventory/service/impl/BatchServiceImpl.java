package org.clean.inventory.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.clean.inventory.entity.Batch;
import org.clean.inventory.service.BatchService;
import org.clean.inventory.mapper.BatchMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author eric
* @description 针对表【batch(物料批次/序列号表)】的数据库操作Service实现
* @createDate 2025-06-22 14:18:06
*/
@Service
public class BatchServiceImpl extends ServiceImpl<BatchMapper, Batch> implements BatchService{

    @Autowired
    private BatchMapper mapper;
    @Override
    public Integer insertBatchSomeColumn(List<Batch> list, int batchSize) {
        return mapper.insertBatchSomeColumn(list);
    }
}




