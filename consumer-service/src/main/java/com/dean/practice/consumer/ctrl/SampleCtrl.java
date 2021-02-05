package com.dean.practice.consumer.ctrl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Dean
 * @date 2020-11-24
 */
@RestController
public class SampleCtrl {

    private final LoadBalancerClient loadBalancerClient;
    private final DiscoveryClient discoveryClient;
    private String serviceId = "producer-service";

    @Autowired
    public SampleCtrl(LoadBalancerClient loadBalancerClient, DiscoveryClient discoveryClient) {
        this.loadBalancerClient = loadBalancerClient;
        this.discoveryClient = discoveryClient;
    }

    @Value("${spring.application.name}")
    private String serverName;

    @GetMapping("/hello")
    public String hello() {
        return "Hello : " + serverName;
    }

    /**
     * 获取所有服务
     */
    @GetMapping("/services")
    public Object services() {
        return discoveryClient.getInstances(serviceId);
    }

    /**
     * 从所有服务中选择一个服务（轮询）
     */
    @GetMapping("/discover")
    public Object discover() {
        return loadBalancerClient.choose(serviceId).getUri().toString();
    }


    @GetMapping("/call")
    public String call() {
        ServiceInstance serviceInstance = loadBalancerClient.choose(serviceId);
        System.out.println("服务地址：" + serviceInstance.getUri());
        System.out.println("服务名称：" + serviceInstance.getServiceId());

        String callServiceResult = new RestTemplate().getForObject(serviceInstance.getUri().toString() + "/hello", String.class);
        System.out.println(callServiceResult);
        return callServiceResult;
    }
}