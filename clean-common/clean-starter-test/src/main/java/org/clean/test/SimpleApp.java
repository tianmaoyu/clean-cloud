package org.clean.test;

import lombok.SneakyThrows;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.clean.test.config.DataSourceConfig;
import org.clean.test.config.MyBatisPlusConfig;
import org.clean.test.entity.TestCoverage;
import org.clean.test.mapper.TestCoverageMapper;

import javax.sql.DataSource;
import java.time.LocalDateTime;

public class SimpleApp {
    @SneakyThrows
    public static void main(String[] args) {

        DataSource dataSource = DataSourceConfig.createDataSource();
        SqlSessionFactory sqlSessionFactory = MyBatisPlusConfig.createSqlSessionFactory(dataSource);
        // 3. 获取SqlSession和Mapper
        try (SqlSession session = sqlSessionFactory.openSession()) {
            // 手动注册Mapper
            if (!session.getConfiguration().hasMapper(TestCoverageMapper.class)) {
                session.getConfiguration().addMapper(TestCoverageMapper.class);
            }
            
            TestCoverageMapper testCoverageMapper = session.getMapper(TestCoverageMapper.class);

            TestCoverage testCoverage = new TestCoverage();
            testCoverage.setProjectName("My Project1");
            testCoverage.setTestClass("TestClass");
            testCoverage.setTestMethod("testMethod");
            testCoverage.setLineCoverage(0.8F);
            testCoverage.setExecutionTime(LocalDateTime.now());
            int insert = testCoverageMapper.insert(testCoverage);
            System.out.println("查询结果: " + insert);
            
            session.commit(); // 手动提交事务

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}