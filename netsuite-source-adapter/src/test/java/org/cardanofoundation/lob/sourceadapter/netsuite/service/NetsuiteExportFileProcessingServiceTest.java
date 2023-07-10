package org.cardanofoundation.lob.sourceadapter.netsuite.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Paths;

@SpringBootTest
public class NetsuiteExportFileProcessingServiceTest {

    private static final String TEST_CSV_FILENAME = "CustomTransactionDetail663.csv";

    @Autowired
    private NetsuiteExportFileProcessingService netsuiteExportFileProcessingService;

    @Test
    void testCsvFileImport() {
        netsuiteExportFileProcessingService.processNetsuiteExportFiles(Paths.get("src", "test", "resources", TEST_CSV_FILENAME));
    }
}
