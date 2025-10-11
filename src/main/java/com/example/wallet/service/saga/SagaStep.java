package com.example.wallet.service.saga;

public interface SagaStep {

    public boolean execute(SagaContext sagaContext);

    public boolean compensate(SagaContext sagaContext);

    public String getStepName();

}

