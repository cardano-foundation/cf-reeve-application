package org.cardanofoundation.lob.sourceadapter.netsuite.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.lob.sourceadapter.netsuite.configuration.DirectoryWatcherConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;

@Component
@EnableAsync
@Log4j2
public class DirectoryWatcherService {

    @Autowired
    private DirectoryWatcherConfig directoryWatcherConfig;

    @Autowired
    private NetsuiteExportFileProcessingService fileProcessingService;

    private WatchService watchService;

    private WatchKey watchKey;

    private final PathMatcher csvFilePathMatcher = FileSystems.getDefault().getPathMatcher("glob:*.csv");

    @PostConstruct
    public void init() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        watchKey = Paths.get(directoryWatcherConfig.getInboundFilesRootPath()).register(
                watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
    }

    @Async
    @Scheduled(fixedDelay = 5000L)
    public void pollFileEvents() throws InterruptedException {
        for (final WatchEvent<?> event : watchKey.pollEvents()) {
            if (event.context() != null && csvFilePathMatcher.matches((Path)event.context())) {
                log.info("Processing file " + event.context());
                fileProcessingService.processNetsuiteExportFiles(Path.of(directoryWatcherConfig.getInboundFilesRootPath(), ((Path)event.context()).toString()));
            }
        }
        watchKey.reset();
    }
}
