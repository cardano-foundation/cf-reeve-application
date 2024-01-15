package org.cardanofoundation.lob.app.netsuite_adapter.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import java.util.Optional;

import static org.scribe.model.Verb.GET;

@Component
@Slf4j
@RequiredArgsConstructor
public class NetSuiteAPI {

    private final OAuthService oAuthService;

    private final ObjectMapper objectMapper;

    @Value("${lob.netsuite.client.url}")
    private String url;

    @Value("${lob.netsuite.client.realm}")
    private String realm;

    @Value("${lob.netsuite.client.token}")
    private String token;

//    @Value("${lob.netsuite.client.oauth_nonce}")
//    private String oauthNonce;

    @Value("${lob.netsuite.client.token_secret}")
    private String tokenSecret;

    public Either<Problem, Optional<String>> retrieveLatestNetsuiteTransactionLines() {
        val response = callForTransactionLinesData();

        if (response.isSuccessful()) {
            log.info("Netsuite response success...code:{}, message:{}", response.getCode(), response.getMessage());
            val body = response.getBody();

            try {
                val bodyJsonTree = objectMapper.readTree(body);
                if (bodyJsonTree.has("error")) {
                    val error = bodyJsonTree.get("error").asInt();
                    val text = bodyJsonTree.get("text").asText();

                    if (error == 105) {
                        log.warn("No data to read from NetSuite API...");

                        return Either.right(Optional.empty());
                    }

                    return Either.left(Problem.builder()
                            .withStatus(Status.valueOf(response.getCode()))
                            .withTitle("NetSuite API error")
                            .withDetail(String.format("Error code: %d, message: %s", error, text))
                            .build());
                }

                return Either.right(Optional.of(response.getBody()));
            } catch (JsonProcessingException e) {
                log.error("Error parsing JSON response from NetSuite API: {}", e.getMessage());

                return Either.left(Problem.builder()
                        .withStatus(Status.valueOf(response.getCode()))
                        .withTitle("NetSuite API error")
                        .withDetail(e.getMessage())
                        .build());
            }
        }

        return Either.left(Problem.builder()
                .withStatus(Status.valueOf(response.getCode()))
                .withTitle("NetSuite API error")
                .withDetail(response.getBody())
                .build());
    }

    private Response callForTransactionLinesData() {
        log.info("Retrieving data from NetSuite...");
        log.info("url: {}", url);

        val request = new OAuthRequest(GET, url);
        request.setRealm(realm);

        val t = new Token(token, tokenSecret);
        oAuthService.signRequest(t, request);

        return request.send();
    }

}
