package com.internetitem.logback.elasticsearch;

import com.fasterxml.jackson.core.JsonGenerator;
import com.internetitem.logback.elasticsearch.util.AbstractPropertyAndEncoder;

import java.io.IOException;

class PropertySerializer<T> {
    private PropertySerializer() {
    }

    private static PropertySerializer INSTANCE = null;

    static synchronized PropertySerializer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PropertySerializer();
        }
        return INSTANCE;
    }

    synchronized void serializeProperty(JsonGenerator jsonGenerator, T event, AbstractPropertyAndEncoder<T> propertyAndEncoder) throws IOException {
        String value = propertyAndEncoder.encode(event);
        if (propertyAndEncoder.allowEmpty() || (value != null && !value.isEmpty())) {
            switch (propertyAndEncoder.getType()) {
                case INT:
                    serializeIntField(jsonGenerator, propertyAndEncoder, value);
                    break;
                case FLOAT:
                    serializeFloatField(jsonGenerator, propertyAndEncoder, value);
                    break;
                case BOOLEAN:
                    serializeBooleanField(jsonGenerator, propertyAndEncoder, value);
                    break;
                default:
                    serializeStringField(jsonGenerator, propertyAndEncoder, value);
            }
        }
    }

    private void serializeStringField(JsonGenerator jsonGenerator, AbstractPropertyAndEncoder<T> propertyAndEncoder, String value) throws IOException {
        jsonGenerator.writeObjectField(propertyAndEncoder.getName(), value);
    }

    private void serializeIntField(JsonGenerator jsonGenerator, AbstractPropertyAndEncoder<T> propertyAndEncoder, String value) throws IOException {
        try {
            jsonGenerator.writeNumberField(propertyAndEncoder.getName(), Integer.valueOf(value));
        } catch (NumberFormatException e) {
            serializeStringField(jsonGenerator, propertyAndEncoder, value);
        }
    }

    private void serializeFloatField(JsonGenerator jsonGenerator, AbstractPropertyAndEncoder<T> propertyAndEncoder, String value) throws IOException {
        try {
            jsonGenerator.writeNumberField(propertyAndEncoder.getName(), Float.valueOf(value));
        } catch (NumberFormatException e) {
            serializeStringField(jsonGenerator, propertyAndEncoder, value);
        }
    }

    private void serializeBooleanField(JsonGenerator jsonGenerator, AbstractPropertyAndEncoder<T> propertyAndEncoder, String value) throws IOException {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            jsonGenerator.writeBooleanField(propertyAndEncoder.getName(), Boolean.valueOf(value));
        } else {
            serializeStringField(jsonGenerator, propertyAndEncoder, value);
        }
    }
}