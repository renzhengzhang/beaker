package me.renzheng.beaker.start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 启动类
 *
 * @author Renzheng Zhang
 * @since 2024/4/18
 */
@EnableAspectJAutoProxy
@SpringBootApplication(scanBasePackages = "me.renzheng.beaker")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
