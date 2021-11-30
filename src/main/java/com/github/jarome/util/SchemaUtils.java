package com.github.jarome.util;

import com.github.jarome.constant.PulsarSerialization;
import com.github.jarome.error.PulsarException;
import org.apache.pulsar.client.api.Schema;

import java.lang.reflect.Method;

public class SchemaUtils {

    private SchemaUtils() {
    }

    private static <T> Schema<?> getGenericSchema(PulsarSerialization PulsarSerialization, Class<T> clazz) throws RuntimeException {
        switch (PulsarSerialization) {
            case JSON:
                return Schema.JSON(clazz);
            case AVRO:
                return Schema.AVRO(clazz);
            case STRING:
                return Schema.STRING;
            case BYTE:
                return Schema.BYTES;
            default: {
                throw new PulsarException("Unknown producer schema.");
            }
        }
    }


    public static Schema<?> getSchema(PulsarSerialization serialisation, Class<?> clazz) {
        if (clazz == byte[].class) {
            return Schema.BYTES;
        }

        return getGenericSchema(serialisation, clazz);
    }


    public static Class<?> getParameterType(Method method) {
        return method.getParameterTypes()[0];
    }

}
