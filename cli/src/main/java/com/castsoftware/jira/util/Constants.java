package com.castsoftware.jira.util;

/**
 * The Class Constants contains all the static variables used in the
 * CastJiraConnector utility
 * 
 * @author FME
 * @version 1.1
 */

public class Constants {

	/** The Constant CAST_APPLICATION_NAME. */
	public static final String CAST_APPLICATION_NAME = "applicationname";

	/** The Constant CAST_USER_NAME. */
	public static final String CAST_USER_NAME = "castusername";

	/** The Constant CAST_USER_PASSWORD. */
	public static final String CAST_USER_PASSWORD = "castuserpassword";

	/** The Constant CAST_REST_API_URL. */
	public static final String CAST_REST_API_URL = "castrestapiurl";

	/** The Constant CAST_DB_PROVIDER. */
	public static final String CAST_DB_PROVIDER = "databaseprovider";
	
	/** The Constant CAST_DB_HOST. */
	public static final String CAST_DB_HOST = "databasehost";

	/** The Constant CAST_DB_NAME. */
	public static final String CAST_DB_NAME = "databasename";

	/** The Constant CAST_DB_PORT. */
	public static final String CAST_DB_PORT = "databaseport";

	/** The Constant CAST_DB_SCHEMA. */
	public static final String CAST_DB_SCHEMA = "databaseschema";

	/** The Constant JIRA_USER_NAME. */
	public static final String JIRA_USER_NAME = "jirausername";

	/** The Constant JIRA_USER_PASSWORD. */
	public static final String JIRA_USER_PASSWORD = "jirauserpassword";

	/** The Constant JIRA_REST_API_URL. */
	public static final String JIRA_REST_API_URL = "jirarestapiurl";

	/** The Constant JIRA_PROJECT_NAME. */
	public static final String JIRA_PROJECT_NAME = "jiraprojectname";

	/** The Constant JIRA_ASSIGNEE_NAME. */
	//public static final String JIRA_ASSIGNEE_NAME = "jiraassignee";

	/** The Constant JIRA_ISSUE_TYPE. */
	public static final String JIRA_ISSUE_TYPE = "jiraissuetype";

	/** The Constant JIRA_DEFAULT_ISSUE_TYPE. */
	public static final String JIRA_DEFAULT_ISSUE_TYPE = "Task";

	/** The Constant LOG_PATH. */
	public static final String LOG_PATH = "logpath";

	/** The Constant DEBUG. */
	public static final String DEBUG = "debug";

	/** The Constant HELP. */
	public static final String VERSION = "version";

	/** The Constant ZEXAMPLES. */
	public static final String ZEXAMPLES = "zexamples";

	/** The Constant HELP. */
	public static final String HELP = "help";
	
	public static final String MARK_ISSUE_RESOLVED = "mark_issue_resolved";
	public static final String RESOLUTION = "resolution";
	public static final String COMPONENT = "component";

    /** The Constant WORKFLOW_FILE. */
    public static final String WORKFLOW_FILE = "workflow.properties";
    
    public static final String WORKFLOW_STATUS_OPEN = "status.open";
    public static final String WORKFLOW_STATUS_DONE = "status.done";
    public static final String WORKFLOW_STATUS_PROGRESS = "status.progress";

    public static final String WORKFLOW_TRANSITION_DONE = "transition.done";
    public static final String WORKFLOW_TRANSITION_REOPEN = "transition.reopen";
    public static final String WORKFLOW_TRANSITION_BLACKLIST = "transition.blacklist";


	// Properties & log file names
	/** The Constant LOG4J_PROPERTIES_FILE. */
	public static final String LOG4J_PROPERTIES_FILE = "log4j.properties";

	/** The Constant PRIORITY_MAPPING_FILE. */
	public static final String PRIORITY_MAPPING_FILE = "PriorityMapping.properties";

	/** The Constant CASTTOJIRA_FIELDS_MAPPING_FILE. */
	public static final String CASTTOJIRA_FIELDS_MAPPING_FILE = "CastToJiraFieldsMapping.template";

