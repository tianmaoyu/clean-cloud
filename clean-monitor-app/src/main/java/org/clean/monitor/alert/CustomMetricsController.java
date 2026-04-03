package org.clean.monitor.alert;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
public class CustomMetricsController {

    private final InstanceRepository instanceRepository;
    private final WebClient webClient;

    public CustomMetricsController(InstanceRepository instanceRepository, WebClient.Builder webClientBuilder) {
        this.instanceRepository = instanceRepository;
        this.webClient = webClientBuilder.build();
    }

    /**
     * 获取所有应用的 myapp.requests.total 指标
     */
    @GetMapping("/requests-total")
    public Mono<ResponseEntity<List<Map<String, Object>>>> getRequestsTotal() {
        Mono<ResponseEntity<List<Map<String, Object>>>> responseEntityMono = instanceRepository.findAll()
                .flatMap(instance -> fetchMetrics(instance, "myapp.requests.total"))
                .collectList()
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.status(500).build());

        return responseEntityMono;
    }

    /**
     * 获取指定实例的 Metrics
     */
    @GetMapping("/instance/{instanceId}/requests-total")
    public Mono<ResponseEntity<Map<String, Object>>> getInstanceRequestsTotal(@PathVariable String instanceId) {
        return instanceRepository.find(InstanceId.of(instanceId))
            .flatMap(instance -> fetchMetrics(instance, "myapp.requests.total"))
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.status(404).build());
    }

    /**
     * 获取指定实例的任意 Metrics
     */
    @GetMapping("/instance/{instanceId}/metric")
    public Mono<ResponseEntity<Map<String, Object>>> getInstanceMetric(
            @PathVariable String instanceId,
            @RequestParam String metricName) {
        return instanceRepository.find(InstanceId.of(instanceId))
            .flatMap(instance -> fetchMetrics(instance, metricName))
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.status(404).build());
    }

    private Mono<Map<String, Object>> fetchMetrics(Instance instance, String metricName) {
        String metricsUrl = instance.getRegistration().getManagementUrl() 
            + "/metrics/" + metricName;

        return webClient.get()
            .uri(metricsUrl)
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> Map.of(
                "appName", instance.getRegistration().getName(),
                "instanceId", instance.getId().getValue(),
                "status", instance.getStatusInfo().getStatus(),
                "metric", response
            ))
            .onErrorResume(e -> Mono.just(Map.of(
                "appName", instance.getRegistration().getName(),
                "instanceId", instance.getId().getValue(),
                "error", e.getMessage()
            )));
    }
}