package ru.practicum.actions.mapper;

import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.ValueMapping;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

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
    @ValueMapping(source = MappingConstants.ANY_REMAINING, target = "VIEW")
    ActionTypeAvro mapActionType(ActionTypeProto source);

    default Instant mapTimestamp(Timestamp source) {
        if (source == null) {
            return Instant.EPOCH;
        }
        return Instant.ofEpochSecond(source.getSeconds(), source.getNanos());
    }
}