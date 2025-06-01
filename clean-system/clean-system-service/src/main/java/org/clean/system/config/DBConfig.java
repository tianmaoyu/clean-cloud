package org.clean.system.config;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DBConfig {

    @Autowired
    private  DataSourceProperties dbProperties;

    @Bean
    public DataSource dataSource() {
        DruidDataSource ds = new DruidDataSource();
        ds.setUrl(dbProperties.getUrl());
        ds.setUsername(dbProperties.getUsername());
        ds.setPassword(dbProperties.getPassword());

        // 启用日志过滤器
        List<Filter> filters = new ArrayList<>();
        filters.add(slf4jLogFilter());
        ds.setProxyFilters(filters);

        return ds;
    }

    @Bean
    public Slf4jLogFilter slf4jLogFilter() {
        Slf4jLogFilter filter = new Slf4jLogFilter();
        filter.setStatementExecutableSqlLogEnable(true);  // 关键：显示完整SQL
        filter.setStatementLogEnabled(true);
        filter.setStatementLogErrorEnabled(true);
        return filter;
    }
}
