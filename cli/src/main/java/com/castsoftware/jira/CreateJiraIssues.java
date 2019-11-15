package com.castsoftware.jira;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.StreamSupport;

import net.sf.json.JSON;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.ProjectRestClient;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.SearchRestClient;
import com.atlassian.jira.rest.client.domain.BasicComponent;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.BasicStatus;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Component;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.IssueType;
import com.atlassian.jira.rest.client.domain.Project;
import com.atlassian.jira.rest.client.domain.Resolution;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.domain.Transition;
import com.atlassian.jira.rest.client.domain.input.IssueInput;
import com.atlassian.jira.rest.client.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;
import com.castsoftware.jira.pojo.ActionPlanViolation;
import com.castsoftware.jira.util.Configuration;
import com.castsoftware.jira.util.Constants;
import com.castsoftware.jira.util.JiraException;
import com.castsoftware.jira.util.JiraHelper;

/**
 * The Class CreateJiraIssues creates the jira issues
 * 
 * @author FME
 * @version 1.1
 */
public class CreateJiraIssues {

    /** The log. */
    public static Log log = LogFactory.getLog(CreateJiraIssues.class);

    private JiraHelper jiraClient;
    private NullProgressMonitor progressMonitor = new NullProgressMonitor();

    private Project project;
    private IssueType issueType;
    private BasicComponent component;

    private boolean markIssueResolved;
    private String resolutionTxt;

    private String appName;

    /** The total num of issues. */
    private int totalNumOfIssues;

    /** The total num of issues added. */
    private int totalNumOfIssuesAdded;

    /** The total num of issues not added by error. */
    private int totalNumOfIssuesNotAddedByError;

    /** The total num of issues not added by exist. */
    private int totalNumOfIssuesNotAddedByExist;

    /** The total num of issues closed. */
    private int totalNumOfIssuesClosed;

    /** The total num of issues reopened. */
    private int totalNumOfIssuesReopen;

    /** The total num of issues closed. */
    private int totalNumOfUnprioritizedIssues;

    private int getTransitionId(String issueKey, String transitionName) 
    {
        Transition rslt = null;
        IssueRestClient issueClient = jiraClient.getIssueClient();
        Issue is = issueClient.getIssue(issueKey, progressMonitor);

        // if not then transition it to the "To Do" status
        Iterable<Transition> trans = issueClient.getTransitions(is,
                progressMonitor);
        for (Transition t : trans) {
            if (t.getName().equalsIgnoreCase(transitionName)) {
                rslt = t;
                break;
            }
        }
        return rslt == null? 0 : rslt.getId();
    }
 
    private boolean transitionTo(Issue is, String toStatus)
    {
        IssueRestClient issueClient = jiraClient.getIssueClient();
        int inProgressCode = getTransitionId(is.getKey(),toStatus);
        if (inProgressCode > 0)
        {
            issueClient.transition(is, new TransitionInput(inProgressCode),
                    progressMonitor);
            issueClient.addComment(progressMonitor, is.getCommentsUri(),
                    Comment.valueOf(String.format("Issue transitioned to '%s' by CAST", toStatus)));
            return true;
        } 
        return false;
    }
    
