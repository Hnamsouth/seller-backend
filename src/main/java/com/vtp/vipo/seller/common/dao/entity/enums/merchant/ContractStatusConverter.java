package com.vtp.vipo.seller.common.dao.entity.enums.merchant;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ContractStatusConverter implements AttributeConverter<ContractStatus, Integer> {

    /**
     * Converts the ContractStatus enum to its integer representation for storage.
     */
    @Override
    public Integer convertToDatabaseColumn(ContractStatus status) {
        return (status == null) ? null : status.getValue();
    }

    /**
     * Converts the integer value from the database back to a ContractStatus enum.
     */
    @Override
    public ContractStatus convertToEntityAttribute(Integer dbData) {
        return (dbData == null) ? null : ContractStatus.of(dbData);
    }
}

