package com.tce.domain.repository;

import com.tce.domain.model.Contract;
import com.tce.domain.model.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {
    @Query("SELECT c FROM Contract c WHERE " +
           "(c.status = 'PENDING' AND c.effectiveDate <= :now) OR " +
           "(c.status = 'ELIGIBLE' AND c.effectiveDate <= :now) OR " +
           "(c.status = 'FAILED' AND c.nextAttemptAt IS NOT NULL AND c.nextAttemptAt <= :now)")
    List<Contract> findExecutableContracts(@Param("now") Instant now);
}
