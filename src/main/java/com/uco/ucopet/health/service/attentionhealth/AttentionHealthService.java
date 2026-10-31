package com.uco.ucopet.health.service.attentionhealth;

import com.uco.ucopet.health.domain.attentionhealth.AttentionHealthDomain;
import com.uco.ucopet.health.repository.attentionhealth.IAttentionHealthRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttentionHealthService {

    private final IAttentionHealthRepository repository;

    public AttentionHealthService(final IAttentionHealthRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void saveAttention(final AttentionHealthDomain incoming) {
        if (incoming == null || incoming.getAttentionId() == null) {
            throw new IllegalArgumentException("El mensaje no trae el id de la atención.");
        }
        AttentionHealthDomain target = repository.findById(incoming.getAttentionId())
                .orElseGet(AttentionHealthDomain::new);
        copyData(target, incoming);
        repository.save(target);
    }

    @Transactional
    public void deactivateAttention(final Long attentionId) {
        if (attentionId == null) {
            throw new IllegalArgumentException("El mensaje no trae el id de la atención.");
        }
        AttentionHealthDomain attention = repository.findById(attentionId)
                .orElseThrow(() -> new IllegalArgumentException("No existe una atención con id: " + attentionId));
        attention.setStatus("CANCELLED");
        repository.save(attention);
    }

    private void copyData(final AttentionHealthDomain target, final AttentionHealthDomain source) {
        target.setAttentionId(source.getAttentionId());
        target.setPetId(source.getPetId());
        target.setTutorId(source.getTutorId());
        target.setDoctorId(source.getDoctorId());
        target.setAppointmentId(source.getAppointmentId());
        target.setHeadquarterId(source.getHeadquarterId());
        target.setAttentionDate(source.getAttentionDate());
        target.setAttentionType(source.getAttentionType());
        target.setReason(source.getReason());
        target.setClinicalAssessment(source.getClinicalAssessment());
        target.setObservations(source.getObservations());
        target.setDiagnosis(source.getDiagnosis());
        target.setStatus(source.getStatus());
        target.setTemperature(source.getTemperature());
        target.setHeartRate(source.getHeartRate());
        target.setRespiratoryRate(source.getRespiratoryRate());
        target.setSystolicPressure(source.getSystolicPressure());
        target.setDiastolicPressure(source.getDiastolicPressure());
        target.setWeight(source.getWeight());
        target.setBodyConditionScore(source.getBodyConditionScore());
        target.setMeasurementDate(source.getMeasurementDate());
        target.setProducts(source.getProducts());
        target.setProcedureIds(source.getProcedureIds());
    }
}
