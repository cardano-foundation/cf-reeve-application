package org.cardanofoundation.lob.sourceadapter.netsuite.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Configuration
@EnableScheduling
public class DirectoryWatcherConfig {

    @Value("${lob.netsuite.inboundFiles.root}")
    private String inboundFilesRoot;

    public String getInboundFilesRootPath() {
        return Path.of(inboundFilesRoot, "in").toAbsolutePath().toString();
    }

    public String getProcessedFilesRootPath() {
        return Path.of(inboundFilesRoot, "processed").toAbsolutePath().toString();
    }

    public String getFailedFilesRootPath() {
        return Path.of(inboundFilesRoot, "failed").toAbsolutePath().toString();
    }

    @Bean(name="inboundReadDirectory")
    public File inboundReadDirectory() throws IOException  {
        return makeDirectory(getInboundFilesRootPath());
    }

    @Bean(name="inboundProcessedDirectory")
    public File inboundProcessedDirectory() throws IOException  {
        return makeDirectory(getProcessedFilesRootPath());
    }

    @Bean(name="inboundFailedDirectory")
    public File inboundFailedDirectory() throws IOException {
        return makeDirectory(getFailedFilesRootPath());
    }

    private File makeDirectory(final String path) throws IOException {
        final File directory = new File(path);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                return directory;
            } else {
                throw new IOException(String.format("Could not create directory %s.", path));
            }
        } else {
            return directory;
        }
    }
}
