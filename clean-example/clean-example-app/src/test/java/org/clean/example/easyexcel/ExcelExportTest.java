package org.clean.example.easyexcel;

import com.alibaba.excel.EasyExcel;
import org.clean.example.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

//@SpringBootTest
public class ExcelExportTest {

    @Test
    public void testUserExportWithEnum() throws Exception {
        // 1. 准备测试数据
        List<UserImportDTO> userList = new ArrayList<>();
        
        UserImportDTO user1 = new UserImportDTO();
        user1.setId(1L);
        user1.setUsername("张三");
        user1.setStatus(UserStatus.ENABLED);
        user1.setEmail("zhangsan@example.com");
        user1.setCreateTime("2023-01-15 09:30:00");
        userList.add(user1);
        
        UserImportDTO user2 = new UserImportDTO();
        user2.setId(2L);
        user2.setUsername("李四");
        user2.setStatus(UserStatus.DISABLED);
        user2.setEmail("lisi@example.com");
        user2.setCreateTime("2023-02-20 14:25:00");
        userList.add(user2);
        
        UserImportDTO user3 = new UserImportDTO();
        user3.setId(3L);
        user3.setUsername("王五");
        user3.setStatus(UserStatus.LOCKED);
        user3.setEmail("wangwu@example.com");
        user3.setCreateTime("2023-03-10 11:15:00");
        userList.add(user3);
        
        UserImportDTO user4 = new UserImportDTO();
        user4.setId(4L);
        user4.setUsername("赵六");
        user4.setStatus(UserStatus.PENDING_ACTIVATION);
        user4.setEmail("zhaoliu@example.com");
        user4.setCreateTime("2023-04-05 16:40:00");
        userList.add(user4);
        
        // 2. 配置导出路径
        String fileName = "用户数据_" + System.currentTimeMillis() + ".xlsx";
        Path outputPath = Paths.get(System.getProperty("user.dir"), "exports", fileName);
        
        // 确保导出目录存在
        Files.createDirectories(outputPath.getParent());
        
        System.out.println("导出文件路径: " + outputPath.toAbsolutePath());
        
        // 3. 执行导出
        try {
            EasyExcel.write(outputPath.toString(), UserImportDTO.class)
                    .sheet("用户列表")
                    .doWrite(userList);
            
            System.out.println("导出成功！共导出 " + userList.size() + " 条记录");
            System.out.println("文件大小: " + Files.size(outputPath) + " 字节");
            
        } catch (Exception e) {
            System.err.println("导出失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        // 4. 验证文件是否生成
        assert Files.exists(outputPath) : "导出文件不存在";
        assert Files.size(outputPath) > 0 : "导出文件为空";
        
        System.out.println("✅ 测试通过！请打开文件验证枚举值是否正确导出");
        System.out.println("文件路径: " + outputPath.toAbsolutePath());
    }
    
    /**
     * 可选：批量测试所有枚举值
     */
    @Test
    public void testAllEnumValuesExport() throws Exception {
        List<UserImportDTO> allStatusUsers = new ArrayList<>();
        
        // 为每种状态创建一个测试用户
        Arrays.stream(UserStatus.values()).forEach(status -> {
            UserImportDTO user = new UserImportDTO();
            user.setId((long) (allStatusUsers.size() + 1));
            user.setUsername("测试用户_" + status.getCode());
            user.setStatus(status);
            user.setEmail(status.getCode() + "@example.com");
            user.setCreateTime(new Date().toString());
            allStatusUsers.add(user);
        });
        
        String fileName = "所有状态测试_" + System.currentTimeMillis() + ".xlsx";
        Path outputPath = Paths.get(System.getProperty("user.dir"), "exports", fileName);
        Files.createDirectories(outputPath.getParent());
        
        EasyExcel.write(outputPath.toString(), UserImportDTO.class)
                .sheet("所有状态")
                .doWrite(allStatusUsers);
        
        System.out.println("所有状态测试导出完成，共 " + allStatusUsers.size() + " 种状态");
        System.out.println("文件路径: " + outputPath.toAbsolutePath());
    }
    @Test
    public void testIntegerEnumExport() throws Exception {
        // 准备测试数据
        List<UserExportDTO> users = new ArrayList<>();

        UserExportDTO user1 = new UserExportDTO();
        user1.setId(101L);
        user1.setUsername("管理员");
        user1.setStatus(UserStatus.ENABLED);
        user1.setRole(UserRole.ADMIN);
        user1.setEmail("admin@example.com");
        users.add(user1);

        UserExportDTO user2 = new UserExportDTO();
        user2.setId(102L);
        user2.setUsername("普通用户");
        user2.setStatus(UserStatus.DISABLED);
        user2.setRole(UserRole.STAFF);
        user2.setEmail("user@example.com");
        users.add(user2);

        // 配置导出路径
        String fileName = "用户角色测试_" + System.currentTimeMillis() + ".xlsx";
        Path outputPath = Paths.get(System.getProperty("user.dir"), "exports", fileName);
        Files.createDirectories(outputPath.getParent());

        // 执行导出
        EasyExcel.write(outputPath.toString(), UserExportDTO.class)
                .sheet("用户角色")
                .doWrite(users);

        System.out.println("✅ 整数枚举导出成功！文件路径: " + outputPath.toAbsolutePath());
        System.out.println("💡 提示: 请检查'角色'列是否显示为数字(1,2,3...)");
    }


}