package com.uco.ucopet.health.repository.reference;

import com.uco.ucopet.health.domain.reference.PetReferenceDomain;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

// Solo lectura: extiende el Repository base (sin save/delete) a proposito.
public interface IPetReferenceRepository extends Repository<PetReferenceDomain, UUID> {

    Optional<PetReferenceDomain> findById(UUID id);
}
