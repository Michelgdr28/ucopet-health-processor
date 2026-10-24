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

// Receptor de la cola de cambio de estado de citas (cancelar, confirmar, completar).
// Las citas no tienen "activo/inactivo": su equivalente es el estado, por eso esta cola lo cambia.
// El mensaje solo trae id, status y cancellationReason; el resto de campos llega en null.
@Component
public class AppointmentDeactivationReceiverMessage {

    private static final Logger log = LoggerFactory.getLogger(AppointmentDeactivationReceiverMessage.class);

    private final AppointmentService appointmentService;
    private final MapperJsonObject mapperJsonObject;

    public AppointmentDeactivationReceiverMessage(final AppointmentService appointmentService,
                                                  final MapperJsonObject mapperJsonObject) {
        this.appointmentService = appointmentService;
        this.mapperJsonObject = mapperJsonObject;
    }

    @RabbitListener(queues = "${health.appointment.deactivate.queue-name}")
    public void receiveMessage(final Message message) {
        final Object idMessage = message.getMessageProperties().getHeader("idMensaje");
        final String json = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("[APPOINTMENT-DEACTIVATE] recibido idMensaje={}", idMessage);

        final AppointmentDomain appointment = mapperJsonObject.execute(json, AppointmentDomain.class)
                .orElseThrow(() -> new AmqpRejectAndDontRequeueException(
                        "El mensaje no es el JSON de una cita: " + json));

        try {
            appointmentService.changeAppointmentStatus(appointment.getId(), appointment.getStatus(),
                    appointment.getCancellationReason());
        } catch (IllegalArgumentException e) {
            log.warn("[APPOINTMENT-DEACTIVATE] rechazado idMensaje={}: {}", idMessage, e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}
