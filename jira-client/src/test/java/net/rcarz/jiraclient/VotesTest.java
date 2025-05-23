/* Update MMA 2025-05-20: use of Jackson for JSON handling and replacement of PowerMock by Mockito */

package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static junit.framework.Assert.*;

public class VotesTest {

    @Test
    public void testVotesInit(){
        new Votes(null,null);
    }

    @Test
    public void testVoteMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();

        node.put("self","someURL");
        node.put("id","1111");
        node.put("votes",12);
        node.put("hasVoted",true);

        Votes votes = new Votes(null, node);

        assertTrue(votes.hasVoted());
        assertEquals("1111",votes.getId());
        assertEquals(12,votes.getVotes());
        assertEquals("someURL",votes.getSelf());
    }

    @Test(expected = JiraException.class)
    public void testJiraExceptionFromRestException() throws Exception {
        RestClient mockRestClient = mock(RestClient.class);
        when(mockRestClient.get(anyString())).thenThrow(RestException.class);
        Votes.get(mockRestClient, "issueNumber");
    }

    @Test(expected = JiraException.class)
    public void testJiraExceptionFromNonJSON() throws Exception {
        RestClient mockRestClient = mock(RestClient.class);
        // Return null or invalid JSON
        when(mockRestClient.get(anyString())).thenReturn(null);
        Votes.get(mockRestClient,"issueNumber");
    }

    @Test
    public void testGetVotesFromID() throws Exception {
        RestClient mockRestClient = mock(RestClient.class);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode returnedFromService = mapper.createObjectNode();

        returnedFromService.put("self", "someURL");
        returnedFromService.put("id", "1111");
        returnedFromService.put("votes", 12);
        returnedFromService.put("hasVoted", true);

        when(mockRestClient.get(anyString())).thenReturn(returnedFromService);

        final Votes votes = Votes.get(mockRestClient, "issueNumber");

        assertTrue(votes.hasVoted());
        assertEquals("1111",votes.getId());
        assertEquals(12,votes.getVotes());
        assertEquals("someURL",votes.getSelf());
    }

    @Test
    public void testVotesJSON(){
        Votes votes = new Votes(null,getTestJSON());

        assertFalse(votes.hasVoted());
        assertEquals("10", votes.getId());
        assertEquals(0, votes.getVotes());
        assertEquals("https://brainbubble.atlassian.net/rest/api/2/issue/FILTA-43/votes", votes.getSelf());
    }

    @Test
    public void testGetToString(){
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();

        node.put("self","someURL");
        node.put("id","1111");
        node.put("votes",12);
        node.put("hasVoted",true);

        Votes votes = new Votes(null, node);

        assertEquals("12", votes.toString());
    }

    private JsonNode getTestJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();

        node.put("self","https://brainbubble.atlassian.net/rest/api/2/issue/FILTA-43/votes");
        node.put("votes",0);
        node.put("hasVoted",false);
        node.put("id","10");

        return node;
    }

}
