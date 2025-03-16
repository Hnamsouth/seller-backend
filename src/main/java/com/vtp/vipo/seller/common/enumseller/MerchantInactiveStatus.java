package com.vtp.vipo.seller.common.enumseller;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Enum representing the various inactive statuses of an entity.
 * Mapped to a `tinyint` column in the database.
 *
 * <ul>
 *   <li>0 - ACTIVE: Entity is active.</li>
 *   <li>1 - STOPPED: Entity has stopped operations.</li>
 *   <li>2 - TEMPORARILY_LOCKED: Entity is temporarily locked.</li>
 *   <li>3 - PERMANENTLY_LOCKED: Entity is permanently locked.</li>
 *   <li>4 - DELETED: Entity is marked as deleted.</li>
 * </ul>
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum MerchantInactiveStatus {

    ACTIVE(0, "Đang hoạt động"),               // Entity is active
    STOPPED(1, "Dừng hoạt động"),             // Entity has stopped operations
    TEMPORARILY_LOCKED(2, "Tạm khóa"),        // Entity is temporarily locked
    PERMANENTLY_LOCKED(3, "Khóa vĩnh viễn"),  // Entity is permanently locked
    DELETED(4, "Đã xóa");                     // Entity is marked as deleted

    Integer value; // Database value corresponding to the status
    String description; // Human-readable description of the status

}
