package org.clean.system.config;

import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.clean.system.entity.Account;
import org.clean.system.entity.User;
import org.clean.system.mapper.AccountMapper;
import org.clean.system.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/*
 * @Description: 延迟注册 解决 k8s 启动时 nacos注册太快, 服务未完全准备 ,访问超时
 */
@Slf4j
@Component
public class NacosDelayedRegistration implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private NacosServiceRegistry nacosServiceRegistry;

    @Autowired
    private NacosRegistration registration;

    @Autowired
    private UserMapper userMapper;

    @Value("${server.port}")
    private int port;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 可在此处添加自定义的就绪检查逻辑，如等待缓存加载、DB连接测试等
        User account = userMapper.selectById(1);
        log.info("首次启动访问数据库成功-开始注册nacos:{}", account);
        registration.setPort(this.port);
        nacosServiceRegistry.register(registration);
    }
}