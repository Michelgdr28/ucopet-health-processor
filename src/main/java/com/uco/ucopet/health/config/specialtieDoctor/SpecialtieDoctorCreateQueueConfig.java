package com.uco.ucopet.health.config.specialtieDoctor;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Nombres fijos como constantes; deben coincidir con los que publica ucopetapi
// (user.specialtieDoctor.create.* en su user.properties).
@Configuration
public class SpecialtieDoctorCreateQueueConfig {

    public static final String EXCHANGE_NAME = "specialtieDoctor.create.exchange";
    public static final String QUEUE_NAME = "specialtieDoctor.create.queue";
    public static final String ROUTING_KEY = "specialtieDoctor.create.routingkey";

    @Bean
    public DirectExchange specialtieDoctorCreateExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue specialtieDoctorCreateQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    @Bean
    public Binding specialtieDoctorCreateBinding() {
        return BindingBuilder.bind(specialtieDoctorCreateQueue())
                .to(specialtieDoctorCreateExchange())
                .with(ROUTING_KEY);
    }
}
