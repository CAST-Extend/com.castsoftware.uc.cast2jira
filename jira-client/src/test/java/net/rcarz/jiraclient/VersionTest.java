/* Update MMA 2025-05-20: use of Jackson for JSON handling and replacement of PowerMock by Mockito */

package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

public class VersionTest {

    @Test
    public void testVersionInit() {
        new Version(null, null);
    }

    @Test
    public void testVersionJSON() {
        Version version = new Version(null, getTestJSON());

        assertEquals("10200", version.getId());
        assertEquals("1.0", version.getName());
        assertFalse(version.isArchived());
        assertFalse(version.isReleased());
        assertEquals("2013-12-01", version.getReleaseDate());
        assertEquals("First Full Functional Build", version.getDescription());
    }

    @Test
    public void testGetVersion() throws Exception {
        RestClient restClient = mock(RestClient.class);
        when(restClient.get(anyString())).thenReturn(getTestJSON());
        Version version = Version.get(restClient, "id");

        assertEquals("10200", version.getId());
        assertEquals("1.0", version.getName());
        assertFalse(version.isArchived());
        assertFalse(version.isReleased());
        assertEquals("2013-12-01", version.getReleaseDate());
        assertEquals("First Full Functional Build", version.getDescription());
    }

    @Test(expected = JiraException.class)
    public void testJiraExceptionFromRestException() throws Exception {
        RestClient mockRestClient = mock(RestClient.class);
        when(mockRestClient.get(anyString())).thenThrow(RestException.class);
        Version.get(mockRestClient, "id");
    }

    @Test(expected = JiraException.class)
    public void testJiraExceptionFromNonJSON() throws Exception {
        RestClient mockRestClient = mock(RestClient.class);
        when(mockRestClient.get(anyString())).thenReturn(null);
        Version.get(mockRestClient, "id");
    }

    private JsonNode getTestJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();

        node.put("id", "10200");
        node.put("description", "First Full Functional Build");
        node.put("name", "1.0");
        node.put("archived", false);
        node.put("released", false);
        node.put("releaseDate", "2013-12-01");

        return node;
    }

    @Test
    public void testMergeWith() throws Exception {
        RestClient mockRestClient = mock(RestClient.class);
        JsonNode json = getTestJSON();
        Version version = new Version(mockRestClient,json);
        version.mergeWith(new Version(mockRestClient,json));
        verify(mockRestClient, times(1)).put(anyString(), any(JsonNode.class));
    }

    @Test(expected = JiraException.class)
    public void testMergeWithFailed() throws Exception {
        RestClient mockRestClient = mock(RestClient.class);
        doThrow(new RuntimeException("Error")).when(mockRestClient).put(anyString(), any(JsonNode.class));

        JsonNode json = getTestJSON();
        Version targetVersion = new Version(mockRestClient, json);

        Version version = new Version(mockRestClient, getTestJSON());
        version.mergeWith(targetVersion);
    }

    @Test(expected = JiraException.class)
    public void testCopyToFailed() throws Exception {
        RestClient mockRestClient = mock(RestClient.class);
        doThrow(new RuntimeException("Error")).when(mockRestClient).post(anyString(), any(JsonNode.class));

        JsonNode json = getTestJSON();
        Project project = new Project(mockRestClient, json);

        Version version = new Version(mockRestClient, getTestJSON());
        version.copyTo(project);
    }

    @Test
    public void testCopyTo() throws Exception {
        RestClient mockRestClient = mock(RestClient.class);
        JsonNode json = getTestJSON();
        Version version = new Version(mockRestClient,json);
        version.copyTo(new Project(mockRestClient,json));
        verify(mockRestClient, times(1)).post(anyString(),any(JsonNode.class));
    }

    @Test
    public void testToString() throws Exception {
        Version version = new Version(null, getTestJSON());
        assertEquals("1.0", version.toString());
    }

    @Test
    public void testGetName() throws Exception {
        Version version = new Version(null, getTestJSON());
        assertEquals("1.0", version.getName());
    }

    @Test
    public void testIsArchived() throws Exception {
        Version version = new Version(null, getTestJSON());
        assertFalse(version.isArchived());
    }

    @Test
    public void testIsReleased() throws Exception {
        Version version = new Version(null, getTestJSON());
        assertFalse(version.isReleased());
    }

    @Test
    public void testGetReleaseDate() throws Exception {
        Version version = new Version(null, getTestJSON());
        assertEquals("2013-12-01",version.getReleaseDate());
    }

    @Test
    public void testGetDescription() throws Exception {
        Version version = new Version(null, getTestJSON());
        assertEquals("First Full Functional Build",version.getDescription());
    }

}
