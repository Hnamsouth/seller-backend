package com.vtp.vipo.seller.config.versioning;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum ApiVersion {

    LEGACY("0.0.0"),

    V1_3_0("1.3.0"); //VIPO-3903: Upload E-Contract: merchant -> merchant_new

    String versionCode;

    //Map the version code to the version
    static final Map<String, ApiVersion> codeToVersion;

    // Precompute sorted versions when the application starts
    static final List<ApiVersion> sortedVersions;

    static {
        sortedVersions = new ArrayList<>();
        sortedVersions.addAll(Arrays.asList(ApiVersion.values()));
        // Sort versions using the versionCode (assumes versionCode is properly formatted)
        sortedVersions.sort(ApiVersion::compareToVersion);

        codeToVersion = Arrays.stream(ApiVersion.values()).collect(Collectors.toMap(
                ApiVersion::getVersionCode,
                Function.identity(),
                (replacement, existing) -> replacement
        ));
    }

    public static ApiVersion getVersionFromCode(String code) {
        if (StringUtils.isBlank(code))
            return LEGACY;
        return codeToVersion.get(code);
    }

    public static ApiVersion getNewestVersion() {
        return sortedVersions.get(sortedVersions.size()-1);
    }

    /**
     * Compares two version strings.
     * <p>
     * The comparison is done by splitting the version strings by the dot (".") separator,
     * and comparing each part of the version (major, minor, patch). If the versions are equal,
     * the method returns 0. If the current version is greater, it returns a positive number; otherwise, it returns a negative number.
     *
     * @return a negative number if version is less than otherVersion, a positive number if version is greater,
     * or 0 if they are equal
     */
    public final int compareToVersion(ApiVersion other) {
        if (ObjectUtils.isEmpty(other))
            return 1;
        if (StringUtils.isBlank(this.versionCode)) {
            if (StringUtils.isBlank(other.getVersionCode()))
                return 0;
            else
                return 1;
        }
        if (StringUtils.isBlank(other.getVersionCode()))
            return 1;

        // Split both versions into parts based on the dot separator
        String[] versionParts = this.versionCode.split("\\.");
        String[] otherParts = other.getVersionCode().split("\\.");

        // Compare each part of the version lexicographically
        for (int i = 0; i < Math.max(versionParts.length, otherParts.length); i++) {
            // Parse the parts as integers, using 0 if a part is missing
            int thisPart = i < versionParts.length ? Integer.parseInt(versionParts[i]) : 0;
            int otherPart = i < otherParts.length ? Integer.parseInt(otherParts[i]) : 0;

            // If the parts are not equal, return the comparison result
            if (thisPart != otherPart) {
                return Integer.compare(thisPart, otherPart);
            }
        }
        // If all parts are equal, return 0
        return 0;
    }

    /**
     * Checks if the current version is newer than the provided version.
     *
     * @param version the version to compare against
     * @return true if the current version is newer, false otherwise
     */
    public boolean isNewerThan(ApiVersion version) {
        return sortedVersions.indexOf(this) >= sortedVersions.indexOf(version);
    }

    /**
     * Checks if the current version is older than the provided version.
     *
     * @param version the version to compare against
     * @return true if the current version is older, false otherwise
     */
    public boolean isOlderThan(ApiVersion version) {
        return sortedVersions.indexOf(this) < sortedVersions.indexOf(version);
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
    public boolean isVersionAtLeast(ApiVersion minimumVersion) {
        if (minimumVersion.equals(ApiVersion.LEGACY))
            return true;

        // If the current version is null, it means no version was set; return false
        return this.isNewerThan(minimumVersion);
    }

}
