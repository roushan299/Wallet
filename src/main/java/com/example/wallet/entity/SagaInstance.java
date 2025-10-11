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
    private SagaStatus sagaStatus = SagaStatus.STARTED;

    @Type(JsonType.class)
    @Column(name = "context", columnDefinition = "json")
    private String context;

    @Column(name = "current_step", nullable = false)
    private String currentStep;

}
