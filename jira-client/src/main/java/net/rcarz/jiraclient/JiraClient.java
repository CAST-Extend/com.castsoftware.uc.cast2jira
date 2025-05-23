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

/* Update MMA 2025-05-19: use of httpclient5 and Jackson for JSON handling */

package net.rcarz.jiraclient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;

/**
 * A simple JIRA REST client.
 */
public class JiraClient {

    private RestClient restclient = null;
    private String username = null;

    /**
     * Creates a JIRA client.
     *
     * @param uri Base URI of the JIRA server
     * @throws JiraException 
     */
    public JiraClient(String uri) throws JiraException {
        this(uri, null);
    }

    /**
     * Creates an authenticated JIRA client.
     *
     * @param uri Base URI of the JIRA server
     * @param creds Credentials to authenticate with
     * @throws JiraException 
     */
    public JiraClient(String uri, ICredentials creds) throws JiraException {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setDefaultMaxPerRoute(20);
        connManager.setMaxTotal(40);
        CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(connManager).build();

        restclient = new RestClient(httpclient, creds, URI.create(uri));

        if (creds != null) {
            username = creds.getLogonName();
        	//intialize connection if required
        	creds.initialize(restclient);
        }
    }

    /**
     * Creates a new issue in the given project.
     *
     * @param project Key of the project to create in
     * @param issueType Name of the issue type to create
     *
     * @return a fluent create instance
     *
     * @throws JiraException when something goes wrong
     */
    public Issue.FluentCreate createIssue(String project, String issueType)
            throws JiraException {

        return Issue.create(restclient, project, issueType);
    }

    /**
     * Retreives the issue with the given key.
     *
     * @param key Issue key (PROJECT-123)
     *
     * @return an issue instance (issue includes all fields)
     *
     * @throws JiraException when something goes wrong
     */
    public Issue getIssue(String key) throws JiraException {
        return Issue.get(restclient, key);
    }

    /**
     * Retreives the issue with the given key.
     *
     * @param key Issue key (PROJECT-123)
     *
     * @param includedFields Specifies which issue fields will be included in
     * the result.
     * <br>Some examples how this parameter works:
     * <ul>
     * <li>*all - include all fields</li>
     * <li>*navigable - include just navigable fields</li>
     * <li>summary,comment - include just the summary and comments</li>
     * <li>*all,-comment - include all fields</li>
     * </ul>
     *
     * @return an issue instance
     *
     * @throws JiraException when something goes wrong
     */
    public Issue getIssue(String key, String includedFields) throws JiraException {
        return Issue.get(restclient, key, includedFields);
    }

    /**
     * Retreives the issue with the given key.
     *
     * @param key Issue key (PROJECT-123)
     *
     * @param includedFields Specifies which issue fields will be included in
     * the result.
     * <br>Some examples how this parameter works:
     * <ul>
     * <li>*all - include all fields</li>
     * <li>*navigable - include just navigable fields</li>
     * <li>summary,comment - include just the summary and comments</li>
     * <li>*all,-comment - include all fields</li>
     * </ul>
     * 
     * @param expand issue fields to expand when getting issue data
     *
     * @return an issue instance
     *
     * @throws JiraException when something goes wrong
     */
    public Issue getIssue(String key, String includedFields,
            String expand) throws JiraException {
        return Issue.get(restclient, key, includedFields, expand);
    }

    /**
     * count issues with the given query.
     *
     * @param jql JQL statement
     *
     * @return the count
     *
     * @throws JiraException when the search fails
     */
    public int countIssues(String jql) throws JiraException {
        return Issue.count(restclient, jql);
    }

    /**
     * Search for issues with the given query.
     *
     * @param jql JQL statement
     *
     * @return a search result structure with results (issues include all
     * navigable fields)
     *
     * @throws JiraException when the search fails
     */
    public Issue.SearchResult searchIssues(String jql)
            throws JiraException {

        return Issue.search(restclient, jql, null, null);
    }

