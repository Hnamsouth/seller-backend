package com.vtp.vipo.seller.common.dao.entity.enums.merchant;

import jakarta.persistence.AttributeConverter;

public class MerchantContractTypeConverter implements AttributeConverter<MerchantContractType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(MerchantContractType status) {
        return (status == null) ? null : status.getValue();
    }

    @Override
    public MerchantContractType convertToEntityAttribute(Integer dbData) {
        return (dbData == null) ? null : MerchantContractType.of(dbData);
    }

}
