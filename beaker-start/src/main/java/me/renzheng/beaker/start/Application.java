package me.renzheng.beaker.start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 *
 * @author Renzheng Zhang
 * @since 2024/4/18
 */
@SpringBootApplication(scanBasePackages = "me.renzheng.beaker")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