    /**
     * Search for issues with the given query and max results.
     *
     * @param jql JQL statement
     * @param maxResults limit the number of results
     *
     * @return a search result structure with results (issues include all
     * navigable fields)
     *
     * @throws JiraException when the search fails
     */
    public Issue.SearchResult searchIssues(String jql, Integer maxResults)
            throws JiraException {

        return Issue.search(restclient, jql, null, maxResults);
    }

    /**
     * Search for issues with the given query and specify which fields to
     * retrieve.
     *
     * @param jql JQL statement
     *
     * @param includedFields Specifies which issue fields will be included in
     * the result.
     * <br>Some examples how this parameter works:
     * <ul>
     * <li>*all - include all fields</li>
     * <li>*navigable - include just navigable fields</li>
     * <li>summary,comment - include just the summary and comments</li>
     * <li>*all,-comment - include all fields</li>
     * </ul>
     *
     *
     * @return a search result structure with results
     *
     * @throws JiraException when the search fails
     */
    public Issue.SearchResult searchIssues(String jql, String includedFields)
            throws JiraException {

        return Issue.search(restclient, jql, includedFields, null);
    }

    /**
     * Search for issues with the given query and specify which fields to
     * retrieve and expand.
     *
     * @param jql JQL statement
     *
     * @param includedFields Specifies which issue fields will be included in
     * the result.
     * <br>Some examples how this parameter works:
     * <ul>
     * <li>*all - include all fields</li>
     * <li>*navigable - include just navigable fields</li>
     * <li>summary,comment - include just the summary and comments</li>
     * <li>*all,-comment - include all fields</li>
     * </ul>
     *
     * @param expandFields Specifies with issue fields should be expanded
     *
     * @return a search result structure with results
     *
     * @throws JiraException when the search fails
     */
    public Issue.SearchResult searchIssues(String jql, String includedFields, String expandFields)
        throws JiraException {

      return Issue.search(restclient, jql, includedFields, expandFields, null, null);
    }
    
    /**
     * Search for issues with the given query and specify which fields to
     * retrieve.
     *
     * @param jql JQL statement
     * @param maxResults limit the number of results
     *
     * @param includedFields Specifies which issue fields will be included in
     * the result.
     * <br>Some examples how this parameter works:
     * <ul>
     * <li>*all - include all fields</li>
     * <li>*navigable - include just navigable fields</li>
     * <li>summary,comment - include just the summary and comments</li>
     * <li>*all,-comment - include all fields</li>
     * </ul>
     *
     *
     * @return a search result structure with results
     *
     * @throws JiraException when the search fails
     */
    public Issue.SearchResult searchIssues(String jql, String includedFields, Integer maxResults)
            throws JiraException {

        return Issue.search(restclient, jql, includedFields, maxResults);
    }

    /**
     * Search for issues with the given query and specify which fields to
     * retrieve. If the total results is bigger than the maximum returned
     * results, then further calls can be made using different values for
     * the <code>startAt</code> field to obtain all the results.
     *
     * @param jql JQL statement
     *
     * @param includedFields Specifies which issue fields will be included in
     * the result.
     * <br>Some examples how this parameter works:
     * <ul>
     * <li>*all - include all fields</li>
     * <li>*navigable - include just navigable fields</li>
     * <li>summary,comment - include just the summary and comments</li>
     * <li>*all,-comment - include all fields</li>
     * </ul>
     * 
     * @param maxResults if non-<code>null</code>, defines the maximum number of
     * results that can be returned 
     * 
     * @param startAt if non-<code>null</code>, defines the first issue to
     * return
     *
     * @return a search result structure with results
     *
     * @throws JiraException when the search fails
     */
    public Issue.SearchResult searchIssues(String jql, String includedFields,
            Integer maxResults, Integer startAt) throws JiraException {

        return Issue.search(restclient, jql, includedFields, null, maxResults,
                startAt);
    }

