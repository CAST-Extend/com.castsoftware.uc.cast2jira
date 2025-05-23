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

/* Update MMA 2025-05-20: use of Jackson for JSON handling */

package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Date;

/**
 * Represents an issue work log.
 */
public class WorkLog extends Resource {

    private User author = null;
    private String comment = null;
    private Date created = null;
    private Date updated = null;
    private User updateAuthor = null;
    private Date started = null;
    private String timeSpent = null;
    private int timeSpentSeconds = 0;

    /**
     * Creates a work log from a JSON payload.
     *
     * @param restClient REST client instance
     * @param json JSON payload
     */
    protected WorkLog(RestClient restClient, JsonNode json) {
        super(restClient);

        if (json != null)
            deserialize(json);
    }

    private void deserialize(JsonNode json) {
        self = Field.getString(json.get("self"));
        id = Field.getString(json.get("id"));
        author = Field.getResource(User.class, json.get("author"), restclient);
        comment = Field.getString(json.get("comment"));
        created = Field.getDate(json.get("created"));
        updated = Field.getDate(json.get("updated"));
        updateAuthor = Field.getResource(User.class, json.get("updateAuthor"), restclient);
        started = Field.getDate(json.get("started"));
        timeSpent = Field.getString(json.get("timeSpent"));
        timeSpentSeconds = Field.getInteger(json.get("timeSpentSeconds"));
    }

    /**
     * Retrieves the given work log record.
     *
     * @param restClient REST client instance
     * @param issue Internal JIRA ID of the associated issue
     * @param id Internal JIRA ID of the work log
     *
     * @return a work log instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static WorkLog get(RestClient restClient, String issue, String id) throws JiraException {

        JsonNode result = null;

        try {
            result = restClient.get(getBaseUri() + "issue/" + issue + "/worklog/" + id);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve work log " + id + " on issue " + issue, ex);
        }

        if (result == null || !result.isObject())
            throw new JiraException("JSON payload is malformed");

        return new WorkLog(restClient, result);
    }

    @Override
    public String toString() {
        return created + " by " + author;
    }

    public User getAuthor() {
        return author;
    }

    public String getComment() {
        return comment;
    }

    public Date getCreatedDate() {
        return created;
    }

    public User getUpdateAuthor() {
        return updateAuthor;
    }

    public Date getUpdatedDate() {
        return updated;
    }

    public Date getStarted(){ return started; }

    public String getTimeSpent(){ return timeSpent; }

    public int getTimeSpentSeconds() {
        return timeSpentSeconds;
    }

}

