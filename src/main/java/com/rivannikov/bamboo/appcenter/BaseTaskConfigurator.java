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

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;


public class BaseTaskConfigurator extends AbstractTaskConfigurator {

    public static final String APP_CENTER_SERVER_URL = "serverUrl";
    public static final String APP_CENTER_TOKEN = "token";
    public static final String APP_CENTER_OWNER = "ownerName";
    public static final String APP_CENTER_APP = "appName";
    public static final String APP_CENTER_DESTINATION = "destination";
    public static final String APP_CENTER_DISTRIBUTION_GROUP = "distributionGroupName";
    public static final String ARTIFACT_PATH = "artifactPath";
    public static final String DEBUG = "debug";

    public BaseTaskConfigurator() {
    }

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        config.put(APP_CENTER_SERVER_URL, params.getString(APP_CENTER_SERVER_URL));
        config.put(APP_CENTER_TOKEN, params.getString(APP_CENTER_TOKEN));
        config.put(APP_CENTER_OWNER, params.getString(APP_CENTER_OWNER));
        config.put(APP_CENTER_APP, params.getString(APP_CENTER_APP));
        config.put(APP_CENTER_DESTINATION, params.getString(APP_CENTER_DESTINATION));
        config.put(APP_CENTER_DISTRIBUTION_GROUP, params.getString(APP_CENTER_DISTRIBUTION_GROUP));
        config.put(ARTIFACT_PATH, params.getString(ARTIFACT_PATH));
        config.put(DEBUG, params.getString(DEBUG));
        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {
        super.populateContextForCreate(context);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);
        context.put(APP_CENTER_SERVER_URL, taskDefinition.getConfiguration().get(APP_CENTER_SERVER_URL));
        context.put(APP_CENTER_TOKEN, taskDefinition.getConfiguration().get(APP_CENTER_TOKEN));
        context.put(APP_CENTER_OWNER, taskDefinition.getConfiguration().get(APP_CENTER_OWNER));
        context.put(APP_CENTER_APP, taskDefinition.getConfiguration().get(APP_CENTER_APP));
        context.put(APP_CENTER_DESTINATION, taskDefinition.getConfiguration().get(APP_CENTER_DESTINATION));
        context.put(APP_CENTER_DISTRIBUTION_GROUP, taskDefinition.getConfiguration().get(APP_CENTER_DISTRIBUTION_GROUP));
        context.put(ARTIFACT_PATH, taskDefinition.getConfiguration().get(ARTIFACT_PATH));
        context.put(DEBUG, taskDefinition.getConfiguration().get(DEBUG));
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);

        validateNotEmpty(params, errorCollection, APP_CENTER_SERVER_URL);
        validateNotEmpty(params, errorCollection, APP_CENTER_TOKEN);
        validateNotEmpty(params, errorCollection, APP_CENTER_OWNER);
        validateNotEmpty(params, errorCollection, APP_CENTER_APP);
        validateNotEmpty(params, errorCollection, APP_CENTER_DESTINATION);
        validateNotEmpty(params, errorCollection, APP_CENTER_DISTRIBUTION_GROUP);

        validateNotEmpty(params, errorCollection, ARTIFACT_PATH);
        final String artifactPath = params.getString(ARTIFACT_PATH);
        if (artifactPath == null || (!artifactPath.endsWith(".apk") && !artifactPath.endsWith(".ipa"))) {
            errorCollection.addError(ARTIFACT_PATH, "Should be path to *.apk or *.ipa file");
        }

    }

    private void validateNotEmpty(@NotNull ActionParametersMap params, @NotNull final ErrorCollection errorCollection, @NotNull String key) {
        final String value = params.getString(key);
        if (StringUtils.isEmpty(value)) {
            errorCollection.addError(key, "This field can't be empty");
        }
    }

}
