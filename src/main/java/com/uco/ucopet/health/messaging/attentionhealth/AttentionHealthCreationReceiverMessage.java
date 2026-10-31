package com.uco.ucopet.health.messaging.attentionhealth;

import com.uco.ucopet.health.domain.attentionhealth.AttentionHealthDomain;
import com.uco.ucopet.health.service.attentionhealth.AttentionHealthService;
import com.uco.ucopet.health.util.gson.MapperJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class AttentionHealthCreationReceiverMessage {

    private static final Logger log = LoggerFactory.getLogger(AttentionHealthCreationReceiverMessage.class);
    private final AttentionHealthService service;
    private final MapperJsonObject mapper;

    public AttentionHealthCreationReceiverMessage(final AttentionHealthService service,
                                                  final MapperJsonObject mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @RabbitListener(queues = "${health.attentionhealth.save.queue-name}")
    public void receiveMessage(final Message message) {
        final Object idMessage = message.getMessageProperties().getHeader("idMensaje");
        final String json = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("[ATTENTIONHEALTH-SAVE] recibido idMensaje={}", idMessage);
        AttentionHealthDomain attention = mapper.execute(json, AttentionHealthDomain.class)
                .orElseThrow(() -> new AmqpRejectAndDontRequeueException(
                        "El mensaje no es el JSON de una atención: " + json));
        try {
            service.saveAttention(attention);
        } catch (IllegalArgumentException exception) {
            throw new AmqpRejectAndDontRequeueException(exception.getMessage(), exception);
        }
    }
}
