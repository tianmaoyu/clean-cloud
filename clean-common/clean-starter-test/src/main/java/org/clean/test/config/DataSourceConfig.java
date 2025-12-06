package org.clean.test.config;

import com.alibaba.druid.pool.DruidDataSource;
import javax.sql.DataSource;

public class DataSourceConfig {
    public static DataSource createDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/coverage_db");
        dataSource.setUsername("postgres");
        dataSource.setPassword("123456");
        dataSource.setDriverClassName("org.postgresql.Driver");
        return dataSource;
    }
}