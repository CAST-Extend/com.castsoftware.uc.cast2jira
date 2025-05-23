package com.castsoftware.jira;

import java.sql.SQLException;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.castsoftware.jira.pojo.ActionPlanViolation;
import com.castsoftware.jira.util.DatabaseConnection;
import com.castsoftware.jira.util.SqlStatements;


/**
 * The Class GetCastActionPlan is the entry point to retrieve action plan from a
 * database
 * 
 * @author FME
 * @version 1.1
 */
public class GetCastActionPlan {

	/** The log. */
	public static Log log = LogFactory.getLog(GetCastActionPlan.class);

	/** The map. */
	private HashMap<Integer, ActionPlanViolation> map = new HashMap<Integer, ActionPlanViolation>(
			0);

	/** The appname. */
	private String appname;

	/** The castusername. */
	private String castusername;

	/** The castuserpassword. */
	private String castuserpassword;

	/** The castrestapiurl. */
	private String castrestapiurl;

	/** The schema profile. */
	private String schemaProfile;

	/** The database provider. */
	private String databaseProvider;

	/** The host. */
	private String host;

	/** The database. */
	private String database;

	/** The port. */
	private String port;

	/**
	 * Instantiates a new gets the cast action plan from CAST REST API.
	 * 
	 * @param appname
	 *            the appname
	 * @param castusername
	 *            the castusername
	 * @param castuserpassword
	 *            the castuserpassword
	 * @param castrestapiurl
	 *            the castrestapiurl
	 */
	public GetCastActionPlan(String appname, String castusername,
			String castuserpassword, String castrestapiurl) {
		setAppname(appname);
		setCastusername(castusername);
		setCastuserpassword(castuserpassword);
		setCastrestapiurl(castrestapiurl);

	}

	/**
	 * Instantiates a new gets the cast action plan From the database.
	 * 
	 * @param appname
	 *            the appname
	 * @param castusername
	 *            the castusername
	 * @param castuserpassword
	 *            the castuserpassword
	 * @param host
	 *            the host
	 * @param database
	 *            the database
	 * @param port
	 *            the port
	 * @param schemaProfile
	 *            the schema profile
	 * @param databaseProvider
	 *            the database provider
	 */
	public GetCastActionPlan(String appname, String castusername,
			String castuserpassword, String host, String database, String port,
			String schemaProfile, String databaseProvider) {
		setAppname(appname);
		setCastusername(castusername);
		setCastuserpassword(castuserpassword);
		setSchemaProfile(schemaProfile);
		setDatabaseProvider(databaseProvider);
		setHost(host);
		setDatabase(database);
		setPort(port);

	}

	/**
	 * Gets the action plan.
	 * 
	 * @return the action plan
	 * @throws Exception
	 *             the exception
	 */
	public HashMap<Integer, ActionPlanViolation> getActionPlan()
			throws Exception {
		execute();
		return map;
	}

	/**
	 * Execute the connection to the database & sql statement.
	 *
	 * @throws Exception
	 *             the exception
	 */
	private void execute() throws Exception {
		try {
			DatabaseConnection conn = new DatabaseConnection(getCastusername(),
					getCastuserpassword(), getHost(), getDatabase(), getPort(),
					getDatabaseProvider());

			SqlStatements sql = new SqlStatements(getAppname(), getSchemaProfile(), getDatabaseProvider());
			map = sql.getActionPlan(conn.getDBConnection());

			conn.closeConnection();
		} catch (Exception e) {
			log.fatal(
					"Action Plan Can not be retrieved. Ensure that database is available are all the parameters provided are the right ones",
					e);
			throw e;
		}
	}

	/**
	 * Gets the appname.
	 * 
	 * @return the appname
	 */
	public String getAppname() {
		return appname;
	}

	/**
	 * Sets the appname.
	 * 
	 * @param appname
	 *            the appname to set
	 */
	public void setAppname(String appname) {
		this.appname = appname;
	}

	/**
	 * Gets the castusername.
	 * 
	 * @return the castusername
	 */
	public String getCastusername() {
		return castusername;
	}

	/**
	 * Sets the castusername.
	 * 
	 * @param castusername
	 *            the castusername to set
	 */
	public void setCastusername(String castusername) {
		this.castusername = castusername;
	}

	/**
	 * Gets the castuserpassword.
	 * 
	 * @return the castuserpassword
	 */
	public String getCastuserpassword() {
		return castuserpassword;
	}

	/**
	 * Sets the castuserpassword.
	 * 
	 * @param castuserpassword
	 *            the castuserpassword to set
	 */
	public void setCastuserpassword(String castuserpassword) {
		this.castuserpassword = castuserpassword;
	}

	/**
	 * Gets the castrestapiurl.
	 * 
	 * @return the castrestapiurl
	 */
	public String getCastrestapiurl() {
		return castrestapiurl;
	}

	/**
	 * Sets the castrestapiurl.
	 * 
	 * @param castrestapiurl
	 *            the castrestapiurl to set
	 */
	public void setCastrestapiurl(String castrestapiurl) {
		this.castrestapiurl = castrestapiurl;
	}

	/**
	 * Gets the schema profile.
	 * 
	 * @return the schemaProfile
	 */
	public String getSchemaProfile() {
		return schemaProfile;
	}

	/**
	 * Sets the schema profile.
	 * 
	 * @param schemaProfile
	 *            the schemaProfile to set
	 */
	public void setSchemaProfile(String schemaProfile) {
		this.schemaProfile = schemaProfile;
	}

	/**
	 * Gets the database provider.
	 * 
	 * @return the databaseProvider
	 */
	public String getDatabaseProvider() {
		return databaseProvider;
	}

	/**
	 * Sets the database provider.
	 * 
	 * @param databaseProvider
	 *            the databaseProvider to set
	 */
	public void setDatabaseProvider(String databaseProvider) {
		this.databaseProvider = databaseProvider;
	}

	/**
	 * Gets the host.
	 * 
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Sets the host.
	 * 
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Gets the database.
	 * 
	 * @return the database
	 */
	public String getDatabase() {
		return database;
	}

	/**
	 * Sets the database.
	 * 
	 * @param database
	 *            the database to set
	 */
	public void setDatabase(String database) {
		this.database = database;
	}

	/**
	 * Gets the port.
	 * 
	 * @return the port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * Sets the port.
	 * 
	 * @param port
	 *            the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}

}
