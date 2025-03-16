package com.vtp.vipo.seller.common.dao.entity.projection;

public interface OrderStatusProjection {

    Long getId();

    String getRootCode();

    String getRootName();

    String getRootDescription();

    String getName();

    String getDescription();

    String getStatusCode();

    String getLanguage();               //order_status_language.language

    String getNameInLanguage();         //order_status_language.name

    String getDescriptionInLanguage();  //order_status_language.description
    
}
