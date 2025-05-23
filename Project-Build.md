Build the entire project with Maven
mvn clean install
mvn clean install 

OR Build Individual Project 

# 1. Build the CLI tool
cd cli
mvn clean install

# 2. Build the Jenkins plugin
cd jenkins
mvn clean install

# 3. After building: 

## Testing the CLI Tool:

java -jar cli/target/CastToJira.jar -applicationname "YourAppName" \
  -castusername "YourCastUsername" \
  -castuserpassword "YourCastPassword" \
  -databasehost "YourDatabaseHost" \
  -databaseport "YourDatabasePort" \
  -databasename "YourDatabaseName" \
  -databaseprovider "CSS/Postgres" \
  -databaseschema "YourSchemaName_central" \
  -jiraissuetype "Bug" \
  -jiraprojectname "YourJiraProjectKey" \
  -jirarestapiurl "https://your-jira-instance" \
  -jirausername "YourJiraUsername" \
  -jirauserpassword "YourJiraPassword"


  ## Testing the Jenkins Plugin:
  1. Build the Jenkins plugin:
    cd jenkins
    mvn clean package
  2. The plugin will be generated as a .hpi file in the jenkins/target/ directory.
    - Go to "Manage Jenkins" → "Manage Plugins" → "Advanced" tab
    - Under "Upload Plugin", select the .hpi file from jenkins/target/
    - Click "Upload" and restart Jenkins if prompted
  3. Install the plugin in your Jenkins instance:

  4. Configure the plugin in Jenkins:
    - Go to "Manage Jenkins" → "Configure System"
    - Find the "CAST Action Plan to Jira Setup" section
    - Add the folder containing the CastToJira.jar file
  
  5. Add a build step to your Jenkins job:
    - In your job configuration, add a build step "Transfer CAST Action Plan to Jira"
    - Configure the CAST database connection, Jira connection, and application linkage