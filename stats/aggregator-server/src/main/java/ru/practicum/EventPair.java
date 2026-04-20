package ru.practicum;

public record EventPair(long first, long second) {

    public static EventPair of(long eventA,long eventB) {
        return new EventPair(Math.min(eventA, eventB), Math.max(eventA, eventB));
    }
}
