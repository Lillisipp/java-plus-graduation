package ru.practicum.actions.controller;


import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.actions.service.UserActionCollectorService;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

/**
 * Класс gRPC-сервиса для обработки действий пользователей.
 * Реализует серверную часть gRPC (определённую в protobuf-файле).
 */

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class CollectorControllerGrpc extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final UserActionCollectorService userActionCollectorService;

    @Override
    public void collectUserAction(UserActionProto userActionProto, StreamObserver<Empty> responseObserver) {
        log.info("Получено действие пользователя: userId={}, eventId={}, actionType={}",
                userActionProto.getUserId(), userActionProto.getEventId(), userActionProto.getActionType());
        try {
            userActionCollectorService.collectUserAction(userActionProto);

            //Ответ клиенту.
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        }catch (Exception e) {
            log.error("Ошибка при обработке действия пользователя: userId={}, eventId={}, actionType={}",
                    userActionProto.getUserId(), userActionProto.getEventId(), userActionProto.getActionType(), e);

            responseObserver.onError(e);
        }
    }
}