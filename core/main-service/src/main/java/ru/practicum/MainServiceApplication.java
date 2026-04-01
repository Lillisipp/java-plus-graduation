package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.ewm.main.model.user.User;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.practicum")
public class MainServiceApplication {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        SpringApplication.run(MainServiceApplication.class, args);
    }
}
