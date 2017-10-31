package com.castsoftware.jira.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;

import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

/**
 * The class Configuration allows to establish the configuration of log4j,
 * Priority Mapping and field mapping This class is the entry point to retrieve
 * action plan from a database
 * 
 * @author FME
 * @version 1.1
 */

public class Configuration {

	/** The log. */
	public static Log log = LogFactory.getLog(Configuration.class);

	/** The priority map. */
	private HashMap<String, String> priorityMap = new HashMap<String, String>(0);

	/** The field labels map. */
	private HashMap<String, String> fieldLabelsMap = new HashMap<String, String>(
			0);

	/**
	 * Sets the update log4j configuration.
	 * 
	 * @param logPathFile
	 *            the log path file
	 * @param debug
	 *            the debug
	 * @param output
	 *            the output
	 */
	public void setUpdateLog4jConfiguration(String logPathFile, boolean debug,
			String output, String appName) {
		Properties props = new Properties();
		try {
			InputStream configStream = this.getClass().getResourceAsStream(
					"/" + Constants.LOG4J_PROPERTIES_FILE);
			
			if (configStream == null)
				throw new IOException();
			
			props.load(configStream);
			configStream.close();
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HHmmss");

				
			// setup the log level
			if (debug) {
				if (output != null) {
					if (output.trim().toLowerCase()
							.equals(Constants.LOGJ4_OUTPUT_CONSOLE_MODE)) {
						props.setProperty(Constants.LOGJ4_ROOTLOGGER,
								Constants.LOGJ4_DEBUG_CONSOLE_MODE);
						System.out
								.println("Debug Log Output selected: console");
					} else if (output.trim().toLowerCase()
							.equals(Constants.LOGJ4_OUTPUT_FILE)) {
						props.setProperty(Constants.LOGJ4_ROOTLOGGER,
								Constants.LOGJ4_DEBUG_FILE_MODE);
						System.out.println("Debug Log Output selected: file");
					} else {
						props.setProperty(Constants.LOGJ4_ROOTLOGGER,
								Constants.LOGJ4_DEBUG_FILE_CONSOLE_MODE);
						System.out
								.println("Debug Log Output selected: console,file");
					}
				} else {
					props.setProperty(Constants.LOGJ4_ROOTLOGGER,
							Constants.LOGJ4_DEBUG_FILE_CONSOLE_MODE);
					System.out.println("Debug Log Output selected: console,file");
				}
			}

			if (logPathFile != null && !output.trim().toLowerCase()
					.equals(Constants.LOGJ4_OUTPUT_CONSOLE_MODE)) {
				// setup the new file path
				props.setProperty(Constants.LOGJ4_APPENDER_FILE_FILE,
						logPathFile + "\\"
								+ format.format(cal.getTime()).toString() 
								+ "-"
								+ appName + "-" + Constants.LOG_FILE_NAME);
				
				System.out.println("Log File:" + logPathFile + "\\"
						+ format.format(cal.getTime()) 
						+ "-"
						+ appName
						+ "-"
						+ Constants.LOG_FILE_NAME);
			} else {
				props.setProperty(Constants.LOGJ4_APPENDER_FILE_FILE, format
						.format(cal.getTime()).toString()
						+ "-"
						+ appName
						+ "-"
						+ Constants.LOG_FILE_NAME);
				
				System.out.println("Log File:" + format.format(cal.getTime())
						+ "-"
						+ appName
						+ "-" + Constants.LOG_FILE_NAME);
			}
			// Reload the configuration once has been updated
			LogManager.resetConfiguration();
			PropertyConfigurator.configure(props);
		} catch (IOException e) {
			System.out
					.println("Error: Cannot load the log4j configuration file ");
		}

	}

