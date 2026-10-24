package com.uco.ucopet.health.domain.appointment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Cita veterinaria. Es la misma tabla "appointments" que crea la API (ucopetapi):
 * aqui el ddl-auto es "none", asi que los nombres de columna deben coincidir con los de la API.
 */
@Entity
@Table(name = "appointments")
public class AppointmentDomain {

    @Id
    private UUID id;

    @Column(name = "tutor_id")
    private UUID tutorId;

    @Column(name = "pet_id")
    private UUID petId;

    @Column(name = "doctor_id")
    private UUID doctorId;

    @Column(name = "appointment_type_id")
    private UUID appointmentTypeId;

    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    @Column(name = "appointment_time")
    private LocalTime appointmentTime;

    @Column(name = "status")
    private String status;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    public AppointmentDomain() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public UUID getTutorId() {
        return tutorId;
    }

    public void setTutorId(final UUID tutorId) {
        this.tutorId = tutorId;
    }

    public UUID getPetId() {
        return petId;
    }

    public void setPetId(final UUID petId) {
        this.petId = petId;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(final UUID doctorId) {
        this.doctorId = doctorId;
    }

    public UUID getAppointmentTypeId() {
        return appointmentTypeId;
    }

    public void setAppointmentTypeId(final UUID appointmentTypeId) {
        this.appointmentTypeId = appointmentTypeId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(final LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(final LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(final String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
