package com.vtp.vipo.seller.common.enumseller;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Enum representing whether multiple stores are allowed for a user/entity.
 * Mapped to a `tinyint` column in the database.
 *
 * <ul>
 *   <li>0 - NOT_ALLOWED: Multiple stores are not allowed.</li>
 *   <li>1 - ALLOWED: Multiple stores are allowed.</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum AllowMultipleStores {

    NOT_ALLOWED(0, "Không cho phép mở nhiều shop"), // Multiple stores are not allowed

    ALLOWED(1, "Cho phép mở nhiều shop");          // Multiple stores are allowed

    int value; // Database value representing the status

    String description; // Human-readable description of the status

    /**
     * Converts a database value to the corresponding AllowMultipleStores enum.
     *
     * @param value the database value
     * @return the corresponding AllowMultipleStores enum
     * @throws IllegalArgumentException if the value does not match any status
     */
    public static AllowMultipleStores fromValue(int value) {
        for (AllowMultipleStores status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}

