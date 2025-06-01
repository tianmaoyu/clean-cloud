package org.clean.system.feign;


import org.clean.system.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "clean-system-service",contextId =  "RedisFeignClient")
public interface RedisFeignClient {
    @GetMapping("/Redis/getUser")
    User getById(@RequestParam("id") Long id);
}
