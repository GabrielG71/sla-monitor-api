package com.slamonitor.ingestor.application.usecase;

import com.slamonitor.ingestor.domain.model.Endpoint;
import com.slamonitor.ingestor.domain.port.CheckResultPublisher;
import com.slamonitor.ingestor.domain.port.HttpPoller;
import com.slamonitor.ingestor.domain.port.PollLockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PollEndpointUseCase {

    private static final Logger log = LoggerFactory.getLogger(PollEndpointUseCase.class);

    private final PollLockRepository lockRepository;
    private final HttpPoller poller;
    private final CheckResultPublisher publisher;

    public PollEndpointUseCase(PollLockRepository lockRepository,
                               HttpPoller poller,
                               CheckResultPublisher publisher) {
        this.lockRepository = lockRepository;
        this.poller = poller;
        this.publisher = publisher;
    }

    public void execute(Endpoint endpoint) {
        if (!lockRepository.tryAcquire(endpoint.getId(), endpoint.getIntervalSecs())) {
            log.debug("Poll skipped for endpoint {} — lock held", endpoint.getId());
            return;
        }
        var result = poller.poll(endpoint);
        publisher.publish(result);
        log.debug("Poll completed for endpoint {}: success={} latency={}ms",
                endpoint.getId(), result.success(), result.latencyMs());
    }
}
