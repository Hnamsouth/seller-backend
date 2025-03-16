package com.vtp.vipo.seller.common.exception;

import com.vtp.vipo.seller.common.utils.Translator;

public enum ErrorCodeResponse {
    UNKNOWN("100", "error.haveSomeError"),
    REQUIRED_FIELD("EVLD_100", "error.common.required-field"),
    INVALID_MIN_5_CHAR("EVLD_001", "invalid.min.5.char"),
    INVALID_MAX_100_CHAR("EVLD_002", "invalid.max.100.char"),
    INVALID_ALL_SPEC_CHAR("EVLD_003", "invalid.all.spec.char"),
    INVALID_MIN_10_CHAR_PROD_1("PROD_EVLD_001", "invalid.min.10.char.prod.1"),
    INVALID_MAX_255_CHAR_PROD_1("PROD_EVLD_002", "invalid.max.255.char.prod.1"),
    INVALID_ALL_SPEC_CHAR_PROD_1("PROD_EVLD_003", "invalid.all.spec.char.prod.1"),
    INVALID_MIN_50_CHAR("EVLD_004", "invalid.min.50.char"),
    INVALID_MAX_5000_CHAR("EVLD_005", "invalid.max.5000.char"),
    INVALID_VALUE_SET("EVLD_006","invalid.value.set"),
    INVALID_GREATER_THAN_O("EVLD_007","invalid.greater.than.0"),
    INVALID_MIN_2_CHAR("EVLD_008", "invalid.min.2.char"),
    INVALID_MAX_255_CHAR("EVLD_009", "invalid.max.255.char"),
    INVALID_MAX_150_CHAR("EVLD_010", "invalid.max.150.char"),
    INVALID_RANGE_PERCENTAGE("EVLD_011", "invalid.range.percentage"),
    INVALID_SCALE_PERCENTAGE("EVLD_012", "invalid.scale.percentage"),
    INVALID_POSITIVE("EVLD_013", "invalid.positive"),
    INVALID_POSITIVE_INTEGER("EVLD_014", "invalid.positive.integer"),
    INVALID_PROD_SPEC_INFO_SIZE("PROD_EVLD_004","invalid.prod.spec.info.size"),
    INVALID_PROD_ATTRIBUTE_INFO_SIZE("PROD_EVLD_005","invalid.prod.attribute.info.size"),
    INVALID_INTEGER("EVLD_015", "invalid.integer"),
    INVALID_EXCEED_STOCK("PROD_EVLD_006","invalid.exceed.stock"),
    INVALID_PRICESTEP_MUST_START_FROM_1_AND_INCREASE_SEQUENTIALLY("PROD_EVLD_007","invalid.priceStep.must.start.from.1.and.increase.sequentially"),
    INVALID_FROMQUANTITY_MUST_START_FROM_MIN_PURCHASE_AND_BE_SEQUENTIAL("PROD_EVLD_008", "invalid.fromQuantity.must.start.from.min.purchase.and.be.sequential"),
    INVALID_FROMQUANTITY_MUST_EQUAL_TOQUANTITY_PLUS_1_OF_PREVIOUS_ELEMENT("PROD_EVLD_009", "invalid.fromQuantity.must.equal.toQuantity.plus.1.of.previous.element"),
    INVALID_UNITPRICE_MUST_BE_LESS_THAN_UNITPRICE_OF_PREVIOUS_ELEMENT("PROD_EVLD_010", "invalid.unitPrice.must.be.less.than.unitPrice.of.previous.element"),
    INVALID_FROMQUANTITY_MUST_BE_LESS_THAN_TOQUANTITY_UNLESS_TOQUANTITY_IS_NULL("PROD_EVLD_011", "invalid.fromQuantity.must.be.less.than.toQuantity.unless.toQuantity.is.null"),
    INVALID_ONLY_LAST_STEPPRICEINFO_CAN_HAVE_TOQUANTITY_NULL("PROD_EVLD_012", "invalid.only.last.StepPriceInfo.can.have.toQuantity.null"),
    INVALID_LAST_STEPPRICEINFO_MUST_HAVE_TOQUANTITY_NULL("PROD_EVLD_013", "invalid.last.StepPriceInfo.must.have.toQuantity.null"),
    COMMON_NOT_FOUND_ID("EVLD_015","common.not.found.id"),
    INVALID_URL("EVLD_016","invalid.url"),
    INVALID_MAX_SIZE("EVLD_017","invalid.size"),
    INVALID_MEDIA_INPUT("EVLD_018","invalid.media.input"),
    INVALID_REQUEST_MAX_MEDIA("EVLD_019","invalid.request.max.media"),
    INVALID_REQUIRED_FIELD("EVLD_020","invalid.required.field"),
    INVALID_STT_SEQUENCE("EVLD_021","invalid.stt.sequence"),
    CONNECTION_TIMEOUT("SYS_ERROR_001","connection.timeout"),
    IO_EXCEPTION("SYS_ERROR_002","io.exception"),
    INVALID_EXIST("EBUS_001","invalid.exist"),
    INVALID_PRODUCT_LOCKED("PROD_BUS_000","invalid.locked.product"),
    INVALID_STATUS("EBUS_002","invalid.status"),
    INVALID_FROM_TO_DATE("EVLD_022","invalid.from.to.date"),
    INVALID_REQUIRED_TRIM("EVLD_023","invalid.required.trim"),
    INVALID_TYPE("EBUS_003","invalid.type"),
    ERROR_EMPTY_TEMP_RECORD("EBUS_004","error.empty.temp.record"),
    INVALID_STOCK_MIN_QUANTITY("EBUS_005","invalid.stock.min.quantity"),
    ERROR_FILE_NOT_FOUND("EIO_001","file.not.found"),
    INVALID_NUM_RECORD("EBUS_006","invalid.num.record"),
    INVALID_SUPPORT_LANGUAGE_EXCEL_INPUT("EVLD_024","invalid.support.language.excel.input"),
    INVALID_HEADER("EVLD_025","invalid.header"),
    INVALID_LINK_IMAGE_VIDEO("EVLD_025","invalid.link.image"),
    INVALID_NUM_SKU("EVLD_026","invalid.num.sku"),
    INVALID_SKU("EVLD_027","invalid.sku"),
    INVALID_AUTHEN("EVLD_028","invalid.authen"),
    INVALID_EXIST_PRODUCT_CODE_CUS("EBUS_007","invalid.exist.productCodeCustomer"),
    INVALID_STRUCTURE_EXCEL("EVLD_029","invalid.structure.excel")
    ;
    private final String message;

    private final String errorCode;

    ErrorCodeResponse(String errorCode, String message) {
        this.message = message;
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageI18N() {
        return Translator.toLocale(message);
    }
    public String getMessageI18NParam(String... param) {
        return Translator.toLocale(message,param);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
