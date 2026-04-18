package ru.practicum.service;

import org.springframework.stereotype.Component;

@Component
public class SimilarityCalculator {

    public double calculate(double minSum, double sumA, double sumB) {
        if (sumA <= 0 || sumB <= 0) {
            return 0.0;
        }

        return minSum / Math.sqrt(sumA * sumB);
    }
}