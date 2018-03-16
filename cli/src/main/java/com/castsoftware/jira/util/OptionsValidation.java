package com.castsoftware.jira.util;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.validator.routines.UrlValidator;

/**
 * The Class OptionsValidation validates the options provided in the command
 * line
 * 
 * @author FME
 * @version 1.1
 */

public class OptionsValidation {

	/** The log. */
	public static Log log = LogFactory.getLog(OptionsValidation.class);

	/** The Command line parser. */
	private CommandLineParser parser;

	/** The Command line. */
	private CommandLine line;

	/** The args. */
	private String[] args;

	/**
	 * Instantiates a new options validation.
	 * 
	 * @param args
	 *            the args
	 */
	public OptionsValidation(String[] args) {
		setArgs(args);

	}

	/**
	 * Gets the options validation.
	 * 
	 * @param options
	 *            the options
	 * @return the options validation
	 * @throws ParseException
	 *             the parse exception
	 */
	public boolean getOptionsValidation(Options options) throws ParseException {

		try {
			parser = new GnuParser();
			line = parser.parse(options, args);
			// setCommandLine(parser.parse(options, getArgs()));
			setCommandLine(line);

			// parse the command line arguments
			// line = parser.parse(options, args);
			/*
			 * If the option is help, then it'll print it & exit
			 */
			if (line.hasOption("h")) { // No need to ask for the parameter
				// "help". Both are synonyms
				throw new ParseException("");
			}

			if (line.hasOption(Constants.VERSION)) { // No need to ask for the
														// parameter
				throw new ParseException("");
			}

			if (line.getOptionValue(Constants.CAST_APPLICATION_NAME) == null) {
				throw new org.apache.commons.cli.ParseException(
						"The Application name is mandatory");
			}

			Configuration updateConfig = new Configuration();
			updateConfig.setUpdateLog4jConfiguration(
					line.getOptionValue(Constants.LOG_PATH),
					line.hasOption(Constants.DEBUG),
					line.getOptionValue(Constants.LOG_OUTPUT),
					line.getOptionValue(Constants.CAST_APPLICATION_NAME));

			if (line.getOptionValue(Constants.CAST_USER_NAME) == null) {
				throw new org.apache.commons.cli.ParseException(
						"The Cast User Name to use Cast REST API is mandatory");
			}

			if (line.getOptionValue(Constants.CAST_USER_PASSWORD) == null) {
				throw new org.apache.commons.cli.ParseException(
						"The Cast User Password to use Cast REST API is mandatory");
			}
			if (line.getOptionValue(Constants.CAST_DB_PROVIDER) == null
					&& line.getOptionValue(Constants.CAST_REST_API_URL) == null) {
				throw new org.apache.commons.cli.ParseException(
						"One of the following parameter has to be provied: "
								+ "\n-databaseprovider" + "\n-castrestapiurl");
			}

			if (line.getOptionValue(Constants.CAST_DB_PROVIDER) != null)
				if (line.getOptionValue(Constants.CAST_DB_PROVIDER).trim()
						.toLowerCase().equals(Constants.DB_CSS)
						|| line.getOptionValue(Constants.CAST_DB_PROVIDER)
								.trim().toLowerCase()
								.equals(Constants.DB_ORACLE)
						|| line.getOptionValue(Constants.CAST_DB_PROVIDER)
								.trim().toLowerCase()
								.equals(Constants.DB_SQLSERVER)) {
					log.debug("Database provider selected is: "
							+ line.getOptionValue(Constants.CAST_DB_PROVIDER)
									.trim().toLowerCase());

					if (line.getOptionValue(Constants.CAST_DB_HOST) == null) {
						throw new org.apache.commons.cli.ParseException(
								"The parameter -databaseprovider has been provided so -databasehost is mandatory");
					}
					if (line.getOptionValue(Constants.CAST_DB_NAME) == null) {
						throw new org.apache.commons.cli.ParseException(
								"The parameter -databaseprovider has been provided so -databasename is mandatory");
					}
					if (line.getOptionValue(Constants.CAST_DB_PORT) == null) {
						throw new org.apache.commons.cli.ParseException(
								"The parameter -databaseprovider has been provided so -databaseport is mandatory");
					}
					if (line.getOptionValue(Constants.CAST_DB_SCHEMA) == null) {
						
						throw new org.apache.commons.cli.ParseException(
								"The parameter -databaseprovider has been provided so -databaseschema is mandatory");
					}else{
						if (!line.getOptionValue(Constants.CAST_DB_SCHEMA).toLowerCase().endsWith("_central")){
							throw new org.apache.commons.cli.ParseException(
									"-dabasechema has to be the central one xxx_central. It is mandatory");
						}
					}

				} else {
					throw new org.apache.commons.cli.ParseException(
							"The database provided value has to be among the supported values. Please review -h");
				}

			if (line.getOptionValue(Constants.CAST_REST_API_URL) != null) {
				throw new org.apache.commons.cli.ParseException(
						"This option is under-construction. Please use the -databaseprovider parameter ");
				/*String[] schemes = {"http","https"};
				UrlValidator urlValidator = new UrlValidator(schemes, UrlValidator.ALLOW_LOCAL_URLS);
	            if (!urlValidator.isValid(line.getOptionValue(Constants.CAST_REST_API_URL))){
	            	throw new org.apache.commons.cli.ParseException(
							"The Cast RESP API url has to be valid and well-formatted. Example:"
							+ "\n: http://localhost:8080/CASTWS ");
	            }*/
			}
			/*else{
				throw new org.apache.commons.cli.ParseException(
						"The Cast RESP API url is mandatory if you do not provide -databaseprovider parameter ");
			}*/

			if (line.getOptionValue(Constants.JIRA_USER_NAME) == null) {
				throw new org.apache.commons.cli.ParseException(
						"The Jira User Name to use Cast REST API is mandatory");
			}

			if (line.getOptionValue(Constants.JIRA_USER_PASSWORD) == null) {
				throw new org.apache.commons.cli.ParseException(
						"The Jira User Password to use Cast REST API is mandatory");
			}

			if (line.getOptionValue(Constants.JIRA_REST_API_URL) == null) {

				throw new org.apache.commons.cli.ParseException(
						"The Jira RESP API url is mandatory");
			}else{
				String[] schemes = {"http","https"};
				UrlValidator urlValidator = new UrlValidator(schemes, UrlValidator.ALLOW_LOCAL_URLS);
				if (!urlValidator.isValid(line.getOptionValue(Constants.JIRA_REST_API_URL))){
	            	throw new org.apache.commons.cli.ParseException(
							"The Jira RESP API url has to be valid and well-formatted. Example:"
							+ "\n: http://localhost:8080/");
	            }
			}
			if (line.getOptionValue(Constants.JIRA_PROJECT_NAME) == null) {
				throw new org.apache.commons.cli.ParseException(
						"The Jira Project Name is mandatory");
			}

		} catch (ParseException exp) {
			// Something went wrong
			throw exp;
		}
		return true;
	}

	/**
	 * Gets the command line.
	 * 
	 * @return the command line
	 */
	public CommandLine getCommandLine() {
		return line;
	}

	/**
	 * Sets the command line.
	 * 
	 * @param line
	 *            the new command line
	 */
	public void setCommandLine(CommandLine line) {
		this.line = line;
	}

	/**
	 * Gets the args.
	 * 
	 * @return the args
	 */
	public String[] getArgs() {
		return args;
	}

	/**
	 * Sets the args.
	 * 
	 * @param args
	 *            the args to set
	 */
	public void setArgs(String[] args) {
		this.args = args;
	}

}
