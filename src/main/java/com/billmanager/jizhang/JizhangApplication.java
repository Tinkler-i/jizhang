package com.billmanager.jizhang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class JizhangApplication {

    public static void main(String[] args) {
        SpringApplication.run(JizhangApplication.class, args);
    }

}