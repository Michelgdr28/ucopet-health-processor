package com.uco.ucopet.health.messaging.appointment;

import com.uco.ucopet.health.domain.appointment.AppointmentDomain;
import com.uco.ucopet.health.service.appointment.AppointmentService;
import com.uco.ucopet.health.util.gson.MapperJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

// Receptor de la cola de crear y actualizar citas.
// Queda conectado a la cola todo el tiempo; el nombre de la cola sale de health.properties.
@Component
public class AppointmentCreationReceiverMessage {

    private static final Logger log = LoggerFactory.getLogger(AppointmentCreationReceiverMessage.class);

    private final AppointmentService appointmentService;
    private final MapperJsonObject mapperJsonObject;

    public AppointmentCreationReceiverMessage(final AppointmentService appointmentService,
                                              final MapperJsonObject mapperJsonObject) {
        this.appointmentService = appointmentService;
        this.mapperJsonObject = mapperJsonObject;
    }

    @RabbitListener(queues = "${health.appointment.save.queue-name}")
    public void receiveMessage(final Message message) {
        final Object idMessage = message.getMessageProperties().getHeader("idMensaje");
        final String json = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("[APPOINTMENT-SAVE] recibido idMensaje={}", idMessage);

        final AppointmentDomain appointment = mapperJsonObject.execute(json, AppointmentDomain.class)
                .orElseThrow(() -> new AmqpRejectAndDontRequeueException(
                        "El mensaje no es el JSON de una cita: " + json));

        try {
            appointmentService.saveAppointment(appointment);
        } catch (IllegalArgumentException e) {
            // Mensaje invalido: reintentarlo no lo arregla. Se rechaza SIN volver a encolar,
            // si no Rabbit lo reentregaria en bucle para siempre.
            log.warn("[APPOINTMENT-SAVE] rechazado idMensaje={}: {}", idMessage, e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}
