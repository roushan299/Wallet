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
import java.util.List;

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

//        SagaStep sagaStepDB = sagaStepRepository.findBySagaInstanceIdAndStatus(sagaInstanceId, StepStatus.PENDING)
//                .stream().filter( s -> s.getStepName().equals(stepName))
//                .findFirst()
//                .orElse(SagaStep.builder().stepName(stepName).status(StepStatus.PENDING).build());

        SagaStep sagaStepDB = sagaStepRepository.findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, StepStatus.PENDING)
                .orElse(SagaStep.builder().stepName(stepName).status(StepStatus.PENDING).build());

        if(sagaStepDB.getId() == null) {
            sagaStepDB = sagaStepRepository.save(sagaStepDB);
        }
        try {
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);

            sagaStepDB.markAsRunning();
            sagaStepRepository.save(sagaStepDB); // updating the status to running in db

            boolean success = step.execute(sagaContext);

            if(success){
                sagaStepDB.markAsCompleted();
                sagaStepRepository.save(sagaStepDB);

                sagaInstance.setCurrentStep(stepName);
                sagaInstance.markAsRunning();
                sagaInstanceRepository.save(sagaInstance);

                log.error("Step {} executed successfully", stepName);
                return true;
            }else {
                sagaStepDB.markAsFailed();
                sagaStepRepository.save(sagaStepDB);
                log.error("Step {} failed", stepName);
                return false;
            }

        } catch (Exception e) {
            sagaStepDB.markAsFailed();
            sagaStepRepository.save(sagaStepDB);
            log.error("Failed to execute step {}", stepName, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean compensateStep(Long sagaInstanceId, String stepName) {
        // 1. Fetch the saga instance from db using saga instance id
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga instance not found"));

        // 2. Fetch the saga step from db using saga instance id and step name
        SagaStepInterface step = sagaStepFactory.getSagaStep(stepName);
        if(step == null) {
            log.error("Step step not found for step name {}", stepName);
            throw new RuntimeException("Step " + stepName + " not found");
        }
        SagaStep sagaStepDB = sagaStepRepository.findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, StepStatus.COMPLETED)
                .orElse(null);

        if(sagaStepDB.getId() == null) {
            log.info("Step {} not found in the db for saga instance {}, so it is alreadu compesated or executed", stepName, sagaInstanceId);
            return true;
        }

        // 3. Take the context from saga instance and call the compensate method
        try{
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStepDB.markAsCompensating();
            sagaStepRepository.save(sagaStepDB); // updating status to running in db

            boolean success = step.compensate(sagaContext);

            // 4. Update appropriate status in the saga step
            if(success){
                sagaStepDB.markAsCompensated();
                sagaStepRepository.save(sagaStepDB); // updating the status to completed in db
                log.info("Step {} compensated successfully", stepName);
                return true;
            }else {
                sagaStepDB.markAsFailed();
                sagaStepRepository.save(sagaStepDB); // updating the status to failed in db
                log.error("Step {} failed", stepName);
                return false;
            }

        } catch (Exception e){
            sagaStepDB.markAsFailed();
            sagaStepRepository.save(sagaStepDB); // updating the status to failed in db
            log.error("Failed to execute step {}", stepName);
            return false;
        }

    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("SagaInstance not found"));
    }

    @Override
    @Transactional
    public void compensateSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga instance not found"));

        // mark the saga instance status as compensating
        sagaInstance.markAsCompensating();
        sagaInstanceRepository.save(sagaInstance);

        //get all the completed steps of a saga
        List<SagaStep> completedSagaSteps = sagaStepRepository.findCompletedStepsBySagaInstanceId(sagaInstanceId);

        boolean allCompensated = true;
        for(SagaStep sagaStep : completedSagaSteps) {
            boolean compensated = this.compensateStep(sagaInstanceId, sagaStep.getStepName());
            if(!compensated) {
                allCompensated = false;
            }
        }

        if(allCompensated){
            sagaInstance.markAsCompensated();
            sagaInstanceRepository.save(sagaInstance);
            log.info("Saga instance {} compensated", sagaInstance);
        }else{
            log.info("Saga instance {} compensation failed", sagaInstance);
        }
    }

    @Override
    @Transactional
    public void failSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga instance not found"));
        sagaInstance.markAsFailed();
        sagaInstanceRepository.save(sagaInstance);
        compensateSaga(sagaInstanceId);
        log.info("Saga {} failed", sagaInstanceId);
    }

    @Override
    @Transactional
    public void completeSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga instance not found"));
        sagaInstance.markAsCompleted();
        sagaInstanceRepository.save(sagaInstance);
    }

}
