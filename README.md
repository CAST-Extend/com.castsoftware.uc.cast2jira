# com.castsoftware.uc.cast2jira
Jenkins plugin and CLI to export CAST action items to Jira.  

# Overview
The CAST AIP Action Plan to Jira Jenkins plugin is designed to allow the user to export the contents of the CAST Engineering Portal (CEP) Action Plan to Jira as a bug.  The export will contain the following information:  
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
| 1.8.0 | Nevin Kaplan  | * eliminate third party AIP in favor of the Atlassian supported version * reworked login to accommodate new Atlassian requirements * reworked status transitions to work with SONY's standard workflow |


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

# Executable Jar configuration
## Jira Issue Customization
The information exported to Jira can be customized by modifying the CastToJiraFieldsMapping.template file, located in the export utility installation folder.  The template consists of two parts, CAST and Jira mappings.  
The CAST mapping section contains a list of all the CAST fields that are exported, formatted as a name value pair, with the value being added to the Jira export.  The second part, Jira field mapping has two fields, Summary and Description. Using them the utility knows where to put the cast information.  

![](https://github.com/CAST-Extend/com.castsoftware.uc.cast2jira/blob/master/img/custome.png)


