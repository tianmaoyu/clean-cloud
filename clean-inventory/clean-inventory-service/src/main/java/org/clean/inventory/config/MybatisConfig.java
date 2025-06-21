package org.clean.inventory.config;

import org.clean.mybatis.FullSqlInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisConfig {
//    @Bean
    public FullSqlInterceptor getFullSqlInterceptor(){
        return new FullSqlInterceptor();
    }
}
