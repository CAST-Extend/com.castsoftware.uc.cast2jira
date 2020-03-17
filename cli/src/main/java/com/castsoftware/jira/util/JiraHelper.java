package com.castsoftware.jira.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.AsyncAppender;

import com.atlassian.jira.rest.client.api.AuditRestClient;
import com.atlassian.jira.rest.client.api.ComponentRestClient;
import com.atlassian.jira.rest.client.api.GroupRestClient;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.MyPermissionsRestClient;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.ProjectRolesRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.SessionRestClient;
import com.atlassian.jira.rest.client.api.UserRestClient;
import com.atlassian.jira.rest.client.api.VersionRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;



public class JiraHelper implements JiraRestClient {
    private AsynchronousJiraRestClientFactory factory = null;
    private JiraRestClient restClient;
    
    public JiraHelper(String jiraRestApiUrl, String jiraUser, String jiraUserPassword)
            throws URISyntaxException {
        
        factory = new AsynchronousJiraRestClientFactory();
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
        return restClient.getProjectRolesRestClient();
    }

    @Override
    public GroupRestClient getGroupClient() {
        // TODO Auto-generated method stub
        return restClient.getGroupClient();
    }

    @Override
    public AuditRestClient getAuditRestClient() {
        // TODO Auto-generated method stub
        return restClient.getAuditRestClient();
    }

    @Override
    public MyPermissionsRestClient getMyPermissionsRestClient() {
        // TODO Auto-generated method stub
        return restClient.getMyPermissionsRestClient();
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub
        restClient.close();
    }

//    @Override
//    public ApacheHttpClient getTransportClient() {
//        return restClient.getTransportClient();
//    }

}
