package ru.practicum.actions.service;

import ru.practicum.ewm.stats.proto.UserActionProto;

public interface UserActionCollectorService {
    void collectUserAction(UserActionProto userActionProto);
}