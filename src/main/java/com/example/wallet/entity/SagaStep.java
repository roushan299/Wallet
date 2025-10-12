package com.example.wallet.entity;

import com.example.wallet.enums.StepStatus;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.calcite.model.JsonType;

@Entity
@Table(name = "saga_steps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saga_instance_id", nullable = false)
    private Long sagaInstanceId;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StepStatus status;

    @Column(name = "error_message", nullable = true)
    private String errorMessage;

    @Type(JsonType.class)
    @Column(name = "step_data")
    private String stepData;

    public void markAsCompensated() {
        this.status = StepStatus.COMPENSATED;
    }

    public void markAsFailed() {
        this.status = StepStatus.FAILED;
    }

    public void markAsPending() {
        this.status = StepStatus.PENDING;
    }

    public void markAsRunning() {
        this.status = StepStatus.RUNNING;
    }

    public void markAsCompleted() {
        this.status = StepStatus.COMPLETED;
    }

    public void markAsCompensating() {
        this.status = StepStatus.COMPENSATING;
    }

}
