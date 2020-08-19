# com.castsoftware.uc.cast2jira
Jenkins plugin and CLI to export CAST action items to Jira.  

Warning: The Extension described in this document is delivered as-is. This Extension is made available by CAST User Community and governed by Open Source License. Please consider all necessary steps to validate and to test the Extension in your environment before using it in production.

# Overview
The CAST AIP Action Plan to Jira Jenkins plugin is designed to allow the user to export the contents of the Engineering Dashboard (ED) Action Plan to Jira as a bug.  The export will contain the following information:  
1.	Business Criteria
2.	Technical Criteria
3.	A description of the issue
4.	A reason for the issue 
5.	Any available references for the issue
6.	An example of what is wrong with the code
7.	An example of what is needed to correct the code
8.	An extract of the code showing where the issue can be found
When the issue is resolved and a new analysis run on the application the plugin will mark the issue as “Resolved”.   

## Target audience
This document is to be used by the CAST Delivery Specialist or onsite administrator with a working knowledge of Jenkins. 

## Summary
This document provides installation, deployment and usage instructions for the CAST to Jira Extension.  

# Release History 
### CastToJira CLI
| Version  | Author | Description |
| ----- | -------------------- | --------- |
| 1.0.0 | Fernando Merino | Initial Version |
| 1.5.4 | Nevin Kaplan | Compatible with CAST between versions 7.3.6 and 8.2 |
| 1.6.0 | Nevin Kaplan | Made compatible with CAST v8.3 |
| 1.7.0 | Nevin Kaplan | Merged component version history |

### CastToJira Jenkins Plugin
| Version  | Author | Description |
| ----- | -------------------- | --------- |
| 1.0.0 | Fernando Merino | Initial Version |
| 2.11.0 | Nevin Kaplan | Allowed for more than one application |
| 1.7.0 | Nevin Kaplan | Merged component version history |

### CLI and Jenkins Plugin
| Version  | Author | Description |
| ----- | -------------------- | --------- |
| 1.8.0 | Nevin Kaplan  | * eliminate third party AIP in favor of the Atlassian supported version<br>* reworked login to accommodate new Atlassian requirements <br>* reworked status transitions to work with SONY's standard workflow |
| 1.9.0 | Nevin Kaplan | Security Enhancements |
| 1.10.0 | Nevin Kaplan| * Added workflow properties<br> * various bug fixes | 
| 1.10.1 | Nevin Kaplan| Corrected workflow issue  | 
| 1.10.2 | Nevin Kaplan| * Converted transaction properties to comma <br> * separated lists <br> * Added debug.workflow property| 
| 1.10.3 | Nevin Kaplan|* Aligned versions <br>*replace crupt jar file | 

# Use Cases
Transfer the CAST Action Plan to Jira to allow easier access by the clients development team.    

