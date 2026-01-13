package org.clean.example.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.clean.Author;
import org.clean.system.entity.User;
import org.clean.system.feign.RedisFeignClient;
import org.clean.system.feign.UserFeignClient;
import org.clean.system.param.UserAddParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Author(value = "eric", date = "2022/12/27")
@Slf4j
@RestController
@RequestMapping("/hello")
public class HellController {

    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private RedisFeignClient redisFeignClient;

    @Author(value = "eric2",date = "2022/12/27")
    @GetMapping("/getUser")
     public User getUser(@RequestParam("id") Long id) {

        User user = userFeignClient.getById(id);
        return user;
    }

    @Author("eric1")
    @GetMapping("/getUser_Redis")
    public User getUser_Redis(@RequestParam("id") Long id) {
        User user = redisFeignClient.getById(id);
        return user;
    }

    @PostMapping("/user/add")
    public User add(@RequestBody @Valid UserAddParam param){
        return userFeignClient.add(param);
    }

//    @Autowired
//    private UserConvert userConvert;

    @SentinelResource(value = "getUser", blockHandler = "blockHandler")
    public String getUser() {
        return "User Info";
    }

    // 限流/降级处理方法
    public String blockHandler(BlockException ex) {
        return "请求被限流";
    }
//
//    @Autowired
//    private DemoService demoService;
//
    @GetMapping("/hi")
    public String hi() {
        log.info(" hi:{}", "getUser");
//
//        User user = new User();
//        user.setAge(18);
//        user.setName("eric");
//
//        UserData dto = userConvert.toDto(user);
//        log.info("dto {}", dto);
//
//        UserVo vo = userConvert.toVo(user);
//        log.info("vo {}", vo);

        new Thread(() -> log.info("这是异步日志")).start();
        return "Hello World";
    }
//
//    @Autowired
//    private MessageSource messageSource;
//
//    @GetMapping("/hello")
//    public String hello(@RequestParam String lang) {
//        Locale locale = new Locale(lang);
//        return messageSource.getMessage("name", null, locale);
//    }
//
//    // 大文件，避免缓存
//    @GetMapping("/export")
//    public void export(HttpServletResponse response, @RequestParam String lang) throws IOException {
//        // 设置响应头信息
//        response.setContentType("application/vnd.ms-excel");
//        response.setCharacterEncoding("utf-8");
//        String fileName = URLEncoder.encode("用户信息", "UTF-8");
//        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
//
//        Locale locale = new Locale(lang);
//        List<List<String>> headers = ExcelHeaderUtil.getHeaders(UserData.class, locale);
//
//        // 创建一个示例数据列表
//        List<UserData> userDataList1 = new ArrayList<>();
//        userDataList1.add(new UserData("张三", 20));
//        userDataList1.add(new UserData("李四", 25));
//
//        List<UserData> userDataList2 = new ArrayList<>();
//        userDataList2.add(new UserData("王五", 30));
//        userDataList2.add(new UserData("赵六", 35));
//
////        String filePath1 = "./test_1.xlsx";
////        // 使用 EasyExcel 写入本地文件
////        EasyExcel.write(filePath1, UserData.class)
////                .sheet(1,"用户信息1") // 第一个 sheet
////                .doWrite(userDataList1);
////
////        EasyExcel.write(filePath1, UserData.class)
////                .sheet(2,"用户信息2") // 第二个 sheet
////                .doWrite(userDataList2);
//
//
//        //多个工作sheet 这个有效
////        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
////        String filePath2 = "./test_2.xlsx";
//        // 创建 ExcelWriter 对象
//        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), UserData.class).build();
//
//
//        // 写入第一个 sheet
//        WriteSheet sheet1 = EasyExcel
//                .writerSheet(0, "用户信息1")
//                .head(headers)
//                .build();
//        excelWriter.write(userDataList1, sheet1);
//
//        // 写入第二个 sheet
//        WriteSheet sheet2 = EasyExcel
//                .writerSheet(1, "用户信息2")
//                .head(headers)
//                .build();
//        excelWriter.write(userDataList2, sheet2);
//        excelWriter.finish();
//
////        response.getOutputStream().write(outputStream.toByteArray());
//
//
////        // 输出流
////        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
////        ExcelWriterBuilder writerBuilder = EasyExcel.write(outputStream, UserData.class);
////
////        // 写入第一个 sheet
////        ExcelWriterSheetBuilder sheetBuilder = writerBuilder.sheet("用户信息1");
////        sheetBuilder.doWrite(userDataList1);
////
////        // 写入第二个 sheet
////        ExcelWriterSheetBuilder sheetBuilder2 = writerBuilder.sheet("用户信息2");
////        sheetBuilder2.doWrite(userDataList2);
////
////        // 完成写入操作
////        writerBuilder.build().finish();
////
////        // 设置内容长度
////        byte[] content = outputStream.toByteArray();
////        response.setContentLength(content.length);
////
////        // 将内容写入响应输出流
////        response.getOutputStream().write(content);
////        response.getOutputStream().flush();
//
//    }
//
//    @PostMapping("/importDataSync")
//    public Integer importDataSync(MultipartFile file) throws IOException {
//
//        // 读取 Excel 文件中的数据
//        List<UserData> userDataList = EasyExcel
//                .read(file.getInputStream())
//                .head(UserData.class)
//                .sheet()
//                .doReadSync();
//
//        for (UserData userData : userDataList) {
//            System.out.println(userData);
//        }
//
//        return userDataList.size();
//    }
//
//    // 导入功能
//    @PostMapping("/importData")
//    public Integer importData(MultipartFile file) throws IOException {
//
//        UserDataListener userDataListener = new UserDataListener();
//
//        EasyExcel.read(file.getInputStream(), UserData.class, userDataListener)
//                .sheet()
//                .doRead();
//
//        List<UserData> userDataList = userDataListener.getList();
//
//        // 处理导入的数据（这里仅打印）
//        for (UserData userData : userDataList) {
//            System.out.println(userData);
//        }
//
//        return userDataList.size();
//    }
//
//    @GetMapping("/downloadEasyExcel")
//    public ResponseEntity<byte[]> downloadEasyExcel() {
//
//        try {
//
//            byte[] excelBytes = demoService.excelReport(1);
//
//            ResponseEntity<byte[]> responseEntity = ExcelResponseUtil.getResponseEntity(excelBytes, "中昂微 动的.xlsx");
//
//            return responseEntity;
//
//        } catch (Exception e) {
//
//            return ResponseEntity.internalServerError().build();
//        }
//
//    }

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @GetMapping("/send")
    public String sendMessage(@RequestParam String topic, @RequestParam String message) {
        rocketMQTemplate.convertAndSend(topic, message);
        return "消息已发送到主题: " + topic;
    }

}


