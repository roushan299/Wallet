package com.example.wallet.service;

import com.example.wallet.entity.SagaInstance;
import com.example.wallet.entity.SagaStep;
import com.example.wallet.enums.SagaStatus;
import com.example.wallet.enums.StepStatus;
import com.example.wallet.repository.SagaInstanceRepository;
import com.example.wallet.repository.SagaStepRepository;
import com.example.wallet.service.saga.SagaContext;
import com.example.wallet.service.saga.SagaOrchestrator;
import com.example.wallet.service.saga.SagaStepInterface;
import com.example.wallet.service.saga.steps.SagaStepFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaOrchestratorImpl implements SagaOrchestrator {

    private final ObjectMapper objectMapper;
    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepFactory sagaStepFactory;
    private final SagaStepRepository sagaStepRepository;

    @Override
    @Transactional
    public Long startSaga(SagaContext context) {
        log.info("SagaContext {} started", context);
        try {
            String contextJson = objectMapper.writeValueAsString(context);

            SagaInstance sagaInstance = SagaInstance.builder()
                    .context(contextJson)
                    .status(SagaStatus.STARTED)
                    .build();
            sagaInstanceRepository.save(sagaInstance);
            log.info("Started saga with id {}", sagaInstance);
            return sagaInstance.getId();
        } catch (Exception e) {
            log.error("Error starting saga instance", e);
            throw new RuntimeException("Error starting saga instance", e);
        }
    }

    @Override
    @Transactional
    public boolean executeStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("SagaInstance not found"));

        SagaStepInterface step = sagaStepFactory.getSagaStep(stepName);

        if(step == null) {
            log.error("Step {} not found", stepName);
            throw new RuntimeException("Step " + stepName + " not found");
        }

        SagaStep sagaStepDB = sagaStepRepository.findBySagaInstanceIdAndStatus(sagaInstanceId, StepStatus.PENDING)
                .stream().filter( s -> s.getStepName().equals(stepName))
                .findFirst()
                .orElse(SagaStep.builder().stepName(stepName).status(StepStatus.PENDING).build());

        if(sagaStepDB.getId() == null) {
            sagaStepDB = sagaStepRepository.save(sagaStepDB);
        }
        try {
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);

            sagaStepDB.setStatus(StepStatus.RUNNING);
            sagaStepRepository.save(sagaStepDB); // updating the status to running in db

            boolean success = step.execute(sagaContext);

            if(success){
                sagaStepDB.setStatus(StepStatus.COMPLETED);
                sagaStepRepository.save(sagaStepDB);

                sagaInstance.setCurrentStep(stepName);
                sagaInstance.setStatus(SagaStatus.RUNNING);
                sagaInstanceRepository.save(sagaInstance);

                log.error("Step {} executed successfully", stepName);
                return true;
            }else {
                sagaStepDB.setStatus(StepStatus.FAILED);
                sagaStepRepository.save(sagaStepDB);
                log.error("Step {} failed", stepName);
                return false;
            }

        } catch (Exception e) {
            sagaStepDB.setStatus(StepStatus.FAILED);
            sagaStepRepository.save(sagaStepDB);
            log.error("Failed to execute step {}", stepName, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean compensateStep(Long sagaInstanceId, String stepName) {
        // 1. Fetch the saga instance from db using saga instance id

        // 2. Fetch the saga step from db using saga instance id and step name

        // 3. Take the context from saga instance and call the compensate method

        // 4. Update appropriate status in the saga step
        return false;
    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return null;
    }

    @Override
    public void compensateSaga(Long sagaInstanceId) {

    }

    @Override
    public void failSaga(Long sagaInstanceId) {

    }

}
