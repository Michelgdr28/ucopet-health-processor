package com.uco.ucopet.health.messaging.appointment;

import com.uco.ucopet.health.domain.appointment.AppointmentDomain;
import com.uco.ucopet.health.util.gson.MapperJsonObject;
import com.uco.ucopet.health.util.gson.MapperJsonObjectJackson;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprueba que el procesador entiende directamente en AppointmentDomain el JSON que publica
 * la API (que tambien publica un AppointmentDomain), incluidos la fecha y la hora.
 */
class AppointmentMessageJsonTest {

    private final MapperJsonObject mapper = new MapperJsonObjectJackson();

    @Test
    void parsesTheJsonPublishedByTheApi() {
        final String id = "5f0c6a8e-3d52-4a53-9b3d-0c4f6a1b2c3d";
        final String json = """
                {"id":"%s","tutorId":"11111111-1111-1111-1111-111111111111",
                 "petId":"22222222-2222-2222-2222-222222222222",
                 "doctorId":"33333333-3333-3333-3333-333333333333",
                 "appointmentTypeId":"44444444-4444-4444-4444-444444444444",
                 "appointmentDate":"2030-01-15","appointmentTime":"10:30:00",
                 "status":"PENDING","cancellationReason":null}
                """.formatted(id);

        final Optional<AppointmentDomain> parsed = mapper.execute(json, AppointmentDomain.class);

        assertTrue(parsed.isPresent(), "el procesador no pudo leer el JSON de la API");
        assertEquals(UUID.fromString(id), parsed.get().getId());
        assertEquals(LocalDate.of(2030, 1, 15), parsed.get().getAppointmentDate());
        assertEquals(LocalTime.of(10, 30), parsed.get().getAppointmentTime());
        assertEquals("PENDING", parsed.get().getStatus());
    }

    @Test
    void parsesTheStatusChangeMessage() {
        // El cambio de estado solo trae id, estado y justificacion: lo demas queda en null
        final String id = "5f0c6a8e-3d52-4a53-9b3d-0c4f6a1b2c3d";
        final String json = """
                {"id":"%s","tutorId":null,"petId":null,"doctorId":null,"appointmentTypeId":null,
                 "appointmentDate":null,"appointmentTime":null,
                 "status":"CANCELLED","cancellationReason":"El tutor no puede asistir"}
                """.formatted(id);

        final Optional<AppointmentDomain> parsed = mapper.execute(json, AppointmentDomain.class);

        assertTrue(parsed.isPresent());
        assertEquals(UUID.fromString(id), parsed.get().getId());
        assertEquals("CANCELLED", parsed.get().getStatus());
        assertEquals("El tutor no puede asistir", parsed.get().getCancellationReason());
        assertNull(parsed.get().getDoctorId());
    }

    @Test
    void roundTripKeepsDateAndTime() {
        final AppointmentDomain original = new AppointmentDomain();
        original.setId(UUID.randomUUID());
        original.setAppointmentDate(LocalDate.of(2030, 6, 1));
        original.setAppointmentTime(LocalTime.of(8, 5));
        original.setStatus("CONFIRMED");

        final Optional<String> json = mapper.execute(original);
        assertTrue(json.isPresent());

        final Optional<AppointmentDomain> back = mapper.execute(json.get(), AppointmentDomain.class);
        assertTrue(back.isPresent());
        assertEquals(original.getId(), back.get().getId());
        assertEquals(original.getAppointmentDate(), back.get().getAppointmentDate());
        assertEquals(original.getAppointmentTime(), back.get().getAppointmentTime());
        assertEquals("CONFIRMED", back.get().getStatus());
    }

    @Test
    void garbageIsNotParsed() {
        assertTrue(mapper.execute("esto no es json", AppointmentDomain.class).isEmpty());
    }
}
