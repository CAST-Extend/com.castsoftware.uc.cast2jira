/**
 * jira-client - a simple JIRA REST client
 * Copyright (c) 2013 Bob Carroll (bob.carroll@alum.rit.edu)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

/* Update MMA 2025-05-19: use of httpclient5 and Jackson for JSON handling */

package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * A simple REST client that speaks JSON.
 */
public class RestClient {

    private final CloseableHttpClient httpClient;
    private final ICredentials creds;
    private final URI uri;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Creates a REST client instance with a URI.
     *
     * @param httpclient Underlying HTTP client to use
     * @param uri Base URI of the remote REST service
     */
    public RestClient(CloseableHttpClient httpclient, URI uri) {
        this(httpclient, null, uri);
    }

    /**
     * Creates an authenticated REST client instance with a URI.
     *
     * @param httpclient Underlying HTTP client to use
     * @param creds Credentials to send with each request
     * @param uri Base URI of the remote REST service
     */
    public RestClient(CloseableHttpClient httpclient, ICredentials creds, URI uri) {
        this.httpClient = httpclient;
        this.creds = creds;
        this.uri = uri;
    }

    /**
     * Build a URI from a path.
     *
     * @param path Path to append to the base URI
     *
     * @return the full URI
     *
     * @throws URISyntaxException when the path is invalid
     */
    public URI buildURI(String path) throws URISyntaxException {
        return buildURI(path, null);
    }

    /**
     * Build a URI from a path and query parameters.
     *
     * @param path Path to append to the base URI
     * @param params Map of key value pairs
     *
     * @return the full URI
     *
     * @throws URISyntaxException when the path is invalid
     */
    public URI buildURI(String path, Map<String, String> params) throws URISyntaxException {
        URIBuilder ub = new URIBuilder(uri);
        String basePath = ub.getPath();
        if (!basePath.endsWith("/")) basePath += "/";
        if (path.startsWith("/")) path = path.substring(1);
        ub.setPath(ub.getPath() + path);

        if (params != null) {
            for (Map.Entry<String, String> ent : params.entrySet())
                ub.addParameter(ent.getKey(), ent.getValue());
        }

        return ub.build();
    }

