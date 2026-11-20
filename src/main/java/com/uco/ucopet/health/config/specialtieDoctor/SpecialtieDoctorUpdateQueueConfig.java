package com.uco.ucopet.health.config.specialtieDoctor;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Nombres fijos como constantes; deben coincidir con los que publica ucopetapi
// (user.specialtieDoctor.update.* en su user.properties).
// Por esta cola llegan actualizar, verificar, rechazar y reactivar; se distinguen por "status".
@Configuration
public class SpecialtieDoctorUpdateQueueConfig {

    public static final String EXCHANGE_NAME = "specialtieDoctor.update.exchange";
    public static final String QUEUE_NAME = "specialtieDoctor.update.queue";
    public static final String ROUTING_KEY = "specialtieDoctor.update.routingkey";

    @Bean
    public DirectExchange specialtieDoctorUpdateExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue specialtieDoctorUpdateQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    @Bean
    public Binding specialtieDoctorUpdateBinding() {
        return BindingBuilder.bind(specialtieDoctorUpdateQueue())
                .to(specialtieDoctorUpdateExchange())
                .with(ROUTING_KEY);
    }
}
