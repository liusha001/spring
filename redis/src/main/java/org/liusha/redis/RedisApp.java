package org.liusha.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * @author chenrf
 * @version 1.0
 * @date 2020/11/15 14:29
 */
@SpringBootApplication
public class RedisApp {

    public static void main(String[] args) {
        ApplicationContext app = SpringApplication.run(RedisApp.class, args);
    }

}
