package com.vtp.vipo.seller.config.versioning;

import org.apache.commons.lang3.ObjectUtils;

/**
 * Utility class for working with API versions.
 * <p>
 * This class provides utility methods to compare API versions and check if the current API version
 * is at least a specified version.
 */
public final class ApiVersionUtils {

    private ApiVersionUtils() {}

    /**
     * Return the current version. if null, return the legacy version
     */
    public static ApiVersion getCurrentVersion() {
        ApiVersion currentVersion = ApiVersionHolder.getApiVersion();
        return ObjectUtils.isNotEmpty(currentVersion) ? currentVersion : ApiVersion.LEGACY;
    }

    /**
     * Checks if the current API version is at least the specified minimum version.
     * <p>
     * The version comparison is done lexicographically, with versions in the format "major.minor.patch".
     * If the current version is greater than or equal to the minimum version, this method returns true.
     *
     * @param minimumVersion the minimum version to compare against (e.g., "1.2.3")
     * @return true if the current API version is at least the minimum version, false otherwise
     */
    public static boolean isVersionAtLeast(ApiVersion minimumVersion) {
        if (minimumVersion.equals(ApiVersion.LEGACY))
            return true;

        // Get the current API version from the ApiVersionHolder (assumes it's already set by the interceptor)
        ApiVersion currentVersion = ApiVersionHolder.getApiVersion();

        // If the current version is null, it means no version was set; return false
        return currentVersion.isNewerThan(minimumVersion);
    }

}