	/**
	 * Load priority mapping.
	 */
	public void loadPriorityMapping() {
		Properties props = new Properties();

		try {
			String priorityMappingFileName = "/" +  Constants.PRIORITY_MAPPING_FILE;
			log.info(String.format("Priority Mapping:  %s", priorityMappingFileName)); 
			
			InputStream configStream = this.getClass().getResourceAsStream(priorityMappingFileName);
			props.load(configStream);
			log.info(props.toString());
			
			
			configStream.close();
			priorityMap.put(Constants.PRIORITY_EXTREME,
					props.getProperty(Constants.PRIORITY_EXTREME));
			priorityMap.put(Constants.PRIORITY_HIGH,
					props.getProperty(Constants.PRIORITY_HIGH));
			priorityMap.put(Constants.PRIORITY_MODERATE,
					props.getProperty(Constants.PRIORITY_MODERATE));
			priorityMap.put(Constants.PRIORITY_LOW,
					props.getProperty(Constants.PRIORITY_LOW));
			priorityMap.put(Constants.PRIORITY_KEEP,
					props.getProperty(Constants.PRIORITY_KEEP));

			if (log.isDebugEnabled())
			{
				log.debug("The following priority mapping will be applied");
				log.debug(Constants.PRIORITY_EXTREME + "="
						+ props.getProperty(Constants.PRIORITY_EXTREME));
				log.debug(Constants.PRIORITY_HIGH + "="
						+ props.getProperty(Constants.PRIORITY_HIGH));
				log.debug(Constants.PRIORITY_MODERATE + "="
						+ props.getProperty(Constants.PRIORITY_MODERATE));
				log.debug(Constants.PRIORITY_LOW + "="
						+ props.getProperty(Constants.PRIORITY_LOW));
				log.debug(Constants.PRIORITY_KEEP + "="
						+ props.getProperty(Constants.PRIORITY_KEEP));
			}
		} catch (IOException e) {
			log.error("Cannot load the PriorityMapping configuration file so the defaut conversion will be applied");
			priorityMap.put(Constants.PRIORITY_EXTREME,
					Constants.PRIORITY_JIRA_BLOCKER_DESC);
			priorityMap.put(Constants.PRIORITY_HIGH,
					Constants.PRIORITY_JIRA_CRITICAL_DESC);
			priorityMap.put(Constants.PRIORITY_MODERATE,
					Constants.PRIORITY_JIRA_MAJOR_DESC);
			priorityMap.put(Constants.PRIORITY_LOW,
					Constants.PRIORITY_JIRA_MINOR_DESC);
			priorityMap.put(Constants.PRIORITY_KEEP,
					Constants.PRIORITY_JIRA_TRIVIAL_DESC);
			log.debug(Constants.PRIORITY_EXTREME + "="
					+ Constants.PRIORITY_JIRA_BLOCKER_DESC);
			log.debug(Constants.PRIORITY_HIGH + "="
					+ Constants.PRIORITY_JIRA_CRITICAL_DESC);
			log.debug(Constants.PRIORITY_MODERATE + "="
					+ Constants.PRIORITY_JIRA_MAJOR_DESC);
			log.debug(Constants.PRIORITY_LOW + "="
					+ Constants.PRIORITY_JIRA_MINOR_DESC);
			log.debug(Constants.PRIORITY_KEEP + "="
					+ Constants.PRIORITY_JIRA_TRIVIAL_DESC);
		}

		// PropertyConfigurator.configure(props);
	}

	/**
	 * Gets the priority mapping conversion.
	 * 
	 * @param priority
	 *            the priority
	 * @return the priority mapping conversion
	 */
	public String getPriorityMappingConversion(String priority) {
		String result;
		if (priorityMap.isEmpty()) {
			loadPriorityMapping();
		}
		if (!priorityMap.containsKey(priority)) {
			log.error("Priority mapping value not found ="
					+ priority
					+ ". Review the PropertyMapping file and ensure it has a mapping. Respect Uper-Lower Cases of Jira priorities");
			log.error("As Priority mapping value not found, the following will be assigned ="
					+ Constants.PRIORITY_JIRA_TRIVIAL_DESC);
			result = priorityMap.get(Constants.PRIORITY_JIRA_TRIVIAL_DESC);
		} else {
			result = priorityMap.get(priority);
		}

		return result;
	}

