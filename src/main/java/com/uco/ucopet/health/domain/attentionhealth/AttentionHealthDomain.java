package com.uco.ucopet.health.domain.attentionhealth;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "attention_health")
public class AttentionHealthDomain {

    @Id
    private Long attentionId;
    @Column(name = "pet_id", nullable = false)
    private UUID petId;
    @Column(name = "tutor_id", nullable = false)
    private UUID tutorId;
    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;
    @Column(name = "appointment_id", unique = true)
    private UUID appointmentId;
    @Column(name = "headquarter_id", nullable = false)
    private UUID headquarterId;
    @Column(name = "attention_date", nullable = false)
    private LocalDateTime attentionDate;
    @Column(name = "attention_type", nullable = false, length = 80)
    private String attentionType;
    @Column(length = 500)
    private String reason;
    @Column(name = "clinical_assessment", length = 2000)
    private String clinicalAssessment;
    @Column(length = 1000)
    private String observations;
    @Column(length = 1000)
    private String diagnosis;
    @Column(nullable = false, length = 30)
    private String status;
    private Double temperature;
    @Column(name = "heart_rate")
    private Integer heartRate;
    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;
    @Column(name = "systolic_pressure")
    private Integer systolicPressure;
    @Column(name = "diastolic_pressure")
    private Integer diastolicPressure;
    private Double weight;
    @Column(name = "body_condition_score")
    private Integer bodyConditionScore;
    @Column(name = "measurement_date")
    private LocalDateTime measurementDate;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "attention_health_products", joinColumns = @JoinColumn(name = "attention_id"))
    private List<AttentionHealthProductDomain> products = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "attention_health_procedures", joinColumns = @JoinColumn(name = "attention_id"))
    @Column(name = "procedure_id", nullable = false)
    private List<UUID> procedureIds = new ArrayList<>();

    public Long getAttentionId() { return attentionId; }
    public UUID getPetId() { return petId; }
    public UUID getTutorId() { return tutorId; }
    public UUID getDoctorId() { return doctorId; }
    public UUID getAppointmentId() { return appointmentId; }
    public UUID getHeadquarterId() { return headquarterId; }
    public LocalDateTime getAttentionDate() { return attentionDate; }
    public String getAttentionType() { return attentionType; }
    public String getReason() { return reason; }
    public String getClinicalAssessment() { return clinicalAssessment; }
    public String getObservations() { return observations; }
    public String getDiagnosis() { return diagnosis; }
    public String getStatus() { return status; }
    public Double getTemperature() { return temperature; }
    public Integer getHeartRate() { return heartRate; }
    public Integer getRespiratoryRate() { return respiratoryRate; }
    public Integer getSystolicPressure() { return systolicPressure; }
    public Integer getDiastolicPressure() { return diastolicPressure; }
    public Double getWeight() { return weight; }
    public Integer getBodyConditionScore() { return bodyConditionScore; }
    public LocalDateTime getMeasurementDate() { return measurementDate; }
    public List<AttentionHealthProductDomain> getProducts() { return new ArrayList<>(products); }
    public List<UUID> getProcedureIds() { return new ArrayList<>(procedureIds); }

    public void setAttentionId(Long value) { attentionId = value; }
    public void setPetId(UUID value) { petId = value; }
    public void setTutorId(UUID value) { tutorId = value; }
    public void setDoctorId(UUID value) { doctorId = value; }
    public void setAppointmentId(UUID value) { appointmentId = value; }
    public void setHeadquarterId(UUID value) { headquarterId = value; }
    public void setAttentionDate(LocalDateTime value) { attentionDate = value; }
    public void setAttentionType(String value) { attentionType = value; }
    public void setReason(String value) { reason = value; }
    public void setClinicalAssessment(String value) { clinicalAssessment = value; }
    public void setObservations(String value) { observations = value; }
    public void setDiagnosis(String value) { diagnosis = value; }
    public void setStatus(String value) { status = value; }
    public void setTemperature(Double value) { temperature = value; }
    public void setHeartRate(Integer value) { heartRate = value; }
    public void setRespiratoryRate(Integer value) { respiratoryRate = value; }
    public void setSystolicPressure(Integer value) { systolicPressure = value; }
    public void setDiastolicPressure(Integer value) { diastolicPressure = value; }
    public void setWeight(Double value) { weight = value; }
    public void setBodyConditionScore(Integer value) { bodyConditionScore = value; }
    public void setMeasurementDate(LocalDateTime value) { measurementDate = value; }
    public void setProducts(List<AttentionHealthProductDomain> value) { products = value == null ? new ArrayList<>() : new ArrayList<>(value); }
    public void setProcedureIds(List<UUID> value) { procedureIds = value == null ? new ArrayList<>() : new ArrayList<>(value); }
}
