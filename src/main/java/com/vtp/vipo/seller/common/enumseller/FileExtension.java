package com.vtp.vipo.seller.common.enumseller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FileExtension {
    PDF("pdf"),
    PNG("png"),
    JPEG("jpeg"),
    JPG("jpg");

    final String extension;

    /**
     * Checks if the provided extension is valid (i.e., exists in the enum).
     *
     * @param extension The file extension to validate.
     * @return {@code true} if the extension is valid; {@code false} otherwise.
     */
    public static boolean isValidExtension(String extension) {
        for (FileExtension ext : FileExtension.values()) {
            if (ext.getExtension().equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}
