package com.castsoftware.jenkins.CastToJira;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class CastJiraLinkage extends AbstractDescribableImpl<CastJiraLinkage>
{
	private String appName;
	private String projName;
	private String schemaName;
	private String jiraIssueType;
	private String jiraComponentName;

	// private String jiraAssignee;

	@DataBoundConstructor
	public CastJiraLinkage(String appName, String projName, String schemaName, String jiraIssueType,
			String jiraComponentName)
	{
		setAppName(appName);
		setProjName(projName);
		setSchemaName(schemaName);
		setJiraIssueType(jiraIssueType);
		setJiraComponentName(jiraComponentName);
	}

	@Override
	public Descriptor<CastJiraLinkage> getDescriptor()
	{
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static class DescriptorImpl extends Descriptor<CastJiraLinkage>
	{
		private String name;

		public DescriptorImpl()
		{
			load();
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException
		{
			req.bindJSON(this, formData);
			save();
			return true;
			// return super.configure(req, formData);
		}

		@Override
		public String getDisplayName()
		{
			return "";
		}

		public String getName()
		{
			return name;
		}
	}

	public String getAppName()
	{
		return appName;
	}

	public void setAppName(String appName)
	{
		this.appName = appName;
	}

	public String getProjName()
	{
		return projName;
	}

	public void setProjName(String projName)
	{
		this.projName = projName;
	}

	public String getSchemaName()
	{
		return schemaName;
	}

	public void setSchemaName(String schemaName)
	{
		this.schemaName = schemaName;
	}

	public String getJiraIssueType()
	{
		return jiraIssueType;
	}

	public void setJiraIssueType(String jiraIssueType)
	{
		this.jiraIssueType = jiraIssueType;
	}

	 public String getJiraComponentName()
	 {
	 return jiraComponentName;
	 }
	
	 public void setJiraComponentName(String jiraComponentName)
	 {
	 this.jiraComponentName = jiraComponentName;
	 }

}
