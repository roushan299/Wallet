package com.example.wallet.service.saga;

public interface SagaStep {

    public boolean execute(SagaContext context);

    public boolean compensate(SagaContext context);

    public String getStepName();

}