# Installation Instructions
1. Download the [Cast2Jira.zip](https://github.com/CAST-Extend/com.castsoftware.uc.cast2jira/blob/master/cast2jira.zip) file. 
1. Explode the contents of the file
1. Login to Jenkins and go to the Manage Plugin screen (Manage Jenkins → Manager Plugins)
1. Go to the Advanced tab - Upload Plugin section
1. Click on the Choose File button and locate the CastToJiraConnector.hpi file downloaded (found in the exploded zip)
1. Click the Upload button
1. If requested, restart Jenkins
1. Go to the Manage Jenkins-->Configure System, CAST Action Plan to Jira Setup section
1. Add the folder of the exploded zip file
1. The tool will close issues when resolved according to CAST, or add a resolution.  
> * To add a resolution check the "Don't close issue flag it as resolved when identified as fixed by CAST" checkbox.    
> * Fill in the Resolution textbox with a resolution that exists in the Jira system.  If it does not exist the plugin will NOT work properly. 

# Usage 
This extension has two components, an executable jar file and Jenkins plugin.  This means that the CAST Action plan items can be transferred as part of a Jenkins job or using a windows, or UNIX, scheduler. When running under Jenkins the plugin is accessing the executable jar file to export all data.  

## Jenkins Configuration
The configuration for the plugin is broken down into three parts.  CAST Database Connection information, Jira Connection information and a mapping between the analyzed application and a Jira project.  The database section is used to configure the location of the CAST database.  This does not include the application schema, which are configured in the linkage section.   

![](https://github.com/CAST-Extend/com.castsoftware.uc.cast2jira/blob/master/img/database.png)

Jira connection section allows for the plugin to be configured to access a specific Jira instance.  The user must have access to add, update and delete issue form Jira.  

![](https://github.com/CAST-Extend/com.castsoftware.uc.cast2jira/blob/master/img/jira.png)

The application linkage section allows the user to configure one, or more applications.  Containing four fields, each application can be configured to specific jira projects, use specific issue types, and attach a component name to the issue.  

![](https://github.com/CAST-Extend/com.castsoftware.uc.cast2jira/blob/master/img/linkage.png)

## Jenkins Global Configuration 

The Jenkins plugin relies on the executable jar file to export all action plan items from CAST to Jira.  Jenkins needs to know the location of this jar file.   This is done using the "Manage Jenkins -> Configure System" page, "CAST Action Plan to Jira Setup" section.  

Normally when an issue is marked as resolved in CAST, the Cast2Jira utility will mark it as closed in Jira.  If a user wants to use another resolution code, this can be done by checking the "Don't close issue flag it as resolved when identified as fixed by CAST" checkbox.  The user is then presented with the "Resolution" field, which must exist in Jira.  Now when the issue is marked as resolved in CAST the tool will use the new resolution.     

![](https://github.com/CAST-Extend/com.castsoftware.uc.cast2jira/blob/master/img/global.png)

# CLI
## Execution

java -jar CastToJira.jar <arguments ...>

| Argument | Description |
|----------|-------------|
| applicationname  | CAST AIP Application name found in AIP Console and Management Studio. |
| castusername  | The User Name used to login the AIP database. |
| castuserpassword  | The User Password used to login the AIP database. |
| databasehost  | Hostname or IP Address where the database server is running. |
| databaseport  | Database listening port |
| databasename  | Database name. If Oracle has been selected as database provider, this parameter is SID. |
| databaseprovider  | Database provider parameter is use to identify the central database server type being used, CSS, Oracle or SQLServer. <br><dir> * CSS – CAST Storage Server <br> * Oracle <br> * SQLServer |
| databaseschema  | The AIP Central database schema name |
| jiraissuetype  | The Jira issue type |
| jiraprojectname  | The Jira project name |
| jirarestapiurl  | The Jira REST API name |
| jirausername   | The Jira user name |
| jirauserpassword  | The Jira user password or API Id |



## Customization

### Jira Issue Customization
The information exported to Jira can be customized by modifying the CastToJiraFieldsMapping.template file, located in the export utility installation folder.  The template consists of two parts, CAST and Jira mappings.  
The CAST mapping section contains a list of all the CAST fields that are exported, formatted as a name-value pair, with the value being added to the Jira export.  The second part, Jira field mapping, has two fields, Summary and Description. Using them the utility knows where to put the CAST information.

Additionally, you can define how Jira custom field configurations are to be processed. At this time, the following  types of custom fields are supported:
1. Single line text fields
1. Multi-line text fields
1. Dropdown lists

Custom fields are typically named `customfield_99999`, in Jira. To populate values in the custom fields from the CastToJira plugin, you need to define them as required fields in Jira. Please refer to Jira documenation for details.

#### Setting up custom field processing
1. From a text editor, open the CastToJiraFieldsMapping.template file for editing.
1. Add this line in the file, to list the custom fields be auto-popluated. In this example, 3 fields are defined - `customField.names=customfield_10001;customfield_10002;customfield_10003`
1. Next, you need to define the type of each of the custom field used. The supported types are, `text` (for text fields) and `single` (for dropdown fields). Here is an example setting for dropdown fields: `customfield_10005.type=select`
1. If you wish to populate the text field with a value from one of the fields retrived from CAST, use the `customfield_10003.JiraField` setting. In the attached sample screenshot, Business Criteria is being assigned to `customfield_10001` and Source Code is displayed in the multi-line text field, `customfield_10002`. If you wish to display a default value instead, set a hard-coded value as in the case of `customfield_10003.label` field shown in the sample. In the case of dropdown fields, the hard-coded value needs to be one of the valid values of the dropdown field.
1. Save the changes to the template file.

![](https://github.com/CAST-Extend/com.castsoftware.uc.cast2jira/blob/master/img/Sample_CastToJiraFieldsMappings.png)

### Using A Custom Jira Workflow
This extension is designed to work with any workflow from the Jira “Software Simplified Workflow” to your custom workflow.  There is no additional configuration required if you are using the Classic Jira workflow.  To configure the extension to work any other workflow update the “workflow.properties”.   This file is provided in the installation package and should have been installed in the same folder as CAST2Jira.jar file.       

![](https://github.com/CAST-Extend/com.castsoftware.uc.cast2jira/blob/master/img/workflow1.png)

<ins>How it works</ins>

The “workflow.properties” file is divided into two parts, status and transition.  The status section is used to identify which status the ticket can transition to. The transition tells the software how to get to that status.  There are four possible states an issue can be transitioned to:
 
* status.open=OPEN
* status.reopen=REOPENED
* status.done=CLOSED
* status.progress=IN PROGRESS

The transitions properties should contain a semicolon separated list of Jira transtion codes:

* transition.done=Start Progress;done;accept issue;release issue
* transition.reopen=reject Issue

The final transition proerty is blacklist, used to prevent the extension from using that transition code.  

* transition.blacklist=CANCEL 

Under normal conditions if an issue does not exist in Jira and has already been marked as corrected in AIP, it will NOT be added to Jira.  The last property, debug.workflow, will add all issues to Jira, even if they are marked as corrected in AIP. When CAST2Jira is run and the debug.workflow is set to true, the issue will be added to Jira.  The next time it is run, it will attempt to close it in Jira.  

All properties are case insensitive. 

