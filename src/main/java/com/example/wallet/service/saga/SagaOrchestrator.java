package com.example.wallet.service.saga;

import com.example.wallet.entity.SagaInstance;

public interface SagaOrchestrator {

    Long startSaga(SagaContext sagaContext);

    boolean executeStep(Long sagaInstanceId, String stepName);

    boolean compensateStep(Long sagaInstanceId, String stepName);

    SagaInstance getSagaInstance(Long sagaInstanceId);

    void compensateSaga(Long sagaInstanceId);

    void failSaga(Long sagaInstanceId);

}
