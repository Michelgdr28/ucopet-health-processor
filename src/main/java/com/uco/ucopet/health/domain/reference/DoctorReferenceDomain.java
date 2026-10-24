package com.uco.ucopet.health.domain.reference;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Vista de SOLO LECTURA de la tabla "doctors" (la gestiona el procesador de usuarios).
 * Se usa unicamente para comprobar que el medico de una cita existe.
 */
@Entity
@Table(name = "doctors")
public class DoctorReferenceDomain {

    @Id
    private UUID id;

    protected DoctorReferenceDomain() {
        // Requerido por JPA
    }

    public UUID getId() {
        return id;
    }
}
