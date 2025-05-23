package io.jenkins.plugins.cast2Jira;

import org.htmlunit.html.HtmlPage;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertTrue;

public class InjectedTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void testClosuresPageLoads() throws Exception {
        JenkinsRule.WebClient webClient = jenkinsRule.createWebClient();
        HtmlPage page = webClient.goTo("closures/");
        assertTrue(page.asNormalizedText().contains("Hello from the Closures page!"));
    }
}
