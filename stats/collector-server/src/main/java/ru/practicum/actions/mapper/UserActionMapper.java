package ru.practicum.actions.mapper;

import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.ValueMapping;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserActionMapper {

    @Mapping(target = "timestamp", source = "timestamp")
    @Mapping(target = "actionType", source = "actionType")
    UserActionAvro toAvro(UserActionProto source);

    @ValueMapping(source = "ACTION_VIEW", target = "VIEW")
    @ValueMapping(source = "ACTION_REGISTER", target = "REGISTER")
    @ValueMapping(source = "ACTION_LIKE", target = "LIKE")
    ActionTypeAvro mapActionType(ActionTypeProto source);

    default long mapTimestamp(Timestamp source) {
        if (source == null) {
            return 0L;
        }
        return source.getSeconds() * 1000 + source.getNanos() / 1_000_000;
    }
}