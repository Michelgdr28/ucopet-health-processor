package com.uco.ucopet.health.repository.reference;

import com.uco.ucopet.health.domain.reference.AppointmentTypeReferenceDomain;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

// Solo lectura: extiende el Repository base (sin save/delete) a proposito.
public interface IAppointmentTypeReferenceRepository extends Repository<AppointmentTypeReferenceDomain, UUID> {

    Optional<AppointmentTypeReferenceDomain> findById(UUID id);
}
