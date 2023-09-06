package org.cardanofoundation.lob.sourceadapter.netsuite.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Data
@Log4j2
@JsonIgnoreProperties(ignoreUnknown = true)
public class LinesLedgerEvent {


    private List<BulkExportLedgerEvent> lines;
    private Boolean more;

}
