package com.castsoftware.jira;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.castsoftware.jira.pojo.ActionPlanViolation;
import com.castsoftware.jira.util.Configuration;
import com.castsoftware.jira.util.Constants;
import com.castsoftware.jira.util.CustomField;
import com.castsoftware.jira.util.JiraException;
import com.castsoftware.jira.util.JiraHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * The Class CreateJiraIssues creates the jira issues
 * 
 * @author FME
 * @version 1.1
 * @param <JiraHelper>
 */
public class CreateJiraIssues {

    /** The log. */
    public static Log log = LogFactory.getLog(CreateJiraIssues.class);

    private JiraHelper jiraClient;
    // private NullProgressMonitor progressMonitor = new NullProgressMonitor();

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

    private List<Transition> getTransitions(Issue is)
            throws InterruptedException, ExecutionException {
        IssueRestClient issueClient = jiraClient.getIssueClient();
        Iterable<Transition> trns = issueClient.getTransitions(is).get();

        List<Transition> rslt = Lists.newArrayList(trns);

        return rslt;
    }

    private Transition nextTransitionId(Issue is, List<String> whiteList, List<String> blackList)
            throws InterruptedException, ExecutionException, JiraException {
        List<Transition> trns = getTransitions(is);

        transition : for (Transition t : trns) {
            for (String s : blackList) {
                if (s.equalsIgnoreCase(t.getName())) {
                    continue transition;
                }
            }
            for (String s : whiteList) {
                if (t.getName().equalsIgnoreCase(s)) {
                    return t;
                }
            }
        }

        throw new JiraException(String.format("Transition not found: %s %s", whiteList.toString(),
                is.getStatus().getName()));
    }

    private boolean transitionTo(Issue is, Transition toStatus) {
        boolean rslt = false;
        IssueRestClient issueClient = jiraClient.getIssueClient();
        int inProgressCode;
        try {
            inProgressCode = getTransitionId(is.getKey(), toStatus);
            if (inProgressCode > 0) {
                issueClient.transition(is, new TransitionInput(inProgressCode));
                issueClient.addComment(is.getCommentsUri(), Comment.valueOf(
                        String.format("Issue transitioned to '%s' by CAST", toStatus.getName())));
                rslt = true;
            }
        } catch (InterruptedException | ExecutionException e) {
            rslt = false;
        }
        return rslt;
    }

