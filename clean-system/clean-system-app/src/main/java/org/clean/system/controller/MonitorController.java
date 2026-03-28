package org.clean.system.controller;

import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.management.HeapDumpWebEndpoint;
import org.springframework.boot.actuate.management.ThreadDumpEndpoint;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ThreadInfo;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "系统监控")
@RestController
@RequestMapping("/monitor")
public class MonitorController {

    @Autowired(required = false)
    private HealthEndpoint healthEndpoint;

    @Autowired(required = false)
    private InfoEndpoint infoEndpoint;

    @Autowired(required = false)
    private HeapDumpWebEndpoint heapDumpWebEndpoint;

    @Autowired(required = false)
    private ThreadDumpEndpoint threadDumpEndpoint;

    @Autowired
    private NacosServiceRegistry serviceRegistry;

    @Autowired
    private NacosRegistration registration;


    @GetMapping("/health")
    public Object health() {
        return healthEndpoint != null ? healthEndpoint.health() : "OK";
    }

    @GetMapping("/info")
    public Object info() {
        return infoEndpoint != null ? infoEndpoint.info() : null;
    }

    @GetMapping(value = "/heapdump", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> heapdump() {
        if (heapDumpWebEndpoint == null) {
            return ResponseEntity.status(503).body(null); // 服务不可用
        }
        WebEndpointResponse<Resource> resourceWebEndpointResponse = heapDumpWebEndpoint.heapDump(true);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=heapdump.hprof")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resourceWebEndpointResponse.getBody());
    }


    @GetMapping(value = "/threaddump", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> threaddump() {
        if (threadDumpEndpoint == null) {
            return ResponseEntity.status(503).body("Thread dump endpoint not available");
        }
        ThreadDumpEndpoint.ThreadDumpDescriptor descriptor = threadDumpEndpoint.threadDump();
        StringBuilder sb = new StringBuilder();
        for (ThreadInfo threadInfo : descriptor.getThreads()) {
            sb.append(threadInfo.toString()).append("\n\n");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(sb.toString());
    }

    @GetMapping("/serviceregistry/status")
    public ResponseEntity<?> getServiceStatus() {
        if (serviceRegistry == null || registration == null) {
            return ResponseEntity.status(503)
                    .body("ServiceRegistry or Registration not available");
        }

        Object status = serviceRegistry.getStatus(registration);
        return ResponseEntity.ok(status);
    }

    @ApiImplicitParam(name = "status", value = "目标状态：UP（上线）或 DOWN（下线）",
            required = true, example = "DOWN")
    @PostMapping("/serviceregistry/status")
    public ResponseEntity<?> setServiceStatus(@RequestParam("status") String status) {
        if (serviceRegistry == null || registration == null) {
            return ResponseEntity.status(503)
                    .body("ServiceRegistry or Registration not available");
        }
        if (!"UP".equalsIgnoreCase(status) && !"DOWN".equalsIgnoreCase(status)) {
            return ResponseEntity.badRequest()
                    .body("Invalid status. Allowed values: UP, DOWN");
        }

        // 执行状态修改（UP 表示上线，DOWN 表示下线）
        serviceRegistry.setStatus(registration, status.toUpperCase());
        Object result = serviceRegistry.getStatus(registration);
        return ResponseEntity.ok(result);

    }

}
