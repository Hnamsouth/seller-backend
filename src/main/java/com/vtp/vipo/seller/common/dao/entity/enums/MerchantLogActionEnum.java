package com.vtp.vipo.seller.common.dao.entity.enums;

import com.vtp.vipo.seller.common.dao.entity.enums.merchant.MerchantContractType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum MerchantLogActionEnum {

    //todo: VIPO-3903: Upload e-contract: consider to remove these actions because the merchant info change rules changes
    MERCHANT_CREATED("Người bán lần đầu đăng nhập hệ thông vipo seller"),
    MERCHANT_REQUEST_TO_CHANGE_INFO("Người bán yêu cầu thay đổi thông tin cá nhân"),

    /* VIPO-3903: Upload e-contract: new action */
    CONTRACT_ATTRIBUTE_CHANGED("đã thay đổi [thuộc tính] thành [giá trị mới]"),
    CONTRACT_TEMPLATE_ADDED("đã thêm mới biểu mẫu hợp đồng cho nhà bán"),
    CONTRACT_TEMPLATE_FILE_CHANGED("đã thay đổi file biểu mẫu hợp đồng"),

    MERCHANT_CHANGE_INFO("Người bán thay đổi thông tin cá nhân"),
    ;

    String msg;

    static final List<String> MERCHANT_CONTRACT_ACTIONS = new ArrayList<>();

    // Static block to populate the map with the status codes and corresponding BuyerOrderStatus
    static {
        MERCHANT_CONTRACT_ACTIONS.add(CONTRACT_ATTRIBUTE_CHANGED.name());
        MERCHANT_CONTRACT_ACTIONS.add(CONTRACT_TEMPLATE_ADDED.name());
        MERCHANT_CONTRACT_ACTIONS.add(CONTRACT_TEMPLATE_FILE_CHANGED.name());
    }

    public static List<String> getMerchantContractActions() {
        return MERCHANT_CONTRACT_ACTIONS;
    }

    public static MerchantLogActionEnum of(String value) {
        if (StringUtils.isBlank(value))
            return null;
        for (MerchantLogActionEnum merchantLogActionEnum : MerchantLogActionEnum.values()){
            if (merchantLogActionEnum.name().equals(value))
                return merchantLogActionEnum;
        }
        return null;

    }

}
