package com.uco.ucopet.health.messaging.procedure;

import com.uco.ucopet.health.domain.procedure.ProcedureDomain;
import com.uco.ucopet.health.service.procedure.ProcedureService;
import com.uco.ucopet.health.util.gson.MapperJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class ProcedureDeactivationReceiverMessage {

    private static final Logger log = LoggerFactory.getLogger(ProcedureDeactivationReceiverMessage.class);

    private final ProcedureService procedureService;
    private final MapperJsonObject mapperJsonObject;

    public ProcedureDeactivationReceiverMessage(final ProcedureService procedureService,
                                                final MapperJsonObject mapperJsonObject) {
        this.procedureService = procedureService;
        this.mapperJsonObject = mapperJsonObject;
    }

    @RabbitListener(queues = "${health.procedure.deactivate.queue-name}")
    public void receiveMessage(final Message message) {
        final Object idMessage = message.getMessageProperties().getHeader("idMensaje");
        final String json = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("[PROCEDURE-DEACTIVATE] recibido idMensaje={}", idMessage);

        final ProcedureDomain procedure = mapperJsonObject.execute(json, ProcedureDomain.class)
                .orElseThrow(() -> new AmqpRejectAndDontRequeueException(
                        "El mensaje no es el JSON de un procedimiento: " + json));

        try {
            procedureService.changeProcedureStatus(procedure.getId(), procedure.getActive());
        } catch (IllegalArgumentException e) {
            log.warn("[PROCEDURE-DEACTIVATE] rechazado idMensaje={}: {}", idMessage, e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}