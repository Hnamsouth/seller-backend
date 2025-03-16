package com.vtp.vipo.seller.common.enumseller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

@RequiredArgsConstructor
@Getter
public enum EvtpErrorMessage {
    NO_HEADER(
            "Header Token is required",
            "Có lỗi xảy ra, vui lòng liên hệ CSKH để nhận hỗ trợ cần thiết"
    ),
    ERROR_TOKEN(
            "Token invalid",
            "Có lỗi xảy ra, vui lòng liên hệ CSKH để nhận hỗ trợ cần thiết"
    ),
    INVALID_SENDER_ADDRESS(
            "Invalid [SENDER_ADDRESS]",
            "Có lỗi xảy ra, vui lòng liên hệ CSKH để nhận hỗ trợ cần thiết"
    ),
    INVALID_SENDER_PROVINCE(
            "Invalid [SENDER_PROVINCE]",
            "Có lỗi xảy ra, vui lòng liên hệ CSKH để nhận hỗ trợ cần thiết"
    ),
    INVALID_SENDER_DISTRICT(
            "Invalid [SENDER_DISTRICT]",
            "Có lỗi xảy ra, vui lòng liên hệ CSKH để nhận hỗ trợ cần thiết"
    ),
    INVALID_SENDER_WARD(
            "Invalid [SENDER_WARD]",
            "Có lỗi xảy ra, vui lòng liên hệ CSKH để nhận hỗ trợ cần thiết"
    ),
    INVALID_RECEIVER_ADDRESS(
            "Invalid [RECEIVER_ADDRESS]",
            "Có lỗi xảy ra, vui lòng liên hệ CSKH để nhận hỗ trợ cần thiết"
    ),
    INVALID_RECEIVER_PROVINCE(
            "Invalid [RECEIVER_PROVINCE]",
            "Có lỗi xảy ra, vui lòng liên hệ CSKH để nhận hỗ trợ cần thiết"
    ),
    INVALID_RECEIVER_DISTRICT(
            "Invalid [RECEIVER_DISTRICT]",
            "Có lỗi xảy ra, vui lòng liên hệ CSKH để nhận hỗ trợ cần thiết"
    ),
    INVALID_RECEIVER_WARD(
            "Invalid [RECEIVER_WARD]",
            "Có lỗi xảy ra, vui lòng liên hệ CSKH để nhận hỗ trợ cần thiết"
    ),
    ERROR_ORDER_PAYMENT(
            "Incorrect data: ORDER_PAYMENT",
            "Có lỗi xảy ra, vui lòng liên hệ CSKH để nhận hỗ trợ cần thiết"
    ),
    ERROR_PRODUCT_TYPE(
            "Incorrect data: PRODUCT_TYPE",
            "Có lỗi xảy ra, vui lòng liên hệ CSKH để nhận hỗ trợ cần thiết"
    ),
    ERROR_ORDER_SERVICE(
            "Incorrect data: ORDER_SERVICE",
            "Có lỗi xảy ra, vui lòng liên hệ CSKH để nhận hỗ trợ cần thiết"
    ),
    NOT_PRICE(
            "Price does not apply to this itinerary!",
            "Bảng giá không áp dụng hành trình này, vui lòng liên hệ CSKH để nhận hỗ trợ cần thiết"
    ),
    SYSTEM_ERROR(
            "Error: system error",
            "Lỗi kết nối, vui lòng thử lại sau"
    );

    private final String error;
    private final String messageError;

    public static String getMessage(String error) {
        final String DEFAULT_MESSAGE = "Có lỗi xảy ra, vui lòng liên hệ CSKH để nhận hỗ trợ cần thiết";
        if (ObjectUtils.isEmpty(error)) {
            return DEFAULT_MESSAGE;
        }

        for (EvtpErrorMessage errMsg : EvtpErrorMessage.values()) {
            if (errMsg.getError().equalsIgnoreCase(error)) {
                return errMsg.getMessageError();
            }
        }

        return DEFAULT_MESSAGE;
    }
}
