package com.slamonitor.ingestor.infrastructure.scheduling;

import com.slamonitor.ingestor.application.usecase.PollEndpointUseCase;
import com.slamonitor.ingestor.domain.port.EndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
public class PollingScheduler {

    private static final Logger log = LoggerFactory.getLogger(PollingScheduler.class);

    private final EndpointRepository endpointRepository;
    private final PollEndpointUseCase pollEndpoint;
    private final Executor pollingExecutor;

    public PollingScheduler(EndpointRepository endpointRepository,
                            PollEndpointUseCase pollEndpoint,
                            @Qualifier("pollingExecutor") Executor pollingExecutor) {
        this.endpointRepository = endpointRepository;
        this.pollEndpoint = pollEndpoint;
        this.pollingExecutor = pollingExecutor;
    }

    @Scheduled(fixedDelayString = "${polling.scheduler-tick-ms:10000}")
    public void tick() {
        var endpoints = endpointRepository.findAllActive();
        log.debug("Polling tick — {} active endpoints", endpoints.size());
        for (var endpoint : endpoints) {
            pollingExecutor.execute(() -> pollEndpoint.execute(endpoint));
        }
    }
}
