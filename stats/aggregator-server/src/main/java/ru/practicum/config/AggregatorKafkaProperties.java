package ru.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "aggregator.kafka")
public class AggregatorKafkaProperties {

    private String userActionsTopic;
    private String eventsSimilarityTopic;
}