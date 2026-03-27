package org.clean.system;

import com.alibaba.cloud.nacos.registry.NacosAutoServiceRegistration;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

@Slf4j
@EnableAspectJAutoProxy
@ComponentScan ("org.clean.*")
@SpringBootApplication
@MapperScan("org.clean.system.mapper")
public class SystemServiceApplication {

    @SneakyThrows
    public static void main(String[] args) {
        ConfigurableApplicationContext application = SpringApplication.run(SystemServiceApplication.class, args);
        //ConfigurableApplicationContext application=SpringApplication.run(Knife4jSpringBootDemoApplication.class, args);
        Environment env = application.getEnvironment();
        log.info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +

                        "Doc: \thttp://{}:{}/doc.html\n"+
                        "----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"));
// // 启动后在注册
//        new Thread(() -> {
//            try {
//                Thread.sleep(15000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            NacosServiceRegistry nacosServiceRegistry = application.getBean(NacosServiceRegistry.class);
//            NacosRegistration registration=application.getBean(NacosRegistration.class);
//            int port = env.getProperty("server.port", Integer.class);
//            registration.setPort(port);
//            nacosServiceRegistry.register(registration);
//        }).start();

    }

}