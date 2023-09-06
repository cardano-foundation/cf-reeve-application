package org.cardanofoundation.lob.sourceadapter.netsuite.controller;

import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.lob.common.model.rest.LedgerEventRegistrationRequest;
import org.cardanofoundation.lob.sourceadapter.netsuite.service.ClientAPI;
import org.cardanofoundation.lob.sourceadapter.netsuite.service.NetsuiteExportFileProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Log4j2
@RequestMapping("/netsuite")
public class NetsuiteController {
    @Autowired
    ClientAPI clientAPI;

    @Autowired
    NetsuiteExportFileProcessingService netsuiteExportFileProcessingService;

    @GetMapping("/client")
    public @ResponseBody String callClient(){
        try {
            String data = clientAPI.makeCall();
            LedgerEventRegistrationRequest result = netsuiteExportFileProcessingService.processNetsuiteExportJson(data);
            return result.toString();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
