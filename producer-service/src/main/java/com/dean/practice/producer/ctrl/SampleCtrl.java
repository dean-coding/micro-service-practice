package com.dean.practice.producer.ctrl;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 *
 * @author Dean
 * @date 2020-11-27
 */
@RestController
@RequestMapping
public class SampleCtrl {

    @Value("${spring.application.name}")
    private String serverName;

    @GetMapping("/hello")
    public String hello() {
        return "Hello : " + serverName;
    }

    @PostMapping("/user")
    public SampleUser setName(@RequestBody @Validated SampleUser user) {
        return user;
    }

    @Data
    private static class SampleUser {

        private Long id;

        private String name;
    }

}