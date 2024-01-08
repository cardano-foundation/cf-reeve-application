package org.cardanofoundation.lob.app.netsuite.resource;

import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.netsuite.NetSuiteService;
import org.cardanofoundation.lob.app.netsuite.domain.entity.NetSuiteIngestion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import static org.cardanofoundation.lob.app.netsuite.util.MoreCompress.decompress;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.zalando.problem.Status.NOT_FOUND;

@RestController
@RequestMapping("/api/netsuite")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "NetSuite", description = "The NetSuite API")
public class NetSuiteResource {

    private final NetSuiteService netsuiteService;

    @PostConstruct
    public void init() {
        log.info("NetSuiteResource init.");
    }

    @RequestMapping(value = "/hello", method = GET, produces = "application/json")
    @Timed(value = "resource.netsuite.hello", histogram = true)
    @Operation(summary = "hello",
            description = "hello desc",
            responses = {
                    @ApiResponse(responseCode = "200", description = "",
                            content = { @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Hello.class)) }),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<?> hello() {
        return ResponseEntity.ok().body(new Hello("hello"));
    }

    @RequestMapping(value = "/ingestion/{id}", method = GET, produces = "application/json")
    @Timed(value = "resource.netsuite.ingestion.find", histogram = true)
    @Operation(summary = "ingestion",
            description = "ingestion desc",
            responses = {
                    @ApiResponse(responseCode = "200", description = "",
                            content = { @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = NetSuiteIngestion.class)) }),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<?> findNetSuiteIngestion(@Parameter(description = "ingestion id", required = true)
                                                   @PathVariable("id") String id) {
        val r = netsuiteService.findIngestionById(id);

        if (r.isEmpty()) {
            var issue = Problem.builder()
                    .withStatus(NOT_FOUND)
                    .withTitle("Not found")
                    .withDetail("Ingestion not found")
                    .build();

            return ResponseEntity.status(issue.getStatus().getStatusCode()).body(issue);
        }

        val netSuiteIngestion = r.get();

        val ingestionPresentation = new IngestionPresentation(
                netSuiteIngestion.getIngestionBodyChecksum(),
                netSuiteIngestion.getIngestionBody(),
                decompress(netSuiteIngestion.getIngestionBody())
        );

        return ResponseEntity.ok().body(ingestionPresentation);
    }

    record Hello(String text) { }

}
