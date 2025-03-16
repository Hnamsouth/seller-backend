package com.vtp.vipo.seller.common.constants;

public final class BaseExceptionConstant {

    private BaseExceptionConstant() {
        throw new IllegalStateException(UTITLITY_CLASS_ERROR);
    }

    public static final String UTITLITY_CLASS_ERROR = "Utility class!!!";

    public static final String VIPO_STATUS_HEADER = "vipo-status";

    public static final String UNKNOWN_ERROR = "00";
    public static final String UNKNOWN_ERROR_DESCRIPTION = "Unknown Error";

    public static final String SUCCESS = "01";
    public static final String SUCCESS_DESCRIPTION = "Successful!";

    public static final String INVALID_DATA_REQUEST = "02";
    public static final String INVALID_DATA_REQUEST_DESCRIPTION = "Invalid Input's request!";

    public static final String NOT_FOUND_ENTITY = "03";
    public static final String NOT_FOUND_ENTITY_DESCRIPTION = "Not found entity!";

    public static final String DUPLICATED_ENTITY = "04";
    public static final String DUPLICATED_ENTITY_DESCRIPTION = "Duplicated Entity!";

    public static final String NOT_MATCH_DATA = "05";
    public static final String NOT_MATCH_DATA_DESCRIPTION = "Not match data!";

    public static final String EXPIRED_VIPO_TOKEN = "06";
    public static final String EXPIRED_VIPO_TOKEN_DESCRIPTION = "Expired Token!";

    public static final String INVALID_SESSION = "07";
    public static final String INVALID_SESSION_DESCRIPTION = "Invalid Session!";

    public static final String FAILED_TO_EXECUTE = "08";
    public static final String FAILED_TO_EXECUTE_DESCRIPTION = "Failed to execute!";

    public static final String CONNECTION_TIMEOUT = "09";
    public static final String CONNECTION_TIMEOUT_DESCRIPTION = "Connection timeout!";

    public static final String TOKEN_EXPIRED = "10";
    public static final String TOKEN_EXPIRED_DESCRIPTION = "Connection timeout!";

    public static final String ENTITY_NAME_EMPTY_OR_NULL = "11";
    public static final String ENTITY_NAME_EMPTY_OR_NULL_DESCRIPTION = "Entity Name is Empty!";

    public static final String FAILED_TO_CALL = "12";
    public static final String FAILED_TO_CALL_DESCRIPTION = "Failed to call!";

    public static final String BUSINESS_ERROR = "13";
    public static final String BUSINESS_ERROR_DESCRIPTION = "Business error!";

    public static final String MISSING_REFRESH_TOKEN = "13";
    public static final String MISSING_REFRESH_TOKEN_DESCRIPTION = "Missing refresh token!";

    public static final String VTP_UNAUTHORIZED = "14";
    public static final String VTP_UNAUTHORIZED_DESCRIPTION = "VTP Unauthorized!";

    public static final String VIPO_INVALID_TOKEN = "15";
    public static final String VIPO_INVALID_TOKEN_DESCRIPTION = "Invalid Token!";

    public static final String EXPIRED_REFRESH_TOKEN = "16";
    public static final String EXPIRED_REFRESH_TOKEN_DESCRIPTION = "Expired refresh token!";

    public static final String UNMATCHED_VTP_USER_ID = "17";
    public static final String UNMATCHED_VTP_USER_ID_DESCRIPTION = "Unmatched VTP User ID!";

    public static final String VIPO_UNAUTHORIZED = "18";
    public static final String VIPO_UNAUTHORIZED_DESCRIPTION = "Unauthorized actions!";

    public static final String FILE_EMPTY_DESCRIPTION = "File is empty!";

    public static final String FILE_WRONG_FORMAT = "File is wrong format";

    public static final String FILE_EXCEED_CAPACITY_ALLOWED = "File exceeds the allowed capacity";

    public static final String UPLOAD_FILE_FAIL = "Upload file fail";

    public static final String DELETE_FILE_FAIL = "Delete file fail";

    public static final String PENDING_MERCHANT = "Merchant information has not been approved";

    public static final String SAVE_DATABASE_FAIL = "Save to database failed";

    public static final String BAD_CREDENTIAL = "19";

    public static final String BAD_CREDENTIAL_DESCRIPTION = "Incorrect login credentials";

    public static final String KEY_SEARCH_NOTFOUND = "Not found key search {}";

    public static final String REFUSE_UPDATE_MERCHANT = "20";

    public static final String REFUSE_UPDATE_MERCHANT_DESCRIPTION  = "Refused to update";

    public static final String INVALID_PAGING_REQUEST_DESCRIPTION = "Invalid paging requests!";

    public static final String FAIL_CREATE_WITHDRAW = "21";

    public static final String FAIL_CREATE_WITHDRAW_DESCRIPTION = "21";

    public static final String FAIL_CREATE_WITHDRAW_TOAST = "22";

}

