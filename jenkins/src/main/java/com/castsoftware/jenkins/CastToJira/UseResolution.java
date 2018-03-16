package com.castsoftware.jenkins.CastToJira;

import org.kohsuke.stapler.DataBoundConstructor;

public class UseResolution
{
    private String resolution;

    @DataBoundConstructor
    public UseResolution(String text)
    {
        this.resolution = text;
    }

	public String getResolution()
	{
		return resolution;
	}
}	
