package ru.practicum.actions.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration // [что делает] регистрирует класс конфигурации
@ConfigurationProperties(prefix = "collector.kafka")
public class CollectorKafkaProperties {
    private String userActionsTopic;
}
