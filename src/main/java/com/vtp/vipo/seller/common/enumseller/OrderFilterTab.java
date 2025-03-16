package com.vtp.vipo.seller.common.enumseller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OrderFilterTab{

    WAITING_PAYMENT(1, "Chờ thanh toán"),

    WAITING_APPROVAL(2, "Chờ duyệt"),

    WAITING_SHIPMENT(3, "Chờ giao hàng"),

    IN_TRANSIT(4, "Đang giao hàng"),

    DELIVERED(5, "Đã giao hàng"),

    RETURN_REFUND_CANCEL(6, "Trả hàng/Hoàn tiền/Hủy"); // value = 6 for ready

    public final Integer value;

    public final String lable;

    public static String getLableFromValue(Integer value){
        for(OrderFilterTab tab : OrderFilterTab.values()){
            if(tab.getValue().equals(value)){
                return tab.getLable();
            }
        }
        return null;
    }

    public static OrderFilterTab getFromValue(Integer value){
        for(OrderFilterTab tab : OrderFilterTab.values()){
            if(tab.getValue().equals(value)){
                return tab;
            }
        }
        return null;
    }

    public static Integer getValueFromEnum(OrderFilterTab value){
        for(OrderFilterTab tab : OrderFilterTab.values()){
            //todo: orders containt of RETURN_REFUND_CANCEL tab not ready in phase 5, remove this condition when ready
            if(value != null && value.equals(OrderFilterTab.RETURN_REFUND_CANCEL)){
                return 123;
            }
            if(tab.equals(value)){
                return tab.getValue();
            }
        }
        return null;
    }

}
