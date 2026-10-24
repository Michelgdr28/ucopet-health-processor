package com.uco.ucopet.health.domain.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Vista de SOLO LECTURA de la tabla "appointment_types".
 * Se usa para comprobar que el tipo de una cita existe y esta activo.
 */
@Entity
@Table(name = "appointment_types")
public class AppointmentTypeReferenceDomain {

    @Id
    private UUID id;

    @Column(name = "is_active")
    private Boolean active;

    protected AppointmentTypeReferenceDomain() {
        // Requerido por JPA
    }

    public UUID getId() {
        return id;
    }

    public Boolean getActive() {
        return active;
    }
}
