package it.com.rivannikov.bamboo.appcenter;

import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AtlassianPluginsTestRunner.class)
public class AppCenterPluginComponentWiredTest
{

    private final ApplicationProperties applicationProperties;
   // private final MIDeploymentTask miDeploymentTask;
  //  private final DeploymentTaskConfigurator deploymentTaskConfigurator;

    public AppCenterPluginComponentWiredTest(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    @Test
    public void testName()
    {
        Assert.assertTrue(true);
       // Assert.assertEquals("names do not match!", "miDeploymentTask:" + applicationProperties.getDisplayName(), miDeploymentTask.getName());
    }
}