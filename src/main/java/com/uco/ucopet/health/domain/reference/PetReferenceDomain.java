package com.uco.ucopet.health.domain.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Vista de SOLO LECTURA de la tabla "Pet" (la gestiona el procesador de usuarios).
 * Se usa para comprobar que la mascota de una cita existe y pertenece al tutor indicado.
 */
@Entity
@Table(name = "Pet")
public class PetReferenceDomain {

    @Id
    private UUID id;

    @Column(name = "tutor_id")
    private UUID tutorId;

    protected PetReferenceDomain() {
        // Requerido por JPA
    }

    public UUID getId() {
        return id;
    }

    public UUID getTutorId() {
        return tutorId;
    }
}
