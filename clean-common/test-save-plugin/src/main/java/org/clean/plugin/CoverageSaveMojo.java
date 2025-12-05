package org.clean.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import java.sql.*;

@Mojo(name = "save-coverage", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class CoverageSaveMojo extends AbstractMojo {
    
    // 1. 测试方法参数
    @Parameter(property = "testClass")
    private String testClass;

    @Parameter(property = "testMethod")
    private String testMethod;
    
    @Parameter(property = "projectName", defaultValue = "${project.artifactId}")
    private String projectName;
    
    // 2. PostgreSQL 数据库连接参数
    @Parameter(property = "dbUrl", required = true)
    private String dbUrl;
    
    @Parameter(property = "dbUsername", required = true)
    private String dbUsername;
    
    @Parameter(property = "dbPassword", required = true)
    private String dbPassword;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("开始处理覆盖率数据...");
        getLog().info("测试方法: " + testClass + "#" + testMethod);
        getLog().info("连接数据库: " + dbUrl);
        
        Connection conn = null;
        try {
            // 1. 注册 PostgreSQL 驱动 (JDBC 4.0 后通常可省略，但显式声明更可靠)
            Class.forName("org.postgresql.Driver");
            
            // 2. 建立数据库连接
            conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            
            // 3. 这里插入你的业务逻辑：解析 jacoco.exec，计算覆盖率，存入数据库
            // 示例：创建一个记录表（如果不存在）
            try (Statement stmt = conn.createStatement()) {
                String createTableSQL = "CREATE TABLE IF NOT EXISTS test_coverage (" +
                        "id SERIAL PRIMARY KEY, " +
                        "project_name VARCHAR(100), " +
                        "test_class VARCHAR(200), " +
                        "test_method VARCHAR(100), " +
                        "line_coverage DECIMAL(5,2), " +
                        "execution_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")";
                stmt.execute(createTableSQL);
                getLog().info("确保数据表已就绪。");
            }
            
            // 4. 示例：插入一条记录 (请替换为真实的覆盖率计算逻辑)
            String insertSQL = "INSERT INTO test_coverage (project_name, test_class, test_method, line_coverage) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, projectName);
                pstmt.setString(2, testClass);
                pstmt.setString(3, testMethod);
                pstmt.setDouble(4, 85.5); // 这里应是计算出的真实覆盖率
                pstmt.executeUpdate();
                getLog().info("覆盖率数据已插入数据库。");
            }
            
            getLog().info("覆盖率数据处理完成！");
            
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("找不到 PostgreSQL JDBC 驱动，请在插件pom.xml中添加依赖", e);
        } catch (SQLException e) {
            throw new MojoExecutionException("数据库连接或操作失败", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    getLog().error("关闭数据库连接时出错", e);
                }
            }
        }
    }
}