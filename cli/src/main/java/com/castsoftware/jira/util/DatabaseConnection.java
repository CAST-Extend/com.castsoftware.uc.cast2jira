package com.castsoftware.jira.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Class DatabaseConnection establishes the database connection depending on
 * the parameters provided in the command line
 * 
 * @author FME
 * @version 1.1
 */

public class DatabaseConnection {

	/** The log. */
	public static Log log = LogFactory.getLog(DatabaseConnection.class);

	/** The database user. */
	private String databaseUser;

	/** The database password. */
	private String databasePassword;

	/** The database host. */
	private String databaseHost;

	/** The database name. */
	private String databaseName;

	/** The database port. */
	private String databasePort;

	/** The database provider. */
	private String databaseProvider;

	/** The connection. */
	private Connection connection = null;

	/**
	 * Instantiates a new database connection.
	 * 
	 * @param databaseUser
	 *            the database user
	 * @param databasePassword
	 *            the database password
	 * @param databaseHost
	 *            the database host
	 * @param databaseName
	 *            the database name
	 * @param databasePort
	 *            the database port
	 * @param databaseProvider
	 *            the database provider
	 * @throws Exception
	 *             the exception
	 * @throws SQLException
	 *             the SQL exception
	 */
	public DatabaseConnection(String databaseUser, String databasePassword,
			String databaseHost, String databaseName, String databasePort,
			String databaseProvider) throws  SQLException {
		setDatabaseUser(databaseUser);
		setDatabasePassword(databasePassword);
		setDatabaseHost(databaseHost);
		setDatabaseName(databaseName);
		setDatabasePort(databasePort);
		setDatabaseProvider(databaseProvider);

		log.debug("Staring " + getDatabaseProvider() + " JDBC Connection");
		try {
			if (getDatabaseProvider().toLowerCase().equals(Constants.DB_CSS)) {
				Class.forName(Constants.DB_JDBC_DRIVER_CSS);
			} else if (getDatabaseProvider().toLowerCase().equals(
					Constants.DB_ORACLE)) {
				Class.forName(Constants.DB_JDBC_DRIVER_ORACLE);
			} else if (getDatabaseProvider().toLowerCase().equals(
					Constants.DB_SQLSERVER)) {
				Class.forName(Constants.DB_JDBC_DRIVER_SQLSERVER);
			}
			log.debug(getDatabaseProvider() + " JDBC Driver Registered!");
			setCreateDBConnection();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	/**
	 * Gets the DB connection.
	 * 
	 * @return the DB connection
	 */
	public Connection getDBConnection() {
		return connection;
	}

	/**
	 * Sets the create db connection.
	 * 
	 * @throws SQLException
	 *             the SQL exception
	 */
	public void setCreateDBConnection() throws SQLException {
		try {
			StringBuffer connectionString = new StringBuffer();
			if (getDatabaseProvider().toLowerCase().equals(Constants.DB_CSS)) {
				connectionString.append(Constants.DB_CONN_STRING_CSS);
			} else if (getDatabaseProvider().toLowerCase().equals(
					Constants.DB_ORACLE)) {
				connectionString.append(Constants.DB_CONN_STRING_ORACLE);
			} else if (getDatabaseProvider().toLowerCase().equals(
					Constants.DB_SQLSERVER)) {
				connectionString.append(Constants.DB_CONN_STRING_SQLSERVER);
			}
			// connectionString.append("jdbc:postgresql://");
			connectionString.append(getDatabaseHost());
			connectionString.append(":");
			connectionString.append(getDatabasePort());
			if (getDatabaseProvider().toLowerCase().equals(Constants.DB_ORACLE)) {
				connectionString.append(":");
			} else {
				connectionString.append("/");
			}
			connectionString.append(getDatabaseName());

			// ("jdbc:oracle:thin:@myhost:1521:orcl", "scott", "tiger");
			// jdbc:jtds:<server_type>://<server>[:<port>][/<database>][;<property>=<value>[;...]]
			// jdbc:jtds:sqlserver://neptune.acme.com:1433/test

			if (log.isDebugEnabled())
			{
				log.debug("setCreateDBConnection() - Connection String: "
						+ connectionString.toString());
			}
			// connection =
			// DriverManager.getConnection("jdbc:postgresql://"+databaseHost
			// +":"+databasePort+"/"+databaseName,
			// databaseUser,databasePassword);
			connection = DriverManager.getConnection(
					connectionString.toString(), getDatabaseUser(),
					getDatabasePassword());
		} catch (SQLException e) {
			log.fatal("setCreateDBConnection() - Connection Failed! Check output log file"
					+ e.getMessage());
			throw e;
		}
		log.debug(" setCreateDBConnection() - Connection done!");
	}

	/**
	 * Close connection.
	 * 
	 * @param connection
	 *            the connection
	 */
	public void closeConnection() {
		Connection connection = getDBConnection();
		try {
			if (connection != null) {
				connection.close();
				log.debug("Connection closed!");
			}
		} catch (SQLException e) {
			log.error("Close Connection Error" + e.getMessage());
		}
	}

	/**
	 * Gets the database user.
	 * 
	 * @return the databaseUser
	 */
	public String getDatabaseUser() {
		return databaseUser;
	}

	/**
	 * Sets the database user.
	 * 
	 * @param databaseUser
	 *            the databaseUser to set
	 */
	public void setDatabaseUser(String databaseUser) {
		this.databaseUser = databaseUser;
	}

	/**
	 * Gets the database password.
	 * 
	 * @return the databasePassword
	 */
	public String getDatabasePassword() {
		return databasePassword;
	}

	/**
	 * Sets the database password.
	 * 
	 * @param databasePassword
	 *            the databasePassword to set
	 */
	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}

	/**
	 * Gets the database host.
	 * 
	 * @return the databaseHost
	 */
	public String getDatabaseHost() {
		return databaseHost;
	}

	/**
	 * Sets the database host.
	 * 
	 * @param databaseHost
	 *            the databaseHost to set
	 */
	public void setDatabaseHost(String databaseHost) {
		this.databaseHost = databaseHost;
	}

	/**
	 * Gets the database name.
	 * 
	 * @return the databaseName
	 */
	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * Sets the database name.
	 * 
	 * @param databaseName
	 *            the databaseName to set
	 */
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	/**
	 * Gets the database port.
	 * 
	 * @return the databasePort
	 */
	public String getDatabasePort() {
		return databasePort;
	}

	/**
	 * Sets the database port.
	 * 
	 * @param databasePort
	 *            the databasePort to set
	 */
	public void setDatabasePort(String databasePort) {
		this.databasePort = databasePort;
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

}
