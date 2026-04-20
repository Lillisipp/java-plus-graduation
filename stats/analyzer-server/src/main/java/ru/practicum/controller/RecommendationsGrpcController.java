package ru.practicum.controller;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.*;
import ru.practicum.service.RecommendationService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class RecommendationsGrpcController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("Получен gRPC-запрос рекомендаций: userId={}, maxResults={}",
                request.getUserId(), request.getMaxResults());

        recommendationService.getRecommendationsForUser(request.getUserId(), request.getMaxResults())
                .forEach(responseObserver::onNext);

        responseObserver.onCompleted();
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("Получен gRPC-запрос похожих событий: eventId={}, userId={}, maxResults={}",
                request.getEventId(), request.getUserId(), request.getMaxResults());

        recommendationService.getSimilarEvents(request.getEventId(), request.getUserId(), request.getMaxResults())
                .forEach(responseObserver::onNext);

        responseObserver.onCompleted();
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("Получен gRPC-запрос суммы взаимодействий: eventIds={}",
                request.getEventIdList());

        recommendationService.getInteractionsCount(request.getEventIdList())
                .forEach(responseObserver::onNext);

        responseObserver.onCompleted();
    }
}
