/* Update MMA 2025-05-20: use of Jackson for JSON handling and replacement of PowerMock by Mockito */

package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.mockito.Mockito.*;

public class WatchesTest {

    @Test
    public void testWatchesInit() {
        Watches watches = new Watches(null, null);
    }

    @Test
    public void testWatchesJSON() {
        Watches watches = new Watches(null, getTestJSON());

        assertFalse(watches.isWatching());
        assertEquals(0, watches.getWatchCount());
        assertEquals("10", watches.getId());
        assertEquals("https://brainbubble.atlassian.net/rest/api/2/issue/FILTA-43/watchers", watches.getSelf());
    }

    @Test(expected = JiraException.class)
    public void testGetWatchersNullReturned() throws Exception {
        RestClient restClient = mock(RestClient.class);
        when(restClient.get(anyString())).thenReturn(null);
        Watches.get(restClient, "someID");
    }

    @Test(expected = JiraException.class)
    public void testGetWatchersGetThrows() throws Exception {
        RestClient restClient = mock(RestClient.class);
        when(restClient.get(anyString())).thenThrow(new RuntimeException("Error"));
        Watches.get(restClient, "TEST-123");
    }

    @Test
    public void testGetWatchers() throws Exception {
        RestClient restClient = mock(RestClient.class);
        when(restClient.get(anyString())).thenReturn(getTestJSON());

        final Watches watches = Watches.get(restClient, "someID");

        assertFalse(watches.isWatching());
        assertEquals(0, watches.getWatchCount());
        assertEquals("10", watches.getId());
        assertEquals("https://brainbubble.atlassian.net/rest/api/2/issue/FILTA-43/watchers", watches.getSelf());
    }

    @Test
    public void testWatchesToString() {
        Watches watches = new Watches(null, getTestJSON());
        assertEquals("0", watches.toString());
    }

    private JsonNode getTestJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();

        node.put("id", "10");
        node.put("self", "https://brainbubble.atlassian.net/rest/api/2/issue/FILTA-43/watchers");
        node.put("watchCount", 0);
        node.put("isWatching", false);

        return node;
    }

}