	private void loadCastToJiraFieldMapping(Properties props, String fieldMapping, String defaultValue)
	{
		if (props.getProperty(fieldMapping) == null || props.getProperty(fieldMapping).isEmpty() ) {
			fieldLabelsMap.put(fieldMapping,props.getProperty(defaultValue));
		} else {
			fieldLabelsMap.put(fieldMapping,props.getProperty(fieldMapping));
		}
	}
	
	
	/**
	 * Load cast to jira fields mapping.
	 */
	public void loadCastToJiraFieldsMapping() {
		Properties props = new Properties();

		try {
			InputStream configStream = this.getClass().getResourceAsStream(
					"/" + Constants.CASTTOJIRA_FIELDS_MAPPING_FILE);
			props.load(configStream);
			configStream.close();
			
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_OBJECT_FULL_NAME,Constants.FIELD_MAPPING_LABEL_OBJECT_FULL_NAME_DEFAULT_VALUE);
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_ADDED_TO_ACTION_PLAN_DATE,Constants.FIELD_MAPPING_LABEL_ADDED_TO_ACTION_PLAN_DATE_DEFAULT_VALUE);
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_ACTION_DEFINED_DESCRIPTION,Constants.FIELD_MAPPING_LABEL_ACTION_DEFINED_DESCRIPTION_DEFAULT_VALUE);
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_METRIC_SHORT_DESCRIPTION,Constants.FIELD_MAPPING_LABEL_METRIC_SHORT_DESCRIPTION_DEFAULT_VALUE);
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_METRIC_LONG_DESCRIPTION ,Constants.FIELD_MAPPING_LABEL_METRIC_LONG_DESCRIPTION_DEFAULT_VALUE );
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_REFERENCE_DESCRIPTION ,Constants.FIELD_MAPPING_LABEL_REFERENCE_DESCRIPTION_DEFAULT_VALUE );
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_REASON_DESCRIPTION ,Constants.FIELD_MAPPING_LABEL_REASON_DESCRIPTION_DEFAULT_VALUE );
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_REMEDIATION_DESCRIPTION ,Constants.FIELD_MAPPING_LABEL_REMEDIATION_DESCRIPTION_DEFAULT_VALUE );
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_REMEDIATION_EXAMPLE_DESCRIPTION ,Constants.FIELD_MAPPING_LABEL_REMEDIATION_EXAMPLE_DESCRIPTION_DEFAULT_VALUE );
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_VIOLATION_EXAMPLE_DESCRIPTION ,Constants.FIELD_MAPPING_LABEL_VIOLATION_EXAMPLE_DESCRIPTION_DEFAULT_VALUE );
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_OUTPUT_DESCRIPTION ,Constants.FIELD_MAPPING_LABEL_OUTPUT_DESCRIPTION_DEFAULT_VALUE );
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_TOTAL_DESCRIPTION ,Constants.FIELD_MAPPING_LABEL_TOTAL_DESCRIPTION_DEFAULT_VALUE );
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_CASTID_DESCRIPTION ,Constants.FIELD_MAPPING_LABEL_CASTID_DESCRIPTION_DEFAULT_VALUE );
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION ,Constants.FIELD_MAPPING_SUMMARY_JIRA_ORDER_DEFAULT_VALUE );
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_DESCRIPTION_JIRA_DESCRIPTION ,Constants.FIELD_MAPPING_DESCRIPTION_JIRA_ORDER_DEFAULT_VALUE );
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_SOURCE_CODE  ,Constants.FIELD_MAPPING_LABEL_SOURCE_CODE_DEFAULT_VALUE );
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_LINE_START  ,Constants.FIELD_MAPPING_LABEL_LINE_START_DEFAULT_VALUE );
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_LINE_END  ,Constants.FIELD_MAPPING_LABEL_LINE_END_DEFAULT_VALUE );
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_TECH_CRITERIA  ,Constants.FIELD_MAPPING_LABEL_TECH_CRITERIA_DEFAULT_VALUE );
			loadCastToJiraFieldMapping(props,Constants.FIELD_MAPPING_LABEL_BUSINESS_CRITERIA  ,Constants.FIELD_MAPPING_LABEL_BUSINESS_CRITERIA_DEFAULT_VALUE );
			
			log.debug("The following Field Labels will be applied");

			log.debug(Constants.FIELD_MAPPING_LABEL_OBJECT_FULL_NAME
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_OBJECT_FULL_NAME));
			log.debug(Constants.FIELD_MAPPING_LABEL_ADDED_TO_ACTION_PLAN_DATE
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_ADDED_TO_ACTION_PLAN_DATE));
			log.debug(Constants.FIELD_MAPPING_LABEL_ACTION_DEFINED_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_ACTION_DEFINED_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_METRIC_SHORT_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_METRIC_SHORT_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_METRIC_LONG_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_METRIC_LONG_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_REFERENCE_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_REFERENCE_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_REASON_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_REASON_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_REMEDIATION_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_REMEDIATION_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_REMEDIATION_EXAMPLE_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_REMEDIATION_EXAMPLE_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_VIOLATION_EXAMPLE_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_VIOLATION_EXAMPLE_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_TOTAL_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_TOTAL_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_OUTPUT_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_OUTPUT_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_CASTID_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_CASTID_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_DESCRIPTION_JIRA_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_DESCRIPTION_JIRA_DESCRIPTION));

		} catch (IOException e) {
			log.error("Cannot load the CastToJiraFieldsMapping configuration file so the defaut conversion will be applied");
			fieldLabelsMap
					.put(Constants.FIELD_MAPPING_LABEL_OBJECT_FULL_NAME,
							Constants.FIELD_MAPPING_LABEL_OBJECT_FULL_NAME_DEFAULT_VALUE);
			fieldLabelsMap
					.put(Constants.FIELD_MAPPING_LABEL_ADDED_TO_ACTION_PLAN_DATE,
							Constants.FIELD_MAPPING_LABEL_ADDED_TO_ACTION_PLAN_DATE_DEFAULT_VALUE);
			fieldLabelsMap
					.put(Constants.FIELD_MAPPING_LABEL_ACTION_DEFINED_DESCRIPTION,
							Constants.FIELD_MAPPING_LABEL_ACTION_DEFINED_DESCRIPTION_DEFAULT_VALUE);
			fieldLabelsMap
					.put(Constants.FIELD_MAPPING_LABEL_METRIC_SHORT_DESCRIPTION,
							Constants.FIELD_MAPPING_LABEL_METRIC_SHORT_DESCRIPTION_DEFAULT_VALUE);
			fieldLabelsMap
					.put(Constants.FIELD_MAPPING_LABEL_METRIC_LONG_DESCRIPTION,
							Constants.FIELD_MAPPING_LABEL_METRIC_LONG_DESCRIPTION_DEFAULT_VALUE);
			fieldLabelsMap
					.put(Constants.FIELD_MAPPING_LABEL_REFERENCE_DESCRIPTION,
							Constants.FIELD_MAPPING_LABEL_REFERENCE_DESCRIPTION_DEFAULT_VALUE);
			fieldLabelsMap
					.put(Constants.FIELD_MAPPING_LABEL_REASON_DESCRIPTION,
							Constants.FIELD_MAPPING_LABEL_REASON_DESCRIPTION_DEFAULT_VALUE);
			fieldLabelsMap
					.put(Constants.FIELD_MAPPING_LABEL_REMEDIATION_DESCRIPTION,
							Constants.FIELD_MAPPING_LABEL_REMEDIATION_DESCRIPTION_DEFAULT_VALUE);
			fieldLabelsMap
					.put(Constants.FIELD_MAPPING_LABEL_REMEDIATION_EXAMPLE_DESCRIPTION,
							Constants.FIELD_MAPPING_LABEL_REMEDIATION_EXAMPLE_DESCRIPTION_DEFAULT_VALUE);
			fieldLabelsMap
					.put(Constants.FIELD_MAPPING_LABEL_VIOLATION_EXAMPLE_DESCRIPTION,
							Constants.FIELD_MAPPING_LABEL_VIOLATION_EXAMPLE_DESCRIPTION_DEFAULT_VALUE);
			fieldLabelsMap
					.put(Constants.FIELD_MAPPING_LABEL_TOTAL_DESCRIPTION,
							Constants.FIELD_MAPPING_LABEL_TOTAL_DESCRIPTION_DEFAULT_VALUE);
			fieldLabelsMap
					.put(Constants.FIELD_MAPPING_LABEL_OUTPUT_DESCRIPTION,
							Constants.FIELD_MAPPING_LABEL_OUTPUT_DESCRIPTION_DEFAULT_VALUE);
			fieldLabelsMap
					.put(Constants.FIELD_MAPPING_LABEL_CASTID_DESCRIPTION,
							Constants.FIELD_MAPPING_LABEL_CASTID_DESCRIPTION_DEFAULT_VALUE);
			fieldLabelsMap.put(
					Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION,
					Constants.FIELD_MAPPING_SUMMARY_JIRA_ORDER_DEFAULT_VALUE);
			fieldLabelsMap
					.put(Constants.FIELD_MAPPING_LABEL_DESCRIPTION_JIRA_DESCRIPTION,
							Constants.FIELD_MAPPING_DESCRIPTION_JIRA_ORDER_DEFAULT_VALUE);

			log.debug(Constants.FIELD_MAPPING_LABEL_OBJECT_FULL_NAME
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_OBJECT_FULL_NAME));
			log.debug(Constants.FIELD_MAPPING_LABEL_ADDED_TO_ACTION_PLAN_DATE
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_ADDED_TO_ACTION_PLAN_DATE));
			log.debug(Constants.FIELD_MAPPING_LABEL_ADDED_TO_ACTION_PLAN_DATE
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_ACTION_DEFINED_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_ACTION_DEFINED_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_METRIC_SHORT_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_METRIC_LONG_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_METRIC_LONG_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_REFERENCE_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_REFERENCE_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_REASON_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_REASON_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_REMEDIATION_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_REMEDIATION_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_REMEDIATION_EXAMPLE_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_REMEDIATION_EXAMPLE_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_VIOLATION_EXAMPLE_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_VIOLATION_EXAMPLE_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_TOTAL_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_TOTAL_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_OUTPUT_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_OUTPUT_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_CASTID_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_CASTID_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION));
			log.debug(Constants.FIELD_MAPPING_LABEL_DESCRIPTION_JIRA_DESCRIPTION
					+ "="
					+ props.getProperty(Constants.FIELD_MAPPING_LABEL_DESCRIPTION_JIRA_DESCRIPTION));
		}

		// PropertyConfigurator.configure(props);
	}

	/**
	 * Gets the cast to jira fields mapping.
	 * 
	 * @param field
	 *            the field
	 * @return the cast to jira fields mapping
	 */
	public String getCastToJiraFieldsMapping(String field) {
		String result;
		if (fieldLabelsMap.isEmpty()) {
			loadCastToJiraFieldsMapping();
		}
		if (!fieldLabelsMap.containsKey(field)) {
			log.error("Field label mapping value not found ="
					+ field
					+ ". Review the CastToJiraFieldsMapping.template file and ensure it has a mapping. Respect Uper-Lower Cases");
			log.error("As Field label mapping value not found, the following will be assigned ="
					+ Constants.FIELD_MAPPING_LABEL_UNKNOWN_DEFAULT_VALUE);
			result = Constants.FIELD_MAPPING_LABEL_UNKNOWN_DEFAULT_VALUE;
		} else {
			result = fieldLabelsMap.get(field)!=null?fieldLabelsMap.get(field):"";
		}

//		result=result.replace("{", "\\u007B").replace("}", "\\u007D");
		return result;
	}

	/*
	 * public String getJiraFields(String field) {
	 * 
	 * 
	 * }
	 */
	/**
	 * Gets the jira summary fields.
	 * 
	 * @param field
	 *            the field
	 * @return the jira summary fields
	 */
	public String getJiraFields(String field) {

		String result;
		if (fieldLabelsMap.isEmpty()) {
			loadCastToJiraFieldsMapping();
		}
		if (!fieldLabelsMap.containsKey(field)) {

			if (field
					.equals(Constants.FIELD_MAPPING_LABEL_SUMMARY_JIRA_DESCRIPTION)) {
				log.error("Field label mapping value not found ="
						+ field
						+ ". Review the Summary.Label property in CastToJiraFieldsMapping.template file and ensure it has a mapping. Respect Uper-Lower Cases");
				log.error("As Field label mapping value not found, the following will be assigned ="
						+ Constants.FIELD_MAPPING_SUMMARY_JIRA_ORDER_DEFAULT_VALUE);
				result = Constants.FIELD_MAPPING_SUMMARY_JIRA_ORDER_DEFAULT_VALUE;
			} else {
				log.error("Field label mapping value not found ="
						+ field
						+ ". Review the Description.JiraField property in CastToJiraFieldsMapping.template file and ensure it has a mapping. Respect Uper-Lower Cases");
				log.error("As Field label mapping value not found, the following will be assigned ="
						+ Constants.FIELD_MAPPING_SUMMARY_JIRA_ORDER_DEFAULT_VALUE);
				result = Constants.FIELD_MAPPING_SUMMARY_JIRA_ORDER_DEFAULT_VALUE;
			}
		} else {
			result = fieldLabelsMap.get(field);
		}

		return result;
	}	
}
