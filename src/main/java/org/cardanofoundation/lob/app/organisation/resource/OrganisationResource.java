package org.cardanofoundation.lob.app.organisation.resource;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.organisation.OrganisationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import static org.zalando.problem.Status.NOT_FOUND;

@RestController
@RequestMapping("/api/organisation")
@RequiredArgsConstructor
public class OrganisationResource {

    private final OrganisationService organisationService;

    @GetMapping("/")
    public ResponseEntity<?> listAll() {
        val allOrgs = organisationService.findAll();

        if (allOrgs.isEmpty()) {
            var issue = Problem.builder()
                    .withTitle("Not found")
                    .withDetail("No organisations found")
                    .withStatus(NOT_FOUND)
                    .build();

            return ResponseEntity.status(issue.getStatus().getStatusCode()).body(issue);
        }

        return ResponseEntity.ok(allOrgs);
    }

}
