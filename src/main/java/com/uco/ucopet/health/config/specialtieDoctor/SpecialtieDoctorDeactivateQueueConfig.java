package com.uco.ucopet.health.config.specialtieDoctor;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Nombres fijos como constantes; deben coincidir con los que publica ucopetapi
// (user.specialtieDoctor.deactivate.* en su user.properties).
@Configuration
public class SpecialtieDoctorDeactivateQueueConfig {

    public static final String EXCHANGE_NAME = "specialtieDoctor.deactivate.exchange";
    public static final String QUEUE_NAME = "specialtieDoctor.deactivate.queue";
    public static final String ROUTING_KEY = "specialtieDoctor.deactivate.routingkey";

    @Bean
    public DirectExchange specialtieDoctorDeactivateExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue specialtieDoctorDeactivateQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    @Bean
    public Binding specialtieDoctorDeactivateBinding() {
        return BindingBuilder.bind(specialtieDoctorDeactivateQueue())
                .to(specialtieDoctorDeactivateExchange())
                .with(ROUTING_KEY);
    }
}
