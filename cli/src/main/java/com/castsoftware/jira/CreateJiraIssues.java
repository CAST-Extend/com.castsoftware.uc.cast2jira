package com.castsoftware.jira;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.Component;
import net.rcarz.jiraclient.Field;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.Issue.FluentTransition;
import net.rcarz.jiraclient.IssueType;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.Project;
import net.rcarz.jiraclient.Resolution;
import net.rcarz.jiraclient.Resource;
import net.rcarz.jiraclient.Status;
import net.rcarz.jiraclient.Transition;
import net.sf.json.JSON;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.castsoftware.jira.pojo.ActionPlanViolation;
import com.castsoftware.jira.util.Configuration;
import com.castsoftware.jira.util.Constants;

/**
 * The Class CreateJiraIssues creates the jira issues
 * 
 * @author FME
 * @version 1.1
 */
public class CreateJiraIssues
{

	/** The log. */
	public static Log log = LogFactory.getLog(CreateJiraIssues.class);

	/** The jira user name. */
	private String jiraUserName;

	/** The jira user password. */
	private String jiraUserPassword;

	/** The jira rest api url. */
	private String jiraRestApiUrl;

	/** The map. */
	private HashMap<Integer, ActionPlanViolation> map;

	/** The jira project name. */
	private String jiraProjectName;

	/** The issue type. */
	private String issueType;

	/** The assignee name. */
	// private String assigneeName;

	private String appName;

	/** The jira. */
	private JiraClient jira;

	private boolean markIssueResolved;
	private String resolutionTxt;
	private String component;

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

	/** The total num of issues closed. */
	private int totalNumOfUnprioritizedIssues;

	/**
	 * Instantiates a new creates the jira issues.
	 * 
	 * @param jiraUserName
	 *            the jira user name
	 * @param jiraUserPassword
	 *            the jira user password
	 * @param jiraRestApiUrl
	 *            the jira rest api url
	 * @param map
	 *            the map
	 * @param jiraProjectName
	 *            the jira project name
	 * @param issueType
	 *            the issue type
	 * @param assigneeName
	 *            the assignee name
	 * @throws Exception
	 *             the exception
	 */

	public CreateJiraIssues(String jiraUserName, String jiraUserPassword, String jiraRestApiUrl,
			HashMap<Integer, ActionPlanViolation> map, String jiraProjectName, String issueType, String appName,
			boolean closeIssueOnResolution, String resolutionTxt, String component) throws Exception
	{
		setJiraUserName(jiraUserName);
		setJiraUserPassword(jiraUserPassword);
		setAppName(appName);
		setJiraRestApiUrl(jiraRestApiUrl);
		setMap(map);
		setJiraProjectName(jiraProjectName);
		if (issueType == null || issueType.trim().length() == 0) {
			setIssueType(Constants.JIRA_DEFAULT_ISSUE_TYPE);
		} else {
			setIssueType(issueType);
		}
		setMarkIssueResolved(closeIssueOnResolution);
		setResolutionTxt(resolutionTxt);
		setComponent(component);

		String txt = "When Identified as fixed by CAST, ";
		if (closeIssueOnResolution) {
			txt = txt + String.format(
					"issues will NOT be closed, insead marked as resolved with a resolution message of \"%s\"",
					resolutionTxt);
		} else {
			txt = txt + String.format("issues will closed, with a resolution message of \"%s\"", resolutionTxt);
		}
		log.info(txt);

		log.debug("Sending BasicCredentials to Jira");
		BasicCredentials creds = new BasicCredentials(getJiraUserName(), getJiraUserPassword());
		log.info("Jira BasicCredentials done");

		log.debug("Starting JiraClient connection!");
		jira = new JiraClient(getJiraRestApiUrl(), creds);
		log.info("Connected to Jira Client");
		setCreateIssuesInJira();

	}

