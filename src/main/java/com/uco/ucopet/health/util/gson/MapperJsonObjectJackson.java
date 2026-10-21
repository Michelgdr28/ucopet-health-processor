package com.uco.ucopet.health.util.gson;


import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@Component
public class MapperJsonObjectJackson implements MapperJsonObject {

    @Override
    public Optional<String> execute(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            return Optional.ofNullable(objectMapper.writeValueAsString(object));
        } catch (JacksonException _) {
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<T> execute(String json, Class<T> destinationClass) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            return Optional.ofNullable(objectMapper.readValue(json, destinationClass));
        } catch (Exception _) {
            return Optional.empty();
        }
    }
}

