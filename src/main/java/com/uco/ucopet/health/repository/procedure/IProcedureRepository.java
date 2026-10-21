package com.uco.ucopet.health.repository.procedure;

import com.uco.ucopet.health.domain.procedure.ProcedureDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IProcedureRepository extends JpaRepository<ProcedureDomain, UUID> {

    Optional<ProcedureDomain> findByCode(String code);

    @Query("select p.code from ProcedureDomain p where p.code is not null")
    List<String> findAllCodes();
}