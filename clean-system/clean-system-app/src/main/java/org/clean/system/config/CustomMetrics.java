package org.clean.system.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CustomMetrics {

    private final Counter requestCounter;

    public CustomMetrics(MeterRegistry registry) {
            requestCounter = Counter.builder("myapp.requests.total")
                .description("Total number of requests")
                .register(registry);
    }

    public void recordRequest() {
        requestCounter.increment();
    }
}