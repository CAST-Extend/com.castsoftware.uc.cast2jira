/* Update MMA 2025-05-20: use of Jackson for JSON handling */

package net.rcarz.jiraclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class TimeTrackingTest {

  private final Issue issue = new Issue(null, Utils.getTestIssue());
  private final TimeTracking time = issue.getTimeTracking();

    public TimeTrackingTest() throws JsonProcessingException {
    }

    @Test
  public void testAttributeMappings() {
    assertEquals("1w", time.getOriginalEstimate());
    assertEquals(144000, time.getOriginalEstimateSeconds());

    assertEquals("2d", time.getRemainingEstimate());
    assertEquals(57600, time.getRemainingEstimateSeconds());

    assertEquals("3d", time.getTimeSpent());
    assertEquals(86400, time.getTimeSpentSeconds());
  }

  @Test
  public void testCreateTimeTracking() throws Exception {

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode();

    node.put("originalEstimate", "1 day");
    node.put("remainingEstimate", "2 days");
    node.put("timeSpent", "3 days");
    node.put("originalEstimateSeconds", 12);
    node.put("remainingEstimateSeconds", 10);
    node.put("timeSpentSeconds", 14);

    TimeTracking timeTracking = new TimeTracking(node);
    assertEquals("1 day", timeTracking.getOriginalEstimate());
    assertEquals("2 days", timeTracking.getRemainingEstimate());
    assertEquals("3 days", timeTracking.getTimeSpent());
    assertEquals(14, timeTracking.getTimeSpentSeconds());
    assertEquals(12, timeTracking.getOriginalEstimateSeconds());
    assertEquals(10, timeTracking.getRemainingEstimateSeconds());
  }

  @Test
  public void testGettersAndSetters() throws Exception {

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode();

    node.put("originalEstimate", "1 day");
    node.put("remainingEstimate", "2 days");
    node.put("timeSpent", "3 days");
    node.put("originalEstimateSeconds", 12);
    node.put("remainingEstimateSeconds", 10);
    node.put("timeSpentSeconds", 14);

    TimeTracking timeTracking = new TimeTracking(node);
    assertEquals("1 day", timeTracking.getOriginalEstimate());
    assertEquals("2 days", timeTracking.getRemainingEstimate());
    assertEquals("3 days", timeTracking.getTimeSpent());
    assertEquals(14, timeTracking.getTimeSpentSeconds());
    assertEquals(12, timeTracking.getOriginalEstimateSeconds());
    assertEquals(10, timeTracking.getRemainingEstimateSeconds());

    timeTracking.setOriginalEstimate("10 days");
    timeTracking.setOrignalEstimateSeconds(1000);
    timeTracking.setRemainingEstimate("5 days");
    timeTracking.setRemainingEstimateSeconds(5904);

    assertEquals("10 days", timeTracking.getOriginalEstimate());
    assertEquals("5 days", timeTracking.getRemainingEstimate());
    assertEquals("3 days", timeTracking.getTimeSpent());
    assertEquals(14, timeTracking.getTimeSpentSeconds());
    assertEquals(1000, timeTracking.getOriginalEstimateSeconds());
    assertEquals(5904, timeTracking.getRemainingEstimateSeconds());
  }

  @Test
  public void testEmptyValues() throws Exception {
    TimeTracking timeTracking = new TimeTracking();
    assertNull(timeTracking.getOriginalEstimate());
    assertNull(timeTracking.getRemainingEstimate());
    assertNull(timeTracking.getTimeSpent());
  }

  @Test
  public void testTimeTrackingFromTimeTracking() throws Exception {

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode();

    node.put("originalEstimate", "1 day");
    node.put("remainingEstimate", "2 days");
    node.put("timeSpent", "3 days");
    node.put("originalEstimateSeconds", 12);
    node.put("remainingEstimateSeconds", 10);
    node.put("timeSpentSeconds", 14);

    TimeTracking timeTracking = new TimeTracking(node);
    assertEquals("1 day", timeTracking.getOriginalEstimate());
    assertEquals("2 days", timeTracking.getRemainingEstimate());
    assertEquals("3 days", timeTracking.getTimeSpent());
    assertEquals(14, timeTracking.getTimeSpentSeconds());
    assertEquals(12, timeTracking.getOriginalEstimateSeconds());
    assertEquals(10, timeTracking.getRemainingEstimateSeconds());

    TimeTracking updated = new TimeTracking(timeTracking);

    assertEquals("1 day", updated.getOriginalEstimate());
    assertEquals("2 days", updated.getRemainingEstimate());
    assertEquals("3 days", updated.getTimeSpent());
    assertEquals(14, updated.getTimeSpentSeconds());
    assertEquals(12, updated.getOriginalEstimateSeconds());
    assertEquals(10, updated.getRemainingEstimateSeconds());
  }

  @Test
  public void testToJSONObject() throws Exception {

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode();

    node.put("originalEstimate", "1 day");
    node.put("remainingEstimate", "2 days");
    node.put("originalEstimateSeconds", 12);
    node.put("remainingEstimateSeconds", 10);

    TimeTracking timeTracking = new TimeTracking(node);
    final JsonNode jsonObject = timeTracking.toJsonNode();

    assertEquals(node,jsonObject);
  }

}
