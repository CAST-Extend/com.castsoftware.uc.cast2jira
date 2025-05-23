package com.castsoftware.jira.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.castsoftware.jira.pojo.ActionPlanViolation;

/**
 * The Class SqlStatements stores the sql statement used to retrieve the action
 * plan stored in the database
 * 
 * @author FME
 * @version 1.1
 */

public class SqlStatements
{

	/** The log. */
	public static Log log = LogFactory.getLog(SqlStatements.class);

	/** The appName. */
	private String appName;

	/** The schema profile. */
	private String schemaProfile;

	/** The database provider. */
	private String databaseProvider;

	/** The map. */
	private final HashMap<Integer, ActionPlanViolation> map = new HashMap<>(0);

	/**
	 * Instantiates a new sql statements.
	 * 
	 * @param appName
	 *            the appName
	 * @param schemaProfile
	 *            the schema profile
	 * @param databaseProvider
	 *            the database provider
	 * @throws Exception
	 *             the exception
	 */
	public SqlStatements(String appName, String schemaProfile, String databaseProvider) throws Exception
	{
		setAppName(appName);
		setSchemaProfile(schemaProfile);
		setDatabaseProvider(databaseProvider);
	}

	/**
	 * Gets the action plan.
	 * 
	 * @param connection
	 *            the connection
	 * @return the action plan
	 * @throws SQLException
	 *             the SQL exception
	 */
	public HashMap<Integer, ActionPlanViolation> getActionPlan(Connection connection) throws SQLException {
		PreparedStatement pst = null;
		ResultSet rs = null;

		try {
			String sqlString = getSQLStatement();
			pst = connection.prepareStatement(sqlString);

			if (log.isDebugEnabled()) {
				log.debug("Sql Statement to execute to get action plan: " + sqlString);
			}

			// record key "setKey()"
			rs = pst.executeQuery();
			ViolationCRC crc = new ViolationCRC();
			String fc = "";
			while (rs.next()) {
				ActionPlanViolation mr = new ActionPlanViolation(rs.getLong("object_id"), rs.getString("tag"), rs.getInt("priority"),
						rs.getTimestamp("fecha").toString(), rs.getTimestamp("first_snapshot_date").toString(),
						rs.getString("action_message"), rs.getString("object_name"), rs.getInt("metric_id"),
						rs.getString("metric"), rs.getString("reason"), rs.getString("desciption"),
						rs.getString("remediation"), rs.getString("reference"), rs.getString("vil_example"),
						rs.getString("rem_exampel"), rs.getString("output"), rs.getString("total"),
						rs.getString("source_path"), rs.getInt("line_start"), rs.getInt("line_end"),
						rs.getString("source_code"), rs.getString("tech_criteria"), rs.getString("business_criteria"),
						rs.getInt("violation_status"));
				try {
					fc = mr.getFieldsConcatenated();
					crc.setHashCode(fc);
					map.put(crc.getHashCode(), mr);
				} catch (Exception ex) {
					log.error(
							"CRC code has not been computed - Review the SqlStatement output because the record will not be added to Jira : "
									+ fc + ". Exception:" + ex.getMessage());
				}
				if (log.isDebugEnabled()) {
					log.debug("Record concatenated: " + fc);
				}
			}

		} catch (SQLException e) {
			log.fatal("getActionPlan(): Error Getting Action Plan!" + e.getMessage());
			throw e;

		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
					log.warn("Failed to close ResultSet: " + ex.getMessage());
				}
			}
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException ex) {
					log.warn("Failed to close PreparedStatement: " + ex.getMessage());
				}
			}
		}

		return map;
	}

	// (select dmd2.metric_description from
	// version737_central.dss_metric_descriptions dmd2
	// where dmd2.metric_id=dmd.metric_id and dmd2.language = dmd.language and
	// dmd2.description_type_id = 1) reason,

	private String getMetricDescriptionStatement(String name, int typeId) {

        return new StringBuffer().append("\n(select dmd").append(typeId).append(".metric_description from ")
                .append(getSchemaProfile()).append(".dss_metric_descriptions dmd").append(typeId).append(" \n")
                .append("where dmd").append(typeId).append(".metric_id=dmd.metric_id and dmd").append(typeId)
                .append(".language = dmd.language AND dmd").append(typeId).append(".description_type_id = ").append(typeId)
                .append(") ").append(name).toString();
	}

	/**
	 * Gets the SQL statement.
	 * 
	 * @return the SQL statement
     */
	private String getSQLStatement() 
	{
		String techCriteria = new StringBuffer().append("\n(select string_agg(")
				.append("dmdp.metric_description,', ') from ").append(getSchemaProfile())
				.append(".dss_metric_type_trees dmt, ").append(getSchemaProfile())
				.append(".dss_metric_descriptions dmdp ").append("where dmt.metric_id=vap.metric_id ")
				.append("AND dmdp.language = 'ENGLISH' ").append("AND dmdp.description_type_id=0 ")
				.append("AND dmdp.metric_id=dmt.metric_parent_id ").append(" ) tech_criteria, \n").toString();

		String businessCriteria = new StringBuffer().append("\n( select string_agg(dmdp.metric_description,' ,')  ")
				.append("from ").append(getSchemaProfile()).append(".dss_metric_type_trees dmt, ")
				.append(getSchemaProfile()).append(".dss_metric_type_trees bdmt, ").append(getSchemaProfile())
				.append(".dss_metric_descriptions dmdp ").append("where dmt.metric_id=vap.metric_id ")
				.append("AND bdmt.metric_id=dmt.metric_parent_id AND dmdp.language = 'ENGLISH' AND dmdp.description_type_id=0 ")
				.append("AND dmdp.metric_id=bdmt.metric_parent_id AND dmdp.metric_id in (60011,60012,60013,60014,60016) ")
				.append(") business_criteria	\n").toString();

        return new StringBuffer()
                .append("SELECT distinct vap.object_id, dmd.metric_id, dmd.metric_description AS metric, dso.object_full_name AS object_name, vap.tag, vap.priority, ")
                .append("dvs.snapshot_id, vap.first_snapshot_date, vap.sel_date AS fecha, vap.action_def AS action_message,  dvs.violation_status, ")
                .append("dsp.line_start,  dsp.line_end, dcs.source_path,  dcs.source_code,")
                .append(getMetricDescriptionStatement("reason", 1)).append(",")
                .append(getMetricDescriptionStatement("desciption", 2)).append(",")
                .append(getMetricDescriptionStatement("remediation", 3)).append(",")
                .append(getMetricDescriptionStatement("reference", 4)).append(",")
                .append(getMetricDescriptionStatement("vil_example", 5)).append(",")
                .append(getMetricDescriptionStatement("rem_exampel", 6)).append(",")
                .append(getMetricDescriptionStatement("output", 7)).append(",")
                .append(getMetricDescriptionStatement("total", 1)).append(",").append(techCriteria)
                .append(businessCriteria).append("\nFROM\n")
                .append(String.format("%s.%s %s,\n", getSchemaProfile(), "viewer_action_plans", "vap"))
                .append(String.format("%s.%s %s,\n ", getSchemaProfile(), "dss_objects", "dso"))
                .append(String.format("%s.%s %s, \n", getSchemaProfile(), "dss_violation_statuses ", "dvs"))
                .append(String.format("%s.%s %s, \n", getSchemaProfile(), "dss_metric_descriptions", "dmd"))
                .append(String.format("%s.%s %s, \n", getSchemaProfile(), "dss_translation_table", "dtt"))
                .append(String.format("%s.%s %s,\n ", getLocalDatabase(), "dss_source_positions", "dsp"))
                .append(String.format("%s.%s %s\n ", getLocalDatabase(), "dss_code_sources", "dcs")).append("\n WHERE ")
                .append("vap.object_id = dso.object_id ").append("and dvs.object_id = vap.object_id\n")
                .append("and dvs.diag_id = vap.metric_id \n")
                .append(String.format(
                        "and dvs.snapshot_id = (select max(snapshot_id) from %s.dss_violation_statuses dvs2 where dvs2.object_id=vap.object_id and dvs2.diag_id=vap.metric_id) \n",
                        getSchemaProfile()))
                .append("and vap.priority > 0 \n")
                .append("and dmd.metric_id = vap.metric_id and dmd.language = 'ENGLISH'  and dmd.description_type_id = 0 \n")
                .append("and dtt.object_id = vap.object_id \n").append("and dtt.site_object_id = dsp.object_id  \n")
                .append("and dsp.source_id = dcs.source_id \n")
                .toString();

	}

	/**
	 * Gets the appName.
	 * 
	 * @return the appName
	 */
	public String getAppName()
	{
		return appName;
	}

	/**
	 * Sets the appName.
	 * 
	 * @param appName
	 *            the appName to set
	 */
	public void setAppName(String appName)
	{
		this.appName = appName;
	}

	/**
	 * Gets the schema profile.
	 * 
	 * @return the schemaProfile
	 */
	public String getSchemaProfile()
	{
		return schemaProfile;
	}

	/**
	 * Gets the local database.
	 * 
	 * @return the schemaProfile
	 */
	public String getLocalDatabase()
	{
		return schemaProfile.replace("central", "local");
	}

	/**
	 * Sets the schema profile.
	 * 
	 * @param schemaProfile
	 *            the schemaProfile to set
	 */
	public void setSchemaProfile(String schemaProfile)
	{
		this.schemaProfile = schemaProfile;
	}

	/**
	 * Gets the database provider.
	 * 
	 * @return the databaseProvider
	 */
	public String getDatabaseProvider()
	{
		return databaseProvider;
	}

	/**
	 * Sets the database provider.
	 * 
	 * @param databaseProvider
	 *            the databaseProvider to set
	 */
	public void setDatabaseProvider(String databaseProvider)
	{
		this.databaseProvider = databaseProvider;
	}

}
