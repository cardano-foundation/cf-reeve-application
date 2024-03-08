package org.cardanofoundation.lob.app.accounting_reporting_core.resource;


import lombok.RequiredArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.AccountingCoreService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
public class AccountingCoreResource {

    // RPC: Remote Procedure Call

    // GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS

    private final AccountingCoreService accountingCoreService;

}


//@RestController
//@RequestMapping("/api/netsuite")
//@Slf4j
//@RequiredArgsConstructor
//@Tag(customerCode = "NetSuite", description = "The NetSuite API")
//public class NetSuiteResource {
//
//    private final NetSuiteService netSuiteService;
//
//    @PostConstruct
//    public void init() {
//        log.info("NetSuiteResource init.");
//    }
//
//    @RequestMapping(value = "/hello", method = GET, produces = "application/json")
//    @Timed(value = "resource.netsuite.hello", histogram = true)
//    @Operation(summary = "hello",
//            description = "hello desc",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "",
//                            content = { @Content(mediaType = "application/json",
//                                    schema = @Schema(implementation = org.cardanofoundation.lob.app.netsuite_adapter.resource.NetSuiteResource.Hello.class)) }),
//                    @ApiResponse(responseCode = "500", description = "Internal server error")
//            }
//    )
//    public ResponseEntity<?> hello() {
//        return ResponseEntity.ok().body(new org.cardanofoundation.lob.app.netsuite_adapter.resource.NetSuiteResource.Hello("hello"));
//    }
//
//    @RequestMapping(value = "/ingestion/{currencyId}", method = GET, produces = "application/json")
//    @Timed(value = "resource.netsuite.ingestion.find", histogram = true)
//    @Operation(summary = "ingestion",
//            description = "ingestion desc",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "",
//                            content = { @Content(mediaType = "application/json",
//                                    schema = @Schema(implementation = NetSuiteIngestion.class)) }),
//                    @ApiResponse(responseCode = "500", description = "Internal server error")
//            }
//    )
//    public ResponseEntity<?> findNetSuiteIngestion(@Parameter(description = "ingestion currencyId", required = true)
//                                                   @PathVariable("id") String id) {
//        val r = netSuiteService.findIngestionById(id);
//
//        if (r.isEmpty()) {
//            var issue = Problem.builder()
//                    .withStatus(NOT_FOUND)
//                    .withTitle("Not found")
//                    .withDetail("Ingestion not found")
//                    .build();
//
//            return ResponseEntity.status(issue.getStatus().getStatusCode()).body(issue);
//        }
//
//        val netSuiteIngestion = r.get();
//
//        val ingestionPresentation = new IngestionPresentation(
//                netSuiteIngestion.getIngestionBodyChecksum(),
//                netSuiteIngestion.getIngestionBody(),
//                MoreCompress.decompress(netSuiteIngestion.getIngestionBody())
//        );
//
//        return ResponseEntity.ok().body(ingestionPresentation);
//    }
//
//    record Hello(String text) { }
