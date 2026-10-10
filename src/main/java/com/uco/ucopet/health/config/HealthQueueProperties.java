package com.uco.ucopet.health.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Nombres de exchange, cola y routing key del procesador de Salud (health.properties).
 * 6 modulos x 2 colas: "save" (crear y actualizar) y "deactivate" (desactivar).
 * Se registra con @ConfigurationPropertiesScan en la clase principal.
 */
@ConfigurationProperties(prefix = "health")
public record HealthQueueProperties(
        ModuleQueues petcare,
        ModuleQueues order,
        ModuleQueues appointment,
        ModuleQueues specialty,
        ModuleQueues procedure,
        ModuleQueues space) {

    public record ModuleQueues(Queue save, Queue deactivate) {
    }

    public record Queue(String exchangeName, String queueName, String routingKeyName) {
    }
}
