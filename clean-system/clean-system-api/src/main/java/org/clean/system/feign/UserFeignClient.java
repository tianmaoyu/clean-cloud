package org.clean.system.feign;


import org.clean.system.entity.User;
import org.clean.system.param.UserAddParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@FeignClient(name = "clean-system-service")
public interface UserFeignClient {
    @GetMapping("/user/getById")
    User getById(@RequestParam("id") Long id);

    @PostMapping("/user/add")
    User add(@RequestBody @Valid UserAddParam param);
}
