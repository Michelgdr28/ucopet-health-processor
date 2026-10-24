package com.uco.ucopet.health.repository.appointment;

import com.uco.ucopet.health.domain.appointment.AppointmentDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Repository
public interface IAppointmentRepository extends JpaRepository<AppointmentDomain, UUID> {

    // Cruce de horario al CREAR una cita
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTime(UUID doctorId,
                                                                 LocalDate appointmentDate,
                                                                 LocalTime appointmentTime);

    // Cruce de horario al ACTUALIZAR: se ignora la propia cita
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndIdNot(UUID doctorId,
                                                                         LocalDate appointmentDate,
                                                                         LocalTime appointmentTime,
                                                                         UUID id);
}
