package ru.practicum.collector;

import ru.practicum.ewm.stats.proto.UserActionProto;

public interface CollectorClient {
    void collectUserAction(UserActionProto request);

}
