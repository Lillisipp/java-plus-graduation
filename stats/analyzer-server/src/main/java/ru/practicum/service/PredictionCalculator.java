package ru.practicum.service;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PredictionCalculator {

    public double calculate(List<Double> similarities, List<Double> weights) {
        double numerator = 0.0;
        double denominator = 0.0;

        for (int i = 0; i < similarities.size(); i++) {
            numerator += similarities.get(i) * weights.get(i);
            denominator += similarities.get(i);
        }

        if (denominator == 0.0) {
            return 0.0;
        }

        return numerator / denominator;
    }
}