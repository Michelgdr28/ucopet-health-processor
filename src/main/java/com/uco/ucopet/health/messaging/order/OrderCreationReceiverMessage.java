package com.uco.ucopet.health.messaging.order;

import com.uco.ucopet.health.domain.order.OrderDomain;
import com.uco.ucopet.health.service.order.OrderService;
import com.uco.ucopet.health.util.gson.MapperJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class OrderCreationReceiverMessage {

    private static final Logger log = LoggerFactory.getLogger(OrderCreationReceiverMessage.class);

    private final OrderService orderService;
    private final MapperJsonObject mapperJsonObject;

    public OrderCreationReceiverMessage(final OrderService orderService,
                                        final MapperJsonObject mapperJsonObject) {
        this.orderService = orderService;
        this.mapperJsonObject = mapperJsonObject;
    }

    @RabbitListener(queues = "${health.order.save.queue-name}")
    public void receiveMessage(final Message message) {
        final Object idMessage = message.getMessageProperties().getHeader("idMensaje");
        final String json = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("[ORDER-SAVE] recibido idMensaje={}", idMessage);

        final OrderDomain order = mapperJsonObject.execute(json, OrderDomain.class)
                .orElseThrow(() -> new AmqpRejectAndDontRequeueException(
                        "El mensaje no es el JSON de una orden: " + json));

        try {
            orderService.processOrderMessage(order);
        } catch (IllegalArgumentException e) {
            log.warn("[ORDER-SAVE] rechazado idMensaje={}: {}", idMessage, e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}
