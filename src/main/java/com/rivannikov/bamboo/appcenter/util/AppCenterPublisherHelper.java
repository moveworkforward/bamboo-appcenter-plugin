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
package com.rivannikov.bamboo.appcenter.util;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskException;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.rivannikov.bamboo.appcenter.model.AppCenterReleaseUploadsCommitResponse;
import com.rivannikov.bamboo.appcenter.model.AppCenterReleaseUploadsResponse;
import com.rivannikov.bamboo.appcenter.model.AppCenterReleasesResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

/**
 * Helper class to initialize the publisher APIs client library.
 * <p>
 * Before making any calls to the API through the client library you need to
 * call the {@link #init()} method.
 * This will run all precondition checks.
 * </p>
 */
public class AppCenterPublisherHelper {

    private final LogUtils LOG;

    private final File workingDirectory;
    private final String serverUrl;
    private final String token;
    private final String owner;
    private final String app;
    private final String destinations;
    private final String distributionGroup;
    private final String artifactPath;

    private AppCenterService appCenterService;

    public void setDebug(boolean debug) {
        LOG.setDebug(debug);
        if (appCenterService != null) {
            appCenterService.setDebug(debug);
        }
    }

    /**
     * @param workingDirectory
     * @param buildLogger
     * @param serverUrl the URL to the AppCenter Server
     * @param token the token is used for authentication for all AppCenter API calls
     * @param owner the {owner_name} for the app that you wish to upload to AppCenter Server
     * @param app the {app_name} for the app that you wish to upload to AppCenter Server
     * @param artifactPath the artifact file path of the apk/ipa to upload
     * @param destination the uploaded release to destinations using testers, groups, or stores
     * @param distributionGroup the distribution group in the org specified
     */
    public AppCenterPublisherHelper(
            File workingDirectory,
            BuildLogger buildLogger,
            String serverUrl,
            String token,
            String owner,
            String app,
            String artifactPath,
            String destination,
            String distributionGroup

    ) {
        this.LOG = new LogUtils(buildLogger);

        this.workingDirectory = workingDirectory;
        this.serverUrl = serverUrl;
        this.token = token;
        this.owner = owner;
        this.app = app;
        this.destinations = destination;
        this.distributionGroup = distributionGroup;
        this.artifactPath = artifactPath;
    }

    /**
     * Performs all necessary setup steps for running requests against the API.
     *
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public void init() throws IllegalArgumentException, IOException {
        LOG.info("Initializing...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(serverUrl), "URL cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(token), "Token cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(owner), "Owner Name cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(app), "App Name cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(destinations), "Destination cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(distributionGroup), "Distribution Group cannot be null or empty!");

        try (DirectoryStream<Path> files = Files.newDirectoryStream(
                Paths.get(workingDirectory.getAbsolutePath()), artifactPath)) {
            for (Path path : files) {
                Preconditions.checkArgument(path != null && path.toFile().exists(), "Artifact(s) not found in path: " + relativeToFullPath(artifactPath));
            }
        }
        LOG.info("Initialized successfully!");

        LOG.info("Creating AppCenter Api Service...");
        appCenterService = new AppCenterService(LOG.getLogger(), serverUrl, token);
        appCenterService.setDebug(LOG.isDebug());
        LOG.info("AppCenter Api Service created!");
    }

    private String relativeToFullPath(String path) {
        if (path != null && !new File(path).isAbsolute()) {
            return new File(workingDirectory, path).getAbsolutePath();
        }
        return path;
    }

    /**
     * Publishes file on AppCenter
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws IllegalArgumentException
     */
    public void publish() throws IllegalArgumentException, TaskException, IOException {

        File artifactFile = null;

        try (DirectoryStream<Path> files = Files.newDirectoryStream(
                Paths.get(workingDirectory.getAbsolutePath()), artifactPath)) {
            for (Path path : files) {
                artifactFile = path.toFile();
                Preconditions.checkArgument(artifactFile != null && artifactFile.exists(), "Artifact file not found in path: " + relativeToFullPath(artifactPath));
                break;
            }
        }

        LOG.info("Connecting to AppCenter...");
        String distributionGroupId = appCenterService.getDistributionGroupId(owner, distributionGroup);
        LOG.info("Connected successfully!");

        if (artifactFile == null) {
            throw new TaskException("Artifact file not found or is NULL!");
        }

        LOG.info("Create an upload resource to AppCenter...");
        AppCenterReleaseUploadsResponse upload = appCenterService.createUpload(owner, app);
        LOG.info("UploadId: \t" + upload.getUploadId());
        LOG.info("UploadUrl: \t" + upload.getUploadUrl());

        LOG.info("Upload artifact " + artifactFile.getName() + " to AppCenter...");
        boolean uploadSuccess = appCenterService.uploadArtifact(upload.getUploadUrl(), artifactFile);
        if (!uploadSuccess) {
            throw new TaskException("Error Publish file " + artifactFile.getName() + " on AppCenter");
        }

        LOG.info("Update upload resource's status to committed...");
        AppCenterReleaseUploadsCommitResponse release = appCenterService.updateUpload(owner, app, upload.getUploadId());
        LOG.info("ReleaseId: \t" + release.getReleaseId());
        LOG.info("ReleaseUrl: \t" + release.getReleaseUrl());


        // 4. Distribute the uploaded release
        LOG.info("Distribute the uploaded release...");
        AppCenterReleasesResponse result = appCenterService.distribute(owner, app, release.getReleaseId(), destinations, distributionGroupId);
        if (result.getId() == null) {
            throw new TaskException("Error Distribute the uploaded release on AppCenter");
        }
        LOG.info("Id: \t" + result.getId());
        LOG.info("MandatoryUpdate: \t" + result.isMandatoryUpdate());
        LOG.info("ProvisioningStatusUrl: \t" + result.getProvisioningStatusUrl());

        LOG.info("=\n\n==================\n\n PUBLISHED SUCCESSFUL \n\n==================\n\n");
    }

}
