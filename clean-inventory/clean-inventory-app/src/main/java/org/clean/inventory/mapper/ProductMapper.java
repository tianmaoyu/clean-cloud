package org.clean.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.clean.inventory.entity.Product;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    Integer insertBatchSomeColumn(List<Product> products);
}