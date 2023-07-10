package org.cardanofoundation.lob.sourceadapter.netsuite.service;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;
import org.scribe.services.HMACSha1SignatureService;
import org.scribe.services.SignatureService;

public class DummyService extends DefaultApi10a {
    @Override
    public String getAccessTokenEndpoint() {
// TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAuthorizationUrl(Token arg0) {
// TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRequestTokenEndpoint() {
// TODO Auto-generated method stub
        return null;
    }

    @Override
    public SignatureService getSignatureService() {
        return new HMACSha256SignatureService();
    }
}
