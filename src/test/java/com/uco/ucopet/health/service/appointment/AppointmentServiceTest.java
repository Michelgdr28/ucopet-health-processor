package com.uco.ucopet.health.service.appointment;

import com.uco.ucopet.health.domain.appointment.AppointmentDomain;
import com.uco.ucopet.health.domain.reference.AppointmentTypeReferenceDomain;
import com.uco.ucopet.health.domain.reference.PetReferenceDomain;
import com.uco.ucopet.health.dto.appointment.AppointmentMessage;
import com.uco.ucopet.health.repository.appointment.IAppointmentRepository;
import com.uco.ucopet.health.repository.reference.IAppointmentTypeReferenceRepository;
import com.uco.ucopet.health.repository.reference.IDoctorReferenceRepository;
import com.uco.ucopet.health.repository.reference.IPetReferenceRepository;
import com.uco.ucopet.health.repository.reference.ITutorReferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppointmentServiceTest {

    @Mock
    private IAppointmentRepository appointmentRepository;
    @Mock
    private IDoctorReferenceRepository doctorRepository;
    @Mock
    private ITutorReferenceRepository tutorRepository;
    @Mock
    private IPetReferenceRepository petRepository;
    @Mock
    private IAppointmentTypeReferenceRepository appointmentTypeRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    // ------------------------------------------------------------------ crear / actualizar

    @Test
    void saveAppointment_createsWhenIdDoesNotExist() {
        final AppointmentMessage message = validMessage();
        givenValidReferences(message);
        when(appointmentRepository.findById(message.getId())).thenReturn(Optional.empty());

        appointmentService.saveAppointment(message);

        final ArgumentCaptor<AppointmentDomain> saved = ArgumentCaptor.forClass(AppointmentDomain.class);
        verify(appointmentRepository).save(saved.capture());
        assertEquals(message.getId(), saved.getValue().getId());
        assertEquals(message.getDoctorId(), saved.getValue().getDoctorId());
        assertEquals(message.getAppointmentDate(), saved.getValue().getAppointmentDate());
        assertEquals(message.getAppointmentTime(), saved.getValue().getAppointmentTime());
        // el mensaje trae "pending " y se normaliza
        assertEquals("PENDING", saved.getValue().getStatus());
        assertNull(saved.getValue().getCancellationReason());
    }

    @Test
    void saveAppointment_updatesWhenIdAlreadyExists() {
        final AppointmentMessage message = validMessage();
        message.setAppointmentTime(LocalTime.of(15, 0));
        givenValidReferences(message);

        final AppointmentDomain existing = new AppointmentDomain();
        existing.setId(message.getId());
        existing.setStatus("PENDING");
        existing.setAppointmentTime(LocalTime.of(9, 0));
        when(appointmentRepository.findById(message.getId())).thenReturn(Optional.of(existing));

        appointmentService.saveAppointment(message);

        final ArgumentCaptor<AppointmentDomain> saved = ArgumentCaptor.forClass(AppointmentDomain.class);
        verify(appointmentRepository).save(saved.capture());
        assertSame(existing, saved.getValue());
        assertEquals(LocalTime.of(15, 0), saved.getValue().getAppointmentTime());
        // al actualizar se ignora la propia cita en el chequeo de horario
        verify(appointmentRepository, never())
                .existsByDoctorIdAndAppointmentDateAndAppointmentTime(any(), any(), any());
    }

    @Test
    void saveAppointment_rejectsMessageWithoutId() {
        final AppointmentMessage message = validMessage();
        message.setId(null);

        assertThrows(IllegalArgumentException.class, () -> appointmentService.saveAppointment(message));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void saveAppointment_rejectsPastDate() {
        final AppointmentMessage message = validMessage();
        message.setAppointmentDate(LocalDate.now().minusDays(1));
        givenValidReferences(message);

        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.saveAppointment(message));
        assertTrue(error.getMessage().contains("pasado"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void saveAppointment_rejectsUnknownDoctor() {
        final AppointmentMessage message = validMessage();
        givenValidReferences(message);
        when(doctorRepository.existsById(message.getDoctorId())).thenReturn(false);

        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.saveAppointment(message));
        assertTrue(error.getMessage().contains("medico"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void saveAppointment_rejectsPetThatBelongsToAnotherTutor() {
        final AppointmentMessage message = validMessage();
        givenValidReferences(message);
        final PetReferenceDomain otherTutorsPet = mock(PetReferenceDomain.class);
        when(otherTutorsPet.getTutorId()).thenReturn(UUID.randomUUID());
        when(petRepository.findById(message.getPetId())).thenReturn(Optional.of(otherTutorsPet));

        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.saveAppointment(message));
        assertTrue(error.getMessage().contains("no pertenece"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void saveAppointment_rejectsInactiveAppointmentType() {
        final AppointmentMessage message = validMessage();
        givenValidReferences(message);
        final AppointmentTypeReferenceDomain inactiveType = mock(AppointmentTypeReferenceDomain.class);
        when(inactiveType.getActive()).thenReturn(false);
        when(appointmentTypeRepository.findById(message.getAppointmentTypeId()))
                .thenReturn(Optional.of(inactiveType));

        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.saveAppointment(message));
        assertTrue(error.getMessage().contains("no esta activo"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void saveAppointment_rejectsDoctorScheduleConflict() {
        final AppointmentMessage message = validMessage();
        givenValidReferences(message);
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                message.getDoctorId(), message.getAppointmentDate(), message.getAppointmentTime()))
                .thenReturn(true);

        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.saveAppointment(message));
        assertTrue(error.getMessage().contains("ya tiene una cita"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void saveAppointment_rejectsCancelledWithoutReason() {
        final AppointmentMessage message = validMessage();
        message.setStatus("CANCELLED");
        message.setCancellationReason("   ");
        givenValidReferences(message);

        assertThrows(IllegalArgumentException.class, () -> appointmentService.saveAppointment(message));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void saveAppointment_rejectsReasonWhenNotCancelled() {
        final AppointmentMessage message = validMessage();
        message.setCancellationReason("El tutor no puede asistir");
        givenValidReferences(message);

        assertThrows(IllegalArgumentException.class, () -> appointmentService.saveAppointment(message));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void saveAppointment_rejectsInvalidStatus() {
        final AppointmentMessage message = validMessage();
        message.setStatus("ARCHIVED");
        givenValidReferences(message);

        assertThrows(IllegalArgumentException.class, () -> appointmentService.saveAppointment(message));
        verify(appointmentRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ cambio de estado

    @Test
    void changeAppointmentStatus_cancelsWithReason() {
        final UUID id = UUID.randomUUID();
        final AppointmentDomain existing = new AppointmentDomain();
        existing.setId(id);
        existing.setStatus("PENDING");
        when(appointmentRepository.findById(id)).thenReturn(Optional.of(existing));

        appointmentService.changeAppointmentStatus(id, "cancelled", "  El tutor no puede asistir  ");

        verify(appointmentRepository).save(existing);
        assertEquals("CANCELLED", existing.getStatus());
        assertEquals("El tutor no puede asistir", existing.getCancellationReason());
    }

    @Test
    void changeAppointmentStatus_clearsReasonWhenNoLongerCancelled() {
        final UUID id = UUID.randomUUID();
        final AppointmentDomain existing = new AppointmentDomain();
        existing.setId(id);
        existing.setStatus("CANCELLED");
        existing.setCancellationReason("Motivo anterior");
        when(appointmentRepository.findById(id)).thenReturn(Optional.of(existing));

        appointmentService.changeAppointmentStatus(id, "CONFIRMED", null);

        assertEquals("CONFIRMED", existing.getStatus());
        assertNull(existing.getCancellationReason());
    }

    @Test
    void changeAppointmentStatus_cannotCancelACompletedAppointment() {
        final UUID id = UUID.randomUUID();
        final AppointmentDomain existing = new AppointmentDomain();
        existing.setId(id);
        existing.setStatus("COMPLETED");
        when(appointmentRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.changeAppointmentStatus(id, "CANCELLED", "Motivo"));
        verify(appointmentRepository, never()).save(any());
        assertEquals("COMPLETED", existing.getStatus());
    }

    @Test
    void changeAppointmentStatus_rejectsUnknownAppointment() {
        final UUID id = UUID.randomUUID();
        when(appointmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.changeAppointmentStatus(id, "CONFIRMED", null));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void changeAppointmentStatus_rejectsMissingId() {
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.changeAppointmentStatus(null, "CONFIRMED", null));
    }

    // ------------------------------------------------------------------ helpers

    private AppointmentMessage validMessage() {
        final AppointmentMessage message = new AppointmentMessage();
        message.setId(UUID.randomUUID());
        message.setTutorId(UUID.randomUUID());
        message.setPetId(UUID.randomUUID());
        message.setDoctorId(UUID.randomUUID());
        message.setAppointmentTypeId(UUID.randomUUID());
        message.setAppointmentDate(LocalDate.now().plusDays(3));
        message.setAppointmentTime(LocalTime.of(10, 30));
        message.setStatus(" pending ");
        message.setCancellationReason(null);
        return message;
    }

    // Deja todas las referencias (medico, tutor, mascota del tutor, tipo activo) en estado valido.
    private void givenValidReferences(final AppointmentMessage message) {
        when(doctorRepository.existsById(message.getDoctorId())).thenReturn(true);
        when(tutorRepository.existsById(message.getTutorId())).thenReturn(true);

        final PetReferenceDomain pet = mock(PetReferenceDomain.class);
        when(pet.getTutorId()).thenReturn(message.getTutorId());
        when(petRepository.findById(message.getPetId())).thenReturn(Optional.of(pet));

        final AppointmentTypeReferenceDomain type = mock(AppointmentTypeReferenceDomain.class);
        when(type.getActive()).thenReturn(true);
        when(appointmentTypeRepository.findById(message.getAppointmentTypeId())).thenReturn(Optional.of(type));

        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                message.getDoctorId(), message.getAppointmentDate(), message.getAppointmentTime()))
                .thenReturn(false);
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndIdNot(
                message.getDoctorId(), message.getAppointmentDate(), message.getAppointmentTime(),
                message.getId()))
                .thenReturn(false);
    }
}
