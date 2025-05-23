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

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Issue change log.
 */
public class ChangeLog extends Resource {
    /**
     * List of change log entries.
     */
    private List<ChangeLogEntry> entries = null;

    /**
     * Creates a change log from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected ChangeLog(RestClient restclient, JsonNode json) {
        super(restclient);

        if (json != null)
            deserialize(json);
    }

    /**
     * Deserializes a change log from a json payload.
     * @param json the json payload
     */
    private void deserialize(JsonNode json) {
        JsonNode entriesNode = json.get(Field.CHANGE_LOG_ENTRIES);
        entries = Field.getResourceArray(ChangeLogEntry.class, entriesNode, restclient);
    }

    /**
     * Returns the list of change log entries in the change log.
     * @return the list of entries
     */
    public List<ChangeLogEntry> getEntries() {
        return entries;
    }
}
