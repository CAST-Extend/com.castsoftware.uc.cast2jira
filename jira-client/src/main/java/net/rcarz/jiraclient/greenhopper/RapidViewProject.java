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

package net.rcarz.jiraclient.greenhopper;

import com.fasterxml.jackson.databind.JsonNode;
import net.rcarz.jiraclient.Field;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.Project;
import net.rcarz.jiraclient.RestClient;

/**
 * Represents a GreenHopper JIRA project.
 */
public class RapidViewProject extends GreenHopperResource {

    private String key = null;
    private String name = null;

    /**
     * Creates a project from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected RapidViewProject(RestClient restclient, JsonNode json) {
        super(restclient);

        if (json != null)
            deserialize(json);
    }

    private void deserialize(JsonNode json) {
        id = Field.getInteger(json.get("id"));
        key = Field.getString(json.get("key"));
        name = Field.getString(json.get("name"));
    }

    /**
     * Retrieves the full JIRA project.
     *
     * @return a Project
     *
     * @throws JiraException when the retrieval fails
     */
    public Project getJiraProject() throws JiraException {
        return Project.get(restclient, key);
    }

    @Override
    public String toString() {
        return key;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }
}

