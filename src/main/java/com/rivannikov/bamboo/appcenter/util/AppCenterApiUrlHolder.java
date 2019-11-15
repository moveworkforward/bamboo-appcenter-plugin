package com.rivannikov.bamboo.appcenter.util;

import com.atlassian.bamboo.build.logger.BuildLogger;

/**
 * Uploading using the App Center Command Line Interface
 * https://docs.microsoft.com/en-us/appcenter/distribution/uploading#uploading-using-the-apis
 */
public class AppCenterApiUrlHolder {
	private final LogUtils LOG;
	private final String releaseUploads;
	private final String releases;
	private final String distributionGroups;

	public AppCenterApiUrlHolder(BuildLogger buildLogger, String baseUrl) {
		LOG = new LogUtils(buildLogger);
		releaseUploads = URI.RELEASE_UPLOADS.url(baseUrl);
		releases = URI.RELEASES.url(baseUrl);
		distributionGroups = URI.DISTRIBUTION_GROUPS.url(baseUrl);
		LOG.info("Init AppCenter URL = " + baseUrl);
	}

	public String getDistributionGroups() {
		return distributionGroups;
	}

	public String getReleases() {
		return releases;
	}

	public String getReleaseUploads() {
		return releaseUploads;
	}

	public enum URI {
		// Create an upload resource and get an upload_url (good for 24 hours)
		RELEASE_UPLOADS("/apps/{owner_name}/{app_name}/release_uploads"),
		// Distribute the uploaded release to destinations using testers, groups, or stores.
		// This is nessesary to view uploaded distribute in the developer portal.
		RELEASES("/apps/{owner_name}/{app_name}/releases/{release_id}"),
		DISTRIBUTION_GROUPS("/orgs/{owner_name}/distribution_groups");

		private final String value;

		URI(String value) {
			this.value = value;
		}

		public String url(String baseUrl) {
			return baseUrl + value;
		}

		public String getValue() {
			return value;
		}
	}
}
