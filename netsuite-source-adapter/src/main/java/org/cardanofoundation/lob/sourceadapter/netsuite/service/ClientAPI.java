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
        OAuthRequest request = new OAuthRequest(Verb.GET, url);
        request.setRealm(realm);
        OAuthService service2 = getService();
        service2.signRequest(getToken(), request);
        log.info(request.getHeaders());
        return request.send();

    }

    private Token getToken() {
        return new Token(token, token_secret);
    }

    private String mockData(){
        return "{\n" +
                "    \"more\": false,\n" +
                "    \"lines\": [\n" +
                "        {\n" +
                "            \"Subsidiary (no hierarchy)\": \"1\",\n" +
                "            \"Type\": \"Journal\",\n" +
                "            \"Date Created\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Last Modified\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Date\": \"30/12/2022\",\n" +
                "            \"Due Date/Receive By\": \"\",\n" +
                "            \"Period\": \"112\",\n" +
                "            \"Tax Period\": \"17\",\n" +
                "            \"Internal ID\": \"1\",\n" +
                "            \"Transaction Number\": \"JOURNAL1ULTIMO\",\n" +
                "            \"Document Number\": \"JE-0001\",\n" +
                "            \"Number\": \"4101110100\",\n" +
                "            \"Name\": \"\",\n" +
                "            \"Tax Number\": \"\",\n" +
                "            \"Project (no hierarchy)\": \"\",\n" +
                "            \"Rate\": \"\",\n" +
                "            \"Account (Main)\": \"\",\n" +
                "            \"Memo\": \"Closing Balances 2022\",\n" +
                "            \"Memo (Main)\": \"Closing Balances 2022\",\n" +
                "            \"Currency\": \"1\",\n" +
                "            \"Exchange Rate\": \"1.00\",\n" +
                "            \"Amount (Debit) (Foreign Currency)\": \"\",\n" +
                "            \"Amount (Credit) (Foreign Currency)\": \"3248600.13\",\n" +
                "            \"Amount (Debit)\": \"\",\n" +
                "            \"Amount (Credit)\": \"3248600.13\",\n" +
                "            \"Intercompany\": \"F\",\n" +
                "            \"Status\": \"approved\",\n" +
                "            \"Approval History\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"Subsidiary (no hierarchy)\": \"1\",\n" +
                "            \"Type\": \"Journal\",\n" +
                "            \"Date Created\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Last Modified\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Date\": \"30/12/2022\",\n" +
                "            \"Due Date/Receive By\": \"\",\n" +
                "            \"Period\": \"112\",\n" +
                "            \"Tax Period\": \"17\",\n" +
                "            \"Internal ID\": \"1\",\n" +
                "            \"Transaction Number\": \"JOURNAL1PENULTIMO\",\n" +
                "            \"Document Number\": \"JE-0001\",\n" +
                "            \"Number\": \"4101120100\",\n" +
                "            \"Name\": \"\",\n" +
                "            \"Tax Number\": \"\",\n" +
                "            \"Project (no hierarchy)\": \"\",\n" +
                "            \"Rate\": \"\",\n" +
                "            \"Account (Main)\": \"\",\n" +
                "            \"Memo\": \"Closing Balances 2022\",\n" +
                "            \"Memo (Main)\": \"Closing Balances 2022\",\n" +
                "            \"Currency\": \"1\",\n" +
                "            \"Exchange Rate\": \"1.00\",\n" +
                "            \"Amount (Debit) (Foreign Currency)\": \"21683239.90\",\n" +
                "            \"Amount (Credit) (Foreign Currency)\": \"\",\n" +
                "            \"Amount (Debit)\": \"21683239.90\",\n" +
                "            \"Amount (Credit)\": \"\",\n" +
                "            \"Intercompany\": \"F\",\n" +
                "            \"Status\": \"approved\",\n" +
                "            \"Approval History\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"Subsidiary (no hierarchy)\": \"1\",\n" +
                "            \"Type\": \"Journal\",\n" +
                "            \"Date Created\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Last Modified\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Date\": \"30/12/2022\",\n" +
                "            \"Due Date/Receive By\": \"\",\n" +
                "            \"Period\": \"112\",\n" +
                "            \"Tax Period\": \"17\",\n" +
                "            \"Internal ID\": \"1\",\n" +
                "            \"Transaction Number\": \"JOURNAL1\",\n" +
                "            \"Document Number\": \"JE-0001\",\n" +
                "            \"Number\": \"2406210100\",\n" +
                "            \"Name\": \"\",\n" +
                "            \"Tax Number\": \"\",\n" +
                "            \"Project (no hierarchy)\": \"\",\n" +
                "            \"Rate\": \"\",\n" +
                "            \"Account (Main)\": \"\",\n" +
                "            \"Memo\": \"Closing Balances 2022\",\n" +
                "            \"Memo (Main)\": \"Closing Balances 2022\",\n" +
                "            \"Currency\": \"1\",\n" +
                "            \"Exchange Rate\": \"1.00\",\n" +
                "            \"Amount (Debit) (Foreign Currency)\": \"3248600.13\",\n" +
                "            \"Amount (Credit) (Foreign Currency)\": \"\",\n" +
                "            \"Amount (Debit)\": \"3248600.13\",\n" +
                "            \"Amount (Credit)\": \"\",\n" +
                "            \"Intercompany\": \"F\",\n" +
                "            \"Status\": \"approved\",\n" +
                "            \"Approval History\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"Subsidiary (no hierarchy)\": \"1\",\n" +
                "            \"Type\": \"Journal\",\n" +
                "            \"Date Created\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Last Modified\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Date\": \"30/12/2022\",\n" +
                "            \"Due Date/Receive By\": \"\",\n" +
                "            \"Period\": \"112\",\n" +
                "            \"Tax Period\": \"17\",\n" +
                "            \"Internal ID\": \"1\",\n" +
                "            \"Transaction Number\": \"JOURNAL1\",\n" +
                "            \"Document Number\": \"JE-0001\",\n" +
                "            \"Number\": \"5204110100\",\n" +
                "            \"Name\": \"\",\n" +
                "            \"Tax Number\": \"\",\n" +
                "            \"Project (no hierarchy)\": \"\",\n" +
                "            \"Rate\": \"\",\n" +
                "            \"Account (Main)\": \"\",\n" +
                "            \"Memo\": \"Closing Balances 2022\",\n" +
                "            \"Memo (Main)\": \"Closing Balances 2022\",\n" +
                "            \"Currency\": \"1\",\n" +
                "            \"Exchange Rate\": \"1.00\",\n" +
                "            \"Amount (Debit) (Foreign Currency)\": \"840015.56\",\n" +
                "            \"Amount (Credit) (Foreign Currency)\": \"\",\n" +
                "            \"Amount (Debit)\": \"840015.56\",\n" +
                "            \"Amount (Credit)\": \"\",\n" +
                "            \"Intercompany\": \"F\",\n" +
                "            \"Status\": \"approved\",\n" +
                "            \"Approval History\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"Subsidiary (no hierarchy)\": \"1\",\n" +
                "            \"Type\": \"Journal\",\n" +
                "            \"Date Created\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Last Modified\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Date\": \"30/12/2022\",\n" +
                "            \"Due Date/Receive By\": \"\",\n" +
                "            \"Period\": \"112\",\n" +
                "            \"Tax Period\": \"17\",\n" +
                "            \"Internal ID\": \"1\",\n" +
                "            \"Transaction Number\": \"JOURNAL1\",\n" +
                "            \"Document Number\": \"JE-0001\",\n" +
                "            \"Number\": \"5205120100\",\n" +
                "            \"Name\": \"\",\n" +
                "            \"Tax Number\": \"\",\n" +
                "            \"Project (no hierarchy)\": \"\",\n" +
                "            \"Rate\": \"\",\n" +
                "            \"Account (Main)\": \"\",\n" +
                "            \"Memo\": \"Closing Balances 2022\",\n" +
                "            \"Memo (Main)\": \"Closing Balances 2022\",\n" +
                "            \"Currency\": \"1\",\n" +
                "            \"Exchange Rate\": \"1.00\",\n" +
                "            \"Amount (Debit) (Foreign Currency)\": \"532216.56\",\n" +
                "            \"Amount (Credit) (Foreign Currency)\": \"\",\n" +
                "            \"Amount (Debit)\": \"532216.56\",\n" +
                "            \"Amount (Credit)\": \"\",\n" +
                "            \"Intercompany\": \"F\",\n" +
                "            \"Status\": \"approved\",\n" +
                "            \"Approval History\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"Subsidiary (no hierarchy)\": \"1\",\n" +
                "            \"Type\": \"Journal\",\n" +
                "            \"Date Created\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Last Modified\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Date\": \"30/12/2022\",\n" +
                "            \"Due Date/Receive By\": \"\",\n" +
                "            \"Period\": \"112\",\n" +
                "            \"Tax Period\": \"17\",\n" +
                "            \"Internal ID\": \"1\",\n" +
                "            \"Transaction Number\": \"JOURNAL1\",\n" +
                "            \"Document Number\": \"JE-0001\",\n" +
                "            \"Number\": \"5205140100\",\n" +
                "            \"Name\": \"\",\n" +
                "            \"Tax Number\": \"\",\n" +
                "            \"Project (no hierarchy)\": \"\",\n" +
                "            \"Rate\": \"\",\n" +
                "            \"Account (Main)\": \"\",\n" +
                "            \"Memo\": \"Closing Balances 2022\",\n" +
                "            \"Memo (Main)\": \"Closing Balances 2022\",\n" +
                "            \"Currency\": \"1\",\n" +
                "            \"Exchange Rate\": \"1.00\",\n" +
                "            \"Amount (Debit) (Foreign Currency)\": \"367289.72\",\n" +
                "            \"Amount (Credit) (Foreign Currency)\": \"\",\n" +
                "            \"Amount (Debit)\": \"367289.72\",\n" +
                "            \"Amount (Credit)\": \"\",\n" +
                "            \"Intercompany\": \"F\",\n" +
                "            \"Status\": \"approved\",\n" +
                "            \"Approval History\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"Subsidiary (no hierarchy)\": \"1\",\n" +
                "            \"Type\": \"Journal\",\n" +
                "            \"Date Created\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Last Modified\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Date\": \"30/12/2022\",\n" +
                "            \"Due Date/Receive By\": \"\",\n" +
                "            \"Period\": \"112\",\n" +
                "            \"Tax Period\": \"17\",\n" +
                "            \"Internal ID\": \"1\",\n" +
                "            \"Transaction Number\": \"JOURNAL1\",\n" +
                "            \"Document Number\": \"JE-0001\",\n" +
                "            \"Number\": \"5205150100\",\n" +
                "            \"Name\": \"\",\n" +
                "            \"Tax Number\": \"\",\n" +
                "            \"Project (no hierarchy)\": \"\",\n" +
                "            \"Rate\": \"\",\n" +
                "            \"Account (Main)\": \"\",\n" +
                "            \"Memo\": \"Closing Balances 2022\",\n" +
                "            \"Memo (Main)\": \"Closing Balances 2022\",\n" +
                "            \"Currency\": \"1\",\n" +
                "            \"Exchange Rate\": \"1.00\",\n" +
                "            \"Amount (Debit) (Foreign Currency)\": \"5195520.56\",\n" +
                "            \"Amount (Credit) (Foreign Currency)\": \"\",\n" +
                "            \"Amount (Debit)\": \"5195520.56\",\n" +
                "            \"Amount (Credit)\": \"\",\n" +
                "            \"Intercompany\": \"F\",\n" +
                "            \"Status\": \"approved\",\n" +
                "            \"Approval History\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"Subsidiary (no hierarchy)\": \"1\",\n" +
                "            \"Type\": \"Journal\",\n" +
                "            \"Date Created\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Last Modified\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Date\": \"30/12/2022\",\n" +
                "            \"Due Date/Receive By\": \"\",\n" +
                "            \"Period\": \"112\",\n" +
                "            \"Tax Period\": \"17\",\n" +
                "            \"Internal ID\": \"1\",\n" +
                "            \"Transaction Number\": \"JOURNAL1\",\n" +
                "            \"Document Number\": \"JE-0001\",\n" +
                "            \"Number\": \"5205160100\",\n" +
                "            \"Name\": \"\",\n" +
                "            \"Tax Number\": \"\",\n" +
                "            \"Project (no hierarchy)\": \"\",\n" +
                "            \"Rate\": \"\",\n" +
                "            \"Account (Main)\": \"\",\n" +
                "            \"Memo\": \"Closing Balances 2022\",\n" +
                "            \"Memo (Main)\": \"Closing Balances 2022\",\n" +
                "            \"Currency\": \"1\",\n" +
                "            \"Exchange Rate\": \"1.00\",\n" +
                "            \"Amount (Debit) (Foreign Currency)\": \"\",\n" +
                "            \"Amount (Credit) (Foreign Currency)\": \"42.70\",\n" +
                "            \"Amount (Debit)\": \"\",\n" +
                "            \"Amount (Credit)\": \"42.70\",\n" +
                "            \"Intercompany\": \"F\",\n" +
                "            \"Status\": \"approved\",\n" +
                "            \"Approval History\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"Subsidiary (no hierarchy)\": \"1\",\n" +
                "            \"Type\": \"Journal\",\n" +
                "            \"Date Created\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Last Modified\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Date\": \"30/12/2022\",\n" +
                "            \"Due Date/Receive By\": \"\",\n" +
                "            \"Period\": \"112\",\n" +
                "            \"Tax Period\": \"17\",\n" +
                "            \"Internal ID\": \"1\",\n" +
                "            \"Transaction Number\": \"JOURNAL1\",\n" +
                "            \"Document Number\": \"JE-0001\",\n" +
                "            \"Number\": \"5205170100\",\n" +
                "            \"Name\": \"\",\n" +
                "            \"Tax Number\": \"\",\n" +
                "            \"Project (no hierarchy)\": \"\",\n" +
                "            \"Rate\": \"\",\n" +
                "            \"Account (Main)\": \"\",\n" +
                "            \"Memo\": \"Closing Balances 2022\",\n" +
                "            \"Memo (Main)\": \"Closing Balances 2022\",\n" +
                "            \"Currency\": \"1\",\n" +
                "            \"Exchange Rate\": \"1.00\",\n" +
                "            \"Amount (Debit) (Foreign Currency)\": \"502.88\",\n" +
                "            \"Amount (Credit) (Foreign Currency)\": \"\",\n" +
                "            \"Amount (Debit)\": \"502.88\",\n" +
                "            \"Amount (Credit)\": \"\",\n" +
                "            \"Intercompany\": \"F\",\n" +
                "            \"Status\": \"approved\",\n" +
                "            \"Approval History\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"Subsidiary (no hierarchy)\": \"1\",\n" +
                "            \"Type\": \"Journal\",\n" +
                "            \"Date Created\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Last Modified\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Date\": \"30/12/2022\",\n" +
                "            \"Due Date/Receive By\": \"\",\n" +
                "            \"Period\": \"112\",\n" +
                "            \"Tax Period\": \"17\",\n" +
                "            \"Internal ID\": \"1\",\n" +
                "            \"Transaction Number\": \"JOURNAL1\",\n" +
                "            \"Document Number\": \"JE-0001\",\n" +
                "            \"Number\": \"6310110100\",\n" +
                "            \"Name\": \"\",\n" +
                "            \"Tax Number\": \"\",\n" +
                "            \"Project (no hierarchy)\": \"\",\n" +
                "            \"Rate\": \"\",\n" +
                "            \"Account (Main)\": \"\",\n" +
                "            \"Memo\": \"Closing Balances 2022\",\n" +
                "            \"Memo (Main)\": \"Closing Balances 2022\",\n" +
                "            \"Currency\": \"1\",\n" +
                "            \"Exchange Rate\": \"1.00\",\n" +
                "            \"Amount (Debit) (Foreign Currency)\": \"2042563.05\",\n" +
                "            \"Amount (Credit) (Foreign Currency)\": \"\",\n" +
                "            \"Amount (Debit)\": \"2042563.05\",\n" +
                "            \"Amount (Credit)\": \"\",\n" +
                "            \"Intercompany\": \"F\",\n" +
                "            \"Status\": \"approved\",\n" +
                "            \"Approval History\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"Subsidiary (no hierarchy)\": \"1\",\n" +
                "            \"Type\": \"Journal\",\n" +
                "            \"Date Created\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Last Modified\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Date\": \"30/12/2022\",\n" +
                "            \"Due Date/Receive By\": \"\",\n" +
                "            \"Period\": \"112\",\n" +
                "            \"Tax Period\": \"17\",\n" +
                "            \"Internal ID\": \"1\",\n" +
                "            \"Transaction Number\": \"JOURNAL1\",\n" +
                "            \"Document Number\": \"JE-0001\",\n" +
                "            \"Number\": \"6310120100\",\n" +
                "            \"Name\": \"\",\n" +
                "            \"Tax Number\": \"\",\n" +
                "            \"Project (no hierarchy)\": \"\",\n" +
                "            \"Rate\": \"\",\n" +
                "            \"Account (Main)\": \"\",\n" +
                "            \"Memo\": \"Closing Balances 2022\",\n" +
                "            \"Memo (Main)\": \"Closing Balances 2022\",\n" +
                "            \"Currency\": \"1\",\n" +
                "            \"Exchange Rate\": \"1.00\",\n" +
                "            \"Amount (Debit) (Foreign Currency)\": \"550353.43\",\n" +
                "            \"Amount (Credit) (Foreign Currency)\": \"\",\n" +
                "            \"Amount (Debit)\": \"550353.43\",\n" +
                "            \"Amount (Credit)\": \"\",\n" +
                "            \"Intercompany\": \"F\",\n" +
                "            \"Status\": \"approved\",\n" +
                "            \"Approval History\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"Subsidiary (no hierarchy)\": \"1\",\n" +
                "            \"Type\": \"Journal\",\n" +
                "            \"Date Created\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Last Modified\": \"27/03/2023 5:17 PM\",\n" +
                "            \"Date\": \"30/12/2022\",\n" +
                "            \"Due Date/Receive By\": \"\",\n" +
                "            \"Period\": \"112\",\n" +
                "            \"Tax Period\": \"17\",\n" +
                "            \"Internal ID\": \"1\",\n" +
                "            \"Transaction Number\": \"JOURNAL1\",\n" +
                "            \"Document Number\": \"JE-0001\",\n" +
                "            \"Number\": \"6310170100\",\n" +
                "            \"Name\": \"\",\n" +
                "            \"Tax Number\": \"\",\n" +
                "            \"Project (no hierarchy)\": \"\",\n" +
                "            \"Rate\": \"\",\n" +
                "            \"Account (Main)\": \"\",\n" +
                "            \"Memo\": \"Closing Balances 2022\",\n" +
                "            \"Memo (Main)\": \"Closing Balances 2022\",\n" +
                "            \"Currency\": \"1\",\n" +
                "            \"Exchange Rate\": \"1.00\",\n" +
                "            \"Amount (Debit) (Foreign Currency)\": \"892905.23\",\n" +
                "            \"Amount (Credit) (Foreign Currency)\": \"\",\n" +
                "            \"Amount (Debit)\": \"892905.23\",\n" +
                "            \"Amount (Credit)\": \"\",\n" +
                "            \"Intercompany\": \"F\",\n" +
                "            \"Status\": \"approved\",\n" +
                "            \"Approval History\": \"\"\n" +
                "        }," +
                "{\n" +
                "            \"Subsidiary (no hierarchy)\": \"1\",\n" +
                "            \"Type\": \"VendBill\",\n" +
                "            \"Date Created\": \"29/08/2023 4:31 PM\",\n" +
                "            \"Last Modified\": \"29/08/2023 4:31 PM\",\n" +
                "            \"Date\": \"01/08/2023\",\n" +
                "            \"Due Date/Receive By\": \"\",\n" +
                "            \"Period\": \"129\",\n" +
                "            \"Tax Period\": \"146\",\n" +
                "            \"Internal ID\": \"3215\",\n" +
                "            \"Transaction Number\": \"VENDBILL140\",\n" +
                "            \"Document Number\": \"AMB-Meetup-07-2023\",\n" +
                "            \"Number\": \"1206310100\",\n" +
                "            \"Name\": \"\",\n" +
                "            \"Tax Number\": \"\",\n" +
                "            \"Project (no hierarchy)\": \"\",\n" +
                "            \"Rate\": \"0.00%\",\n" +
                "            \"Account (Main)\": \"1383\",\n" +
                "            \"Memo\": \"VAT\",\n" +
                "            \"Memo (Main)\": \"AMB-Meetup-07-2023\",\n" +
                "            \"Currency\": \"2\",\n" +
                "            \"Exchange Rate\": \".87533\",\n" +
                "            \"Amount (Debit) (Foreign Currency)\": \"\",\n" +
                "            \"Amount (Credit) (Foreign Currency)\": \"\",\n" +
                "            \"Amount (Debit)\": \"\",\n" +
                "            \"Amount (Credit)\": \"\",\n" +
                "            \"Intercompany\": \"F\",\n" +
                "            \"Status\": \"open\",\n" +
                "            \"Approval History\": \"\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
    }
}

