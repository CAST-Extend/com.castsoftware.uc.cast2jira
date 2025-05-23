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

/* Update MMA 2025-05-19: use of Jackson for JSON handling */

package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

/**
 * Represents an custom field option.
 */
public class CustomFieldOption extends Resource {

    private String value = null;

    /**
     * Creates a custom field option from a JSON payload.
     *
     * @param restClient REST client instance
     * @param json JSON payload
     */
    protected CustomFieldOption(RestClient restClient, JsonNode json) {
        super(restClient);

        if (json != null)
            deserialize(json);
    }

    private void deserialize(JsonNode json) {
        self = Field.getString(json.get("self"));
        id = Field.getString(json.get("id"));
        value = Field.getString(json.get("value"));
    }

    /**
     * Retrieves the given custom field option record.
     *
     * @param restClient REST client instance
     * @param id Internal JIRA ID of the custom field option
     *
     * @return a custom field option instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static CustomFieldOption get(RestClient restClient, String id)
        throws JiraException {

        JsonNode result;

        try {
            result = restClient.get(getBaseUri() + "customFieldOption/" + id);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve custom field option " + id, ex);
        }

        if (result == null || !result.isObject())
            throw new JiraException("JSON payload is malformed");

        return new CustomFieldOption(restClient, result);
    }

    @Override
    public String toString() {
        return getValue();
    }

    public String getValue() {
        return value;
    }
}

