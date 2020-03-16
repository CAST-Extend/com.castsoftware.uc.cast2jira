package com.castsoftware.jenkins.CastToJira;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.castsoftware.jira.util.DatabaseConnection;
import com.castsoftware.jira.util.JiraHelper;

import hudson.Extension;
import hudson.Launcher;
import hudson.RelativePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BatchFile;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import net.sf.json.JSONObject;

/**
 * <p>
 * This plugin has been designed to transfer Cast Actions Plans to Jira
 * 
 * <p>
 * When a build is performed, the
 * {@link #perform(AbstractBuild, Launcher, BuildListener)} method will be
 * invoked.
 * 
 * @author FME
 */
public class CastJiraConnectorBuilder extends Builder // implements
// SimpleBuildStep
{
    private ArrayList<CastJiraLinkage> castJiraLinkage;

    private String castUserName;
    private String castUserPassword;

    private String useDatabase;
    private String databaseHost;
    private String databaseName;
    private String databasePort;

    private String jiraRestApiUrl;
    private String jiraUser;
    private String jiraUserPassword;

    private boolean debugEnabled;
    private String workFlow;
    int returnValue = -1;

    private static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
    }

    public static boolean isSolaris() {
        return (OS.indexOf("sunos") >= 0);
    }

    // Fields in config.jelly must match the parameter names in the
    // "DataBoundConstructor"
    /**
     * Instantiates a new cast jira connector plugin.
     */
    @DataBoundConstructor
    public CastJiraConnectorBuilder(String useDatabase, String databaseHost, String databasePort,
            String databaseName, String castUserName, String castUserPassword,
            String jiraRestApiUrl, String jiraUser, String jiraUserPassword,
            ArrayList<CastJiraLinkage> castJiraLinkage, boolean debugEnabled, String workFlow) {
        this.setDatabaseHost(databaseHost);
        this.setDatabaseName(databaseName);
        this.setDatabasePort(databasePort);
        this.setCastUserName(castUserName);
        this.setCastUserPassword(castUserPassword);
        this.setUseDatabase(useDatabase);

        this.setJiraRestApiUrl(jiraRestApiUrl);
        this.setJiraUserPassword(jiraUserPassword);
        this.setJiraUser(jiraUser);

        this.setDebugEnabled(debugEnabled);
        this.setWorkFlow(workFlow);

        this.castJiraLinkage = castJiraLinkage;

    }

    private boolean processAplication(AbstractBuild build, Launcher launcher,
            BuildListener listener, String appName, String schemaName, String projName,
            String jiraIssueType, String jiraComponentName) throws InterruptedException {
        boolean rslt = true;

        boolean useResolution = this.getDescriptor().isUseResolution();
        String resolution = this.getDescriptor().getResolution();
        if (resolution == null || resolution.isEmpty()) {
            useResolution = false;
        }

        String command = new StringBuffer()
                .append(" -applicationname \"#APP_NAME#\" -castusername \"#CAST_USER_NAME#\" ")
                .append("-castuserpassword \"#CAST_USER_PASSWORD#\" -databasehost \"#DATABASE_HOST#\" -databasename \"#DATABASE_NAME#\" -databaseport \"#DATABASE_PORT#\" ")
                .append("-databaseprovider \"#DATABASE_PROVIDER#\" -databaseschema \"#DATABASE_SCHEMA#\" -jiraissuetype \"#JIRA_ISSUE_TYPE#\" ")
                .append("-jiraprojectname \"#JIRA_PROJECT_NAME#\" -jirarestapiurl \"#JIRA_REST_URL#\" -jirausername \"#JIRA_USER_NAME#\" -jirauserpassword \"#JIRA_USER_PASSWORD#\"")
                .toString();

        if (useResolution) {
            String resolutionCommand = String.format(" -mark_issue_resolved %s -resolution \"%s\"",
                    useResolution, resolution);
            command = command + resolutionCommand;
        }

        if (jiraComponentName != null && jiraComponentName.length() > 0) {
            command = String.format("%s -component \"%s\"", command, jiraComponentName);
        }

        String extractorLocation = this.getDescriptor().getJiraExportLoc();
        if (extractorLocation == null || extractorLocation.isEmpty()) {
            listener.getLogger().println("Extraction location is empty");
            return false;
        }
        extractorLocation = new File(extractorLocation + "/CastToJira.jar").toString();

        String castUserName = this.getCastUserName();
        String castUserPassword = this.getCastUserPassword();
        String databaseHost = this.getDatabaseHost();
        String databaseName = this.getDatabaseName();
        String databasePort = this.getDatabasePort();
        String jiraRestUrl = this.getJiraRestApiUrl();
        String jiraUserName = this.getJiraUser();
        String jiraUserPassword = this.getJiraUserPassword();

        command = command.replaceAll("#LOCATION#", extractorLocation);
        command = command.replaceAll("#APP_NAME#", appName);
        command = command.replaceAll("#CAST_USER_NAME#", castUserName);
        command = command.replaceAll("#CAST_USER_PASSWORD#", castUserPassword);
        command = command.replaceAll("#DATABASE_HOST#", databaseHost);
        command = command.replaceAll("#DATABASE_NAME#", databaseName);
        command = command.replaceAll("#DATABASE_PORT#", databasePort);
        command = command.replaceAll("#DATABASE_PROVIDER#", "CSS");
        command = command.replaceAll("#DATABASE_SCHEMA#", schemaName);
        command = command.replaceAll("#JIRA_ISSUE_TYPE#", jiraIssueType);
        command = command.replaceAll("#JIRA_PROJECT_NAME#", projName);
        command = command.replaceAll("#JIRA_REST_URL#", jiraRestUrl);
        command = command.replaceAll("#JIRA_USER_NAME#", jiraUserName);
        command = command.replaceAll("#JIRA_USER_PASSWORD#", jiraUserPassword);

        command = new StringBuffer().append("java -jar ").append(extractorLocation).append(" ")
                .append(command).toString();

        if (isUnix()) {
            listener.getLogger().println("Executing Unix Shell");
            Shell shell = new Shell(command);
            shell.perform(build, launcher, listener);

        } else if (isWindows()) {
            listener.getLogger().println("Executing Windows Batch");

            // command = String.format("@@echo off%n%s", command);

            BatchFile batchFile = new BatchFile(command);
            rslt = batchFile.perform(build, launcher, listener);

        } else {
            listener.getLogger().println("Error - Unsupported Operating System");
        }

        return rslt;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener)
            throws InterruptedException {
        // This is where you 'build' the project.
        PrintStream logger = listener.getLogger();

        if (debugEnabled) {
            logger.println(getLogDateTime() + "- " + "castUserName: " + getCastUserName());
            logger.println(getLogDateTime() + "- " + "castUserPassword: ******");
            logger.println(getLogDateTime() + "- " + "useDatabase: " + getUseDatabase());
            logger.println(getLogDateTime() + "- " + "databaseHost: " + getDatabaseHost());
            logger.println(getLogDateTime() + "- " + "databaseName: " + getDatabaseName());
            logger.println(getLogDateTime() + "- " + "databasePort: " + getDatabasePort());
            logger.println(getLogDateTime() + "- " + "jiraRestApiUrl: " + getJiraRestApiUrl());
            logger.println(getLogDateTime() + "- " + "jiraUser: " + getJiraUser());
            logger.println(getLogDateTime() + "- " + "jiraUserPassword: *******");
            logger.println(getLogDateTime() + "- " + "debugEnabled: " + getDebugEnabled());
            logger.println("Workflow: " + getWorkFlow());
        }
        logger.println(getLogDateTime() + "- "
                + "Logger configuration. Details in Job Console & Jenkins - All System Logs");
        logger.println(getLogDateTime() + "- " + "Getting Action Plan...");

        String jiraUtilLoc = getDescriptor().getJiraExportLoc();
        if (jiraUtilLoc == null) {
            logger.println("ERROR: Jira Export Utility Location is not configured");
            return false;
        }

        for (CastJiraLinkage link : this.castJiraLinkage) {
            logger.println("Generating Action Plan for " + link.getAppName());

            processAplication(build, launcher, listener, link.getAppName(), link.getSchemaName(),
                    link.getProjName(), link.getJiraIssueType(), link.getJiraComponentName());
        }

        if (getReturnValue() == 0) {
            return true;
        } else {
            if (getWorkFlow().trim().toLowerCase().equals("no")) {
                return true;
            }
        }

        return true;
    }

    /**
     * @return the castUserName
     */
    public String getCastUserName() {
        return castUserName;
    }

    /**
     * @param castUserName
     *            the castUserName to set
     */
    public void setCastUserName(String castUserName) {
        this.castUserName = castUserName;
    }

    /**
     * @return the castUserPassword
     */
    public String getCastUserPassword() {
        return castUserPassword;
    }

    /**
     * @param castUserPassword
     *            the castUserPassword to set
     */
    public void setCastUserPassword(String castUserPassword) {
        this.castUserPassword = castUserPassword;
    }

    /**
     * @return the useDatabase
     */
    public String getUseDatabase() {
        return useDatabase == null ? "css" : useDatabase;
    }

    /**
     * @param useDatabase
     *            the useDatabase to set
     */
    public void setUseDatabase(String useDatabase) {
        this.useDatabase = useDatabase;
    }

    /**
     * @return the databaseHost
     */
    public String getDatabaseHost() {
        return databaseHost;
    }

    /**
     * @param databaseHost
     *            the databaseHost to set
     */
    public void setDatabaseHost(String databaseHost) {
        this.databaseHost = databaseHost;
    }

    /**
     * @return the databaseName
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * @param databaseName
     *            the databaseName to set
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * @return the databasePort
     */
    public String getDatabasePort() {
        return databasePort;
    }

    /**
     * @param databasePort
     *            the databasePort to set
     */
    public void setDatabasePort(String databasePort) {
        this.databasePort = databasePort;
    }

    /**
     * @return the jiraRestApiUrl
     */
    public String getJiraRestApiUrl() {
        return jiraRestApiUrl;
    }

    /**
     * @param jiraRestApiUrl
     *            the jiraRestApiUrl to set
     */
    public void setJiraRestApiUrl(String jiraRestApiUrl) {
        this.jiraRestApiUrl = jiraRestApiUrl;
    }

    /**
     * @return the jiraUser
     */
    public String getJiraUser() {
        return jiraUser;
    }

    /**
     * @param jiraUser
     *            the jiraUser to set
     */
    public void setJiraUser(String jiraUser) {
        this.jiraUser = jiraUser;
    }

    /**
     * @return the jiraUserPassword
     */
    public String getJiraUserPassword() {
        return jiraUserPassword;
    }

    /**
     * @param jiraUserPassword
     *            the jiraUserPassword to set
     */
    public void setJiraUserPassword(String jiraUserPassword) {
        this.jiraUserPassword = jiraUserPassword;
    }

    /**
     * @return the debugEnabled
     */
    public boolean getDebugEnabled() {
        return debugEnabled;
    }

    /**
     * @param debugEnabled
     *            the debugEnabled to set
     */
    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    /**
     * @return the returnValue
     */
    public int getReturnValue() {
        return returnValue;
    }

    /**
     * @param returnValue
     *            the returnValue to set
     */
    public void setReturnValue(int returnValue) {
        this.returnValue = returnValue;
    }

    /**
     * Gets the log date time.
     *
     * @return the log date time
     */
    private String getLogDateTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        return format.format(cal.getTime()).toString();
    }

    /**
     * @return the workFlow
     */
    public String getWorkFlow() {
        return workFlow;
    }

    /**
     * @param workFlow
     *            the workFlow to set
     */
    public void setWorkFlow(String workFlow) {
        this.workFlow = workFlow;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link CastJiraConnectorPlugin}. Used as a singleton. The
     * class is marked as public so that it can be accessed from views.
     * 
     * <p>
     * See
     * <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension
    // This indicates to Jenkins that this is an implementation of an extension
    // point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        private String jiraExportLoc;
        private JSONObject useResolution;
        private String resolution;

        // private NullProgressMonitor pm = new NullProgressMonitor();

        /**
         * To persist global configuration information, simply store it in a
         * field and call save().
         * 
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */

        /**
         * In order to load the persisted global configuration, you have to call
         * load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        private JiraHelper jiraClient = null;
        private JiraHelper forceJiraClient(String jiraRestApiUrl, String jiraUser,
                String jiraUserPassword) throws URISyntaxException {
            
            if (!jiraRestApiUrl.isEmpty() && !jiraUser.isEmpty() && !jiraUserPassword.isEmpty()) {
                jiraClient = new JiraHelper(jiraRestApiUrl, jiraUser, jiraUserPassword);
            } else {
                throw new IllegalArgumentException("Jira Connection error, invalide username or password");
            }
            return jiraClient;
        }

        private JiraHelper getJiraClient(String jiraRestApiUrl, String jiraUser,
                String jiraUserPassword, boolean force) throws URISyntaxException {
            if (jiraClient == null || force) {
                jiraClient = forceJiraClient(jiraRestApiUrl, jiraUser, jiraUserPassword);
            }
            return jiraClient;
        }

        /**
         * Performs on-the-fly validation of the form field
         * 
         * @param value
         *            the value
         * @return the form validation
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         * @throws ServletException
         *             the servlet exception
         */
        public FormValidation doCheckAppName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set the Application Name. It is mandatory");
            return FormValidation.ok();
        }

        public FormValidation doCheckCastUserName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set the Cast User Name. It is mandatory");
            return FormValidation.ok();
        }

        public FormValidation doCheckCastUserPassword(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set the Cast User Password. It is mandatory");
            return FormValidation.ok();
        }

        public FormValidation doCheckDatabaseHost(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set the Database Host. It is mandatory");
            return FormValidation.ok();
        }

        public FormValidation doCheckDatabaseName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set the Database Name. It is mandatory");
            return FormValidation.ok();
        }

        public FormValidation doCheckDatabasePort(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set the Database Port. It is mandatory.");
            return FormValidation.ok();
        }

        @Override
        public CastJiraConnectorBuilder newInstance(StaplerRequest req, JSONObject formData) {
            return req.bindJSON(CastJiraConnectorBuilder.class, formData);
        }

        public FormValidation doCheckSchemaProfile(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set the Database Schema. It is mandatory.");
            if (!value.toLowerCase().endsWith("_central"))
                return FormValidation.error(
                        "Please set Database Schema has to be the central one xxx_central. It is mandatory.");
            return FormValidation.ok();
        }

        public ListBoxModel doFillSchemaNameItems(
                @QueryParameter("databaseHost") @RelativePath("..") final String databaseHost,
                @QueryParameter("databaseName") @RelativePath("..") final String databaseName,
                @QueryParameter("databasePort") @RelativePath("..") final String databasePort,
                @QueryParameter("castUserName") @RelativePath("..") final String castUserName,
                @QueryParameter("castUserPassword") @RelativePath("..") final String castUserPassword,
                @QueryParameter("useDatabase") @RelativePath("..") final String useDatabase) {
            ListBoxModel m = new ListBoxModel();
            m.add("");
            DatabaseConnection conn = null;

            Secret password = hudson.util.Secret.decrypt(castUserPassword);

            if (databaseHost != null && databaseName != null && databasePort != null
                    && castUserName != null && castUserPassword != null) {
                try {
                    conn = new DatabaseConnection(castUserName, castUserPassword, databaseHost,
                            databaseName, databasePort, useDatabase);
                    String sql = "select schema_name from information_schema.schemata where schema_name like '%_central' order by schema_name";
                    PreparedStatement pst = conn.getDBConnection().prepareStatement(sql);
                    ResultSet rs = pst.executeQuery();
                    while (rs.next()) {
                        m.add(rs.getString("schema_name"));
                    }
                } catch (SQLException e) {
                    return m;
                } finally {
                    if (conn != null)
                        conn.closeConnection();
                }
            }
            return m;
        }

        public ListBoxModel doFillAppNameItems(
                @QueryParameter("databaseHost") @RelativePath("..") final String databaseHost,
                @QueryParameter("databaseName") @RelativePath("..") final String databaseName,
                @QueryParameter("databasePort") @RelativePath("..") final String databasePort,
                @QueryParameter("castUserName") @RelativePath("..") final String castUserName,
                @QueryParameter("castUserPassword") @RelativePath("..") final String castUserPassword,
                @QueryParameter("useDatabase") @RelativePath("..") final String useDatabase,
                @QueryParameter("schemaName") final String schemaName

        ) {
            ListBoxModel m = new ListBoxModel();
            DatabaseConnection conn = null;

            if (databaseHost != null && databaseName != null && databasePort != null
                    && castUserName != null && castUserPassword != null && schemaName != null) {
                try {
                    conn = new DatabaseConnection(castUserName, castUserPassword, databaseHost,
                            databaseName, databasePort, useDatabase);
                    String sql = new StringBuffer().append("select distinct app_name from ")
                            .append(schemaName).append(".csv_portf_tree").toString();
                    PreparedStatement pst = conn.getDBConnection().prepareStatement(sql);
                    ResultSet rs = pst.executeQuery();
                    while (rs.next()) {
                        m.add(rs.getString("app_name"));
                    }
                } catch (SQLException e) {
                    return m;
                } finally {
                    if (conn != null)
                        conn.closeConnection();
                }
            }
            return m;
        }

        public FormValidation doTestJiraConnection(
                @QueryParameter("jiraRestApiUrl") final String jiraRestApiUrl,
                @QueryParameter("jiraUser") final String jiraUser,
                @QueryParameter("jiraUserPassword") final String jiraUserPassword) {
            Logger log = LogManager.getLogManager().getLogger("hudson.WebAppMain");

            log.info("Jira Login Validation");

            Iterable<BasicProject> projects;
            try {
                log.info(String.format("User: %s URL: %s", jiraUser, jiraRestApiUrl));

                jiraClient = getJiraClient(jiraRestApiUrl, jiraUser, jiraUserPassword, true);

                projects = jiraClient.getProjectClient().getAllProjects().get();
                
                if (projects != null && projects.iterator().hasNext()) 
                    return FormValidation.ok("Jira connection OK");
                else 
                    return FormValidation.error("Invalid username or password");
            } catch (URISyntaxException ex) {
                log.info(ex.getMessage());
                return FormValidation.error("Unable to acces Jira API");
            } catch (RestClientException ex) {
                log.info(ex.getMessage());
                Throwable cause = ex;
                while (cause.getCause() != null) {
                    cause = cause.getCause();
                }
                String message = cause.getMessage();
                return FormValidation.error(message);
            } catch (IllegalArgumentException | InterruptedException | ExecutionException ex) {
                return FormValidation.error(ex.getMessage());
            }
        }

        public ListBoxModel doFillProjNameItems(
                @QueryParameter("jiraRestApiUrl") @RelativePath("..") final String jiraRestApiUrl,
                @QueryParameter("jiraUser") @RelativePath("..") final String jiraUser,
                @QueryParameter("jiraUserPassword") @RelativePath("..") final String jiraUserPassword) {
            ListBoxModel m = new ListBoxModel();
            Iterable<BasicProject> projects;
            try {
                jiraClient = getJiraClient(jiraRestApiUrl, jiraUser, jiraUserPassword,false);
                if (jiraClient != null) {
                    projects = jiraClient.getProjectClient().getAllProjects().get();
                    for (BasicProject bp : projects) {
                        m.add(bp.getName(), bp.getKey());
                    }
                }
            } catch (URISyntaxException | IllegalArgumentException | InterruptedException
                    | ExecutionException e) {
                // nothing to do here
            }
            return m;
        }

        public ListBoxModel doFillJiraIssueTypeItems(
                @QueryParameter("jiraRestApiUrl") @RelativePath("..") final String jiraRestApiUrl,
                @QueryParameter("jiraUser") @RelativePath("..") final String jiraUser,
                @QueryParameter("jiraUserPassword") @RelativePath("..") final String jiraUserPassword) {
            ListBoxModel m = new ListBoxModel();

            Iterable<IssueType> issueTypes;
            try {
                jiraClient = getJiraClient(jiraRestApiUrl, jiraUser, jiraUserPassword,false);
                issueTypes = jiraClient.getMetadataClient().getIssueTypes().get();
                for (IssueType bp : issueTypes) {
                    m.add(bp.getName());
                }
            } catch (URISyntaxException | IllegalArgumentException | InterruptedException
                    | ExecutionException e) {
                // nothing to do here
            }
            return m;
        }

        public ListBoxModel doFillJiraComponentNameItems(
                @QueryParameter("jiraRestApiUrl") @RelativePath("..") final String jiraRestApiUrl,
                @QueryParameter("jiraUser") @RelativePath("..") final String jiraUser,
                @QueryParameter("jiraUserPassword") @RelativePath("..") final String jiraUserPassword,
                @QueryParameter("projName") final String projName) {
            ListBoxModel m = new ListBoxModel();
            m.add("");
            if (jiraRestApiUrl != null && jiraUser != null && jiraUserPassword != null) {
                Iterable<BasicProject> projects;
                try {
                    jiraClient = getJiraClient(jiraRestApiUrl, jiraUser, jiraUserPassword,false);
                    projects = jiraClient.getProjectClient().getAllProjects().get();
                    for (BasicProject bp : projects) {
                        if (bp.getKey().equalsIgnoreCase(projName)) {
                            Project proj = jiraClient.getProjectClient().getProject(bp.getKey())
                                    .get();
                            Iterable<BasicComponent> components = proj.getComponents();
                            for (BasicComponent bc : components) {
                                m.add(bc.getName());
                            }
                        }
                    }

                } catch (URISyntaxException | IllegalArgumentException | InterruptedException
                        | ExecutionException e) {
                    // nothing to do here
                }

            }
            return m;
        }

        public FormValidation doTestDatabaseConnection(
                @QueryParameter("useDatabase") final String useDatabase,
                @QueryParameter("databaseHost") final String databaseHost,
                @QueryParameter("databaseName") final String databaseName,
                @QueryParameter("databasePort") final String databasePort,
                @QueryParameter("castUserName") final String castUserName,
                @QueryParameter("castUserPassword") final String castUserPassword)
                throws IOException, ServletException {
            if (castUserName.isEmpty())
                return FormValidation.error("Please fill-in the Cast User Name");
            if (castUserPassword.isEmpty())
                return FormValidation.error("Please fill-in the Cast User Password");
            if (databaseHost.isEmpty())
                return FormValidation.error("Please fill-in the database host");
            if (databaseName.isEmpty())
                return FormValidation.error("Please fill-in the database name");
            if (databasePort.isEmpty())
                return FormValidation.error("Please fill-in the database port");
            try {
                System.out.println(castUserName);

                DatabaseConnection conn = new DatabaseConnection(castUserName, castUserPassword,
                        databaseHost, databaseName, databasePort, useDatabase);

                if (conn.getDBConnection() == null) {
                    return FormValidation.error("Connection Failed");
                } else {
                    conn.closeConnection();
                }

            } catch (Exception e) {
                return FormValidation.error("Connection Failed: " + e.getMessage());
            }
            return FormValidation.ok("Connection Test Successful");
        }

        public FormValidation doCheckJiraRestApiUrl(@QueryParameter String value)
                throws IOException, ServletException {
            boolean isWarning = false;
            boolean isError = false;
            String msg = "";
            try {
                URL url = new URL(value);
                URLConnection conn = url.openConnection();
                conn.connect();
            } catch (MalformedURLException e) {
                msg = "URL syntax not valid. Please set a valid URL";
                isError = true;
            } catch (IllegalArgumentException e) {
                msg = "URL syntax not valid. Please set a valid URL";
                isError = true;
            } catch (IOException e) {
                msg = "Please set URL to a valid Jira REST AIP";
                isWarning = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (isError)
                return FormValidation.error(msg);
            else if (isWarning)
                return FormValidation.warning(msg);
            else
                return FormValidation.ok();
        }

        public FormValidation doCheckJiraUser(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.isEmpty())
                return FormValidation.error("Please set the Jira User Name. It is mandatory.");
            else
                return FormValidation.ok();
        }

        public FormValidation doCheckJiraUserPassword(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.isEmpty())
                return FormValidation.error("Please set the Jira User Password. It is mandatory.");
            else
                return FormValidation.ok();
        }

        public FormValidation doCheckJiraProjectName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.isEmpty())
                return FormValidation
                        .error("Please set the Jira Project Key Name. It is mandatory.");
            return FormValidation.ok();
        }

        public FormValidation doCheckJiraIssueType(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.isEmpty())
                return FormValidation.warning(
                        "If tou do not set the Jira Issue Type, 'Task' will be applied by default");
            return FormValidation.ok();
        }

        @SuppressWarnings("rawtypes")
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project
            // types
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Cast To Jira Integration Plugin";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            jiraExportLoc = formData.getString("jiraExportLoc");
            useResolution = (JSONObject) formData.getJSONObject("useResolution");
            if (!useResolution.isNullObject()) {
                resolution = useResolution.getString("resolution");
            } else {
                resolution = null;
            }

            save();
            return super.configure(req, formData);
        }

        public String getJiraExportLoc() {
            return jiraExportLoc;
        }

        public void setJiraExportLoc(String jiraExportLoc) {
            this.jiraExportLoc = jiraExportLoc;
        }

        public JSONObject getUseResolution() {
            return useResolution;
        }

        public boolean isUseResolution() {
            return useResolution == null ? false : true;
        }

        public void setUseResolution(JSONObject useResolution) {
            this.useResolution = useResolution;
        }

        public String getResolution() {
            return resolution;
        }

        public void setResolution(String resolution) {
            this.resolution = resolution;
        }

    } // end DescriptorImpl class

    public ArrayList<CastJiraLinkage> getCastJiraLinkage() {
        return castJiraLinkage;
    }

}
