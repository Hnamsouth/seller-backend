package com.vtp.vipo.seller.config.versioning;

/**
 * This class holds the API version for the current thread.
 * The API version is stored using a `ThreadLocal` variable, allowing each thread to have its own version value.
 * This is useful in scenarios where different threads (e.g., request handling threads) need to access
 * different versions of the API simultaneously.
 */
public class ApiVersionHolder {

    // ThreadLocal variable to store the API version specific to the current thread
    private static final ThreadLocal<ApiVersion> apiVersion = new ThreadLocal<>();

    /**
     * Sets the API version for the current thread.
     * This method is typically called at the start of request processing to associate an API version with the thread.
     *
     * @param version the API version to set for the current thread (e.g., "1.0.2", "2.3")
     */
    public static void setApiVersion(ApiVersion version) {
        apiVersion.set(version);
    }

    /**
     * Retrieves the API version associated with the current thread.
     * This method is typically called to access the API version in the request handler or service layer.
     *
     * @return the API version for the current thread, or null if not set
     */
    public static ApiVersion getApiVersion() {
        return apiVersion.get();
    }

    /**
     * Clears the API version for the current thread.
     * This is useful for cleaning up after request processing or when the API version is no longer needed.
     */
    public static void clear() {
        apiVersion.remove();
    }
}