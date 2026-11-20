package com.uco.ucopet.health.messaging.specialtieDoctor;

import com.uco.ucopet.health.config.specialtieDoctor.SpecialtieDoctorUpdateQueueConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// Por esta cola llegan actualizar, verificar, rechazar y reactivar; se distinguen por "status".
@Component
public class SpecialtieDoctorUpdateReceiverMessage {

    private static final String FLOW = "UPDATE";

    private final SpecialtieDoctorMessageReader messageReader;

    public SpecialtieDoctorUpdateReceiverMessage(final SpecialtieDoctorMessageReader messageReader) {
        this.messageReader = messageReader;
    }

    @RabbitListener(queues = SpecialtieDoctorUpdateQueueConfig.QUEUE_NAME)
    public void receiveMessage(final Message message) {
        messageReader.process(message, FLOW);
    }
}
