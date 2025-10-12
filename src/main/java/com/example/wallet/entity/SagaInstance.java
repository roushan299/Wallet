package com.example.wallet.entity;

import com.example.wallet.enums.SagaStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.calcite.model.JsonType;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

@Entity
@Table(name = "saga_instance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaInstance {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SagaStatus status = SagaStatus.STARTED;

    @Type(JsonType.class)
    @Column(name = "context", columnDefinition = "json")
    private String context;

    @Column(name = "current_step", nullable = true)
    private String currentStep;

    public void markAsStarted() {
        this.status = SagaStatus.STARTED;
    }

    public void markAsRunning() {
        this.status = SagaStatus.RUNNING;
    }

    public void markAsCompleted() {
        this.status = SagaStatus.COMPLETED;
    }

    public void markAsFailed() {
        this.status = SagaStatus.FAILED;
    }

    public void markAsCompensating() {
        this.status = SagaStatus.COMPENSATING;
    }

    public void markAsCompensated() {
        this.status = SagaStatus.COMPENSATED;
    }

}
