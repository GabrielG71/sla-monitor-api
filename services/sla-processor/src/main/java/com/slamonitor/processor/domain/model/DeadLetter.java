package com.slamonitor.processor.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dead_letters")
public class DeadLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String topic;

    @Column(name = "partition_n", nullable = false)
    private int partitionN;

    @Column(name = "offset_n", nullable = false)
    private long offsetN;

    @Column(name = "failed_at", nullable = false, updatable = false)
    private Instant failedAt;

    @Column(name = "error_class", length = 255)
    private String errorClass;

    @Column(name = "error_msg")
    private String errorMsg;

    @Column(columnDefinition = "text")
    private String payload;

    protected DeadLetter() {}

    public DeadLetter(String topic, int partitionN, long offsetN, Instant failedAt,
                      String errorClass, String errorMsg, String payload) {
        this.topic = topic;
        this.partitionN = partitionN;
        this.offsetN = offsetN;
        this.failedAt = failedAt;
        this.errorClass = errorClass;
        this.errorMsg = errorMsg;
        this.payload = payload;
    }

    public UUID getId()         { return id; }
    public String getTopic()    { return topic; }
    public int getPartitionN()  { return partitionN; }
    public long getOffsetN()    { return offsetN; }
    public Instant getFailedAt(){ return failedAt; }
    public String getErrorClass(){ return errorClass; }
    public String getErrorMsg() { return errorMsg; }
    public String getPayload()  { return payload; }
}
