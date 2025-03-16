package com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest;

import com.vtp.vipo.seller.common.dao.entity.projection.WithdrawalRequestProjection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum WithdrawRequestAction {

    RECREATE, CANCEL, VIEW_DETAIL;

    public static List<WithdrawRequestAction> getActionsByStatus(WithdrawRequestStatusEnum status) {
        LinkedList<WithdrawRequestAction> res = new LinkedList<>();
        switch (status) {
            case PENDING:
                res.add(CANCEL);  break;
            case REJECTED:
            case CANCELED:
                res.add(RECREATE); break;
        }
        res.add(VIEW_DETAIL);
        return res;
    }

    public static List<WithdrawRequestAction> getActionsByStatus(WithdrawalRequestProjection data) {
        LinkedList<WithdrawRequestAction> res = new LinkedList<>();
        switch (data.getWithdrawalRequestStatus()) {
            case PENDING:
                res.add(CANCEL);  break;
            case REJECTED:
            case CANCELED:
                if(data.getReCreated().equals(0))
                    res.add(RECREATE);
                break;
        }
        res.add(VIEW_DETAIL);
        return res;
    }
}
