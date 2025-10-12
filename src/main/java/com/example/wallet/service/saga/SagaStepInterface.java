package com.example.wallet.service.saga;

public interface SagaStepInterface {

    public boolean execute(SagaContext context);

    public boolean compensate(SagaContext context);

    public String getStepName();

}