	/** The Constant LOG_FILE_NAME. */
	public static final String LOG_FILE_NAME = "sendtojira.log";

	// Values of priority in CAST
	/** The Constant PRIORITY_EXTREME. */
	public static final String PRIORITY_EXTREME = "1";

	/** The Constant PRIORITY_HIGH. */
	public static final String PRIORITY_HIGH = "2";

	/** The Constant PRIORITY_MODERATE. */
	public static final String PRIORITY_MODERATE = "3";

	/** The Constant PRIORITY_LOW. */
	public static final String PRIORITY_LOW = "4";

	/** The Constant PRIORITY_KEEP. */
	public static final String PRIORITY_KEEP = "-1";

	// Values of priority in CAST Description
	/** The Constant PRIORITY_EXTREME_DESC. */
	public static final String PRIORITY_EXTREME_DESC = "Extreme";

	/** The Constant PRIORITY_HIGH_DESC. */
	public static final String PRIORITY_HIGH_DESC = "High";

	/** The Constant PRIORITY_MODERATE_DESC. */
	public static final String PRIORITY_MODERATE_DESC = "Moderate";

	/** The Constant PRIORITY_LOW_DESC. */
	public static final String PRIORITY_LOW_DESC = "Low";

	/** The Constant PRIORITY_KEEP_DESC. */
	public static final String PRIORITY_KEEP_DESC = "Keep";

	// Values of priority in JIRA 6.1 by Default
	/** The Constant PRIORITY_JIRA_BLOCKER_DESC. */
	public static final String PRIORITY_JIRA_BLOCKER_DESC = "Blocker";

	/** The Constant PRIORITY_JIRA_CRITICAL_DESC. */
	public static final String PRIORITY_JIRA_CRITICAL_DESC = "Critical";

	/** The Constant PRIORITY_JIRA_MAJOR_DESC. */
	public static final String PRIORITY_JIRA_MAJOR_DESC = "Major";

	/** The Constant PRIORITY_JIRA_MINOR_DESC. */
	public static final String PRIORITY_JIRA_MINOR_DESC = "Minor";

	/** The Constant PRIORITY_JIRA_TRIVIAL_DESC. */
	public static final String PRIORITY_JIRA_TRIVIAL_DESC = "Trivial";

