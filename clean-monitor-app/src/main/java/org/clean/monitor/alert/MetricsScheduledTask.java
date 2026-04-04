package org.clean.monitor.alert;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
@Service
public class MetricsScheduledTask {

    private final InstanceRepository instanceRepository;
    private final WebClient webClient;

    public MetricsScheduledTask(InstanceRepository instanceRepository, WebClient.Builder webClientBuilder) {
        this.instanceRepository = instanceRepository;
        this.webClient = webClientBuilder.build();
    }
//
//    /**
//     * 每30秒获取一次所有实例的 myapp.requests.total
//     */
//    @Scheduled(fixedRate = 3000) // 30秒执行一次
//    public void fetchMetricsScheduled() {
//        instanceRepository.findAll()
//            .subscribe(instance -> fetchAndLogMetrics(instance, "myapp.requests.total"));
//    }
//
//    /**
//     * 每30秒获取一次所有实例的 JVM 内存使用情况
//     */
//    @Scheduled(fixedRate = 3000)
//    public void fetchJvmMemoryMetrics() {
//        instanceRepository.findAll()
//            .subscribe(instance -> fetchAndLogMetrics(instance, "jvm.memory.used"));
//    }
//
//    /**
//     * 每30秒获取一次所有实例的 CPU 使用率
//     */
//    @Scheduled(fixedRate = 3000)
//    public void fetchCpuMetrics() {
//        instanceRepository.findAll()
//            .subscribe(instance -> fetchAndLogMetrics(instance, "system.cpu.usage"));
//    }
//
//    /**
//     * 每30秒获取一次所有实例的 HTTP 请求数量
//     */
//    @Scheduled(fixedRate = 3000)
//    public void fetchHttpRequestMetrics() {
//        instanceRepository.findAll()
//            .subscribe(instance -> fetchAndLogMetrics(instance, "http.server.requests"));
//    }

    /**
     * 每30秒获取一次所有实例的 数据库连接池状态
     */
    @Scheduled(fixedRate = 3000)
    public void fetchDatabaseConnectionMetrics() {
        Flux<Instance> all = instanceRepository.findAll();
        all.subscribe(instance -> fetchAndLogMetrics(instance, "myapp.requests.total"));
        all.subscribe(instance -> fetchAndLogMetrics(instance, "http.server.requests"));
        all.subscribe(instance -> fetchAndLogMetrics(instance, "system.cpu.usage"));

    }

//    /**
//     * 固定延迟，两次执行间隔30秒
//     */
//    @Scheduled(fixedDelay = 3000)
//    public void fetchMetricsWithDelay() {
//        instanceRepository.findAll()
//            .subscribe(instance -> fetchAndLogMetrics(instance, "myapp.requests.total"));
//    }
//
//    /**
//     * Cron 表达式：每分钟执行一次
//     */
//    @Scheduled(cron = "0 * * * * ?")
//    public void fetchMetricsCron() {
//        instanceRepository.findAll()
//            .subscribe(instance -> fetchAndLogMetrics(instance, "myapp.requests.total"));
//    }

    private void fetchAndLogMetrics(Instance instance, String metricName) {
        String metricsUrl = instance.getRegistration().getManagementUrl()
            + "/metrics/" + metricName;

        webClient.get()
            .uri(metricsUrl)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .subscribe(
                response -> {
                    logInstanceInfo(instance, response);
                },
                error -> log.error("获取 Metrics 失败 [{}]: {}",
                    instance.getId().getValue(), error.getMessage())
            );
    }

    private void logInstanceInfo(Instance instance, Map<String, Object> metricsData) {
        String instanceId = instance.getId().getValue();
        String appName = instance.getRegistration().getName();
        String managementUrl = instance.getRegistration().getManagementUrl();
        String status = instance.getStatusInfo().getStatus();

        log.info("========== Metrics 信息 ==========");
        log.info("实例 ID: {}", instanceId);
        log.info("应用名称: {}", appName);
        log.info("管理地址: {}", managementUrl);
        log.info("状态: {}", status);
        log.info("指标数据: {}", metricsData);
        log.info("================================");
    }
}