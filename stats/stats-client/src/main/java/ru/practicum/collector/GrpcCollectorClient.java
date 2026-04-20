package ru.practicum.collector;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

@Slf4j
@Component
public class GrpcCollectorClient implements CollectorClient{
    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;

    @Override
    public void collectUserAction(UserActionProto request) {
        log.info("Отправка user action в Collector: userId={}, eventId={}, actionType={}",
                request.getUserId(),
                request.getEventId(),
                request.getActionType());

        client.collectUserAction(request);

        log.info("Collector успешно принял user action: userId={}, eventId={}",
                request.getUserId(),
                request.getEventId());
    }
}
