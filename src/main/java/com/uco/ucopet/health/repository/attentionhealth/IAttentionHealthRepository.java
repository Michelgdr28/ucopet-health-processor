package com.uco.ucopet.health.repository.attentionhealth;

import com.uco.ucopet.health.domain.attentionhealth.AttentionHealthDomain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAttentionHealthRepository extends JpaRepository<AttentionHealthDomain, Long> {
}
