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
 //Receptor de la cola de crear y actualizar espacios.
//Queda conectado a la cola todo el tiempo el nombre de la cola sale de health.properties

@Component
public class SpaceCreationReceiverMessage {

    private static final Logger log = LoggerFactory.getLogger(SpaceCreationReceiverMessage.class);

    private final SpaceService spaceService;
    private final MapperJsonObject mapperJsonObject;

    public SpaceCreationReceiverMessage(SpaceService spaceService, MapperJsonObject mapperJsonObject) {
        this.spaceService = spaceService;
        this.mapperJsonObject = mapperJsonObject;
    }

    @RabbitListener(queues = "${health.space.save.queue-name}")
    public void receiveMessage(Message message) {
        Object idMessage = message.getMessageProperties().getHeader("idMensaje");
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("[SPACE-SAVE] recibido idMensaje={}", idMessage);

        SpaceDomain space = mapperJsonObject.execute(json, SpaceDomain.class)
                .orElseThrow(() -> new AmqpRejectAndDontRequeueException("El mensaje no es el JSON de un espacio: " + json));

        try {
            spaceService.saveSpace(space);
        } catch (IllegalArgumentException e) {
            // Mensaje invalido: reintentarlo no lo arregla. Se rechaza SIN volver a encolar,
            // si no Rabbit lo reentregaria en bucle para siempre.
            log.warn("[SPACE-SAVE] rechazado idMensaje={}: {}", idMessage, e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}

