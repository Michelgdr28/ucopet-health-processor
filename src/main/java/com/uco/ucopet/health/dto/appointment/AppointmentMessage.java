package com.uco.ucopet.health.dto.appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Cuerpo JSON de los mensajes de citas que publica la API (es el AppointmentDTO de ucopetapi).
 * Tiene exactamente los mismos campos que ese DTO para que la deserializacion nunca falle por
 * propiedades desconocidas (los nombres de tutor, mascota, etc. viajan pero aqui no se usan).
 */
public class AppointmentMessage {

    private UUID id;
    private UUID tutorId;
    private String tutorName;
    private UUID petId;
    private String petName;
    private UUID doctorId;
    private String doctorName;
    private UUID appointmentTypeId;
    private String appointmentTypeName;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;
    private String cancellationReason;

    public AppointmentMessage() {
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

    public String getTutorName() {
        return tutorName;
    }

    public void setTutorName(final String tutorName) {
        this.tutorName = tutorName;
    }

    public UUID getPetId() {
        return petId;
    }

    public void setPetId(final UUID petId) {
        this.petId = petId;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(final String petName) {
        this.petName = petName;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(final UUID doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(final String doctorName) {
        this.doctorName = doctorName;
    }

    public UUID getAppointmentTypeId() {
        return appointmentTypeId;
    }

    public void setAppointmentTypeId(final UUID appointmentTypeId) {
        this.appointmentTypeId = appointmentTypeId;
    }

    public String getAppointmentTypeName() {
        return appointmentTypeName;
    }

    public void setAppointmentTypeName(final String appointmentTypeName) {
        this.appointmentTypeName = appointmentTypeName;
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
