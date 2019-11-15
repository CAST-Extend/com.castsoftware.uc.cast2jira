package com.castsoftware.jira.util;

import java.net.URI;
import java.net.URISyntaxException;

import com.atlassian.jira.rest.client.ComponentRestClient;
import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.MetadataRestClient;
import com.atlassian.jira.rest.client.ProjectRestClient;
import com.atlassian.jira.rest.client.ProjectRolesRestClient;
import com.atlassian.jira.rest.client.SearchRestClient;
import com.atlassian.jira.rest.client.SessionRestClient;
import com.atlassian.jira.rest.client.UserRestClient;
import com.atlassian.jira.rest.client.VersionRestClient;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import com.sun.jersey.client.apache.ApacheHttpClient;


public class JiraHelper implements JiraRestClient {
    private JerseyJiraRestClientFactory factory = null;
    private JiraRestClient restClient;
    
    public JiraHelper(String jiraRestApiUrl, String jiraUser, String jiraUserPassword)
            throws URISyntaxException {
        
        factory = new JerseyJiraRestClientFactory();
        URI jiraServerUri = new URI(jiraRestApiUrl);
        restClient = factory.createWithBasicHttpAuthentication(jiraServerUri,
                jiraUser, jiraUserPassword);
    }

    @Override
    public IssueRestClient getIssueClient() {
        return restClient.getIssueClient();
    }

    @Override
    public SessionRestClient getSessionClient() {
        return restClient.getSessionClient();
    }

    @Override
    public UserRestClient getUserClient() {
        return restClient.getUserClient();
    }

    @Override
    public ProjectRestClient getProjectClient() {
        return restClient.getProjectClient();
    }

    @Override
    public ComponentRestClient getComponentClient() {
        return restClient.getComponentClient();
    }

    @Override
    public MetadataRestClient getMetadataClient() {
        return restClient.getMetadataClient();
    }

    @Override
    public SearchRestClient getSearchClient() {
        return restClient.getSearchClient();
    }

    @Override
    public VersionRestClient getVersionRestClient() {
        return restClient.getVersionRestClient();
    }

    @Override
    public ProjectRolesRestClient getProjectRolesRestClient() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApacheHttpClient getTransportClient() {
        return restClient.getTransportClient();
    }

}
