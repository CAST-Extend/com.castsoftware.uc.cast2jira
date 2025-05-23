package io.jenkins.plugins.cast2Jira;

import hudson.Extension;
import hudson.model.RootAction;

@Extension
public class ClosuresAction implements RootAction {

    @Override
    public String getIconFileName() {
        return null; // No icon in side panel
    }

    @Override
    public String getDisplayName() {
        return "Closures";
    }

    @Override
    public String getUrlName() {
        return "closures"; // This creates /closures/ endpoint
    }
}
