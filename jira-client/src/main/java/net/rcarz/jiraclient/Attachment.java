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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;

/**
 * Represents an issue attachment.
 */
public class Attachment extends Resource {

    private User author = null;
    private String filename = null;
    private Date created = null;
    private int size = 0;
    private String mimeType = null;
    private String content = null;

    /**
     * Creates an attachment from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload (Jackson JsonNode)
     */
    protected Attachment(RestClient restclient, JsonNode json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JsonNode json) {
        self = Field.getString(json.get("self"));
        id = Field.getString(json.get("id"));
        author = Field.getResource(User.class, json.get("author"), restclient);
        filename = Field.getString(json.get("filename"));
        created = Field.getDate(json.get("created"));
        size = Field.getInteger(json.get("size"));
        mimeType = Field.getString(json.get("mimeType"));
        content = Field.getString(json.get("content"));
    }

    /**
     * Retrieves the given attachment record.
     *
     * @param restclient REST client instance
     * @param id Internal JIRA ID of the attachment
     *
     * @return an attachment instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static Attachment get(RestClient restclient, String id)
        throws JiraException {

        JsonNode result = null;

        try {
            result = restclient.get(getBaseUri() + "attachment/" + id);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve attachment " + id, ex);
        }

        if (result == null || !result.isObject())
            throw new JiraException("JSON payload is malformed");

        return new Attachment(restclient, result);
    }
    
    /**
     * Downloads attachment to byte array
     *
     * @return a byte[]
     *
     * @throws JiraException when the download fails
     */
    public byte[] download() 
    	throws JiraException{
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	try {
        	HttpGet get = new HttpGet(content);
            CloseableHttpResponse response = restclient.getHttpClient().execute(get);
        	HttpEntity entity = response.getEntity();
        	if (entity != null) {
        	    try (InputStream inputStream = entity.getContent()) {
                    int next = inputStream.read();
                    while (next > -1) {
                        bos.write(next);
                        next = inputStream.read();
                    }
                    bos.flush();
                }
        	}
    	} catch(IOException e){
    		  throw new JiraException(String.format("Failed downloading attachment from %s: %s", this.content, e.getMessage()));
    	}
    	return bos.toByteArray();
    }

    @Override
    public String toString() {
        return getContentUrl();
    }

    public User getAuthor() {
        return author;
    }

    public Date getCreatedDate() {
        return created;
    }

    public String getContentUrl() {
        return content;
    }

    public String getFileName() {
        return filename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public int getSize() {
        return size;
    }
}

