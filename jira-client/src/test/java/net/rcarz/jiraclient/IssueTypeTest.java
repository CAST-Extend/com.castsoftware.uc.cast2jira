/* Update MMA 2025-05-20: use of Jackson for JSON handling */

package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

public class IssueTypeTest {

    @Test
    public void testIssueTypeInit() {
        IssueType issueType = new IssueType(null, null);
    }

    @Test
    public void testGetIssueType() {
        IssueType issueType = new IssueType(null, getTestJSON());

        assertFalse(issueType.isSubtask());
        assertEquals("Story", issueType.getName());
        assertEquals("7", issueType.getId());
        assertEquals("https://brainbubble.atlassian.net/images/icons/issuetypes/story.png", issueType.getIconUrl());
        assertEquals("This is a test issue type.", issueType.getDescription());
    }

    @Test
    public void testFields() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode testJSON = (ObjectNode) getTestJSON();
        ObjectNode fields = mapper.createObjectNode();

        fields.put("key1","key1Value");
        fields.put("key2","key2Value");
        testJSON.set("fields", fields);

        IssueType issueType = new IssueType(null, testJSON);
        JsonNode fieldsNode = issueType.getFields();

        assertEquals(2, fieldsNode.size());
        assertEquals("key1Value", fieldsNode.get("key1").asText());
        assertEquals("key2Value", fieldsNode.get("key2").asText());
    }

    @Test
    public void testLoadIssueType() throws Exception {
        final RestClient restClient = PowerMockito.mock(RestClient.class);
        when(restClient.get(anyString())).thenReturn(getTestJSON());
        IssueType issueType = IssueType.get(restClient,"someID");
        assertFalse(issueType.isSubtask());
        assertEquals("Story", issueType.getName());
        assertEquals("7", issueType.getId());
        assertEquals("https://brainbubble.atlassian.net/images/icons/issuetypes/story.png", issueType.getIconUrl());
        assertEquals("This is a test issue type.", issueType.getDescription());
    }

    @Test(expected = JiraException.class)
    public void testJiraExceptionFromRestException() throws Exception {
        final RestClient mockRestClient = PowerMockito.mock(RestClient.class);
        when(mockRestClient.get(anyString())).thenThrow(RestException.class);
        IssueType.get(mockRestClient, "issueNumber");
    }

    @Test(expected = JiraException.class)
    public void testJiraExceptionFromNonJSON() throws Exception {
        final RestClient mockRestClient = PowerMockito.mock(RestClient.class);
        IssueType.get(mockRestClient,"issueNumber");
    }

    @Test
    public void testIssueTypeToString(){
        IssueType issueType = new IssueType(null, getTestJSON());

        assertEquals("Story", issueType.toString());
    }

    private JsonNode getTestJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();

        node.put("self", "https://brainbubble.atlassian.net/rest/api/2/issuetype/7");
        node.put("id", "7");
        node.put("description", "This is a test issue type.");
        node.put("iconUrl", "https://brainbubble.atlassian.net/images/icons/issuetypes/story.png");
        node.put("name", "Story");
        node.put("subtask", false);

        return node;
    }

}
