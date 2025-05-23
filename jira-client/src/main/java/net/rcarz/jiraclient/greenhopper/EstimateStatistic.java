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

/**
 * GreenHopper estimate statistics for rapid views.
 */
public class EstimateStatistic {

    private String statFieldId = null;
    private Double statFieldValue = 0.0;
    private String statFieldText = null;

    /**
     * Creates an estimate statistic from a JSON payload.
     *
     * @param json JSON payload
     */
    protected EstimateStatistic(JsonNode json) {
        statFieldId = Field.getString(json.get("statFieldId"));

        JsonNode statFieldValueNode = json.get("statFieldValue");
        if (statFieldValueNode != null && statFieldValueNode.isObject()) {
            statFieldValue = Field.getDouble(statFieldValueNode.get("value"));
            statFieldText = Field.getString(statFieldValueNode.get("text"));
        }
    }

    public String getFieldId() {
        return statFieldId;
    }

    public Double getFieldValue() {
        return statFieldValue;
    }

    public String getFieldText() {
        return statFieldText;
    }
}

