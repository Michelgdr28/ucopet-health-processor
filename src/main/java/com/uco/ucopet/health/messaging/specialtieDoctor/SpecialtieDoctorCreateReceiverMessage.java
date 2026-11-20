package com.uco.ucopet.health.messaging.specialtieDoctor;

import com.uco.ucopet.health.config.specialtieDoctor.SpecialtieDoctorCreateQueueConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SpecialtieDoctorCreateReceiverMessage {

    private static final String FLOW = "CREATE";

    private final SpecialtieDoctorMessageReader messageReader;

    public SpecialtieDoctorCreateReceiverMessage(final SpecialtieDoctorMessageReader messageReader) {
        this.messageReader = messageReader;
    }

    @RabbitListener(queues = SpecialtieDoctorCreateQueueConfig.QUEUE_NAME)
    public void receiveMessage(final Message message) {
        messageReader.process(message, FLOW);
    }
}
