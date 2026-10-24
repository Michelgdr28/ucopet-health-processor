package com.uco.ucopet.health.repository.appointment;

import com.uco.ucopet.health.domain.appointment.AppointmentDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Repository
public interface IAppointmentRepository extends JpaRepository<AppointmentDomain, UUID> {

    // Cruce de horario al CREAR una cita. Las citas con el estado indicado (CANCELLED)
    // no cuentan: una cita cancelada libera el horario del medico.
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(UUID doctorId,
                                                                             LocalDate appointmentDate,
                                                                             LocalTime appointmentTime,
                                                                             String status);

    // Cruce de horario al ACTUALIZAR: se ignora la propia cita y las canceladas
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndIdNotAndStatusNot(UUID doctorId,
                                                                                     LocalDate appointmentDate,
                                                                                     LocalTime appointmentTime,
                                                                                     UUID id,
                                                                                     String status);
}
