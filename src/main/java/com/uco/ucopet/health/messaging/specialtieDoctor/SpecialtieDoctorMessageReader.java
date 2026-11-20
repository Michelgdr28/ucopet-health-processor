package com.uco.ucopet.health.messaging.specialtieDoctor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee los mensajes de especialidad-doctor que publica ucopetapi como árbol JSON (sin mapear a clase,
 * para no depender del formato de verifiedAt). Valida los campos mínimos y registra el mensaje.
 * Nunca lanza excepción: un mensaje inválido se descarta con WARN para que no se reintente en bucle.
 * No escribe en la base de datos.
 */
@Component
public class SpecialtieDoctorMessageReader {

    private static final Logger log = LoggerFactory.getLogger(SpecialtieDoctorMessageReader.class);

    private static final String[] REQUIRED_FIELDS = {"id", "doctor", "specialtie", "headquarterId"};
    private static final int MAX_LOGGED_BODY = 500;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void process(final Message message, final String flow) {
        final Object idMessage = message.getMessageProperties().getHeader("idMensaje");
        final byte[] rawBody = message.getBody();
        final String json = rawBody == null ? "" : new String(rawBody, StandardCharsets.UTF_8);

        final JsonNode root;
        try {
            root = objectMapper.readTree(json);
        } catch (JacksonException e) {
            log.warn("[SPECIALTIE-DOCTOR-{}] descartado idMensaje={}: JSON inválido ({}). Cuerpo: {}",
                    flow, idMessage, e.getOriginalMessage(), truncate(json));
            return;
        }

        if (root == null || !root.isObject()) {
            log.warn("[SPECIALTIE-DOCTOR-{}] descartado idMensaje={}: el cuerpo no es un objeto JSON. Cuerpo: {}",
                    flow, idMessage, truncate(json));
            return;
        }

        final List<String> missing = new ArrayList<>();
        for (String field : REQUIRED_FIELDS) {
            if (!hasText(root, field)) {
                missing.add(field);
            }
        }
        if (!missing.isEmpty()) {
            log.warn("[SPECIALTIE-DOCTOR-{}] descartado idMensaje={}: faltan campos obligatorios {}. Cuerpo: {}",
                    flow, idMessage, missing, truncate(json));
            return;
        }

        final String id = root.get("id").asString();
        final String status = hasText(root, "status") ? root.get("status").asString() : null;

        log.info("[SPECIALTIE-DOCTOR-{}] recibido idMensaje={} id={} status={}", flow, idMessage, id, status);
    }

    private static boolean hasText(final JsonNode root, final String field) {
        final JsonNode node = root.get(field);
        return node != null && node.isString() && !node.asString().isBlank();
    }

    private static String truncate(final String text) {
        return text.length() <= MAX_LOGGED_BODY ? text : text.substring(0, MAX_LOGGED_BODY) + "...";
    }
}