	/**
	 * Sets the create issues in jira.
	 * 
	 * @return true, if successful
	 * @throws Exception
	 *             the exception
	 */
	public boolean setCreateIssuesInJira() throws Exception
	{
		ActionPlanViolation temp;
		Configuration loadConfig = new Configuration();
		loadConfig.loadPriorityMapping();
		loadConfig.loadCastToJiraFieldsMapping();
		setTotalNumOfIssues(getMap().size());

		log.info("Collecting Resolution Id's");
		List<Resolution> rList = new ArrayList<Resolution>();
		for (int ii = 1; ii < 20000; ii++) {
			Resolution r = null;
			try {
				r = Resolution.get(jira.getRestClient(), String.format("%s", ii));
				rList.add(r);
			} catch (JiraException e) {
				if (!e.getMessage().contains("Failed to retrieve resolution")) {
					throw e;
				} else {
					if (ii > 50 && ii < 10000) {
						ii = 10000 - 1;
					} else if (ii < 50) {
						continue;
					} else {
						if (ii % 100 == 0) {
							break;
						}
						if (ii % 100 == 0) {
							ii++;
						}
						ii = (((ii + 99) / 100) * 100) - 1;
					}
				}
			}
		}
		for (Resolution l : rList) {
			log.info(String.format("%s, (%s)", l.getName(), l.getId()));
		}
		log.info(String.format("Found %d resolutions", rList.size()));

		for (int key : getMap().keySet()) {
			temp = getMap().get(key);
			int priority = temp.getPriority();

			log.info("------------------------------------------------");
			loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_ADDED_TO_ACTION_PLAN_DATE);
			loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_REASON_DESCRIPTION);
			loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_OUTPUT_DESCRIPTION);
			loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_REFERENCE_DESCRIPTION);

			log.info(loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_METRIC_SHORT_DESCRIPTION)
					+ temp.getMetricShortDescription());

			loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_METRIC_LONG_DESCRIPTION);
			loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_OBJECT_FULL_NAME);
			loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_REMEDIATION_DESCRIPTION);
			loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_REMEDIATION_EXAMPLE_DESCRIPTION);
			loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_TOTAL_DESCRIPTION);
			loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_VIOLATION_EXAMPLE_DESCRIPTION);
			loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_ACTION_DEFINED_DESCRIPTION);
			loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_LINE_START);
			loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_LINE_END);
			loadConfig.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_SOURCE_CODE);

			Issue newIssue = null;
			try {
				/*
				 * Check if issue is already in Jira
				 */
				String projName = getJiraProjectName().replace(" ", "\\\\x0020").replace("(", "").replace(")", "");
				String srchStr = String.format("%s~ %s-%d", Field.DESCRIPTION, projName, key);
				log.debug(String.format("Search For: %s", srchStr));

				Issue.SearchResult sr = jira.searchIssues(srchStr);

				// get the project object from Jira
				List<Project> projects = jira.getProjects();
				String pn = getJiraProjectName();
				String cn = getComponent();
				Project project = null;
				List<Component> comp = new ArrayList<Component>();
				Component newComp = null;
				for (Project p : projects) {
					if (p.getName().equals(pn)) {
						project = Project.get(jira.getRestClient(), p.getKey());
						List<Component> projComps = project.getComponents();
						for (Component c : projComps) {
							if (c.getName().equals(cn)) {
								newComp = c;
								comp.add(c);
								break;
							}
						}
						break;
					}
				}

				if (priority < 1 || priority > 3) {
					log.info(String.format("Unprioritized or Low priority issue, NOT added (%d)", temp.getObjectId()));
					setTotalNumOfUnprioritizedIssues(++totalNumOfUnprioritizedIssues);
				} else if (sr.total == 0) { // issue was NOT found
					/* Create a new issue. */
					log.debug("The new issue CRC code is: " + Integer.toString(key));
					log.debug("Iterating or looping map Action Plan violations foreach loop");
					log.debug("key: " + Integer.toString(key) + " value: " + getMap().get(key));
					log.info("Project Name: " + getJiraProjectName());
					log.info("Issue Type: " + getIssueType());
					log.info("Priority: " + priority);

					String projectId = project.getId();

					Issue.FluentCreate nif = jira.createIssue(projectId, getIssueType());
					nif.field(Field.ISSUE_TYPE, getIssueType())
							.field(Field.PRIORITY,
									loadConfig.getPriorityMappingConversion(Integer.toString(temp.getPriority())))
							.field(Field.SUMMARY,
									getJiraFieldComposition(temp, loadConfig,
											Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION))
							.field(Field.DESCRIPTION, getJiraFieldComposition(temp, loadConfig,
									Constants.FIELD_MAPPING_LABEL_DESCRIPTION_JIRA_DESCRIPTION)
									+ getJiraProjectName().replace(" ", "\\\\x0020").replace("(", "").replace(")", "")
									+ "-" + Integer.toString(key))
							.field(Field.REPORTER, getJiraUserName());

					if (cn != null) {
						nif.field(Field.COMPONENTS, comp);
						log.info(String.format("Using component: %s", cn));						
					} else {
						log.info("No assigned component");
					}

					newIssue = nif.execute();

					newIssue.addComment("Added By CAST");
					log.info(String.format("The newIssue created with Jira ID: %s and key %d (%d)", newIssue, key,
							temp.getObjectId()));
					setTotalNumOfIssuesAdded(++totalNumOfIssuesAdded);

				} else { // issue was found in Jira
					log.debug(String.format("Found %d in Jira for object id=%d", sr.total, temp.getObjectId()));
					for (Issue forUpdate : sr.issues) {
						Status s = forUpdate.getStatus();
						Resolution res = forUpdate.getResolution();
						List<Component> compList = forUpdate.getComponents();

						String components = "[";
						boolean first = true;
						for (Component c: compList)
						{
							components = String.format("%s%s%s", components, first?"":",",c.toString());
							first=false;
						}
						components = components + "]";
						
						log.info(String.format(
								"Issue found in Jira, Issue Id=%s, Status=%s, Resolution=%s, Object Id=%d, Components: %s",
								forUpdate.getKey(), s.getName(), res != null ? res.getName() : "", temp.getObjectId(),
										components));

						if (newComp != null) {
							boolean found = false;
							for (Component c : compList) {
								if (c.getName().equals(newComp.getName())) {
									found = true;
									break;
								}
							}
							if (compList != null)
							{
								if (!found) {
									compList.add(newComp);
									forUpdate.update().field(Field.COMPONENTS, compList).execute();
								} else {
									log.info(String.format("Component [%s] is already attached to this issue",newComp.getName()));
								}
							}
						}
						List<Transition> transIds = forUpdate.getTransitions();

						// the issue has been corrected in CAST
						if (temp.getViolationStatus() == 2) {
							log.info("Issue identified as fixed in CAST");

							if (s.getName().equalsIgnoreCase("Done") || s.getName().equalsIgnoreCase("Closed")
									|| s.getName().equalsIgnoreCase("Resolved")) {
								log.info("Issue closed in Jira - nothing further to do");
							} else {
								log.info("Issue is still open in Jira");
								if (isMarkIssueResolved()) {
									log.info("Marking issue as resolved in Jira");
									String resTxt = getResolutionTxt();

									if (res == null || !res.getName().equals(resTxt)) {
										Transition finalStatus = null;
										for (Transition t : transIds) {
											if (t.getName().toLowerCase().contains("resolve")) {
												finalStatus = t;
												break;
											}
										}

										Resolution resol = null;
										for (Resolution r : rList) {
											if (r.getName().equalsIgnoreCase(resTxt)) {
												resol = r;
												break;
											}
										}

										if (resol == null) {
											log.error(String.format("Resolution Not Found! (%s)", resTxt));
										} else {
											String trName = null;

											forUpdate.transition().field(Field.RESOLUTION, resol)
													.execute("Resolve Issue");
											forUpdate.addComment("Resolution Identified by CAST");

											setTotalNumOfIssuesClosed(++totalNumOfIssuesClosed);
										}
									}
								} else {
									log.info("Closing issue in Jira");
									// get the last status to close the issue
									Transition finalStatus = (Transition) transIds.get(transIds.size() - 1);
									forUpdate.transition().execute(finalStatus.getName());
									forUpdate.addComment("Resolution Identified by CAST");

									setTotalNumOfIssuesClosed(++totalNumOfIssuesClosed);

									log.info(String.format("Issue updated in Jira, Issue Id=%s, Status=%s",
											forUpdate.getKey(), finalStatus.getName()));
								}
							}
							// issue is still open in cast, check if it is in
							// Jira
						} else if (temp.getViolationStatus() == 4) {
							log.info("Issue is unresolved in CAST");
							log.info(String.format("Issue is %s in Jira", s.getName()));
							if (s.getName().equalsIgnoreCase("Done") || s.getName().equalsIgnoreCase("Closed")
									|| s.getName().equalsIgnoreCase("Resolved")) {
								log.info("Reopen Jira issue");
								forUpdate.transition().execute(getReopenTransition(forUpdate));
								forUpdate.addComment("Reopened by CAST");
							} else {
								log.info("Issue is unresolved in Jira - nothing further to do");
							}
						} else {
							log.info("Issue already exists and CAST status has not changed - nothing further required");
						}
					}

					setTotalNumOfIssuesNotAddedByExist(++totalNumOfIssuesNotAddedByExist);
				}

			} catch (JiraException ex) {
				setTotalNumOfIssuesNotAddedByError(++totalNumOfIssuesNotAddedByError);
				if (ex.getCause() != null) {
					if (ex.getCause().getMessage() == null) {
						ex.printStackTrace();
						throw new Exception(ex.toString());
					} else if (ex.getCause().getMessage().toString().contains("Forbidden (403)")) {
						log.error("JiraException Error: Forbidden (403) review the logging jira user & password - "
								+ ex.getCause().getMessage());
						throw new Exception(ex.toString());
					} else {
						log.error("JiraException Error: " + ex.getCause().getMessage());
					}
				}
				log.error("JiraException Error: " + ex.getMessage()
						+ ". Please review that values provided are available in JIRA (issuetype, priority, assignee, etc).");
			}

		}

		return false;

	}

	private String getReopenTransition(Issue forUpdate) throws JiraException
	{
		List<Transition> transIds = forUpdate.getTransitions();
		for (Transition t : transIds) {
			if (t.getName().equalsIgnoreCase("Reopen Issue") || t.getName().equalsIgnoreCase("To Do"))
				return t.getName();
		}
		return "Bad Transition";
	}

	public static List<String> getJiraProjectNames(String jiraRestApiUrl, String jiraUser, String jiraUserPassword)
	{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		List<String> m = new ArrayList();

		if (jiraRestApiUrl != null && jiraUser != null && jiraUserPassword != null) {
			try {
				BasicCredentials creds = new BasicCredentials(jiraUser, jiraUserPassword);
				JiraClient jira = new JiraClient(jiraRestApiUrl, creds);

				List<Project> projects = jira.getProjects();
				for (int ii = 0; ii < projects.size(); ii++) {
					Project prj = projects.get(ii);
					m.add(prj.getName());
				}

			} catch (JiraException ex) {
				throw new RuntimeException(ex);
			}
		}
		return m;
	}

	public static List<String> getJiraIssueTypeNames(String jiraRestApiUrl, String jiraUser, String jiraUserPassword)
	{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		List<String> m = new ArrayList();

		if (jiraRestApiUrl != null && jiraUser != null && jiraUserPassword != null) {
			try {
				BasicCredentials creds = new BasicCredentials(jiraUser, jiraUserPassword);
				JiraClient jira = new JiraClient(jiraRestApiUrl, creds);

				List<net.rcarz.jiraclient.IssueType> issuesType = jira.getIssueTypes();
				for (int ii = 0; ii < issuesType.size(); ii++) {
					IssueType item = issuesType.get(ii);
					m.add(item.getName());
				}
			} catch (JiraException ex) {
				throw new RuntimeException(ex);
			}
		}
		return m;
	}

	/**
	 * Gets the map.
	 * 
	 * @return the map
	 */
	public HashMap<Integer, ActionPlanViolation> getMap()
	{
		return map;
	}

	/**
	 * Sets the map.
	 * 
	 * @param map
	 *            the map to set
	 */
	public void setMap(HashMap<Integer, ActionPlanViolation> map)
	{
		this.map = map;
	}

	/**
	 * Sets the jira project name.
	 * 
	 * @param jiraProjectName
	 *            the new jira project name
	 */
	public void setJiraProjectName(String jiraProjectName)
	{
		this.jiraProjectName = jiraProjectName;
	}

	/**
	 * Gets the jira user name.
	 * 
	 * @return the jiraUserName
	 */
	public String getJiraUserName()
	{
		return jiraUserName;
	}

	/**
	 * Sets the jira user name.
	 * 
	 * @param jiraUserName
	 *            the jiraUserName to set
	 */
	public void setJiraUserName(String jiraUserName)
	{
		this.jiraUserName = jiraUserName;
	}

	/**
	 * Gets the jira user password.
	 * 
	 * @return the jiraUserPassword
	 */
	public String getJiraUserPassword()
	{
		return jiraUserPassword;
	}

	/**
	 * Sets the jira user password.
	 * 
	 * @param jiraUserPassword
	 *            the jiraUserPassword to set
	 */
	public void setJiraUserPassword(String jiraUserPassword)
	{
		this.jiraUserPassword = jiraUserPassword;
	}

	/**
	 * Gets the jira rest api url.
	 * 
	 * @return the jiraRestApiUrl
	 */
	public String getJiraRestApiUrl()
	{
		return jiraRestApiUrl;
	}

	/**
	 * Sets the jira rest api url.
	 * 
	 * @param jiraRestApiUrl
	 *            the jiraRestApiUrl to set
	 */
	public void setJiraRestApiUrl(String jiraRestApiUrl)
	{
		this.jiraRestApiUrl = jiraRestApiUrl;
	}

	/**
	 * Gets the jira project name.
	 * 
	 * @return the jiraProjectName
	 */
	public String getJiraProjectName()
	{
		return jiraProjectName;
	}

	/**
	 * Gets the issue type.
	 * 
	 * @return the issueType
	 */
	public String getIssueType()
	{
		return issueType;
	}

	/**
	 * Sets the issue type.
	 * 
	 * @param issueType
	 *            the issueType to set
	 */
	public void setIssueType(String issueType)
	{
		this.issueType = issueType;
	}

	public boolean isMarkIssueResolved()
	{
		return markIssueResolved;
	}

	public void setMarkIssueResolved(boolean markIssueResolved)
	{
		this.markIssueResolved = markIssueResolved;
	}

	public String getResolutionTxt()
	{
		return resolutionTxt;
	}

	public void setResolutionTxt(String resolutionTxt)
	{
		this.resolutionTxt = resolutionTxt;
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
	public String getJiraFieldComposition(ActionPlanViolation temp, Configuration fieldmap, String fieldType)
	{
		String field = new String();
		String fields;
		if (fieldType.equals(Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION)) {
			fields = fieldmap.getJiraFields(Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION);
		} else {
			fields = fieldmap.getJiraFields(Constants.FIELD_MAPPING_LABEL_DESCRIPTION_JIRA_DESCRIPTION);
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
								.append(getAppName()).append("*\n\n");
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
				} else if (field.equals(Constants.FIELD_MAPPING_LABEL_REMEDIATION_EXAMPLE_DESCRIPTION)) {
					result.append(temp.getRemediationExample());
				} else if (field.equals(Constants.FIELD_MAPPING_LABEL_TOTAL_DESCRIPTION)) {
					result.append(temp.getTotales());
				} else if (field.equals(Constants.FIELD_MAPPING_LABEL_VIOLATION_EXAMPLE_DESCRIPTION)) {
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
				result.append(fieldmap.getCastToJiraFieldsMapping(Constants.FIELD_MAPPING_LABEL_CASTID_DESCRIPTION));
				int maxChar = 30*1024;
				int maxLength = (result.length() < maxChar)?result.length():maxChar;
				
				result.setLength(maxLength);
				result.append(" ");
			} else if (fieldType.equals(Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION)) {
				// need to limit to 255 characters
				int maxChar = 250;
				int maxLength = (result.length() < maxChar)?result.length():maxChar;
				
				result.setLength(maxLength);
			}

		}

		return result.toString();
	}

	public String getComponent()
	{
		return component;
	}

	public void setComponent(String component)
	{
		this.component = component;
	}

	/**
	 * @return the totalNumOfIssues
	 */
	public int getTotalNumOfIssues()
	{
		return totalNumOfIssues;
	}

	/**
	 * @param totalNumOfIssues
	 *            the totalNumOfIssues to set
	 */
	public void setTotalNumOfIssues(int totalNumOfIssues)
	{
		this.totalNumOfIssues = totalNumOfIssues;
	}

	/**
	 * @return the totalNumOfIssuesAdded
	 */
	public int getTotalNumOfIssuesAdded()
	{
		return totalNumOfIssuesAdded;
	}

	/**
	 * @param totalNumOfIssuesAdded
	 *            the totalNumOfIssuesAdd to set
	 */
	public void setTotalNumOfIssuesAdded(int totalNumOfIssuesAddeded)
	{
		this.totalNumOfIssuesAdded = totalNumOfIssuesAddeded;
	}

	/**
	 * @return the totalNumOfIssuesNotAddedByError
	 */
	public int getTotalNumOfIssuesNotAddedByError()
	{
		return totalNumOfIssuesNotAddedByError;
	}

	/**
	 * @param totalNumOfIssuesNotAddedByError
	 *            the totalNumOfIssuesNotAddedByError to set
	 */
	public void setTotalNumOfIssuesNotAddedByError(int totalNumOfIssuesNotAddedByError)
	{
		this.totalNumOfIssuesNotAddedByError = totalNumOfIssuesNotAddedByError;
	}

	/**
	 * @return the totalNumOfIssuesNotAddedByExist
	 */
	public int getTotalNumOfIssuesNotAddedByExist()
	{
		return totalNumOfIssuesNotAddedByExist;
	}

	/**
	 * @param totalNumOfIssuesNotAddedByExist
	 *            the totalNumOfIssuesNotAddedByExist to set
	 */
	public void setTotalNumOfIssuesNotAddedByExist(int totalNumOfIssuesNotAddedByExist)
	{
		this.totalNumOfIssuesNotAddedByExist = totalNumOfIssuesNotAddedByExist;
	}

	public int getTotalNumOfIssuesClosed()
	{
		return totalNumOfIssuesClosed;
	}

	public void setTotalNumOfIssuesClosed(int totalNumOfIssuesClosed)
	{
		this.totalNumOfIssuesClosed = totalNumOfIssuesClosed;
	}

	public void setTotalNumOfUnprioritizedIssues(int totalNumOfUnprioritizedIssues)
	{
		this.totalNumOfUnprioritizedIssues = totalNumOfUnprioritizedIssues;
	}

	public int getTotalNumOfUnprioritizedIssues()
	{
		return totalNumOfUnprioritizedIssues;
	}

	public String getAppName()
	{
		return appName;
	}

	public void setAppName(String appName)
	{
		this.appName = appName;
	}

}
