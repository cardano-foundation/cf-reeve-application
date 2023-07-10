package org.cardanofoundation.lob.sourceadapter.netsuite.service;

import jakarta.persistence.Lob;
import lombok.extern.log4j.Log4j2;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.DefaultApi10a;
import org.scribe.exceptions.OAuthSignatureException;
import org.scribe.model.*;
import org.scribe.oauth.OAuth10aServiceImpl;
import org.scribe.oauth.OAuthService;
import org.scribe.services.SignatureService;
import org.scribe.utils.OAuthEncoder;
import org.scribe.utils.Preconditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Random;

@Component
@Log4j2
public class ClientAPI {
    private final RestTemplate rest;
    private final HttpHeaders headers;
    @Value("${lob.netsuite.client.url}")
    private String url;

    @Value("${lob.netsuite.client.realm}")
    private String realm;

    @Value("${lob.netsuite.client.token}")
    private String token;

    @Value("${lob.netsuite.client.consumer_key}")
    private String consumer_key;

    @Value("${lob.netsuite.client.oauth_nonce}")
    private String oauth_nonce;

    @Value("${lob.netsuite.client.consumer_secret}")
    private String consumer_secret;

    @Value("${lob.netsuite.client.token_secret}")
    private String token_secret;


    public ClientAPI() {
        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");

    }
    //#HttpClient

    public String makeCall() throws IOException {
        //return mockData();
        Response result = callWithHttpPost();
        log.info(result.getHeaders());
        return result.getBody().toString();

    }


    private OAuthService getService() {
        return new ServiceBuilder()
                .provider(DummyService.class)
                .apiKey(consumer_key)
                .apiSecret(consumer_secret)
                .signatureType(SignatureType.Header)
                .build();

    }

    private Response callWithHttpPost() {
        OAuthRequest request = new OAuthRequest(Verb.GET,

                url);

        request.setRealm(realm);
        //request.addHeader(OAuthConstants.SIGN_METHOD, "HMAC-SHA256");
        //request.addHeader("content-type", "application/json");
        //request.addPayload("{}");
       OAuthService service2 = getService();
        service2.signRequest(getToken(), request);
        log.info(request.getHeaders());
        return request.send();

    }

    private Token getToken() {
        return new Token(token, token_secret);
    }

}