    /**
     * Search for issues with the given query and specify which fields to
     * retrieve. If the total results is bigger than the maximum returned
     * results, then further calls can be made using different values for
     * the <code>startAt</code> field to obtain all the results.
     *
     * @param jql JQL statement
     *
     * @param includedFields Specifies which issue fields will be included in
     * the result.
     * <br>Some examples how this parameter works:
     * <ul>
     * <li>*all - include all fields</li>
     * <li>*navigable - include just navigable fields</li>
     * <li>summary,comment - include just the summary and comments</li>
     * <li>*all,-comment - include all fields</li>
     * </ul>
     *
     * @param expandFields Specifies with issue fields should be expanded
     *
     * @param maxResults if non-<code>null</code>, defines the maximum number of
     * results that can be returned
     *
     * @param startAt if non-<code>null</code>, defines the first issue to
     * return
     *
     * @return a search result structure with results
     *
     * @throws JiraException when the search fails
     */
    public Issue.SearchResult searchIssues(String jql, String includedFields, String expandFields,
                                           Integer maxResults, Integer startAt) throws JiraException {

      return Issue.search(restclient, jql, includedFields, expandFields, maxResults,
          startAt);
    }

    /**
     *
     * @return a list of all priorities available in the Jira installation
     * @throws JiraException
     */
    public List<Priority> getPriorities() throws JiraException {
        try {
            URI uri = restclient.buildURI(Resource.getBaseUri() + "priority");
            JsonNode response = restclient.get(uri);
            if (!response.isArray()) {
                throw new JiraException("Expected JSON array for priorities");
            }

            List<Priority> priorities = new ArrayList<>(response.size());
            for (JsonNode p : response) {
                priorities.add(new Priority(restclient, p));
            }

            return priorities;
        } catch (Exception ex) {
            throw new JiraException(ex.getMessage(), ex);
        }
    }

    /**
     * Get a list of options for a custom field
     *
     * @param field field id
     * @param project Key of the project context
     * @param issueType Name of the issue type
     *
     * @return a search result structure with results
     *
     * @throws JiraException when the search fails
     */
    public List<CustomFieldOption> getCustomFieldAllowedValues(String field, String project, String issueType) throws JiraException {
        JsonNode createMetadata = Issue.getCreateMetadata(restclient, project, issueType);
        JsonNode fieldMetadata = createMetadata.get(field);

        if (fieldMetadata == null || !fieldMetadata.has("allowedValues")) {
            throw new JiraException("Field metadata for '" + field + "' is missing or does not contain allowed values");
        }

        return Field.getResourceArray(CustomFieldOption.class, fieldMetadata.get("allowedValues"), restclient);
    }

    /**
     * Get a list of options for a components
     *
     * @param project Key of the project context
     * @param issueType Name of the issue type
     *
     * @return a search result structure with results
     *
     * @throws JiraException when the search fails
     */
    public List<Component> getComponentsAllowedValues(String project, String issueType) throws JiraException {
        JsonNode createMetadata = Issue.getCreateMetadata(restclient, project, issueType);
        JsonNode fieldMetadata = createMetadata.get(Field.COMPONENTS);

        if (fieldMetadata == null || !fieldMetadata.has("allowedValues")) {
            throw new JiraException("Component field metadata is missing or invalid");
        }

        return Field.getResourceArray(Component.class, fieldMetadata.get("allowedValues"), restclient);
    }

    public RestClient getRestClient() {
        return restclient;
    }

    public String getSelf() {
        return username;
    }

