package org.clean.system.service.impl;

import com.alibaba.excel.EasyExcel;
import org.clean.system.dto.UserDto;
import org.clean.system.service.DemoService;
import org.springframework.cloud.sleuth.SpanName;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class DemoServiceImpl implements DemoService {

    @Override
    public void doSomething() {
        System.out.println("Executing doSomething...");
    }

    @Override
    public void doSomethingElse() {
        System.out.println("Executing doSomethingElse...");
    }

    @SpanName("excel-report")
    public byte[] excelReport(Integer id) throws IOException {

       List<UserDto> userDataList = null;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            EasyExcel.write(outputStream, UserDto.class).sheet("用户列表").doWrite(userDataList);

            return outputStream.toByteArray();
        }
    }
}