    /**
     * Access Jira and retrieve the transition id
     * 
     * @param issueKey
     * @param transitionName
     * @return the id of the transition
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private int getTransitionId(String issueKey, Transition trnsId)
            throws ExecutionException, InterruptedException {
        Transition rslt = null;
        IssueRestClient issueClient = jiraClient.getIssueClient();
        Issue is = issueClient.getIssue(issueKey).get();

        // if not then transition it to the "To Do" status
        Iterable<Transition> trans = issueClient.getTransitions(is).get();
        for (Transition t : trans) {
            if (t.getName().equalsIgnoreCase(trnsId.getName())) {
                rslt = t;
                break;
            }
        }
        return rslt == null ? 0 : rslt.getId();
    }

    /**
     * Method used to create and maintain Jira issues.
     * 
     * @param jiraUserName
     * @param jiraUserPassword
     * @param jiraRestApiUrl
     * @param pProjectKey
     * @param pIssueType
     * @param markIssueResolved
     * @param resolutionTxt
     * @param pComponent
     * @param pViolationList
     * @throws JiraException
     */
    public CreateJiraIssues(String jiraUserName, String jiraUserPassword, String jiraRestApiUrl,
            String pProjectKey, String pIssueType, boolean markIssueResolved, String resolutionTxt,
            String pComponent, HashMap<Integer, ActionPlanViolation> pViolationList)
            throws JiraException {

        try {
            jiraClient = new JiraHelper(jiraRestApiUrl, jiraUserName, jiraUserPassword);

            try {
                project = jiraClient.getProjectClient().getProject(pProjectKey).get();
            } catch (ExecutionException e) {
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

                Iterable<Resolution> resolutions;
                boolean validResotion = false;
                try {
                    resolutions = jiraClient.getMetadataClient().getResolutions().get();
                    for (Resolution l : resolutions) {
                        if (l.getName().equalsIgnoreCase(this.resolutionTxt)) {
                            validResotion = true;
                        }
                    }
                } catch (ExecutionException e) {
                    throw new JiraException(
                            String.format("Invalid resolution: %s", this.resolutionTxt));
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
        } catch (URISyntaxException | InterruptedException ex) {
            throw new JiraException("Unable to establish a connection with Jira", ex);
        }

        /**
         * get workflow configuration
         */
        Configuration config = new Configuration();
        String statusOpen = config.getWorkflow(Constants.WORKFLOW_STATUS_OPEN);
        String statusReopen = config.getWorkflow(Constants.WORKFLOW_STATUS_REOPEN);
        String statusDone = config.getWorkflow(Constants.WORKFLOW_STATUS_DONE);
        String statusProgress = config.getWorkflow(Constants.WORKFLOW_STATUS_PROGRESS);
        List<String> transitionDone = Arrays
                .asList(config.getWorkflow(Constants.WORKFLOW_TRANSITION_DONE).split(";"));
        List<String> transitionReopen = Arrays
                .asList(config.getWorkflow(Constants.WORKFLOW_TRANSITION_REOPEN).split(";"));
        List<String> transitionBlacklist = Arrays
                .asList(config.getWorkflow(Constants.WORKFLOW_TRANSITION_BLACKLIST).split(";"));
        boolean debugWorkflow = Boolean.valueOf(config.getWorkflow(Constants.WORKFLOW_DEBUG));

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
                            project.getKey(), srchStr)).claim();
                    int totalIssuesFound = searchResult.getTotal();
                    if (totalIssuesFound > 0) {
                        for (BasicIssue issue : searchResult.getIssues()) {
                            Issue is = issueClient.getIssue(issue.getKey()).claim();

                            log.info(String.format("Matching Jira Issue found: %s", is.getKey()));
                            String issueStatusCode = is.getStatus().getName();
                            if (castIssueCorrected
                                    && !issueStatusCode.equalsIgnoreCase(statusDone)) {
                                // The issue has been marked as done in CAST, do
                                // the same in Jira

                                Transition transitTo = null;
                                while (true) {
                                    transitTo = nextTransitionId(is, transitionDone,
                                            transitionBlacklist);
                                    if (!transitionTo(is, transitTo)) {
                                        throw new JiraException(String.format(
                                                "Unable to transition to %s", transitTo.getName()));
                                    }

                                    is = issueClient.getIssue(issue.getKey()).get();
                                    if (issueStatusCode.equals(is.getStatus().getName())
                                            || statusDone
                                                    .equalsIgnoreCase(is.getStatus().getName())) {
                                        break;
                                    }
                                    issueStatusCode = is.getStatus().getName();
                                }
                                log.info("Issue closed");
                                this.totalNumOfIssuesClosed++;
                                continue;
                            } else if (!castIssueCorrected
                                    && !(issueStatusCode.equalsIgnoreCase(statusOpen)
                                            || issueStatusCode.equalsIgnoreCase(statusReopen))) {
                                // The issue is still open in CAST but marked as
                                // closed in Jira, reopen it now

                                Transition transitTo = null;
                                while (true) {
                                    transitTo = nextTransitionId(is, transitionReopen,
                                            transitionBlacklist);
                                    if (!transitionTo(is, transitTo)) {
                                        throw new JiraException(String.format(
                                                "Unable to transition to %s", transitTo.getName()));
                                    }

                                    is = issueClient.getIssue(issue.getKey()).get();
                                    if (issueStatusCode.equals(is.getStatus().getName())
                                            || statusReopen
                                                    .equalsIgnoreCase(is.getStatus().getName())) {
                                        break;
                                    }
                                    issueStatusCode = is.getStatus().getName();
                                }

                                this.totalNumOfIssuesReopen++;
                                log.info("Issue Reopened");
                            } else {
                                log.info("No action required");
                            }

                            this.totalNumOfIssuesNotAddedByExist++;
                        }
                    } else if (debugWorkflow || !castIssueCorrected) {
                        /* Create a new issue. */

                        loadConfiguration(config, violation);

                        IssueInputBuilder iib = new IssueInputBuilder(project, issueType);
                        iib.setProjectKey(project.getKey());
                        iib.setIssueType(issueType);

                        iib.setSummary(getJiraFieldComposition(violation, config,
                                Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION));

                        String description = getJiraFieldComposition(violation, config,
                                Constants.FIELD_MAPPING_LABEL_DESCRIPTION_JIRA_DESCRIPTION)
                                + srchStr;
                        iib.setDescription(description);

                        if (component != null) {
                            iib.setComponents(component);
                            log.info(String.format("Adding component: [%s]", component.getName()));
                        }

                        // set custom fields here
                        List<CustomField> cfl = config.getCustomFields();
                        if (cfl != null) {
                            for (CustomField cf : cfl) {
                                String jiraField = String.format("%s.JiraField", cf.getName()); 
                                String value = getJiraFieldComposition(violation, config,jiraField);
                                if (value != null && !value.isEmpty()) {
                                    switch (cf.getType().toLowerCase()) {
                                        case CustomField.CUSTOM_TEXT_FIELD_TYPE :
                                            iib.setFieldValue(cf.getName(), value);
                                            break;
                                        case CustomField.CUSTOM_SELECT_FIELD_TYPE :
                                            ComplexIssueInputFieldValue cv = ComplexIssueInputFieldValue
                                                    .with("value", value.replace("\n", ""));
                                            iib.setFieldValue(cf.getName(), cv);
                                            break;
                                    }
                                }
                            }
                        }

                        IssueInput issue = iib.build();
                        BasicIssue issueObj = issueClient.createIssue(issue).get();
                        log.info(String.format("New issue created: %s", issueObj.getKey()));

                        // add comment
                        Issue is = issueClient.getIssue(issueObj.getKey()).get();
                        issueClient.addComment(is.getCommentsUri(),
                                Comment.valueOf("Issue create by CAST"));

                        this.totalNumOfIssuesAdded++;
                    } else {
                        log.info("Issue has already been closed in AIP");
                    }
                }
            } catch (RestClientException | JiraException | InterruptedException
                    | ExecutionException ex) {
                this.totalNumOfIssuesNotAddedByError++;
                log.error(ex.getMessage());
            }
        }
    }

    /**
     * Fill in the issue using the template
     * 
     * @param loadConfig
     * @param violation
     */
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
     * @throws JiraException 
     */
    public String getJiraFieldComposition(ActionPlanViolation temp, Configuration fieldmap,
            String fieldType) throws JiraException {
        String field = new String();
        String fields=null;
        
        if (fieldType.endsWith(".JiraField"))
        {
            fields = fieldmap.getJiraFields(fieldType);
        } else {
            return "";
        }
//        if (fieldType.equals(Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION)) {
//            fields = fieldmap.getJiraFields(Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION);
//        } else {
//            fields = fieldmap
//                    .getJiraFields(Constants.FIELD_MAPPING_LABEL_DESCRIPTION_JIRA_DESCRIPTION);
//        }
        
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
                
                // check custom fields 
                List<CustomField> cfl = fieldmap.getCustomFields();
                if (cfl != null) {
                    for (CustomField cf: cfl)
                    {
                        String name=String.format("%s.label",cf.getName());
                        if (field.equals(name)) {
                            String value = fieldmap.getFieldValue(name);
                            result.append(value);
                        }
                    }
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

    /**
     * Return the total number of issues retrieved from CAST
     * 
     * @return
     */
    public int getTotalNumOfIssues() {
        return totalNumOfIssues;
    }

    /**
     * Total number of issues add to Jira
     * 
     * @return
     */
    public int getTotalNumOfIssuesAdded() {
        return totalNumOfIssuesAdded;
    }

    /**
     * return the total number of errors encountered while attempting to add an
     * issue
     * 
     * @return
     */
    public int getTotalNumOfIssuesNotAddedByError() {
        return totalNumOfIssuesNotAddedByError;
    }

    /**
     * The number of issue that have already exist and were not added to Jira
     * 
     * @return
     */
    public int getTotalNumOfIssuesNotAddedByExist() {
        return totalNumOfIssuesNotAddedByExist;
    }

    /**
     * Total number of issues closed in Jira
     * 
     * @return
     */
    public int getTotalNumOfIssuesClosed() {
        return totalNumOfIssuesClosed;
    }

    /**
     * field not being used?
     * 
     * @return
     */
    public int getTotalNumOfUnprioritizedIssues() {
        return totalNumOfUnprioritizedIssues;
    }

    public int getTotalNumOfIssuesReopen() {
        return totalNumOfIssuesReopen;
    }

}