package com.tce.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status;

    @Column(nullable = false)
    private Instant effectiveDate;

    private Instant expirationDate;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant creationDate;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant lastModifiedDate;

    @Version
    private Integer version;

    @Builder.Default
    private Integer retryCount = 0;

    private Instant nextAttemptAt;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Rule> rules = new ArrayList<>();

    public void addRule(Rule rule) {
        rules.add(rule);
        rule.setContract(this);
    }

    public void removeRule(Rule rule) {
        rules.remove(rule);
        rule.setContract(null);
    }
}
