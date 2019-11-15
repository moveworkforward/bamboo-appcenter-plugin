/*
 *  Copyright Roman Ivannikov. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.rivannikov.bamboo.appcenter;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.deployments.execution.DeploymentTaskContext;
import com.atlassian.bamboo.deployments.execution.DeploymentTaskType;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.rivannikov.bamboo.appcenter.util.AppCenterPublisherHelper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class AppCenterDeploymentTask implements DeploymentTaskType {

    @NotNull
    public TaskResult execute(@NotNull DeploymentTaskContext taskContext) throws TaskException {
        final TaskResultBuilder builder = TaskResultBuilder.newBuilder(taskContext).failed(); //Initially set to Failed.
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        final String appCenterApiUrl = taskContext.getConfigurationMap().get(BaseTaskConfigurator.APP_CENTER_SERVER_URL);
        final String appCenterToken = taskContext.getConfigurationMap().get(BaseTaskConfigurator.APP_CENTER_TOKEN);
        final String appCenterOwner = taskContext.getConfigurationMap().get(BaseTaskConfigurator.APP_CENTER_OWNER);
        final String appCenterApp = taskContext.getConfigurationMap().get(BaseTaskConfigurator.APP_CENTER_APP);
        final String appCenterDestination = taskContext.getConfigurationMap().get(BaseTaskConfigurator.APP_CENTER_DESTINATION);
        final String appCenterDistributionGroup = taskContext.getConfigurationMap().get(BaseTaskConfigurator.APP_CENTER_DISTRIBUTION_GROUP);
        final String artifactPath = taskContext.getConfigurationMap().get(BaseTaskConfigurator.ARTIFACT_PATH);
        final boolean debug = Boolean.valueOf(taskContext.getConfigurationMap().getOrDefault(BaseTaskConfigurator.DEBUG, "false"));

        try {
            AppCenterPublisherHelper helper = new AppCenterPublisherHelper(
                    taskContext.getWorkingDirectory(),
                    buildLogger,
                    appCenterApiUrl,
                    appCenterToken,
                    appCenterOwner,
                    appCenterApp,
                    artifactPath,
                    appCenterDestination,
                    appCenterDistributionGroup
            );
            helper.setDebug(debug);
            helper.init();
            helper.publish();

            builder.success();
        } catch (IllegalArgumentException e) {
            buildLogger.addBuildLogEntry("Exception: " + e.getMessage());
            builder.failed();
        } catch (IOException e) {
            buildLogger.addBuildLogEntry("Exception: " + e.getMessage());
            builder.failed();
        }

        return builder.build();
    }
}