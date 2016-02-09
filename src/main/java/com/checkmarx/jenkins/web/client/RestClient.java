package com.checkmarx.jenkins.web.client;

import com.checkmarx.jenkins.web.model.AnalyzeRequest;
import com.checkmarx.jenkins.web.model.AnalyzeResponse;
import com.checkmarx.jenkins.web.model.AuthenticationRequest;
import org.apache.log4j.Logger;
import org.kohsuke.stapler.HttpResponses;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Map;


/**
 * @author tsahi
 * @since 02/02/16
 */
public class RestClient {
    private static final String REST_BASE_URI = "/CxRestAPI/api/";
    private static final String REST_AUTHENTICATION_URI = REST_BASE_URI + "auth/login";
    private static final String REST_ANALYZE_URI = REST_BASE_URI + "projects/{projectId}/analyze";
    private String serverUri;
    private AuthenticationRequest authenticationRequest;
    private Client client = ClientBuilder.newClient();

    public RestClient(String serverUri, AuthenticationRequest authenticationRequest) {
        this.serverUri = serverUri;
        this.authenticationRequest = authenticationRequest;
    }

    public AnalyzeResponse analyzeOpenSources(AnalyzeRequest request) {
        Cookie cookie = authenticate();
        String analyzeUri =  REST_ANALYZE_URI.replace("{projectId}", String.valueOf(request.getProjectId()));
        AnalyzeResponse response = client
                .target(serverUri + analyzeUri)
                .request()
                .cookie(cookie)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON), AnalyzeResponse.class);

        return response;
    }

    private Cookie authenticate() {
        Response response = client
                .target(serverUri + REST_AUTHENTICATION_URI)
                .request()
                .post(Entity.entity(authenticationRequest, MediaType.APPLICATION_JSON));

        validateResponse(response);

        Map<String, NewCookie> cookiesMap = response.getCookies();
        Map.Entry cookieEntry = (Map.Entry) cookiesMap.entrySet().toArray()[0];
        return (Cookie) cookieEntry.getValue();
    }

    private void validateResponse(Response response) {
        if (response.getStatus() >= 400) {
            throw new WebApplicationException(response);
        }
    }
}