    private JsonNode request(HttpUriRequestBase req) throws RestException, IOException {
        req.addHeader("Accept", "application/json");

        if (creds != null)
            creds.authenticate(req);

        try (CloseableHttpResponse resp = httpClient.execute(req)) {
            HttpEntity ent = resp.getEntity();
            StringBuilder result = new StringBuilder();

            if (ent != null) {
                Header contentEncodingHeader = resp.getFirstHeader("Content-Encoding");
                String encoding = contentEncodingHeader != null ? contentEncodingHeader.getValue() : null;

                if (encoding == null) {
                    Charset charset = null;
                    Header contentTypeHeader = resp.getFirstHeader("Content-Type");
                    if (contentTypeHeader != null) {
                        ContentType contentType = ContentType.parse(contentTypeHeader.getValue());
                        charset = contentType.getCharset();
                        if (charset != null) {
                            encoding = charset.name();
                        }
                    }
                }

                try (InputStreamReader isr = encoding != null ?
                        new InputStreamReader(ent.getContent(), encoding) :
                        new InputStreamReader(ent.getContent());
                     BufferedReader br = new BufferedReader(isr);) {
                    String line = "";

                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }
                }
            }

            if (resp.getCode() >= 300)
                throw new RestException(resp.getReasonPhrase(), resp.getCode(), result.toString(), resp.getHeaders());

            if (ent == null) return null;

            try (InputStream contentStream = ent.getContent()) {
                return MAPPER.readTree(contentStream);
            }
        }
    }

    private JsonNode request(ClassicHttpRequest req, String payload)
        throws RestException, IOException {

        if (payload != null) {
            // Remove the try-catch and just create the entity directly
            StringEntity ent = new StringEntity(payload, ContentType.create("application/json", "UTF-8"));
            req.addHeader("Content-Type", "application/json");
            req.setEntity(ent);
        }

        return request(req);
    }
    
    private JsonNode request(ClassicHttpRequest req, File file)
        throws RestException, IOException {
        if (file != null) {
            File fileUpload = file;
            req.setHeader("X-Atlassian-Token", "nocheck");
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", fileUpload, ContentType.DEFAULT_BINARY, fileUpload.getName());
            HttpEntity ent = builder.build();
            req.setEntity(ent);
        }
        return request(req);
    }

    private JsonNode request(ClassicHttpRequest req, Issue.NewAttachment... attachments)
        throws RestException, IOException {
        if (attachments != null) {
            req.setHeader("X-Atlassian-Token", "nocheck");
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            for(Issue.NewAttachment attachment : attachments) {
                String filename = attachment.getFilename();
                Object content = attachment.getContent();
                if (content instanceof byte[]) {
                    builder.addBinaryBody("file", (byte[]) content, ContentType.DEFAULT_BINARY, filename);
                } else if (content instanceof InputStream) {
                    builder.addBinaryBody("file", (InputStream) content, ContentType.DEFAULT_BINARY, filename);
                } else if (content instanceof File) {
                    builder.addBinaryBody("file", (File) content, ContentType.DEFAULT_BINARY, filename);
                } else if (content == null) {
                    throw new IllegalArgumentException("Missing content for the file " + filename);
                } else {
                    throw new IllegalArgumentException(
                        "Expected file type byte[], java.io.InputStream or java.io.File but provided " +
                            content.getClass().getName() + " for the file " + filename);
                }
            }
            HttpEntity ent = builder.build();
            req.setEntity(ent);
        }
        return request(req);
    }

    private JsonNode request(ClassicHttpRequest req, JsonNode payload)
        throws RestException, IOException {

        if (payload != null) {
            String jsonString = MAPPER.writeValueAsString(payload);
            StringEntity ent = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
            req.addHeader("Content-Type", "application/json");
            req.setEntity(ent);
        }
        return request(req);
    }

    /**
     * Executes an HTTP DELETE with the given URI.
     *
     * @param uri Full URI of the remote endpoint
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     */
    public JsonNode delete(URI uri) throws RestException, IOException {
        return request(new HttpDelete(uri));
    }

    /**
     * Executes an HTTP DELETE with the given path.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JsonNode delete(String path) throws RestException, IOException, URISyntaxException {
        return delete(buildURI(path));
    }

    /**
     * Executes an HTTP GET with the given URI.
     *
     * @param uri Full URI of the remote endpoint
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     */
    public JsonNode get(URI uri) throws RestException, IOException {
        return request(new HttpGet(uri));
    }

    /**
     * Executes an HTTP GET with the given path.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     * @param params Map of key value pairs
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JsonNode get(String path, Map<String, String> params) throws RestException, IOException, URISyntaxException {
        return get(buildURI(path, params));
    }

    /**
     * Executes an HTTP GET with the given path.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JsonNode get(String path) throws RestException, IOException, URISyntaxException {
        return get(path, null);
    }


    /**
     * Executes an HTTP POST with the given URI and payload.
     *
     * @param uri Full URI of the remote endpoint
     * @param payload JSON-encoded data to send to the remote service
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     */
    public JsonNode post(URI uri, JsonNode payload) throws RestException, IOException {
        return request(new HttpPost(uri), payload);
    }

    /**
     * Executes an HTTP POST with the given URI and payload.
     *
     * At least one JIRA REST endpoint expects malformed JSON. The payload
     * argument is quoted and sent to the server with the application/json
     * Content-Type header. You should not use this function when proper JSON
     * is expected.
     *
     * @see <a href="https://jira.atlassian.com/browse/JRA-29304">https://jira.atlassian.com/browse/JRA-29304</a>
     *
     * @param uri Full URI of the remote endpoint
     * @param payload Raw string to send to the remote service
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     */
    public JsonNode post(URI uri, String payload) throws RestException, IOException {
    	if(payload == null) {
            // No payload, just send empty POST
            return request(new HttpPost(uri), (String) null);
    	}
        // Send payload as is (assumed to be valid JSON or the server expects raw string)
        return request(new HttpPost(uri), payload);
    }

    /**
     * Executes an HTTP POST with the given path and payload.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     * @param payload JSON-encoded data to send to the remote service
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JsonNode post(String path, JsonNode payload)
        throws RestException, IOException, URISyntaxException {

        return post(buildURI(path), payload);
    }
    
    /**
     * Executes an HTTP POST with the given path.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JsonNode post(String path)
        throws RestException, IOException, URISyntaxException {

        // Send an empty JSON object as payload using Jackson's ObjectMapper
        return post(buildURI(path), MAPPER.createObjectNode());
    }
    
    /**
     * Executes an HTTP POST with the given path and file payload.
     * 
     * @param path Full URI of the remote endpoint
     * @param file java.io.File
     * 
     * @throws URISyntaxException 
     * @throws IOException 
     * @throws RestException 
     */
    public JsonNode post(String path, File file) throws RestException, IOException, URISyntaxException{
        return request(new HttpPost(buildURI(path)), file);
    }

    /**
     * Executes an HTTP POST with the given path and file payloads.
     *
     * @param path    Full URI of the remote endpoint
     * @param attachments   the name of the attachment
     *
     * @throws URISyntaxException
     * @throws IOException
     * @throws RestException
     */
    public JsonNode post(String path, Issue.NewAttachment... attachments)
        throws RestException, IOException, URISyntaxException
    {
        return request(new HttpPost(buildURI(path)), attachments);
    }

    /**
     * Executes an HTTP PUT with the given URI and payload.
     *
     * @param uri Full URI of the remote endpoint
     * @param payload JSON-encoded data to send to the remote service
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     */
    public JsonNode put(URI uri, JsonNode payload) throws RestException, IOException {
        return request(new HttpPut(uri), payload);
    }

    /**
     * Executes an HTTP PUT with the given path and payload.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     * @param payload JSON-encoded data to send to the remote service
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JsonNode put(String path, JsonNode payload)
        throws RestException, IOException, URISyntaxException {

        return put(buildURI(path), payload);
    }
    
    /**
     * Exposes the http client.
     *
     * @return the httpClient property
     */
    public CloseableHttpClient getHttpClient(){
        return this.httpClient;
    }

    /**
     *
     * @return the ObjectMapper property
     */
    public ObjectMapper getJsonMapper() {
        return MAPPER;
    }
}

