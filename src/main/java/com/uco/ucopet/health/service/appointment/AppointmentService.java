package com.uco.ucopet.health.service.appointment;

import com.uco.ucopet.health.domain.appointment.AppointmentDomain;
import com.uco.ucopet.health.domain.reference.AppointmentTypeReferenceDomain;
import com.uco.ucopet.health.domain.reference.PetReferenceDomain;
import com.uco.ucopet.health.repository.appointment.IAppointmentRepository;
import com.uco.ucopet.health.repository.reference.IAppointmentTypeReferenceRepository;
import com.uco.ucopet.health.repository.reference.IDoctorReferenceRepository;
import com.uco.ucopet.health.repository.reference.IPetReferenceRepository;
import com.uco.ucopet.health.repository.reference.ITutorReferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Reglas de negocio de Appointment en el procesador de Salud.
 * La API valida lo mismo para dar una respuesta rapida, pero aqui es donde mandan:
 * entre que la API publico el mensaje y este lo consume, la BD pudo cambiar
 * (por ejemplo, otro mensaje ya ocupo el horario del medico).
 * cualquier mensaje invalido lanza IllegalArgumentException; el receiver lo rechaza sin reencolar.
 */
@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private static final ZoneId APPLICATION_ZONE_ID = ZoneId.systemDefault();
    private static final String PENDING_STATUS = "PENDING";
    private static final String CONFIRMED_STATUS = "CONFIRMED";
    private static final String CANCELLED_STATUS = "CANCELLED";
    private static final String COMPLETED_STATUS = "COMPLETED";
    private static final Set<String> ALLOWED_STATUSES = Set.of(
            PENDING_STATUS,
            CONFIRMED_STATUS,
            CANCELLED_STATUS,
            COMPLETED_STATUS
    );

    private final IAppointmentRepository appointmentRepository;
    private final IDoctorReferenceRepository doctorRepository;
    private final ITutorReferenceRepository tutorRepository;
    private final IPetReferenceRepository petRepository;
    private final IAppointmentTypeReferenceRepository appointmentTypeRepository;

    public AppointmentService(final IAppointmentRepository appointmentRepository,
                              final IDoctorReferenceRepository doctorRepository,
                              final ITutorReferenceRepository tutorRepository,
                              final IPetReferenceRepository petRepository,
                              final IAppointmentTypeReferenceRepository appointmentTypeRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.tutorRepository = tutorRepository;
        this.petRepository = petRepository;
        this.appointmentTypeRepository = appointmentTypeRepository;
    }

    // CREATE y UPDATE llegan por la misma cola: si el id no existe se crea; si existe se actualiza.
    // Como el PUT de la API, un update reemplaza todos los campos de la cita.
    // "incoming" es la cita que llego en el mensaje (no es la que se guarda: sus datos se copian).
    @Transactional
    public void saveAppointment(final AppointmentDomain incoming) {
        if (incoming == null || incoming.getId() == null) {
            throw new IllegalArgumentException("El mensaje no trae el id de la cita.");
        }

        final Optional<AppointmentDomain> existing = appointmentRepository.findById(incoming.getId());
        final String previousStatus = existing.map(AppointmentDomain::getStatus).orElse(null);

        validateSchedule(incoming);
        final String status = validateStatusRules(incoming.getStatus(), incoming.getCancellationReason(),
                previousStatus);
        validateRelationships(incoming);
        // Una cita cancelada no ocupa el horario del medico, asi que no compite por el.
        if (!CANCELLED_STATUS.equals(status)) {
            validateDoctorAvailability(incoming.getDoctorId(), incoming.getAppointmentDate(),
                    incoming.getAppointmentTime(), existing.isPresent() ? incoming.getId() : null);
        }

        final AppointmentDomain appointment = existing.orElseGet(AppointmentDomain::new);
        copyData(appointment, incoming, status);
        appointmentRepository.save(appointment);
        log.info("[APPOINTMENT] {} id={}", existing.isPresent() ? "actualizada" : "creada", appointment.getId());
    }

    // Cambia solo el estado (y la justificacion de cancelacion) de una cita existente.
    @Transactional
    public void changeAppointmentStatus(final UUID id, final String status, final String cancellationReason) {
        if (id == null) {
            throw new IllegalArgumentException("El mensaje no trae el id de la cita.");
        }

        final AppointmentDomain appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe una cita con id: " + id));

        final String previousStatus = appointment.getStatus();
        final String normalizedStatus = validateStatusRules(status, cancellationReason, previousStatus);

        // Si la cita estaba cancelada su horario pudo ser tomado por otra: al reactivarla se revisa de nuevo.
        if (CANCELLED_STATUS.equals(previousStatus) && !CANCELLED_STATUS.equals(normalizedStatus)) {
            validateDoctorAvailability(appointment.getDoctorId(), appointment.getAppointmentDate(),
                    appointment.getAppointmentTime(), appointment.getId());
        }

        appointment.setStatus(normalizedStatus);
        appointment.setCancellationReason(CANCELLED_STATUS.equals(normalizedStatus)
                ? cancellationReason.trim()
                : null);
        appointmentRepository.save(appointment);
        log.info("[APPOINTMENT] estado actualizado id={} status={}", id, normalizedStatus);
    }

    private void copyData(final AppointmentDomain target, final AppointmentDomain source, final String status) {
        target.setId(source.getId());
        target.setTutorId(source.getTutorId());
        target.setPetId(source.getPetId());
        target.setDoctorId(source.getDoctorId());
        target.setAppointmentTypeId(source.getAppointmentTypeId());
        target.setAppointmentDate(source.getAppointmentDate());
        target.setAppointmentTime(source.getAppointmentTime());
        target.setStatus(status);
        target.setCancellationReason(CANCELLED_STATUS.equals(status)
                ? source.getCancellationReason().trim()
                : null);
    }

    private void validateSchedule(final AppointmentDomain appointment) {
        if (appointment.getAppointmentDate() == null) {
            throw new IllegalArgumentException("La fecha de la cita es obligatoria");
        }
        if (appointment.getAppointmentDate().isBefore(LocalDate.now(APPLICATION_ZONE_ID))) {
            throw new IllegalArgumentException("La fecha de la cita no puede estar en el pasado");
        }
        if (appointment.getAppointmentTime() == null) {
            throw new IllegalArgumentException("La hora de la cita es obligatoria");
        }
    }

    // Devuelve el estado normalizado (sin espacios y en mayusculas) si cumple las reglas.
    private String validateStatusRules(final String rawStatus, final String cancellationReason,
                                       final String previousStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            throw new IllegalArgumentException("El estado de la cita es obligatorio");
        }

        final String status = rawStatus.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException(
                    "El estado de la cita solo puede ser PENDING, CONFIRMED, CANCELLED o COMPLETED");
        }

        final boolean hasReason = cancellationReason != null && !cancellationReason.isBlank();
        final boolean cancelled = CANCELLED_STATUS.equals(status);
        if (cancelled && !hasReason) {
            throw new IllegalArgumentException(
                    "La justificacion de cancelacion es obligatoria cuando la cita se cancela");
        }
        if (!cancelled && hasReason) {
            throw new IllegalArgumentException(
                    "La justificacion de cancelacion solo debe enviarse cuando la cita esta cancelada");
        }
        if (cancelled && COMPLETED_STATUS.equalsIgnoreCase(previousStatus)) {
            throw new IllegalArgumentException("No se puede cancelar una cita que ya esta completada");
        }
        return status;
    }

    private void validateRelationships(final AppointmentDomain appointment) {
        validateDoctor(appointment.getDoctorId());
        validateTutorAndPet(appointment.getTutorId(), appointment.getPetId());
        validateAppointmentType(appointment.getAppointmentTypeId());
    }

    private void validateDoctor(final UUID doctorId) {
        if (doctorId == null) {
            throw new IllegalArgumentException("El doctorId es obligatorio para la cita");
        }
        if (!doctorRepository.existsById(doctorId)) {
            throw new IllegalArgumentException("No existe un medico con el id indicado");
        }
    }

    private void validateTutorAndPet(final UUID tutorId, final UUID petId) {
        if (tutorId == null) {
            throw new IllegalArgumentException("El tutorId es obligatorio para la cita");
        }
        if (petId == null) {
            throw new IllegalArgumentException("El petId es obligatorio para la cita");
        }
        if (!tutorRepository.existsById(tutorId)) {
            throw new IllegalArgumentException("No existe un tutor con el id indicado");
        }

        final PetReferenceDomain pet = petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("No existe una mascota con el id indicado"));
        if (!tutorId.equals(pet.getTutorId())) {
            throw new IllegalArgumentException("La mascota indicada no pertenece al tutor de la cita");
        }
    }

    private void validateAppointmentType(final UUID appointmentTypeId) {
        if (appointmentTypeId == null) {
            throw new IllegalArgumentException("El appointmentTypeId es obligatorio para la cita");
        }

        final AppointmentTypeReferenceDomain appointmentType = appointmentTypeRepository.findById(appointmentTypeId)
                .orElseThrow(() -> new IllegalArgumentException("No existe un tipo de cita con el id indicado"));
        if (!Boolean.TRUE.equals(appointmentType.getActive())) {
            throw new IllegalArgumentException("El tipo de cita indicado no esta activo");
        }
    }

    // Revisa que el medico no tenga otra cita ACTIVA (no cancelada) a esa fecha y hora.
    // excludedAppointmentId es la propia cita cuando se actualiza o reactiva; null al crear.
    private void validateDoctorAvailability(final UUID doctorId, final LocalDate date, final LocalTime time,
                                            final UUID excludedAppointmentId) {
        final boolean scheduleTaken = excludedAppointmentId == null
                ? appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                        doctorId, date, time, CANCELLED_STATUS)
                : appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndIdNotAndStatusNot(
                        doctorId, date, time, excludedAppointmentId, CANCELLED_STATUS);

        if (scheduleTaken) {
            throw new IllegalArgumentException("El medico ya tiene una cita programada para esa fecha y hora");
        }
    }
}
