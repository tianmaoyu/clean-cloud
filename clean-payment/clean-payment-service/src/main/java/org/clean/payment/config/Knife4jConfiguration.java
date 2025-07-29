package org.clean.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
@EnableSwagger2WebMvc
public class Knife4jConfiguration {
    @Bean
    public Docket dockerBean() {
        Docket docket=new Docket(DocumentationType.SWAGGER_2)
                .groupName("payment")
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.clean.payment.controller"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }
}