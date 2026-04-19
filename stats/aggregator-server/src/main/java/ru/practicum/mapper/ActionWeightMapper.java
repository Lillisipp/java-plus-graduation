package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;

@UtilityClass
public class ActionWeightMapper {
    public double toWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}
