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
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.RestClient;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

/**
 * GreenHopper sprint statistics.
 */
public class SprintReport {

    private RestClient restclient = null;
    private Sprint sprint = null;
    private List<SprintIssue> completedIssues = null;
    private List<SprintIssue> incompletedIssues = null;
    private List<SprintIssue> puntedIssues = null;
    private EstimateSum completedIssuesEstimateSum = null;
    private EstimateSum incompletedIssuesEstimateSum = null;
    private EstimateSum allIssuesEstimateSum = null;
    private EstimateSum puntedIssuesEstimateSum = null;
    private List<String> issueKeysAddedDuringSprint = null;

    /**
     * Creates a sprint report from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected SprintReport(RestClient restclient, JsonNode json) {
        this.restclient = restclient;

        if (json != null)
            deserialize(json);
    }

    private void deserialize(JsonNode json) {
        sprint = GreenHopperField.getResource(Sprint.class, json.get("sprint"), restclient);
        completedIssues = GreenHopperField.getResourceArray(SprintIssue.class, json.get("completedIssues"), restclient);
        incompletedIssues = GreenHopperField.getResourceArray(SprintIssue.class, json.get("incompletedIssues"), restclient);
        puntedIssues = GreenHopperField.getResourceArray(SprintIssue.class, json.get("puntedIssues"), restclient);
        completedIssuesEstimateSum = GreenHopperField.getEstimateSum(json.get("completedIssuesEstimateSum"));
        incompletedIssuesEstimateSum = GreenHopperField.getEstimateSum(json.get("incompletedIssuesEstimateSum"));
        allIssuesEstimateSum = GreenHopperField.getEstimateSum(json.get("allIssuesEstimateSum"));
        puntedIssuesEstimateSum = GreenHopperField.getEstimateSum(json.get("puntedIssuesEstimateSum"));
        issueKeysAddedDuringSprint = GreenHopperField.getStringArray(json.get("issueKeysAddedDuringSprint"));
    }

    /**
     * Retrieves the sprint report for the given rapid view and sprint.
     *
     * @param restClient REST client instance
     * @param rv Rapid View instance
     * @param sprint Sprint instance
     *
     * @return the sprint report
     *
     * @throws JiraException when the retrieval fails
     */
    public static SprintReport get(RestClient restClient, RapidView rv, Sprint sprint)
        throws JiraException {

        final int rvId = rv.getId();
        final int sprintId = sprint.getId();
        JsonNode result;

        try {
            URI reporturi = restClient.buildURI(
                GreenHopperResource.RESOURCE_URI + "rapid/charts/sprintreport",
                new HashMap<String, String>() {{
                    put("rapidViewId", Integer.toString(rvId));
                    put("sprintId", Integer.toString(sprintId));
                }});
            result = restClient.get(reporturi);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve sprint report", ex);
        }

        if (result == null || !result.isObject())
            throw new JiraException("JSON payload is malformed");

        JsonNode jo = result;

        if (!jo.has("contents") || !(jo.get("contents").isObject()))
            throw new JiraException("Sprint report content is malformed");

        return new SprintReport(restClient, jo.get("contents"));
    }

    public Sprint getSprint() {
        return sprint;
    }

    public List<SprintIssue> getCompletedIssues() {
        return completedIssues;
    }

    public List<SprintIssue> getIncompletedIssues() {
        return incompletedIssues;
    }

    public List<SprintIssue> getPuntedIssues() {
        return puntedIssues;
    }

    public EstimateSum getCompletedIssuesEstimateSum() {
        return completedIssuesEstimateSum;
    }

    public EstimateSum getIncompletedIssuesEstimateSum() {
        return incompletedIssuesEstimateSum;
    }

    public EstimateSum getAllIssuesEstimateSum() {
        return allIssuesEstimateSum;
    }

    public EstimateSum getPuntedIssuesEstimateSum() {
        return puntedIssuesEstimateSum;
    }

    public List<String> getIssueKeysAddedDuringSprint() {
        return issueKeysAddedDuringSprint;
    }
}


