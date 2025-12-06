package org.clean.test.config;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

public class MyBatisPlusConfig {
    public static SqlSessionFactory createSqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        // 设置数据源
        sessionFactory.setDataSource(dataSource);
        // 设置实体类所在包（别名扫描）
        sessionFactory.setTypeAliasesPackage("org.clean.test.entity");
        // 手动注册Mapper接口
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:mapper/**/*.xml"));

        return sessionFactory.getObject();
    }
}