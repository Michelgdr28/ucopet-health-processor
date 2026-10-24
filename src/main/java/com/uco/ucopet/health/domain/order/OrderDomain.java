package com.uco.ucopet.health.domain.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderDomain {

    @Id
    private UUID id;

    // La base genera id_order (BIGSERIAL): Hibernate nunca lo escribe, solo lo lee.
    @Column(name = "id_order", nullable = false, unique = true, insertable = false, updatable = false)
    @Generated(event = EventType.INSERT)
    private Long idOrder;

    @Column(name = "tutor_id", nullable = false)
    private UUID tutorId;

    @Column(name = "pet_id", nullable = false)
    private UUID petId;

    @Column(name = "procedure_id", nullable = false)
    private UUID procedureId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private OrderState state;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "is_authorized", nullable = false)
    private Boolean isAuthorized;

    public OrderDomain() {
    }

    public UUID getId() {
        return id;
    }

    public Long getIdOrder() {
        return idOrder;
    }

    public UUID getTutorId() {
        return tutorId;
    }

    public UUID getPetId() {
        return petId;
    }

    public UUID getProcedureId() {
        return procedureId;
    }

    public OrderState getState() {
        return state;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Boolean getIsAuthorized() {
        return isAuthorized;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setIdOrder(Long idOrder) {
        this.idOrder = idOrder;
    }

    public void setTutorId(UUID tutorId) {
        this.tutorId = tutorId;
    }

    public void setPetId(UUID petId) {
        this.petId = petId;
    }

    public void setProcedureId(UUID procedureId) {
        this.procedureId = procedureId;
    }

    public void setState(OrderState state) {
        this.state = state;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setIsAuthorized(Boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
    }
}
