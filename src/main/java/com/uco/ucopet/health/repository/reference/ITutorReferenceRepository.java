package com.uco.ucopet.health.repository.reference;

import com.uco.ucopet.health.domain.reference.TutorReferenceDomain;
import org.springframework.data.repository.Repository;

import java.util.UUID;

// Solo lectura: extiende el Repository base (sin save/delete) a proposito.
public interface ITutorReferenceRepository extends Repository<TutorReferenceDomain, UUID> {

    boolean existsById(UUID id);
}
