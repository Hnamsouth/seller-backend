package com.vtp.vipo.seller.services.order;

import com.vtp.vipo.seller.common.dao.entity.OrderPackageEntity;
import com.vtp.vipo.seller.common.dto.response.order.LogisticsTrackInfoVO;
import java.util.List;

public interface OrderTrackingService {

    /**
     * Retrieves logistic tracking information for a given order package.
     * This method fetches the tracking entities associated with the order package, maps them to
     * LogisticsTrackInfoVO objects, and sorts them by tracking time in descending order.
     *
     * @param orderPackage the order package entity for which to retrieve tracking information
     * @return a sorted list of LogisticsTrackInfoVO containing tracking details, or an empty list if no tracking is available
     */
    List<LogisticsTrackInfoVO> getLogisticTrackInfo(OrderPackageEntity orderPackage);

}
