//package org.clean.monitor;
//
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.EnableAspectJAutoProxy;
//import org.springframework.core.env.Environment;
//
//import java.net.InetAddress;
//
//@Slf4j
//@EnableAspectJAutoProxy
//@SpringBootApplication
//public class SystemServiceApplication {
//
//    @SneakyThrows
//    public static void main(String[] args) {
//        ConfigurableApplicationContext application = SpringApplication.run(SystemServiceApplication.class, args);
//        //ConfigurableApplicationContext application=SpringApplication.run(Knife4jSpringBootDemoApplication.class, args);
//        Environment env = application.getEnvironment();
//        log.info("\n----------------------------------------------------------\n\t" +
//                        "Application '{}' is running! Access URLs:\n\t" +
//
//                        "Doc: \thttp://{}:{}/doc.html\n"+
//                        "----------------------------------------------------------",
//                env.getProperty("spring.application.name"),
//                InetAddress.getLocalHost().getHostAddress(),
//                env.getProperty("server.port"));
//
//
//    }
//
//}