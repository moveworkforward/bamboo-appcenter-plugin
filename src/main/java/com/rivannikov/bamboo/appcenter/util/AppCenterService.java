package com.rivannikov.bamboo.appcenter.util;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rivannikov.bamboo.appcenter.model.*;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Nonnull;
import javax.net.ssl.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class AppCenterService {

    private final LogUtils LOG;
    private final String token;
    private final RestTemplate restTemplate;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void setDebug(boolean debug) {
        LOG.setDebug(debug);
    }

    /**
     * URLs API AppCenter
     */
    private AppCenterApiUrlHolder appCenterApiUrlHolder;

    public AppCenterService(BuildLogger buildLogger, String serverUrls, String token) {
        this.LOG = new LogUtils(buildLogger);
        this.token = token;
        trustAllCertificates();
        restTemplate = createRestTemplate();
        appCenterApiUrlHolder = new AppCenterApiUrlHolder(buildLogger, serverUrls);
    }

    private RestTemplate createRestTemplate() {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = null;
        try {
            sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate;

    }

    /**
     * 1. Create an upload resource
     */
    public AppCenterReleaseUploadsResponse createUpload(String ownerName, String appName) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(appCenterApiUrlHolder.getReleaseUploads());

        // URI (URL) parameters
        Map<String, Object> urlParams = new HashMap();
        urlParams.put("owner_name", ownerName);
        urlParams.put("app_name", appName);

        String url = uriBuilder.buildAndExpand(urlParams).toUriString();

        HttpHeaders headers = httpAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> responseEntity = exchangePOST(new HttpEntity(headers), url);

        return gson.fromJson(responseEntity.getBody(), AppCenterReleaseUploadsResponse.class);
    }

    /**
     * 2. Upload Artifact file
     */
    public boolean uploadArtifact(String url, File artifact) {
        HttpHeaders headers = httpAuthHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap();
        body.add("ipa", new FileSystemResource(artifact));

        String decodeUrl = url;
        try {
            decodeUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // not going to happen - value came from JDK's own StandardCharsets
            LOG.error(e.toString());
        }

        ResponseEntity<String> responseEntity = exchangePOST(new HttpEntity(body, headers), decodeUrl);

        return (responseEntity != null && responseEntity.getStatusCode().equals(HttpStatus.NO_CONTENT));
    }

    /**
     * 3. Update upload resource's status to committed
     */
    public AppCenterReleaseUploadsCommitResponse updateUpload(String ownerName, String appName, String uploadId) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(appCenterApiUrlHolder.getReleaseUploads() + "/{upload_id}");

        // URI (URL) parameters
        Map<String, Object> urlParams = new HashMap();
        urlParams.put("owner_name", ownerName);
        urlParams.put("app_name", appName);
        urlParams.put("upload_id", uploadId);

        String url = uriBuilder.buildAndExpand(urlParams).toUriString();

        HttpHeaders headers = httpAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        AppCenterReleaseUploadsCommitRequest body = new AppCenterReleaseUploadsCommitRequest();
        body.setStatus("committed");

        ResponseEntity<String> responseEntity = exchangePATCH(new HttpEntity(body, headers), url);

        return gson.fromJson(responseEntity.getBody(), AppCenterReleaseUploadsCommitResponse.class);
    }

    /**
     * 4. Distribute the uploaded release
     */
    public AppCenterReleasesResponse distribute(String ownerName, String appName, String releaseId, String destination, String distributionGroupId) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(appCenterApiUrlHolder.getReleases() + "/{destination}");

        // URI (URL) parameters
        Map<String, Object> urlParams = new HashMap();
        urlParams.put("owner_name", ownerName);
        urlParams.put("app_name", appName);
        urlParams.put("release_id", releaseId);
        urlParams.put("destination", destination);

        String url = uriBuilder.buildAndExpand(urlParams).toUriString();

        HttpHeaders headers = httpAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        AppCenterReleasesRequest body = new AppCenterReleasesRequest();
        body.setId(distributionGroupId);

        ResponseEntity<String> responseEntity = exchangePOST(new HttpEntity(gson.toJson(body), headers), url);

        return gson.fromJson(responseEntity.getBody(), AppCenterReleasesResponse.class);
    }

    /**
     * Returns a list of distribution groups in the org specified
     */
    public List<AppCenterDistributionGroup> distributionGroups(String ownerName) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(appCenterApiUrlHolder.getDistributionGroups());

        // URI (URL) parameters
        Map<String, Object> urlParams = new HashMap();
        urlParams.put("owner_name", ownerName);

        String url = uriBuilder.buildAndExpand(urlParams).toUriString();

        HttpHeaders headers = httpAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> responseEntity = exchangeGET(new HttpEntity(headers), url);

        Type listType = new TypeToken<ArrayList<AppCenterDistributionGroup>>(){}.getType();
        return gson.fromJson(responseEntity.getBody(), listType);
    }


    /**
     * Return of distribution group ID by name
     */
    public String getDistributionGroupId(String ownerName, String distributionGroupName) {
        List<AppCenterDistributionGroup> distributionGroups = distributionGroups(ownerName);
        if (distributionGroups != null) {
            Predicate<? super AppCenterDistributionGroup> appCenterDistributionGroupFilter;
            appCenterDistributionGroupFilter = appCenterDistributionGroup -> appCenterDistributionGroup.getName().equalsIgnoreCase(distributionGroupName.trim());
            AppCenterDistributionGroup distributionGroup = distributionGroups.stream().filter(appCenterDistributionGroupFilter).findFirst().get();
            return distributionGroup.getId();
        }
        return null;
    }


    private HttpHeaders httpAuthHeaders() {
        String credentials = null;
        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isEmpty()) {
            credentials = token;
        }
        headers.set("X-API-Token", credentials);

        return headers;
    }

    private ResponseEntity exchangeGET(HttpEntity entity, String url) {
        return exchange(entity, url, HttpMethod.GET);
    }

    private ResponseEntity exchangePOST(HttpEntity entity, String url) {
        return exchange(entity, url, HttpMethod.POST);
    }

    private ResponseEntity exchangePATCH(HttpEntity entity, String url) {
        return exchange(entity, url, HttpMethod.PATCH);
    }

    private ResponseEntity exchangePUT(HttpEntity entity, String url) {
        return exchange(entity, url, HttpMethod.PUT);
    }

    private ResponseEntity exchange(HttpEntity entity, String url, HttpMethod httpMethod) {
        LOG.debug("[" + httpMethod.name() + "] " + url);
        ResponseEntity<String> responseEntity;
        try {
            responseEntity = restTemplate.exchange(url, httpMethod, entity, String.class);
            if (responseEntity == null) {
                throw new RuntimeException(url + " : responseEntity is EMPTY!");
            }
            HttpStatus statusCode = responseEntity.getStatusCode();
            if (HttpStatus.OK.equals(statusCode)
                    || HttpStatus.CREATED.equals(statusCode)
                    || HttpStatus.NO_CONTENT.equals(statusCode)) {
                LOG.debug("Response " + url);
                LOG.debug(responseEntity.getBody());
            } else {
                throw new RuntimeException("Fail response code [" + statusCode + "] " + responseEntity.getBody());
            }
        } catch (HttpClientErrorException httpClientErrorException) {
            LOG.error("Fail response code [" + httpClientErrorException.getStatusCode() + "] " + httpClientErrorException.getMessage() +
                    "\n" + httpClientErrorException.getResponseBodyAsString());
            return new ResponseEntity(httpClientErrorException.getResponseBodyAsString(), httpClientErrorException.getStatusCode());
        }

        return responseEntity;
    }

    @Nonnull
    private static TrustManager[] getTrustManagers() {
        return new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                        return myTrustedAnchors;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };
    }

    public static void trustAllCertificates() {
        try {
            TrustManager[] trustAllCerts = getTrustManagers();

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });


        } catch (Exception e) {
        }
    }

}
