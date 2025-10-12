package com.example.wallet.repository;

import com.example.wallet.entity.SagaStep;
import com.example.wallet.enums.StepStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SagaStepRepository extends CrudRepository<SagaStep, Long> {

    List<SagaStep> findBySagaInstanceId(Long sagaInstanceId);

    List<SagaStep> findBySagaInstanceIdAndStatus(Long sagaInstanceId, StepStatus stepStatus);
    
    Optional<SagaStep> findBySagaInstanceIdAndStepNameAndStatus(Long sagaInstanceId, String stepName, StepStatus stepStatus);

    @Query("Select s from SagaStep s where s.sagaInstanceId = :sagaInstanceId and s.status = 'COMPLETED'")
    List<SagaStep> findCompletedStepsBySagaInstanceId(@Param("sagaInstanceId") Long sagaInstanceId);

    @Query("Select s from SagaStep s where s.sagaInstanceId = :sagaInstanceId and s.status in ('COMPLETED' , 'COMPENSATED')")
    List<SagaStep> findCompletedOrCompensatedStepsBySagaInstanceId(@Param("sagaInstanceId") Long sagaInstanceId);

}
