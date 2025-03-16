package com.vtp.vipo.seller.common.enumseller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the various statuses of a merchant in the system.
 *
 * <p>This enum defines the lifecycle states of a merchant and includes both a numeric value
 * for database storage and a description for user-friendly representation.</p>
 *
 * <p>Statuses:</p>
 * <ul>
 *   <li>{@link #PENDING_APPROVAL}: The merchant is waiting for approval.</li>
 *   <li>{@link #APPROVED}: The merchant has been approved.</li>
 *   <li>{@link #REJECTED}: The merchant's application was rejected.</li>
 *   <li>{@link #PENDING_EDIT_APPROVAL}: The merchant is awaiting approval after editing their information.</li>
 * </ul>
 *
 * <p>Example Usage:</p>
 * <pre>
 * MerchantStatusEnum status = MerchantStatusEnum.APPROVED;
 * int statusValue = status.getValue(); // 1
 * String description = status.getDescription(); // "Đã duyệt"
 * </pre>
 *
 * @see com.vtp.vipo.seller.common.dto.Merchant
 * @version 1.0
 */
@Getter
@RequiredArgsConstructor
public enum MerchantStatusEnum {

    /**
     * The merchant is waiting for approval.
     */
    PENDING_APPROVAL(0, "Chờ duyệt"),

    /**
     * The merchant has been approved.
     */
    APPROVED(1, "Đã duyệt"),

    /**
     * The merchant's application was rejected.
     */
    REJECTED(2, "Từ chối"),

    /**
     * The merchant is awaiting approval after editing their information.
     */
    PENDING_EDIT_APPROVAL(3, "Chờ duyệt sau khi sửa thông tin");

    /**
     * Numeric value representing the status in the database.
     */
    private final int value;

    /**
     * Human-readable description of the status.
     */
    private final String description;

}