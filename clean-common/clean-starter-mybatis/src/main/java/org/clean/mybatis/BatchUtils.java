package org.clean.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.PriorityQueue;

@Slf4j
public class BatchUtils<E>  {

    /**
     * 执行批量操作
     */
    public static <E> Boolean insertBatch(BaseMapper<E> baseMapper, Collection<E> list) {

        Assert.notEmpty(list, "list can not be empty");

        return insertBatch(baseMapper,list, list.size());

    }

    public static <E> Boolean insertBatch(BaseMapper<E> baseMapper,Collection<E> list, int batchSize) {

        Assert.notEmpty(list, "list can not be empty");
        // 1. 获取实体类类型
        Class<E> entityClass = (Class<E>) ReflectionKit.getSuperClassGenericType(
                baseMapper.getClass(), BaseMapper.class, 0);

        // 2. 获取SqlSessionFactory（使用实体类获取）
        SqlSessionFactory sqlSessionFactory = SqlHelper.sqlSessionFactory(entityClass);

        Class<?> mapperInterface = baseMapper.getClass().getInterfaces()[0];
//        boolean transaction = TransactionSynchronizationManager.isSynchronizationActive();
       // todo 事务情况,是否自动提交,和手动提交
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            BaseMapper mapper = (BaseMapper)sqlSession.getMapper(mapperInterface);

            int i = 0;
            for (E entity : list) {
                // 只是添加到批处理缓存
                mapper.insert(entity);
                if (i >= 1 && i % batchSize == 0) {
                    sqlSession.flushStatements();
                }
                i++;
            }

            sqlSession.flushStatements();

            return true;
        }
    }
}