	/** 
	 * Constants used to map the Jira Field Labels. 
	 * */
	public static final String FIELD_MAPPING_LABEL_OBJECT_FULL_NAME = "ObjectFullName.label";
	public static final String FIELD_MAPPING_LABEL_ADDED_TO_ACTION_PLAN_DATE = "AddedToActionPlanDate.label";
	public static final String FIELD_MAPPING_LABEL_METRIC_SHORT_DESCRIPTION = "MetricShortDescription.label";
	public static final String FIELD_MAPPING_LABEL_METRIC_LONG_DESCRIPTION = "MetricLongDescription.label";
	public static final String FIELD_MAPPING_LABEL_REFERENCE_DESCRIPTION = "Reference.label";
	public static final String FIELD_MAPPING_LABEL_REASON_DESCRIPTION = "Reason.label";
	public static final String FIELD_MAPPING_LABEL_REMEDIATION_DESCRIPTION = "Remediation.label";
	public static final String FIELD_MAPPING_LABEL_REMEDIATION_EXAMPLE_DESCRIPTION = "RemediationExample.label";
	public static final String FIELD_MAPPING_LABEL_TOTAL_DESCRIPTION = "Total.label";
	public static final String FIELD_MAPPING_LABEL_VIOLATION_EXAMPLE_DESCRIPTION = "ViolationExample.label";
	public static final String FIELD_MAPPING_LABEL_ACTION_DEFINED_DESCRIPTION = "ActionDefined.label";
	public static final String FIELD_MAPPING_LABEL_OUTPUT_DESCRIPTION = "Output.label";
	public static final String FIELD_MAPPING_LABEL_CASTID_DESCRIPTION = "CastId.label";
	public static final String FIELD_MAPPING_LABEL_SOURCE_CODE = "SourceCode.label";
	public static final String FIELD_MAPPING_LABEL_LINE_START = "LineStart.label";
	public static final String FIELD_MAPPING_LABEL_LINE_END = "LineEnd.label";
	public static final String FIELD_MAPPING_LABEL_TECH_CRITERIA = "TechCriteria.label";
	public static final String FIELD_MAPPING_LABEL_BUSINESS_CRITERIA = "BusinessCriteria.label";

	
	/**
	 * Constants used to store the Jira field label default values
	 */
	public static final String FIELD_MAPPING_LABEL_OBJECT_FULL_NAME_DEFAULT_VALUE = "*Object Full Name :*";
	public static final String FIELD_MAPPING_LABEL_ADDED_TO_ACTION_PLAN_DATE_DEFAULT_VALUE = "*Added to Action Plan Date :*";
	public static final String FIELD_MAPPING_LABEL_METRIC_SHORT_DESCRIPTION_DEFAULT_VALUE = "*Metric Short Description :*";
	public static final String FIELD_MAPPING_LABEL_METRIC_LONG_DESCRIPTION_DEFAULT_VALUE = "*Metric Long Description :*";
	public static final String FIELD_MAPPING_LABEL_REFERENCE_DESCRIPTION_DEFAULT_VALUE = "*Reference :*";
	public static final String FIELD_MAPPING_LABEL_REASON_DESCRIPTION_DEFAULT_VALUE = "*Reason :*";
	public static final String FIELD_MAPPING_LABEL_REMEDIATION_DESCRIPTION_DEFAULT_VALUE = "*Remediation :*";
	public static final String FIELD_MAPPING_LABEL_TOTAL_DESCRIPTION_DEFAULT_VALUE = "*Total :*";
	public static final String FIELD_MAPPING_LABEL_REMEDIATION_EXAMPLE_DESCRIPTION_DEFAULT_VALUE = "*Remediation Example :*";
	public static final String FIELD_MAPPING_LABEL_VIOLATION_EXAMPLE_DESCRIPTION_DEFAULT_VALUE = "*Violation Example :*";
	public static final String FIELD_MAPPING_LABEL_OUTPUT_DESCRIPTION_DEFAULT_VALUE = "*Output :*";
	public static final String FIELD_MAPPING_LABEL_ACTION_DEFINED_DESCRIPTION_DEFAULT_VALUE = "*Action Defined by User :*";
	public static final String FIELD_MAPPING_LABEL_CASTID_DESCRIPTION_DEFAULT_VALUE = "*CastId (Do not Delete or Modified) :*";
	public static final String FIELD_MAPPING_LABEL_SOURCE_CODE_DEFAULT_VALUE = "*Violation Source Code :*";
	public static final String FIELD_MAPPING_LABEL_LINE_START_DEFAULT_VALUE = "Starting on line :";
	public static final String FIELD_MAPPING_LABEL_LINE_END_DEFAULT_VALUE = "Ending at line :";
	public static final String FIELD_MAPPING_LABEL_TECH_CRITERIA_DEFAULT_VALUE = ":";
	public static final String FIELD_MAPPING_LABEL_BUSINESS_CRITERIA_DEFAULT_VALUE = "*Business Criteria :* ";

	/** The Constant FIELD_MAPPING_LABEL_UNKNOWN_DEFAULT_VALUE. */
	public static final String FIELD_MAPPING_LABEL_UNKNOWN_DEFAULT_VALUE = "*Field :*";

	/** The Constant FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION. */
	public static final String FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION = "Summary.JiraField";

	/** The Constant FIELD_MAPPING_LABEL_DESCRIPTION_JIRA_DESCRIPTION. */
	public static final String FIELD_MAPPING_LABEL_DESCRIPTION_JIRA_DESCRIPTION = "Description.JiraField";

