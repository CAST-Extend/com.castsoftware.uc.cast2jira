/* Update MMA 2025-05-20: fixed non-working tests by removing live searches to Jira */

package net.rcarz.jiraclient;

import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.util.Collections;

public class SearchTest {

  @Test
  public void testSimpleSearch() throws Exception {
    // Mock JiraClient
    JiraClient jira = mock(JiraClient.class);

    String key = "JRA-1";

    // Mock Resolution with name "Fixed"
    Resolution resolution = mock(Resolution.class);
    when(resolution.getName()).thenReturn("Fixed");

    // Mock Issue with expected key and resolution
    Issue issue = mock(Issue.class);
    when(issue.getKey()).thenReturn(key);
    when(issue.getResolution()).thenReturn(resolution);

    // Create a mock for SearchResult without calling the constructor
    Issue.SearchResult searchResult = PowerMockito.mock(Issue.SearchResult.class);

    java.lang.reflect.Field issuesField = Issue.SearchResult.class.getDeclaredField("issues");
    issuesField.setAccessible(true);
    issuesField.set(searchResult, Collections.singletonList(issue));

    // Set other fields similarly
    java.lang.reflect.Field startField = Issue.SearchResult.class.getDeclaredField("start");
    startField.setAccessible(true);
    startField.setInt(searchResult, 0);

    java.lang.reflect.Field maxField = Issue.SearchResult.class.getDeclaredField("max");
    maxField.setAccessible(true);
    maxField.setInt(searchResult, 50);

    java.lang.reflect.Field totalField = Issue.SearchResult.class.getDeclaredField("total");
    totalField.setAccessible(true);
    totalField.setInt(searchResult, 1);

    // Mock the constructor call so when the SearchResult constructor is called by JiraClient, the spy is returned
    PowerMockito.whenNew(Issue.SearchResult.class).withAnyArguments().thenReturn(searchResult);

    // Mock jira.searchIssues to return the mocked SearchResult
    when(jira.searchIssues(eq("key = " + key))).thenReturn(searchResult);

    // Call the mocked method
    Issue.SearchResult result = jira.searchIssues("key = " + key);

    assertNotNull(result);
    assertEquals("should return exactly 1 issue", 1, result.issues.size());
    assertEquals("with key " + key, key, result.issues.get(0).getKey());
    assertEquals("and resolution Fixed", "Fixed", result.issues.get(0).getResolution().getName());
  }

  @Test
  public void testExpandingChangeLogInSearch() throws Exception {
    // Create a mock JiraClient
    JiraClient jira = mock(JiraClient.class);

    String key = "JRA-2048";

    // Create a mocked ChangeLogItem with "Closed" in toString
    ChangeLogItem closedItem = mock(ChangeLogItem.class);
    when(closedItem.getToString()).thenReturn("Closed");

    // Create ChangeLogEntry that contains the mocked ChangeLogItem
    ChangeLogEntry entry = mock(ChangeLogEntry.class);
    when(entry.getItems()).thenReturn(java.util.Collections.singletonList(closedItem));

    // Create ChangeLog containing the single entry
    ChangeLog changeLog = mock(ChangeLog.class);
    when(changeLog.getEntries()).thenReturn(java.util.Collections.singletonList(entry));

    // Create a mocked Issue with the ChangeLog
    Issue issue = mock(Issue.class);
    when(issue.getChangeLog()).thenReturn(changeLog);

    // Create a mock for SearchResult without calling the constructor
    Issue.SearchResult searchResult = PowerMockito.mock(Issue.SearchResult.class);

    java.lang.reflect.Field issuesField = Issue.SearchResult.class.getDeclaredField("issues");
    issuesField.setAccessible(true);
    issuesField.set(searchResult, Collections.singletonList(issue));

    // Set other fields similarly
    java.lang.reflect.Field startField = Issue.SearchResult.class.getDeclaredField("start");
    startField.setAccessible(true);
    startField.setInt(searchResult, 0);

    java.lang.reflect.Field maxField = Issue.SearchResult.class.getDeclaredField("max");
    maxField.setAccessible(true);
    maxField.setInt(searchResult, 50);

    java.lang.reflect.Field totalField = Issue.SearchResult.class.getDeclaredField("total");
    totalField.setAccessible(true);
    totalField.setInt(searchResult, 1);

    // Mock the constructor call so when the SearchResult constructor is called by JiraClient, the spy is returned
    PowerMockito.whenNew(Issue.SearchResult.class).withAnyArguments().thenReturn(searchResult);

    // Mock jira.searchIssues to return the mocked SearchResult when called with the query and changelog expand param
    when(jira.searchIssues(eq("key = " + key), isNull(), eq("changelog"))).thenReturn(searchResult);

    // Call the mocked method
    Issue.SearchResult result = jira.searchIssues("key = " + key, null, "changelog");

    assertEquals("should return exactly 1 issue", 1, result.issues.size());

    ChangeLog resultChangeLog = result.issues.get(0).getChangeLog();
    assertNotNull("returned issue should have a changeLog", resultChangeLog);

    boolean closedStatusFound = false;
    for (ChangeLogEntry e : resultChangeLog.getEntries()) {
      for (ChangeLogItem item : e.getItems()) {
        closedStatusFound |= "Closed".equals(item.getToString());
      }
    }

    assertTrue("ChangeLog should contain Closed entry", closedStatusFound);
  }

}
