package ru.practicum.ewm.stats.avro.serialization;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;

public class GeneralAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    private Class<T> targetType;

    @SuppressWarnings("unchecked")
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        targetType = (Class<T>) configs.get("avro.target.type");
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            DatumReader<T> reader = new SpecificDatumReader<>(targetType);
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка десериализации Avro-сообщения", e);
        }
    }
}