/* Update MMA 2025-05-20: use of Jackson for JSON handling */

package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

public class StatusTest {

    private final String statusID = "10004";
    private final String description = "Issue is currently in progress.";
    private final String iconURL = "https://site/images/icons/statuses/open.png";

    @Test
    public void testJSONDeserializer() throws IOException, URISyntaxException {
        Status status = new Status(new RestClient(null, new URI("/123/asd")), getTestJSON());
        assertEquals(status.getDescription(), description);
        assertEquals(status.getIconUrl(), iconURL);
        assertEquals("Open", status.getName());
        assertEquals(status.getId(), statusID);
    }

    @Test
    public void testGetStatus() throws Exception {
        final RestClient restClient = PowerMockito.mock(RestClient.class);
        when(restClient.get(anyString())).thenReturn(getTestJSON());
        Status status = Status.get(restClient,"someID");
        assertEquals(status.getDescription(), description);
        assertEquals(status.getIconUrl(), iconURL);
        assertEquals("Open", status.getName());
        assertEquals(status.getId(), statusID);
    }

    @Test(expected = JiraException.class)
    public void testJiraExceptionFromRestException() throws Exception {
        final RestClient mockRestClient = PowerMockito.mock(RestClient.class);
        when(mockRestClient.get(anyString())).thenThrow(RestException.class);
        Status.get(mockRestClient, "issueNumber");
    }

    @Test(expected = JiraException.class)
    public void testJiraExceptionFromNonJSON() throws Exception {
        final RestClient mockRestClient = PowerMockito.mock(RestClient.class);
        Status.get(mockRestClient,"issueNumber");
    }

    private JsonNode getTestJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();

        node.put("description", description);
        node.put("name", "Open");
        node.put("iconUrl", iconURL);
        node.put("id", statusID);

        return node;
    }

    @Test
    public void testStatusToString() throws URISyntaxException {
        Status status = new Status(new RestClient(null, new URI("/123/asd")), getTestJSON());
        assertEquals("Open",status.toString());
    }

}