    public CreateJiraIssues(String jiraUserName, String jiraUserPassword, String jiraRestApiUrl,
            String pProjectKey, String pIssueType, boolean markIssueResolved, String resolutionTxt,
            String pComponent, HashMap<Integer, ActionPlanViolation> pViolationList)
            throws JiraException {

        try {
            jiraClient = new JiraHelper(jiraRestApiUrl, jiraUserName, jiraUserPassword);

            project = jiraClient.getProjectClient().getProject(pProjectKey, progressMonitor);
            if (project == null) {
                throw new JiraException(
                        String.format("Invalid project short name [%s]", pProjectKey));
            }
            log.info(String.format("Working with Jira project [%s]", project.getName()));

            for (IssueType it : project.getIssueTypes()) {
                if (it.getName().equals(pIssueType)) {
                    issueType = it;
                    break;
                }
            }
            if (issueType == null) {
                throw new JiraException(
                        String.format("Issue type [%s] does not exist in project [%s]",
                                project.getName(), pIssueType));
            } else {
                log.info(String.format("Working with issue type [%s]", issueType.getName()));
            }

            // can the component be assigned for the provided project?
            if (pComponent != null && pComponent.trim().length() > 0) {
                for (BasicComponent c : project.getComponents()) {
                    if (c.getName().equals(pComponent))
                        component = c;
                }
                if (component == null) {
                    throw new JiraException(
                            String.format("Component [%s] does not exist in project [%s]",
                                    project.getName(), pIssueType));
                } else {
                    log.info(String.format("Working with component [%s]", component.getName()));
                }
            } else {
                log.info("Component not being used");
            }

            this.markIssueResolved = markIssueResolved;
            this.resolutionTxt = resolutionTxt;

            String txt = "When Identified as fixed by CAST, the Jira issue will be ";
            if (markIssueResolved) {

                Iterable<Resolution> resolutions = jiraClient.getMetadataClient()
                        .getResolutions(progressMonitor);
                boolean validResotion = false;
                for (Resolution l : resolutions) {
                    if (l.getName().equalsIgnoreCase(this.resolutionTxt)) {
                        validResotion = true;
                    }
                }
                if (!validResotion)
                    throw new JiraException(
                            String.format("Invalid resolution: %s", this.resolutionTxt));

                txt += String.format("marked as resolved with a resolution message of \"%s\"",
                        this.resolutionTxt);
            } else {
                this.resolutionTxt = "Done";
                txt += String.format("closed, with a resolution message of \"%s\"",
                        this.resolutionTxt);
            }
            log.info(txt);
        } catch (URISyntaxException ex) {
            throw new JiraException("Unable to establish a connection with Jira", ex);
        }

        Configuration loadConfig = new Configuration();
        loadConfig.loadPriorityMapping();
        loadConfig.loadCastToJiraFieldsMapping();

        IssueRestClient issueClient = jiraClient.getIssueClient();

        // loop through all CAST violations
        for (int key : pViolationList.keySet()) {
            try { // if an error occurs don't stop the job, just report it
                ActionPlanViolation violation = pViolationList.get(key);
                log.info(String.format("Violation %d: %s", key & 0xFFFFFFFFL,
                        violation.getMetricShortDescription()));
                totalNumOfIssues++;

                int priority = violation.getPriority();
                if (priority < 1 || priority > 3) {
                    log.info(String.format("Unprioritized or Low priority issue, NOT added (%d)",
                            violation.getObjectId()));
                    totalNumOfUnprioritizedIssues++;
                } else {
                    String srchStr = String.format("%s-%d", project.getName(), key);

                    // has the issue been corrected in CAST
                    boolean castIssueCorrected = (violation.getViolationStatus() == 2);

                    SearchRestClient searchClient = jiraClient.getSearchClient();
                    SearchResult searchResult = searchClient.searchJql(String.format(
                            "project = '%s' AND description ~ '%s' ORDER BY priority DESC",
                            project.getKey(), srchStr), progressMonitor);
                    int totalIssuesFound = searchResult.getTotal();
                    if (totalIssuesFound > 0) {
                        for (BasicIssue issue : searchResult.getIssues()) {
                            Issue is = issueClient.getIssue(issue.getKey(), progressMonitor);
                            log.info(String.format("Matching Jira Issue found: %s", is.getKey()));
                            String issueStatusCode = is.getStatus().getName();
                            if (castIssueCorrected
                                    && !issueStatusCode.equalsIgnoreCase("Released")) {
                                // The issue has been marked as done in CAST, do
                                // the same in Jira
                                
                                transitionTo(is, "In Progress");
                                transitionTo(is, "Done");
                                transitionTo(is, "Accepted");
                                if (!transitionTo(is, "Released"))
                                {
                                    throw new JiraException("Unable to Accept issue");
                                }
                                
                                log.info("Issue closed");
                                this.totalNumOfIssuesClosed++;
                                continue;
                            } else if (!castIssueCorrected
                                    && !issueStatusCode.equalsIgnoreCase("To Do")
                                    && !issueStatusCode.equalsIgnoreCase("In Progress")) {
                                // The issue is still open in CAST but marked as
                                // closed in Jira, reopen it now
                                if (!transitionTo(is, "In Progress"))
                                {
                                    throw new JiraException("Can't transition to 'To Do'");
                                }
                                
                                this.totalNumOfIssuesReopen++;
                                log.info("Issue Reopened");
                            } else {
                                log.info("No action required");
                            }

                            this.totalNumOfIssuesNotAddedByExist++;
                        }
                    } else if (!castIssueCorrected) {
                        /* Create a new issue. */

                        loadConfiguration(loadConfig, violation);

                        IssueInputBuilder iib = new IssueInputBuilder(project, issueType);
                        iib.setProjectKey(project.getKey());
                        iib.setIssueType(issueType);

                        iib.setSummary(getJiraFieldComposition(violation, loadConfig,
                                Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION));

                        String description = getJiraFieldComposition(violation, loadConfig,
                                Constants.FIELD_MAPPING_LABEL_DESCRIPTION_JIRA_DESCRIPTION)
                                + srchStr;
                        iib.setDescription(description);

                        if (component != null) {
                            iib.setComponents(component);
                            log.info(String.format("Adding component: [%s]", component.getName()));
                        }

                        IssueInput issue = iib.build();
                        BasicIssue issueObj = issueClient.createIssue(issue, progressMonitor);
                        log.info(String.format("New issue created: %s", issueObj.getKey()));

                        // is the current status "To Do"?
                        Issue is = issueClient.getIssue(issueObj.getKey(), progressMonitor);
                        BasicStatus status = is.getStatus();
                        if (!status.getName().equals("To Do")) {
                            // if not then transition it to the "To Do" status
                            Iterable<Transition> trans = issueClient.getTransitions(is,
                                    progressMonitor);
                            for (Transition t : trans) {
                                if (t.getName().equalsIgnoreCase("to do")) {
                                    issueClient.transition(is, new TransitionInput(t.getId()),
                                            progressMonitor);
                                    issueClient.addComment(progressMonitor, is.getCommentsUri(),
                                            Comment.valueOf("Transition to 'To Do' by CAST"));
                                    break;
                                }
                            }
                        }

                        this.totalNumOfIssuesAdded++;
                    }
                }
            } catch (RestClientException | JiraException ex) {
                this.totalNumOfIssuesNotAddedByError++;
                log.error(ex.getMessage());
            }
        }
    }

