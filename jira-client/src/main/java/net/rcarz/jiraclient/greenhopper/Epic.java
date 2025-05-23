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
import net.rcarz.jiraclient.RestClient;

/**
 * Represents a GreenHopper epic issue.
 */
public class Epic extends GreenHopperIssue {

    public String epicLabel = null;
    public String epicColour = null;
    public EpicStats epicStats = null;

    /**
     * Creates an epic issue from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Epic(RestClient restclient, JsonNode json) {
        super(restclient, json);

        if (json != null)
            deserialize(json);
    }

    private void deserialize(JsonNode json) {
        epicLabel = Field.getString(json.get("epicLabel"));
        epicColour = Field.getString(json.get("epicColor"));
        epicStats = GreenHopperField.getEpicStats(json.get("epicStats"));
    }

    public String getEpicLabel() {
        return epicLabel;
    }

    public String getEpicColour() {
        return epicColour;
    }

    public EpicStats getEpicStats() {
        return epicStats;
    }
}

