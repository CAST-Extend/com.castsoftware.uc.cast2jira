/* Update MMA 2025-05-20:  code optimization */

package com.castsoftware.jenkins.CastToJira;

import org.kohsuke.stapler.DataBoundConstructor;

public class UseResolution
{
    private final String resolution;

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
