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

/**
 * Represents an issue status.
 */
public class Status extends Resource {

    private String description = null;
    private String iconUrl = null;
    private String name = null;

    /**
     * Creates a status from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Status(RestClient restclient, JsonNode json) {
        super(restclient);

        if (json != null)
            deserialize(json);
    }

    private void deserialize(JsonNode json) {
        self = Field.getString(json.get("self"));
        id = Field.getString(json.get("id"));
        description = Field.getString(json.get("description"));
        iconUrl = Field.getString(json.get("iconUrl"));
        name = Field.getString(json.get("name"));
    }

    /**
     * Retrieves the given status record.
     *
     * @param restClient REST client instance
     * @param id Internal JIRA ID of the status
     *
     * @return a status instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static Status get(RestClient restClient, String id) throws JiraException {

        JsonNode result;

        try {
            result = restClient.get(getBaseUri() + "status/" + id);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve status " + id, ex);
        }

        if (result == null || !result.isObject())
            throw new JiraException("JSON payload is malformed");

        return new Status(restClient, result);
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}

