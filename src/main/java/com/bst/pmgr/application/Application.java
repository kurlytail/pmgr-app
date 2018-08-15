package com.bst.pmgr.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.context.request.RequestContextListener;

@SpringBootApplication
@ComponentScan("com.bst")
@EntityScan({"com.bst.pmgr.entities", "com.bst.persistence.model"})
//@EnableAutoConfiguration(exclude = RepositoryRestMvcAutoConfiguration.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }
}