    /**
     * Obtains the list of all projects in Jira.
     * @return all projects; not all data is returned for each project; to get
     * the extra data use {@link #getProject(String)}
     * @throws JiraException failed to obtain the project list.
     */
    public List<Project> getProjects() throws JiraException {
        try {
            URI uri = restclient.buildURI(Resource.getBaseUri() + "project");
            JsonNode response = restclient.get(uri);

            if (!response.isArray()) {
                throw new JiraException("Expected an array of projects in JSON response");
            }

            List<Project> projects = new ArrayList<>(response.size());
            for (JsonNode projectNode : response) {
                projects.add(new Project(restclient, projectNode));
            }

            return projects;
        } catch (Exception ex) {
            throw new JiraException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Obtains information about a project, given its project key.
     * @param key the project key
     * @return the project
     * @throws JiraException failed to obtain the project
     */
    public Project getProject(String key) throws JiraException {
        try {
            URI uri = restclient.buildURI(Resource.getBaseUri() + "project/" + key);
            JsonNode response = restclient.get(uri);
            return new Project(restclient, response);
        } catch (Exception ex) {
            throw new JiraException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Obtains the list of all issue types in Jira.
     * @return all issue types
     * @throws JiraException failed to obtain the issue type list.
     */
    public List<IssueType> getIssueTypes() throws JiraException {
        try {
            URI uri = restclient.buildURI(Resource.getBaseUri() + "issuetype");
            JsonNode response = restclient.get(uri);

            if (!response.isArray()) {
                throw new JiraException("Expected an array of issue types in JSON response");
            }

            List<IssueType> issueTypes = new ArrayList<>(response.size());
            for (JsonNode issueTypeNode : response) {
                issueTypes.add(new IssueType(restclient, issueTypeNode));
            }

            return issueTypes;
        } catch (Exception ex) {
            throw new JiraException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Creates a new component in the given project.
     *
     * @param project Key of the project to create in
     *
     * @return a fluent create instance
     */
    public Component.FluentCreate createComponent(String project) {
        return Component.create(restclient, project);
    }
    
    /**
     * Obtains a component given its ID.
     * 
     * @param id the component ID
     * 
     * @return the component
     * 
     * @throws JiraException failed to obtain the component
     */
    public Component getComponent(String id) throws JiraException {
        return Component.get(restclient, id);
    }
    
    public ArrayList<IssueHistory> filterChangeLog(List<IssueHistory> histoy,String fields) {
        ArrayList<IssueHistory> result = new ArrayList<IssueHistory>(histoy.size());
        fields = "," + fields + ",";

        for (IssueHistory record : histoy) {
            ArrayList<IssueHistoryItem> list = new ArrayList<IssueHistoryItem>(record.getChanges().size());
            for (IssueHistoryItem item : record.getChanges()) {
                if (fields.contains(item.getField())) {
                    list.add(item);
                }
            }

            if (!list.isEmpty()) {
                result.add(new IssueHistory(record,list));
            }
        }
        return result;
    }

    public ArrayList<IssueHistory> getIssueChangeLog(Issue issue) throws JiraException {
        try {
            ArrayList<IssueHistory> changes = null;
            JsonNode response = getNextPortion(issue, 0);

            while (true) {
                JsonNode changelogNode = response.get("changelog");

                if (changelogNode == null || changelogNode.isMissingNode()) {
                    throw new JiraException("Missing 'changelog' node in response");
                }

                int total = changelogNode.path("total").asInt();
                JsonNode histories = changelogNode.path("histories");

                if (!histories.isArray()) {
                    throw new JiraException("Expected 'histories' to be an array");
                }

                if (changes == null) {
                    changes = new ArrayList<>(total);
                }

                for (JsonNode historyNode : histories) {
                    changes.add(new IssueHistory(restclient, historyNode));
                }

                if (changes.size() >= total) {
                    break;
                } else {
                    response = getNextPortion(issue,changes.size());
                }
            } 
           
            return changes;
        } catch (Exception ex) {
            throw new JiraException(ex.getMessage(), ex);
        }
    }

    private JsonNode getNextPortion(Issue issue, Integer startAt)
            throws URISyntaxException, RestException, IOException {

        Map<String, String> params = new HashMap<String, String>();
        if (startAt != null) {
            params.put("startAt", String.valueOf(startAt));
        }

        params.put("expand","changelog.fields");
        URI uri = restclient.buildURI(Issue.getBaseUri() + "issue/" + issue.id, params);
        return restclient.get(uri);
    }
}
