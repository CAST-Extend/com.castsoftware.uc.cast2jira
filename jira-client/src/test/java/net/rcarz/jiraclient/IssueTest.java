/* Update MMA 2025-05-20: use of Jackson for JSON handling */

package net.rcarz.jiraclient;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

public class IssueTest {

    /**
     * If no exception thrown the test is passed.
     */
    @Test
    public void testCreateIssue() throws JsonProcessingException {
        new Issue(null, Utils.getTestIssue());
    }

    @Test
    public void testGetIssueStatus() throws JsonProcessingException {

        String statusName = "To Do";
        String statusID = "10004";
        String description = "Issue is currently in progress.";
        String iconURL = "https://brainbubble.atlassian.net/images/icons/statuses/open.png";

        Issue issue = new Issue(null, Utils.getTestIssue());
        assertNotNull(issue.getStatus());
        assertEquals(description, issue.getStatus().getDescription());
        assertEquals(iconURL, issue.getStatus().getIconUrl());

        assertEquals(statusName, issue.getStatus().getName());
        assertEquals(statusID, issue.getStatus().getId());
    }

    @Test
    public void getReporter() throws JsonProcessingException {
        Issue issue = new Issue(null, Utils.getTestIssue());
        assertNotNull(issue.getReporter());

        User reporter = issue.getReporter();

        assertEquals("Joseph McCarthy", reporter.getDisplayName());
        assertEquals("joseph", reporter.getName());
        assertTrue(reporter.isActive());
        assertEquals("joseph.b.mccarthy2012@googlemail.com", reporter.getEmail());

        Map<String, String> avatars = reporter.getAvatarUrls();

        assertNotNull(avatars);
        assertEquals(4, avatars.size());

        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=16", avatars.get("16x16"));
        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=24", avatars.get("24x24"));
        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=32", avatars.get("32x32"));
        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=48", avatars.get("48x48"));
    }

    @Test
    public void testGetIssueType() throws JsonProcessingException {
        Issue issue = new Issue(null, Utils.getTestIssue());
        IssueType issueType = issue.getIssueType();
        assertNotNull(issueType);

        assertFalse(issueType.isSubtask());
        assertEquals("Story", issueType.getName());
        assertEquals("7", issueType.getId());
        assertEquals("https://brainbubble.atlassian.net/images/icons/issuetypes/story.png", issueType.getIconUrl());
        assertEquals("This is a test issue type.", issueType.getDescription());
    }

    @Test
    public void testGetVotes() throws JsonProcessingException {
        Issue issue = new Issue(null, Utils.getTestIssue());
        Votes votes = issue.getVotes();
        assertNotNull(votes);

        assertFalse(votes.hasVoted());
        assertEquals(0, votes.getVotes());
    }

    @Test
    public void testGetWatchers() throws JsonProcessingException {
        Issue issue = new Issue(null, Utils.getTestIssue());
        Watches watches = issue.getWatches();

        assertNotNull(watches);

        assertFalse(watches.isWatching());
        assertEquals(0, watches.getWatchCount());
        assertEquals("https://brainbubble.atlassian.net/rest/api/2/issue/FILTA-43/watchers", watches.getSelf());
    }

    @Test
    public void testGetVersion() throws JsonProcessingException {
        Issue issue = new Issue(null, Utils.getTestIssue());
        List<Version> versions = issue.getFixVersions();

        assertNotNull(versions);
        assertEquals(1, versions.size());

        Version version = versions.get(0);

        Assert.assertEquals("10200", version.getId());
        Assert.assertEquals("1.0", version.getName());
        assertFalse(version.isArchived());
        assertFalse(version.isReleased());
        Assert.assertEquals("2013-12-01", version.getReleaseDate());
        Assert.assertEquals("First Full Functional Build", version.getDescription());
    }

    @Test
    public void testPlainTimeTracking() throws JsonProcessingException {
        Issue issue = new Issue(null,Utils.getTestIssue());

        assertEquals(Integer.valueOf(144000), issue.getTimeEstimate());
        assertEquals(Integer.valueOf(86400), issue.getTimeSpent());
    }

    @Test
    public void testCreatedDate() throws JsonProcessingException {
        Issue issue = new Issue(null,Utils.getTestIssue());
        assertEquals(new DateTime(2013, 9, 29, 20, 16, 19, 854, DateTimeZone.forOffsetHours(1)).toDate(), issue.getCreatedDate());
    }

    @Test
    public void testUpdatedDate() throws JsonProcessingException {
      Issue issue = new Issue(null,Utils.getTestIssue());
      assertEquals(new DateTime(2013, 10, 9, 22, 24, 55, 961, DateTimeZone.forOffsetHours(1)).toDate(), issue.getUpdatedDate());
    }

    @Test
    public void testAddRemoteLink() throws JiraException, JsonProcessingException {
        final TestableRestClient restClient = new TestableRestClient();
        Issue issue = new Issue(restClient, Utils.getTestIssue());
        issue.addRemoteLink("test-url", "test-title", "test-summary");
        assertEquals("/rest/api/latest/issue/FILTA-43/remotelink", restClient.postPath);

        ObjectMapper mapper = new ObjectMapper();
        String actualPayload = mapper.writeValueAsString(restClient.postPayload);

        String expectedPayload = "{\"object\":{\"url\":\"test-url\",\"title\":\"test-title\",\"summary\":\"test-summary\"}}";
        assertEquals(expectedPayload, actualPayload);
    }


    @Test
    public void testRemoteLink() throws JiraException, JsonProcessingException {
        final TestableRestClient restClient = new TestableRestClient();
        Issue issue = new Issue(restClient, Utils.getTestIssue());
        issue.remoteLink()
                .globalId("gid")
                .title("test-title")
                .summary("summary")
                .application("app-type", "app-name")
                .relationship("fixes")
                .icon("icon", "icon-url")
                .status(true, "status-icon", "status-title", "status-url")
                .create();
        assertEquals("/rest/api/latest/issue/FILTA-43/remotelink", restClient.postPath);

        ObjectMapper mapper = new ObjectMapper();
        String actualPayload = mapper.writeValueAsString(restClient.postPayload);

        String expectedPayload =
                "{\"globalId\":\"gid\"," +
                "\"application\":" +
                        "{\"type\":\"app-type\",\"name\":\"app-name\"}," +
                "\"relationship\":\"fixes\"," +
                "\"object\":{" +
                        "\"url\":\"gid\"," +
                        "\"title\":\"test-title\"," +
                        "\"summary\":\"summary\"," +
                        "\"icon\":" +
                            "{\"url16x16\":\"icon\",\"title\":\"icon-url\"}," +
                        "\"status\":{\"resolved\":true,\"icon\":" +
                            "{\"title\":\"status-title\",\"url16x16\":\"status-icon\",\"link\":\"status-url\"}" +
                "}}}";

        assertEquals(expectedPayload, actualPayload);
    }


    private static class TestableRestClient extends RestClient {

        public String postPath = "not called";
        public JsonNode postPayload = NullNode.getInstance();

        public TestableRestClient() {
            super(null, null);
        }

        @Override
        public JsonNode post(String path, JsonNode payload) {
            postPath = path;
            postPayload = payload;
            return null;
        }

    }

}
