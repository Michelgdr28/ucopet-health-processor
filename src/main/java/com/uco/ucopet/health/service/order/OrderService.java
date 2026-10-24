package com.uco.ucopet.health.service.order;

import com.uco.ucopet.health.domain.order.OrderDomain;
import com.uco.ucopet.health.repository.order.IOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

//Reglas de Order en el procesador de Salud.
//La API ya guarda la orden antes de publicarla, por eso aqui solo se valida y se registra:
//un upsert podria resucitar ordenes borradas o devolver a PENDING una orden ya autorizada.

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final IOrderRepository orderRepository;

    public OrderService(final IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public void processOrderMessage(final OrderDomain message) {
        if (message == null || message.getId() == null) {
            throw new IllegalArgumentException("El mensaje no trae el id de la orden.");
        }
        if (message.getPetId() == null) {
            throw new IllegalArgumentException("El ID de la mascota es obligatorio en la orden.");
        }
        if (message.getProcedureId() == null) {
            throw new IllegalArgumentException("El ID del procedimiento es obligatorio en la orden.");
        }
        if (message.getTutorId() == null) {
            throw new IllegalArgumentException("El ID del tutor es obligatorio en la orden.");
        }

        final Optional<OrderDomain> existing = orderRepository.findById(message.getId());
        if (existing.isPresent()) {
            final OrderDomain order = existing.get();
            log.info("[ORDER] orden ya persistida por la API id={} idOrder={} state={}",
                    order.getId(), order.getIdOrder(), order.getState());
        } else {
            log.warn("[ORDER] mensaje para una orden que no existe en la base, se descarta id={}",
                    message.getId());
        }
    }
}
