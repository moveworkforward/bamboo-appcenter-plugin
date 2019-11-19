package ut.com.rivannikov.bamboo.appcenter;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.logger.NullBuildLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rivannikov.bamboo.appcenter.model.AppCenterReleaseUploadsCommitResponse;
import com.rivannikov.bamboo.appcenter.model.AppCenterReleaseUploadsResponse;
import com.rivannikov.bamboo.appcenter.model.AppCenterReleasesResponse;
import com.rivannikov.bamboo.appcenter.util.AppCenterService;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

@Ignore
public class AppCenterPluginComponentUnitTest
{
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final BuildLogger logger = new NullBuildLogger();
    private final String apiUrls = System.getProperty("appCenterServerUrl", "Empty server Url");
    private final String token =  System.getProperty("appCenterToken", "Empty token");
    private final String ownerName =  System.getProperty("appCenterOwnerName", "Empty ownerName");
    private final String appName =  System.getProperty("appCenterAppName", "Empty appName");
    private final AppCenterService appCenterService = new AppCenterService(logger, apiUrls, token);

    private boolean hockeyappUpload(AppCenterReleaseUploadsResponse releaseUploads, String filename) {
        File artifact = new File(filename);
        return appCenterService.uploadArtifact(releaseUploads.getUploadUrl(), artifact);
    }

    @Test
    public void testUploadApp() {
        AppCenterReleaseUploadsResponse uploads = new AppCenterReleaseUploadsResponse();
        uploads.setUploadId("f2e54970-e81e-0137-4eb4-12e1a2d38976");
        uploads.setUploadUrl("https://rink.hockeyapp.net/api/sonoma/apps/38f197a1-4f24-41d5-aec7-2ab5a49db81b/app_versions/upload?upload_id=f2e54970-e81e-0137-4eb4-12e1a2d38976%7C807c18da-3f58-46f5-9f38-de61eccac1a1");
        hockeyappUpload(uploads, "test.apk");
    }


    // https://api.appcenter.ms/v0.1/apps/kb-4b1m/Sportmaster-2.0-CI_TEST/release_uploads/5f14b060-e8e5-0137-4f41-12e1a2d38976
    @Test
    public void testUpdateApp() {
        appCenterService.setDebug(true);

        String distributionGroupId = appCenterService.getDistributionGroupId(ownerName,"SPORTMASTER_CI_TEST");

        // 1. Create an upload resource
        AppCenterReleaseUploadsResponse uploads = appCenterService.createUpload(ownerName, appName);
        System.out.println(uploads.getUploadId());
        System.out.println(uploads.getUploadUrl());
        // 2. Upload Artifact file
        boolean uploadSuccess = hockeyappUpload(uploads, "test.apk");
        AppCenterReleasesResponse result = null;
        if (uploadSuccess) {
            // 3. Update upload resource's status to committed
            AppCenterReleaseUploadsCommitResponse release = appCenterService.updateUpload(ownerName, appName, uploads.getUploadId());
            System.out.println(gson.toJson(release));

            // 4. Distribute the uploaded release
            result = appCenterService.distribute(ownerName, appName, release.getReleaseId(), "groups", distributionGroupId);
        }

        Assert.assertNotNull(result.getId());
    }

}