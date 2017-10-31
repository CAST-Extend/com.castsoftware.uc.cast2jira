package com.castsoftware.jira;

import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.castsoftware.jira.pojo.ActionPlanViolation;
import com.castsoftware.jira.util.Constants;
import com.castsoftware.jira.util.OptionsValidation;


/**
 * SendToJira is the main class. It is the staring class to send action plan
 * violations to Jira
 * 
 * @author FME
 * @version 1.1
 */
public class SendToJira {

	/** The log. */
	public static Log log = LogFactory.getLog(SendToJira.class);
	
	/** The return value. */
	private static int returnValue = 0;

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		int index;
		
		System.out.println("CAST2Jira v" + SendToJira.class.getPackage().getImplementationVersion() + " (c) 2017, CAST Software, All Rights Reserved" );

		/*for (index = 0; index < args.length; ++index) {
			System.out.println("args[" + index + "]: " + args[index]);
		}*/

		final Options options = createOptions();
		OptionsValidation validation = new OptionsValidation(args);

		GetCastActionPlan gap = null;
		try {
			CommandLineParser parser = new GnuParser();
			CommandLine line = parser.parse(options, args);

			if (validation.getOptionsValidation(options)) {

				if (line.getOptionValue(Constants.CAST_REST_API_URL) != null) {
					/*
					 * Constructor of GetActionPlan to get the action plan from
					 * CAST REST API
					 */
					gap = new GetCastActionPlan(
							line.getOptionValue(Constants.CAST_APPLICATION_NAME),
							line.getOptionValue(Constants.CAST_USER_NAME),
							line.getOptionValue(Constants.CAST_USER_PASSWORD),
							line.getOptionValue(Constants.CAST_REST_API_URL));
				} else if (line.getOptionValue(Constants.CAST_DB_PROVIDER) != null) {
					/*
					 * Constructor of GetActionPlan to get the action plan from
					 * a database
					 */
					gap = new GetCastActionPlan(
							line.getOptionValue(Constants.CAST_APPLICATION_NAME),
							line.getOptionValue(Constants.CAST_USER_NAME),
							line.getOptionValue(Constants.CAST_USER_PASSWORD),
							line.getOptionValue(Constants.CAST_DB_HOST), line
									.getOptionValue(Constants.CAST_DB_NAME),
							line.getOptionValue(Constants.CAST_DB_PORT), line
									.getOptionValue(Constants.CAST_DB_SCHEMA),
							line.getOptionValue(Constants.CAST_DB_PROVIDER));
				}

				HashMap<Integer, ActionPlanViolation> map = gap.getActionPlan();
				log.info("Action Plan - Number of Violations: " + map.size());
				if (map.size() != 0) {

					String issueType;
					if (line.hasOption(Constants.JIRA_ISSUE_TYPE)
							&& line.getOptionValue(Constants.JIRA_ISSUE_TYPE) != null) {
						issueType = line
								.getOptionValue((Constants.JIRA_ISSUE_TYPE));
					} else {
						issueType = Constants.JIRA_DEFAULT_ISSUE_TYPE;
					}
					
					boolean markIssueResolved = false;
					String resolutionTxt = "Resolution Identified by CAST";
					if (line.hasOption(Constants.MARK_ISSUE_RESOLVED) )
					{
						String ov = line.getOptionValue(Constants.MARK_ISSUE_RESOLVED);
						if (ov!=null && ov.equalsIgnoreCase("true")) 
						{
							markIssueResolved=true;
							if (line.hasOption(Constants.RESOLUTION))
							{
								String rt = line.getOptionValue(Constants.RESOLUTION);
								if (rt!=null)
								{
									resolutionTxt = rt;
								}
							}
						}
					}
					
					
					CreateJiraIssues createissues = new CreateJiraIssues(
							line.getOptionValue(Constants.JIRA_USER_NAME),
							line.getOptionValue(Constants.JIRA_USER_PASSWORD),
							line.getOptionValue(Constants.JIRA_REST_API_URL),
							map,
							line.getOptionValue(Constants.JIRA_PROJECT_NAME),
							issueType, 
//							assigneeName,
							line.getOptionValue(Constants.CAST_APPLICATION_NAME),
							markIssueResolved,
							resolutionTxt,
							line.getOptionValue(Constants.COMPONENT));
					
					returnValue=createissues.getTotalNumOfIssuesNotAddedByError();
					
					log.info("Final Report : ");
					log.info("Number of Total Issues Processed : " + createissues.getTotalNumOfIssues());
					log.info("Number of Total Issues Added : " + createissues.getTotalNumOfIssuesAdded());
					log.info("Number of Total Issues Closed : " + createissues.getTotalNumOfIssuesClosed());
					log.info("Number of Total Issues Not Added because of Unprioritized or Low Priority Action Plan Item : " + createissues.getTotalNumOfUnprioritizedIssues());
					log.info("Number of Total Issues Not Added by Error : " + createissues.getTotalNumOfIssuesNotAddedByError());
					log.info("Number of Total Issues Not Added by Previous Existence in Jira: " + createissues.getTotalNumOfIssuesNotAddedByExist());
				} else {
					log.info("No violations in the action plan. Please review it");
				}
			}
		} catch (ParseException exp) {
			// Something went wrong
			log.error("ParseException Error: " + exp.getMessage());
			new HelpFormatter().printHelp(SendToJira.class.getCanonicalName(),
					options);
			System.exit(returnValue);

		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println("Please review the log in order to find out why the action plan delivery to Jira has not been successful added");
			System.exit(returnValue);
		}
		System.exit(returnValue);
	}

	/**
	 * Creates the options.
	 * 
	 * @return the options
	 */
	private static Options createOptions() {
		Options options = new Options();
		OptionGroup group = new OptionGroup();
		
		options.addOption(Constants.MARK_ISSUE_RESOLVED,true,"When set to true, when CAST identifies an issue as "
				+ "fixed it will be marked in Jira as resolved, NOT closed.  By default this flag is set to false");
		
		options.addOption(Constants.RESOLUTION,true,"When mark_issue_resolved is active, this arguement is "
				+ "used for the value of Jira resolution field");

		options.addOption(Constants.COMPONENT,true,"Identify the application project in the Jira \"components\" field");

		options.addOption(
				Constants.CAST_APPLICATION_NAME,
				true,
				"Application name for which you want to transfer the action plan to Jira. "
						+ "Please, the application name is case sensitive so has to be equal to the one introduced in CMS ");
		options.addOption(
				Constants.CAST_USER_NAME,
				true,
				"Cast User Name to login using Cast REST AIP or database depending on data source selection. "
						+ "If you use the paramenter -castrestapiurl it will be use as CAST REST AIP User. "
						+ "If you use the parameter -databaseprovider it will be use as Database User  ");
		options.addOption(
				Constants.CAST_USER_PASSWORD,
				true,
				"Cast User password to login using Cast REST AIP or database depending on source selection + "
						+ "If you use the paramenter -castrestapiurl it will be use as CAST REST AIP User password. "
						+ "If you use the parameter -databaseprovider it will be use as Database User Password  ");

		options.addOption(Constants.CAST_DB_HOST, true,
				"Hosname or IP Adresss where the database server is running");
		options.addOption(
				Constants.CAST_DB_NAME,
				true,
				"Database name. If Oracle has been selected as database provider, this parameter is SID");
		options.addOption(Constants.CAST_DB_PORT, true,
				"Database listening port");
		options.addOption(Constants.CAST_DB_SCHEMA, true,
				"Central schema where action plan is stored");
		options.addOption(
				Constants.JIRA_USER_NAME,
				true,
				"Jira User Name to login using REST AIP. All the violations included in the action plan will be assigned to this user");
		options.addOption(Constants.JIRA_USER_PASSWORD, true,
				"Jira User password to login using REST AIP ");
		options.addOption(Constants.JIRA_REST_API_URL, true,
				"URL to Jira REST AIP");
		options.addOption(
				Constants.JIRA_PROJECT_NAME,
				true,
				"Jira Project Name key. It is case-sensitive so must respect the same upper-lower case it has in Jira");

		options.addOption(
				Constants.JIRA_ISSUE_TYPE,
				true,
				"JIRA can be used to track many different types of issues. The currently defined by default issue types are listed below but check it. In addition, you can add more in the administration section of Jira if you want to use a new one in the command line of CastJiraConnector. It is case sensitive."
						+ "\nFOR REGULAR ISSUES"
						+ "\nBug - A problem which impairs or prevents the functions of the product."
						+ "\nImprovement - An improvement or enhancement to an existing feature or task."
						+ "\nNew Feature - A new feature of the product, which has yet to be developed."
						+ "\nTask - A task that needs to be done."
						+ "\nIf the parameter is not provided 'Task' issue will be used for all violations included in Action Plan");

		/**
		options.addOption(
				Constants.JIRA_ASSIGNEE_NAME,
				true,
				"Jira user which all violation will be assigned (it must be included in jira-developer group at least). If it is not provided, the violations will be assigned to the user used to loggin in Jira");
		**/
		options.addOption(Constants.LOG_PATH, true,
				"Folder where process log will be located");
		options.addOption(
				Constants.DEBUG,
				false,
				"if it is present, the debug mode will be activated. By default INFO level is setup. In order to setup the output use the following values:"
						+ "\nconsole"
						+ "\nfile"
						+ "\nIf you do not provided any of the values described above the output will be send to both");
		options.addOption(new Option(
				Constants.LOG_OUTPUT,
				true,
				"This paratemer allows to change the standard output. The accepted values are the following:"
						+ "\n\t file"
						+ "\n\t console"
						+ "\nBy default both standard outputs are active"));

		// options.addOption("h", "help", false, "Print Help Message");
		group.addOption(new Option(
				Constants.CAST_REST_API_URL,
				true,
				"URL to CAST REST AIP."
						+ "This parameter is mutually exclusive with -databaseprovider. Only one of the two must be provided on the command line "));
		group.addOption(new Option(
				Constants.CAST_DB_PROVIDER,
				true,
				"Datababase provider parameter is mutually exclusive with -castrestapiurl. Only one of the two must be provided on the command line."
						+ "\nThe valid values are the following: "
						+ "\nCSS"
						+ "\nOracle"
						+ "\nSQLServer\n"
						+ "\nThis parameter has to be provided together with the following ones in order to establish a database connection:"
						+ "\n-databasehost"
						+ "\n-databasename"
						+ "\n-databaseport" + "\n-databaseschema"));
		options.addOptionGroup(group);

		options.addOption(Constants.VERSION, false, "Cast Jira Connector 1.1."
				+ "\nTested in Jira version 6.1" + "\nCast Storage Service 1.0"
				+ "\nOracle & MSSQLServer available but not tested");

		options.addOption(
				Constants.ZEXAMPLES,
				false,
				"This option shows command line execution examples.The following examples take in account that lib folder is located in the same folder of CastJiraConnector.jar."
						+ "It means if CastJiraConnector.jar is located in c:\\temp\\CastJiraConnector the lib folder will be placed at c:\\temp\\CastJiraConnector\\lib"
						+ "\nExample with debug option activated and assigning issues to other user different to the user used to logging in Jira"
						+ "\njava -cp 'C:\\temp\\CastjiraConnector\\CastJiraConnector_lib\\*.jar' -jar CastJiraConnector.jar -applicationname MyTelco -castusername operator -castuserpassword CastAIP -databaseprovider CSS -databasehost localhost -databasename postgres -databaseport 2278 -databaseschema demo_central -jirarestapiurl http://localhost:8082/ -jiraprojectname CONN -jirausername fme -jirauserpassword 3203@ndromed@  -jiraassignee HLR -logpath c:\\temp -debug"
						+ "\nExample without debug option activated and assigning issues to the same user used to loggin in Jira"
						+ "\njava -cp 'C:\\temp\\CastjiraConnector\\CastJiraConnector_lib\\*.jar' -jar CastJiraConnector.jar -applicationname MyTelco -castusername operator -castuserpassword CastAIP -databaseprovider CSS -databasehost localhost -databasename postgres -databaseport 2278 -databaseschema demo_central -jirarestapiurl http://localhost:8082/ -jiraprojectname CONN -jirausername fme -jirauserpassword 3203@ndromed@ -logpath c:\\temp -debug"
						+ "\nExample without debug option activated and assigning issues to the same user used to loggin in Jira and establishing 'BUG' as issue type"
						+ "\njava -cp 'C:\\temp\\CastjiraConnector\\CastJiraConnector_lib\\*.jar' -jar CastJiraConnector.jar -applicationname MyTelco -castusername operator -castuserpassword CastAIP -databaseprovider CSS -databasehost localhost -databasename postgres -databaseport 2278 -databaseschema demo_central -jirarestapiurl http://localhost:8082/ -jiraprojectname CONN -jirausername fme -jirauserpassword 3203@ndromed@ -jiraissuetype Bug -logpath c:\\temp -debug");

		return options;
	}

}
