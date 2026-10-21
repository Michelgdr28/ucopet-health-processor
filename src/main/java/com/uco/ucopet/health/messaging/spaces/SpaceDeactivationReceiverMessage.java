package com.uco.ucopet.health.messaging.spaces;

import com.uco.ucopet.health.domain.space.SpaceDomain;
import com.uco.ucopet.health.service.space.SpaceService;
import com.uco.ucopet.health.util.gson.MapperJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

//Receptor de la cola de desactivar espacios.
@Component
public class SpaceDeactivationReceiverMessage {

    private static final Logger log = LoggerFactory.getLogger(SpaceDeactivationReceiverMessage.class);

    private final SpaceService spaceService;
    private final MapperJsonObject mapperJsonObject;

    public SpaceDeactivationReceiverMessage(SpaceService spaceService, MapperJsonObject mapperJsonObject) {
        this.spaceService = spaceService;
        this.mapperJsonObject = mapperJsonObject;
    }

    @RabbitListener(queues = "${health.space.deactivate.queue-name}")
    public void receiveMessage(Message message) {
        Object idMessage = message.getMessageProperties().getHeader("idMensaje");
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("[SPACE-DEACTIVATE] recibido idMensaje={}", idMessage);

        SpaceDomain space = mapperJsonObject.execute(json, SpaceDomain.class)
                .orElseThrow(() -> new AmqpRejectAndDontRequeueException("El mensaje no es el JSON de un espacio: " + json));

        try {
            spaceService.deactivateSpace(space.getId());
        } catch (IllegalArgumentException e) {
            log.warn("[SPACE-DEACTIVATE] rechazado idMensaje={}: {}", idMessage, e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}