    private void loadConfiguration(Configuration loadConfig, ActionPlanViolation violation) {
        // log.info("------------------------------------------------");
        loadConfig.getCastToJiraFieldsMapping(
                Constants.FIELD_MAPPING_LABEL_ADDED_TO_ACTION_PLAN_DATE);
        loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_REASON_DESCRIPTION);
        loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_OUTPUT_DESCRIPTION);
        loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_REFERENCE_DESCRIPTION);

        log.info(loadConfig
                .getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_METRIC_SHORT_DESCRIPTION)
                + violation.getMetricShortDescription());

        loadConfig
                .getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_METRIC_LONG_DESCRIPTION);
        loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_OBJECT_FULL_NAME);
        loadConfig
                .getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_REMEDIATION_DESCRIPTION);
        loadConfig.getCastToJiraFieldsMapping(
                Constants.FIELD_MAPPING_LABEL_REMEDIATION_EXAMPLE_DESCRIPTION);
        loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_TOTAL_DESCRIPTION);
        loadConfig.getCastToJiraFieldsMapping(
                Constants.FIELD_MAPPING_LABEL_VIOLATION_EXAMPLE_DESCRIPTION);
        loadConfig.getCastToJiraFieldsMapping(
                Constants.FIELD_MAPPING_LABEL_ACTION_DEFINED_DESCRIPTION);
        loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_LINE_START);
        loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_LINE_END);
        loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_SOURCE_CODE);
    }

    /**
     * Gets the jira field composition.
     * 
     * @param temp
     *            the temp
     * @param fieldmap
     *            the fieldmap
     * @param fieldType
     *            the field type
     * @return the jira field composition
     */
    public String getJiraFieldComposition(ActionPlanViolation temp, Configuration fieldmap,
            String fieldType) {
        String field = new String();
        String fields;
        if (fieldType.equals(Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION)) {
            fields = fieldmap.getJiraFields(Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION);
        } else {
            fields = fieldmap
                    .getJiraFields(Constants.FIELD_MAPPING_LABEL_DESCRIPTION_JIRA_DESCRIPTION);
        }
        StringBuilder result = new StringBuilder();
        StringTokenizer tokens = new StringTokenizer(fields, ";");

        boolean noteAdded = false;
        while (tokens.hasMoreTokens()) {
            field = tokens.nextToken();
            if (log.isDebugEnabled()) {
                log.debug(" Field to include in Jira " + fieldType + " : " + field);
            }
            /**
             * Add the field label
             */
            if (fields.indexOf(field) != -1) {
                if (fieldType.equals(Constants.FIELD_MAPPING_LABEL_DESCRIPTION_JIRA_DESCRIPTION)) {
                    if (!noteAdded) {
                        // add CAST reference
                        result.append(
                                "*NOTE: This defect was generated by CAST Analytics and approved by for correction. If you need further information on this defect, login to the CAST engineering dashboard for ")
                                .append(this.appName).append("*\n\n");
                        noteAdded = true;
                    }

                    result.append(fieldmap.getCastToJiraFieldsMapping(field)).append(" ");
                }

                /**
                 * Add CAST Action plan data
                 */
                if (field.equals(Constants.FIELD_MAPPING_LABEL_ACTION_DEFINED_DESCRIPTION)) {
                    result.append(temp.getActionDef());
                } else if (field.equals(Constants.FIELD_MAPPING_LABEL_ADDED_TO_ACTION_PLAN_DATE)) {
                    result.append(temp.getActionDate());
                } else if (field.equals(Constants.FIELD_MAPPING_LABEL_METRIC_LONG_DESCRIPTION)) {
                    result.append(temp.getMetricLongDescription());
                } else if (field.equals(Constants.FIELD_MAPPING_LABEL_METRIC_SHORT_DESCRIPTION)) {
                    result.append(temp.getMetricShortDescription());
                } else if (field.equals(Constants.FIELD_MAPPING_LABEL_OBJECT_FULL_NAME)) {
                    result.append(temp.getObjectFullName());
                } else if (field.equals(Constants.FIELD_MAPPING_LABEL_OUTPUT_DESCRIPTION)) {
                    result.append(temp.getOutput());
                } else if (field.equals(Constants.FIELD_MAPPING_LABEL_REASON_DESCRIPTION)) {
                    result.append(temp.getReason());
                } else if (field.equals(Constants.FIELD_MAPPING_LABEL_REFERENCE_DESCRIPTION)) {
                    result.append(temp.getReference());
                } else if (field.equals(Constants.FIELD_MAPPING_LABEL_REMEDIATION_DESCRIPTION)) {
                    result.append(temp.getRemediation());
                } else if (field
                        .equals(Constants.FIELD_MAPPING_LABEL_REMEDIATION_EXAMPLE_DESCRIPTION)) {
                    result.append(temp.getRemediationExample());
                } else if (field.equals(Constants.FIELD_MAPPING_LABEL_TOTAL_DESCRIPTION)) {
                    result.append(temp.getTotales());
                } else if (field
                        .equals(Constants.FIELD_MAPPING_LABEL_VIOLATION_EXAMPLE_DESCRIPTION)) {
                    result.append(temp.getViolationExample());
                } else if (field.equals(Constants.FIELD_MAPPING_LABEL_SOURCE_CODE)) {
                    result.append(temp.getSourceCode());
                } else if (field.equals(Constants.FIELD_MAPPING_LABEL_TECH_CRITERIA)) {
                    result.append(temp.getTechCriteria());
                } else if (field.equals(Constants.FIELD_MAPPING_LABEL_BUSINESS_CRITERIA)) {
                    result.append(temp.getBusinessCriteria());
                }
                /**
                 * do we add a space or carriage return - Summary field gets a
                 * space - Description field get a carriage return
                 */
                if (fieldType.equals(Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION)) {
                    result.append(" ");
                } else {
                    result.append("\n");
                }
            }

        }
        if (result.length() == 0) {
            if (fieldType.equals(Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION)) {
                log.debug("Default field content to include in Jira for Summary field");
                result.append(temp.getMetricShortDescription());
            } else {
                log.debug("Default field content to include in Jira for Description field");
                result.append(temp.getObjectFullName());
                result.append(temp.getActionDate());
                result.append(temp.getActionDef());
                result.append(temp.getMetricLongDescription());
                result.append(temp.getReason());
                result.append(temp.getReference());
                result.append(temp.getRemediation());
                result.append(temp.getViolationExample());
                result.append(temp.getRemediationExample());
                result.append(temp.getTotales());
                result.append(temp.getOutput());
            }

        } else {
            if (fieldType.equals(Constants.FIELD_MAPPING_LABEL_DESCRIPTION_JIRA_DESCRIPTION)) {
                result.append(fieldmap.getCastToJiraFieldsMapping(
                        Constants.FIELD_MAPPING_LABEL_CASTID_DESCRIPTION));
                int maxChar = 30 * 1024;
                int maxLength = (result.length() < maxChar) ? result.length() : maxChar;

                result.setLength(maxLength);
                result.append(" ");
            } else if (fieldType.equals(Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION)) {
                // need to limit to 255 characters
                int maxChar = 250;
                int maxLength = (result.length() < maxChar) ? result.length() : maxChar;

                result.setLength(maxLength);
            }

        }

        return result.toString();
    }

    public int getTotalNumOfIssues() {
        return totalNumOfIssues;
    }

    public int getTotalNumOfIssuesAdded() {
        return totalNumOfIssuesAdded;
    }

    public int getTotalNumOfIssuesNotAddedByError() {
        return totalNumOfIssuesNotAddedByError;
    }

    public int getTotalNumOfIssuesNotAddedByExist() {
        return totalNumOfIssuesNotAddedByExist;
    }

    public int getTotalNumOfIssuesClosed() {
        return totalNumOfIssuesClosed;
    }

    public int getTotalNumOfUnprioritizedIssues() {
        return totalNumOfUnprioritizedIssues;
    }

    public int getTotalNumOfIssuesReopen() {
        return totalNumOfIssuesReopen;
    }

}
