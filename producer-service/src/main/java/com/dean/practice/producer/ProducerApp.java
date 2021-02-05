package com.dean.practice.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 *
 * @author Dean
 * @date 2020-11-24
 */
@EnableSwagger2
@EnableFeignClients
@EnableDiscoveryClient
@SpringCloudApplication
public class ProducerApp {

    public static void main(String[] args) {
        SpringApplication.run(ProducerApp.class, args);
    }

}
