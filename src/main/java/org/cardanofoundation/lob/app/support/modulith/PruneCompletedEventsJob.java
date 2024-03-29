package org.cardanofoundation.lob.app.support.modulith;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.core.EventPublicationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PruneCompletedEventsJob {

    //private final CompletedEventPublications completedEventPublications;
    private final EventPublicationRepository eventPublicationRepository;

//    @Value("${lob.spring.modulith.completed-events-pruning-interval:PT1H}")
//    private Duration pruneInterval;

    @PostConstruct
    public void init() {
        log.info("Starting PruneCompletedEventsJob...");
    }

    //@Scheduled(fixedRateString = "${lob.spring.modulith.completed-events-pruning-rate:PT5M}")
    @Scheduled(cron = "0 0 3 * * ?")
    public void execute() {
        log.info("Pruning completed events...");

        //completedEventPublications.deletePublications(eventPublication -> eventPublication.getCompletionDate().isPresent());
        //completedEventPublications.deletePublicationsOlderThan(Duration.ofHours(1));
        eventPublicationRepository.deleteCompletedPublications();

        log.info("Completed events pruned.");
    }

}
