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

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a JIRA user.
 */
public class User extends Resource {

    private boolean active = false;
    private Map<String, String> avatarUrls = null;
    private String displayName = null;
    private String email = null;
    private String name = null;

    /**
     * Creates a user from a JSON payload.
     *
     * @param restClient REST client instance
     * @param json       JSON payload
     */
    protected User(RestClient restClient, JsonNode json) {
        super(restClient);

        if (json != null)
            deserialize(json);
    }

    /**
     * Retrieves the given user record.
     *
     * @param restClient REST client instance
     * @param username   User logon name
     * @return a user instance
     * @throws JiraException when the retrieval fails
     */
    public static User get(RestClient restClient, String username) throws JiraException {

        JsonNode result = null;

        Map<String, String> params = new HashMap<String, String>();
        params.put("username", username);

        try {
            result = restClient.get(getBaseUri() + "user", params);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve user " + username, ex);
        }

        if (result == null || !result.isObject())
            throw new JiraException("JSON payload is malformed");

        return new User(restClient, result);
    }

    private void deserialize(JsonNode json) {
        self = Field.getString(json.get("self"));
        id = Field.getString(json.get("id"));
        active = Field.getBoolean(json.get("active"));
        avatarUrls = Field.getMap(String.class, String.class, json.get("avatarUrls"));
        displayName = Field.getString(json.get("displayName"));
        email = getEmailFromMap(json);
        name = Field.getString(json.get("name"));
    }

    /**
     * API changes email address might be represented as either "email" or "emailAddress"
     *
     * @param node JSON object for the User
     * @return String email address of the JIRA user.
     */
    private String getEmailFromMap(JsonNode node) {
        if (node.has("email")) {
            return Field.getString(node.get("email"));
        } else {
            return Field.getString(node.get("emailAddress"));
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean isActive() {
        return active;
    }

    public Map<String, String> getAvatarUrls() {
        return avatarUrls;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}

