package com.uco.ucopet.health.service.appointment;

import com.uco.ucopet.health.domain.appointment.AppointmentDomain;
import com.uco.ucopet.health.domain.reference.AppointmentTypeReferenceDomain;
import com.uco.ucopet.health.domain.reference.PetReferenceDomain;
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
        final AppointmentDomain incoming = validAppointment();
        givenValidReferences(incoming);
        when(appointmentRepository.findById(incoming.getId())).thenReturn(Optional.empty());

        appointmentService.saveAppointment(incoming);

        final ArgumentCaptor<AppointmentDomain> saved = ArgumentCaptor.forClass(AppointmentDomain.class);
        verify(appointmentRepository).save(saved.capture());
        assertEquals(incoming.getId(), saved.getValue().getId());
        assertEquals(incoming.getDoctorId(), saved.getValue().getDoctorId());
        assertEquals(incoming.getAppointmentDate(), saved.getValue().getAppointmentDate());
        assertEquals(incoming.getAppointmentTime(), saved.getValue().getAppointmentTime());
        // el mensaje trae "pending " y se normaliza
        assertEquals("PENDING", saved.getValue().getStatus());
        assertNull(saved.getValue().getCancellationReason());
    }

    @Test
    void saveAppointment_updatesWhenIdAlreadyExists() {
        final AppointmentDomain incoming = validAppointment();
        incoming.setAppointmentTime(LocalTime.of(15, 0));
        givenValidReferences(incoming);

        final AppointmentDomain existing = new AppointmentDomain();
        existing.setId(incoming.getId());
        existing.setStatus("PENDING");
        existing.setAppointmentTime(LocalTime.of(9, 0));
        when(appointmentRepository.findById(incoming.getId())).thenReturn(Optional.of(existing));

        appointmentService.saveAppointment(incoming);

        final ArgumentCaptor<AppointmentDomain> saved = ArgumentCaptor.forClass(AppointmentDomain.class);
        verify(appointmentRepository).save(saved.capture());
        assertSame(existing, saved.getValue());
        assertEquals(LocalTime.of(15, 0), saved.getValue().getAppointmentTime());
        // al actualizar se ignora la propia cita en el chequeo de horario
        verify(appointmentRepository, never())
                .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(any(), any(), any(), any());
    }

    @Test
    void saveAppointment_rejectsMessageWithoutId() {
        final AppointmentDomain incoming = validAppointment();
        incoming.setId(null);

        assertThrows(IllegalArgumentException.class, () -> appointmentService.saveAppointment(incoming));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void saveAppointment_rejectsPastDate() {
        final AppointmentDomain incoming = validAppointment();
        incoming.setAppointmentDate(LocalDate.now().minusDays(1));
        givenValidReferences(incoming);

        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.saveAppointment(incoming));
        assertTrue(error.getMessage().contains("pasado"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void saveAppointment_rejectsUnknownDoctor() {
        final AppointmentDomain incoming = validAppointment();
        givenValidReferences(incoming);
        when(doctorRepository.existsById(incoming.getDoctorId())).thenReturn(false);

        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.saveAppointment(incoming));
        assertTrue(error.getMessage().contains("medico"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void saveAppointment_rejectsPetThatBelongsToAnotherTutor() {
        final AppointmentDomain incoming = validAppointment();
        givenValidReferences(incoming);
        final PetReferenceDomain otherTutorsPet = mock(PetReferenceDomain.class);
        when(otherTutorsPet.getTutorId()).thenReturn(UUID.randomUUID());
        when(petRepository.findById(incoming.getPetId())).thenReturn(Optional.of(otherTutorsPet));

        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.saveAppointment(incoming));
        assertTrue(error.getMessage().contains("no pertenece"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void saveAppointment_rejectsInactiveAppointmentType() {
        final AppointmentDomain incoming = validAppointment();
        givenValidReferences(incoming);
        final AppointmentTypeReferenceDomain inactiveType = mock(AppointmentTypeReferenceDomain.class);
        when(inactiveType.getActive()).thenReturn(false);
        when(appointmentTypeRepository.findById(incoming.getAppointmentTypeId()))
                .thenReturn(Optional.of(inactiveType));

        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.saveAppointment(incoming));
        assertTrue(error.getMessage().contains("no esta activo"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void saveAppointment_rejectsDoctorScheduleConflict() {
        final AppointmentDomain incoming = validAppointment();
        givenValidReferences(incoming);
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                incoming.getDoctorId(), incoming.getAppointmentDate(), incoming.getAppointmentTime(), "CANCELLED"))
                .thenReturn(true);

        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.saveAppointment(incoming));
        assertTrue(error.getMessage().contains("ya tiene una cita"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void saveAppointment_cancelledAppointmentDoesNotCompeteForTheSchedule() {
        // El horario aparece "ocupado", pero una cita que llega CANCELADA no ocupa nada
        final AppointmentDomain incoming = validAppointment();
        incoming.setStatus("CANCELLED");
        incoming.setCancellationReason("El tutor no puede asistir");
        givenValidReferences(incoming);
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                any(), any(), any(), any()))
                .thenReturn(true);

        appointmentService.saveAppointment(incoming);

        verify(appointmentRepository).save(any(AppointmentDomain.class));
    }

    @Test
    void saveAppointment_rejectsCancelledWithoutReason() {
        final AppointmentDomain incoming = validAppointment();
        incoming.setStatus("CANCELLED");
        incoming.setCancellationReason("   ");
        givenValidReferences(incoming);

        assertThrows(IllegalArgumentException.class, () -> appointmentService.saveAppointment(incoming));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void saveAppointment_rejectsReasonWhenNotCancelled() {
        final AppointmentDomain incoming = validAppointment();
        incoming.setCancellationReason("El tutor no puede asistir");
        givenValidReferences(incoming);

        assertThrows(IllegalArgumentException.class, () -> appointmentService.saveAppointment(incoming));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void saveAppointment_rejectsInvalidStatus() {
        final AppointmentDomain incoming = validAppointment();
        incoming.setStatus("ARCHIVED");
        givenValidReferences(incoming);

        assertThrows(IllegalArgumentException.class, () -> appointmentService.saveAppointment(incoming));
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
        final AppointmentDomain existing = cancelledAppointment();
        when(appointmentRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

        appointmentService.changeAppointmentStatus(existing.getId(), "CONFIRMED", null);

        assertEquals("CONFIRMED", existing.getStatus());
        assertNull(existing.getCancellationReason());
    }

    @Test
    void changeAppointmentStatus_reactivatingChecksTheDoctorScheduleAgain() {
        // Mientras estuvo cancelada otra cita pudo tomar su horario
        final AppointmentDomain existing = cancelledAppointment();
        when(appointmentRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndIdNotAndStatusNot(
                existing.getDoctorId(), existing.getAppointmentDate(), existing.getAppointmentTime(),
                existing.getId(), "CANCELLED"))
                .thenReturn(true);

        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.changeAppointmentStatus(existing.getId(), "CONFIRMED", null));

        assertTrue(error.getMessage().contains("ya tiene una cita"));
        verify(appointmentRepository, never()).save(any());
        assertEquals("CANCELLED", existing.getStatus());
    }

    @Test
    void changeAppointmentStatus_cancellingDoesNotCheckTheSchedule() {
        final UUID id = UUID.randomUUID();
        final AppointmentDomain existing = new AppointmentDomain();
        existing.setId(id);
        existing.setStatus("CONFIRMED");
        when(appointmentRepository.findById(id)).thenReturn(Optional.of(existing));

        appointmentService.changeAppointmentStatus(id, "CANCELLED", "Motivo");

        verify(appointmentRepository, never())
                .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndIdNotAndStatusNot(
                        any(), any(), any(), any(), any());
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

    private AppointmentDomain validAppointment() {
        final AppointmentDomain appointment = new AppointmentDomain();
        appointment.setId(UUID.randomUUID());
        appointment.setTutorId(UUID.randomUUID());
        appointment.setPetId(UUID.randomUUID());
        appointment.setDoctorId(UUID.randomUUID());
        appointment.setAppointmentTypeId(UUID.randomUUID());
        appointment.setAppointmentDate(LocalDate.now().plusDays(3));
        appointment.setAppointmentTime(LocalTime.of(10, 30));
        appointment.setStatus(" pending ");
        appointment.setCancellationReason(null);
        return appointment;
    }

    private AppointmentDomain cancelledAppointment() {
        final AppointmentDomain appointment = new AppointmentDomain();
        appointment.setId(UUID.randomUUID());
        appointment.setDoctorId(UUID.randomUUID());
        appointment.setAppointmentDate(LocalDate.now().plusDays(3));
        appointment.setAppointmentTime(LocalTime.of(10, 30));
        appointment.setStatus("CANCELLED");
        appointment.setCancellationReason("Motivo anterior");
        return appointment;
    }

    // Deja todas las referencias (medico, tutor, mascota del tutor, tipo activo) en estado valido.
    // El horario del medico queda libre (los metodos "exists" de los mocks devuelven false por defecto).
    private void givenValidReferences(final AppointmentDomain appointment) {
        when(doctorRepository.existsById(appointment.getDoctorId())).thenReturn(true);
        when(tutorRepository.existsById(appointment.getTutorId())).thenReturn(true);

        final PetReferenceDomain pet = mock(PetReferenceDomain.class);
        when(pet.getTutorId()).thenReturn(appointment.getTutorId());
        when(petRepository.findById(appointment.getPetId())).thenReturn(Optional.of(pet));

        final AppointmentTypeReferenceDomain type = mock(AppointmentTypeReferenceDomain.class);
        when(type.getActive()).thenReturn(true);
        when(appointmentTypeRepository.findById(appointment.getAppointmentTypeId())).thenReturn(Optional.of(type));
    }
}
