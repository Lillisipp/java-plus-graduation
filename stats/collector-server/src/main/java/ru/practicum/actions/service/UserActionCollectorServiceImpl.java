package ru.practicum.actions.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.actions.mapper.UserActionMapper;
import ru.practicum.actions.producer.UserActionProducer;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionProto;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionCollectorServiceImpl implements UserActionCollectorService{

    private final UserActionMapper userActionMapper;
    private final UserActionProducer userActionProducer;

    @Override
    public void collectUserAction(UserActionProto userActionProto) {
        log.debug("Начинаем преобразование UserActionProto в UserActionAvro: userId={}, eventId={}",
                userActionProto.getUserId(), userActionProto.getEventId());

        UserActionAvro avroMessage = userActionMapper.toAvro(userActionProto);
        log.debug("Сообщение преобразовано: userId={}, eventId={}, actionType={}",
                avroMessage.getUserId(), avroMessage.getEventId(), avroMessage.getActionType());

        userActionProducer.send(avroMessage);

        log.info("Сообщение отправлено в Kafka: userId={}, eventId={}, actionType={}",
                avroMessage.getUserId(), avroMessage.getEventId(), avroMessage.getActionType());

    }
}
