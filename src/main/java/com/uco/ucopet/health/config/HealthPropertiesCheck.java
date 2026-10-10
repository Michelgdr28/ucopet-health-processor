package com.uco.ucopet.health.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * TEMPORAL: al arrancar imprime las 12 colas cargadas de health.properties,
 * para comprobar que el binding funciona. Borrar cuando existan los consumidores.
 */
@Component
public class HealthPropertiesCheck implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(HealthPropertiesCheck.class);

    private final HealthQueueProperties properties;

    public HealthPropertiesCheck(HealthQueueProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        show("petcare", properties.petcare());
        show("order", properties.order());
        show("appointment", properties.appointment());
        show("specialty", properties.specialty());
        show("procedure", properties.procedure());
        show("space", properties.space());
    }

    private void show(String module, HealthQueueProperties.ModuleQueues queues) {
        show(module + ".save", queues.save());
        show(module + ".deactivate", queues.deactivate());
    }

    private void show(String name, HealthQueueProperties.Queue queue) {
        log.info("[HEALTH] {} -> exchange={} queue={} routingKey={}",
                name, queue.exchangeName(), queue.queueName(), queue.routingKeyName());
    }
}