	/** The Constant FIELD_MAPPING_SUMMARY_JIRA_ORDER_DEFAULT_VALUE. */
	public static final String FIELD_MAPPING_SUMMARY_JIRA_ORDER_DEFAULT_VALUE = "MetricShortDescription.label";

	/** The Constant FIELD_MAPPING_DESCRIPTION_JIRA_ORDER_DEFAULT_VALUE. */
	public static final String FIELD_MAPPING_DESCRIPTION_JIRA_ORDER_DEFAULT_VALUE = "SourceCode.label;ObjectFullName.label;AddedToActionPlanDate.label;ActionDefined.label;MetricLongDescription.label;Reason.label;Reference.label;Remediation.label;ViolationExample.label;RemediationExample.label;Total.label";

	/** The Constant FIELD_VALUE_WHEN_IS_NULL. */
	public static final String FIELD_VALUE_WHEN_IS_NULL = "N/A";

	// log4j properties that could be changed dynamically
	/** The Constant LOGJ4_ROOTLOGGER. */
	public static final String LOGJ4_ROOTLOGGER = "log4j.rootLogger";

	/** The Constant LOGJ4_APPENDER_FILE_FILE. */
	public static final String LOGJ4_APPENDER_FILE_FILE = "log4j.appender.file.File";

	/** The Constant LOGJ4_DEBUG_FILE_CONSOLE_MODE. */
	public static final String LOGJ4_DEBUG_FILE_CONSOLE_MODE = "DEBUG, file, console";

	/** The Constant LOGJ4_DEBUG_FILE_MODE. */
	public static final String LOGJ4_DEBUG_FILE_MODE = "DEBUG, file";

	/** The Constant LOGJ4_DEBUG_CONSOLE_MODE. */
	public static final String LOGJ4_DEBUG_CONSOLE_MODE = "DEBUG, console";

	/** The Constant LOGJ4_OUTPUT_CONSOLE_MODE. */
	public static final String LOGJ4_OUTPUT_CONSOLE_MODE = "console";

	/** The Constant LOGJ4_OUTPUT_FILE_MODE. */
	public static final String LOGJ4_OUTPUT_FILE_MODE = "file";

	/** The Constant LOG_OUTPUT. */
	public static final String LOG_OUTPUT = "logoutput";

	/** The Constant LOGJ4_OUTPUT_FILE. */
	public static final String LOGJ4_OUTPUT_FILE = "file";

	/** The Constant LOGJ4_OUTPUT_CONSOLE. */
	public static final String LOGJ4_OUTPUT_CONSOLE = "console";

	// Database Providers
	/** The Constant DB_CSS. */
	public static final String DB_CSS = "css";

	/** The Constant DB_ORACLE. */
	public static final String DB_ORACLE = "oracle";

	/** The Constant DB_SQLSERVER. */
	public static final String DB_SQLSERVER = "sqlserver";

	// Database connection Strings

	/** The Constant DB_CONN_STRING_CSS. */
	public static final String DB_CONN_STRING_CSS = "jdbc:postgresql://";

	/** The Constant DB_CONN_STRING_ORACLE. */
	public static final String DB_CONN_STRING_ORACLE = "jdbc:oracle:thin:@";

	/** The Constant DB_CONN_STRING_SQLSERVER. */
	public static final String DB_CONN_STRING_SQLSERVER = "jdbc:jtds:sqlserver://";

	// Database Drivers

	/** The Constant DB_JDBC_DRIVER_CSS. */
	public static final String DB_JDBC_DRIVER_CSS = "org.postgresql.Driver";

	/** The Constant DB_JDBC_DRIVER_ORACLE. */
	public static final String DB_JDBC_DRIVER_ORACLE = "oracle.jdbc.OracleDriver";

	/** The Constant DB_JDBC_DRIVER_SQLSERVER. */
	public static final String DB_JDBC_DRIVER_SQLSERVER = "net.sourceforge.jtds.jdbc.Driver";

}
