package com.uco.ucopet.health.messaging.specialtieDoctor;

import com.uco.ucopet.health.config.specialtieDoctor.SpecialtieDoctorDeactivateQueueConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SpecialtieDoctorDeactivateReceiverMessage {

    private static final String FLOW = "DEACTIVATE";

    private final SpecialtieDoctorMessageReader messageReader;

    public SpecialtieDoctorDeactivateReceiverMessage(final SpecialtieDoctorMessageReader messageReader) {
        this.messageReader = messageReader;
    }

    @RabbitListener(queues = SpecialtieDoctorDeactivateQueueConfig.QUEUE_NAME)
    public void receiveMessage(final Message message) {
        messageReader.process(message, FLOW);
    }
}
