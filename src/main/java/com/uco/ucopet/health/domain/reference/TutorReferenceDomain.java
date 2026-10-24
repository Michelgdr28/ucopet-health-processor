package com.uco.ucopet.health.domain.reference;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Vista de SOLO LECTURA de la tabla "tutor_pet" (la gestiona el procesador de usuarios).
 * Se usa unicamente para comprobar que el tutor de una cita existe.
 */
@Entity
@Table(name = "tutor_pet")
public class TutorReferenceDomain {

    @Id
    private UUID id;

    protected TutorReferenceDomain() {
        // Requerido por JPA
    }

    public UUID getId() {
        return id;
    }
}
