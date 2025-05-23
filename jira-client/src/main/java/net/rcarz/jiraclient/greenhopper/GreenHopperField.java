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
import net.rcarz.jiraclient.RestClient;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * Utility functions for translating between JSON and fields.
 */
public final class GreenHopperField {

    public static final String DATE_TIME_FORMAT = "d/MMM/yy h:m a";

    public static final String NO_DATE = "None";

    private GreenHopperField() { }

    /**
     * Gets a date-time from the given object.
     *
     * @param dt Date-Time as a string
     *
     * @return the date-time or null
     */
    public static DateTime getDateTime(Object dt) {
        if(dt == null || ((String)dt).equals(NO_DATE)){
            return null;
        }
        return DateTime.parse((String)dt, DateTimeFormat.forPattern(DATE_TIME_FORMAT));
    }

    /**
     * Gets an epic stats object from the given object.
     *
     * @param es a JsonNode instance
     *
     * @return a EpicStats instance or null if es isn't a JsonNode instance
     */
    public static EpicStats getEpicStats(JsonNode es) {
        if (es != null && es.isObject() && !es.isEmpty()) {
            return new EpicStats(es);
        }
        return null;
    }

    /**
     * Gets an estimate statistic object from the given object.
     *
     * @param es a JsonNode instance
     *
     * @return a EstimateStatistic instance or null if es isn't a JsonNode instance
     */
    public static EstimateStatistic getEstimateStatistic(JsonNode es) {
        if (es != null && es.isObject() && !es.isEmpty()) {
            return new EstimateStatistic(es);
        }
        return null;
    }

    /**
     * Gets an estimate sum object from the given object.
     *
     * @param es a JsonNode instance
     *
     * @return a EstimateSum instance or null if es isn't a JsonNode instance
     */
    public static EstimateSum getEstimateSum(JsonNode es) {
        if (es != null && es.isObject() && !es.isEmpty()) {
            return new EstimateSum(es);
        }
        return null;
    }

    /**
     * Gets a list of integers from the given object.
     *
     * @param ia a JsonNode instance
     *
     * @return a list of integers
     */
    public static List<Integer> getIntegerArray(JsonNode ia) {
        List<Integer> results = new ArrayList<>();

        if (ia != null && ia.isArray()) {
            for (JsonNode node : ia) {
                if (node.isInt()) {
                    results.add(node.intValue());
                }
            }
        }

        return results;
    }

    /**
     * Gets a GreenHopper resource from the given object.
     *
     * @param type Resource data type
     * @param r a JsonNode instance
     * @param restClient REST client instance
     *
     * @return a Resource instance or null if r isn't a JSONObject instance
     */
    public static <T extends GreenHopperResource> T getResource(
        Class<T> type, JsonNode r, RestClient restClient) {

        if (r == null || !r.isObject()) {
            return null;
        }
        JsonNode node = (JsonNode) r;

        if (type == Epic.class)
            return type.cast(new Epic(restClient, node));
        else if (type == Marker.class)
            return type.cast(new Marker(restClient, node));
        else if (type == RapidView.class)
            return type.cast(new RapidView(restClient, node));
        else if (type == RapidViewProject.class)
            return type.cast(new RapidViewProject(restClient, node));
        else if (type == Sprint.class)
            return type.cast(new Sprint(restClient, node));
        else if (type == SprintIssue.class)
            return type.cast(new SprintIssue(restClient, node));

        return null;
    }

    /**
     * Gets a list of GreenHopper resources from the given object.
     *
     * @param type Resource data type
     * @param ra a JsonNode instance
     * @param restClient REST client instance
     *
     * @return a list of Resources found in ra
     */
    public static <T extends GreenHopperResource> List<T> getResourceArray(
        Class<T> type, JsonNode ra, RestClient restClient) {

        List<T> results = new ArrayList<>();

        if (ra != null && ra.isArray()) {
            for (JsonNode node : ra) {
                T item = getResource(type, node, restClient);
                if (item != null)
                    results.add(item);
            }
        }

        return results;
    }

    /**
     * Gets a list of strings from the given object.
     *
     * @param node a JsonNode instance
     *
     * @return a list of strings
     */
    public static List<String> getStringArray(JsonNode node) {
        List<String> results = new ArrayList<>();

        if (node != null && node.isArray()) {
            for (JsonNode element : node) {
                if (element.isTextual()) {
                    results.add(element.asText());
                }
            }
        }

        return results;
    }
}

