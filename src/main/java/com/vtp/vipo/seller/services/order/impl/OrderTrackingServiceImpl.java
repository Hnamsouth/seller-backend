package com.vtp.vipo.seller.services.order.impl;

import com.vtp.vipo.seller.common.BaseService;
import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.dao.entity.OrderPackageEntity;
import com.vtp.vipo.seller.common.dao.entity.OrderTrackingEntity;
import com.vtp.vipo.seller.common.dao.repository.OrderTrackingEntityRepository;
import com.vtp.vipo.seller.common.dto.response.order.LogisticsTrackInfoVO;
import com.vtp.vipo.seller.common.dto.response.order.LogisticsTrackingVTP;
import com.vtp.vipo.seller.common.dto.response.order.OrderStatusDTO;
import com.vtp.vipo.seller.common.dto.response.order.OrderStatusLanguageDTO;
import com.vtp.vipo.seller.services.order.BuyerOrderStatusService;
import com.vtp.vipo.seller.services.order.OrderTrackingService;
import com.vtp.vipo.seller.services.order.SellerOrderStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTrackingServiceImpl extends BaseService<OrderTrackingEntity, Long, OrderTrackingEntityRepository> implements OrderTrackingService {

    private final BuyerOrderStatusService buyerOrderStatusService;


    /**
     * Retrieves logistic tracking information for a given order package.
     * This method fetches the tracking entities associated with the order package, maps them to
     * LogisticsTrackInfoVO objects, and sorts them by tracking time in descending order.
     *
     * @param orderPackage the order package entity for which to retrieve tracking information
     * @return a sorted list of LogisticsTrackInfoVO containing tracking details, or an empty list if no tracking is available
     */
    @Override
    public List<LogisticsTrackInfoVO> getLogisticTrackInfo(OrderPackageEntity orderPackage) {
//        String lang = vipoLanguageUtils.getCurrentLanguage();

        List<OrderTrackingEntity> orderTrackingEntities = repo.findAllByPackageIdAndSourceOrOrderByTime(orderPackage.getId());
//        List<OrderTrackingEntity> orderTrackingEntitiesDomestic = repo.findAllByPackageIdAndSourceOrOrderByTime(orderPackage.getId(), Constants.VIETTELPOST_SOURCE);
//        List<OrderTrackingEntity> orderTrackingEntitiesNotDomestic = repo.findAllByPackageIdAndSourceNotDomestic(orderPackage.getId(), Constants.VIETTELPOST_SOURCE);

//        List<LogisticsTrackInfoVO> logisticsTrackInfoVOs = groupOrderTrackingStatus(orderTrackingEntities, lang);
        List<LogisticsTrackInfoVO> logisticsTrackInfoVOs = groupOrderTrackingStatus(orderTrackingEntities, Constants.VIETNAMESE_LANGUAGE);
//        List<LogisticsTrackInfoVO> logisticsTrackInfoVONotDomestic = groupOrderTrackingStatus(orderTrackingEntitiesNotDomestic, lang);

        return logisticsTrackInfoVOs;
    }

    private List<LogisticsTrackInfoVO> groupOrderTrackingStatus(List<OrderTrackingEntity> orderTrackingEntitiesAll, String lang) {
        Map<String, LogisticsTrackInfoVO> logisticsTrackInfoVOMap = new LinkedHashMap<>();
        for (OrderTrackingEntity orderTrackingEntity : orderTrackingEntitiesAll) {
            String key;
            if (ObjectUtils.isEmpty(orderTrackingEntity.getStatusOfPartner())) {
                key = String.format("%s_%s", orderTrackingEntity.getSource(), orderTrackingEntity.getId());
            } else {
                key = String.format("%s_%s", orderTrackingEntity.getSource(), orderTrackingEntity.getStatusOfPartner());
            }
            if (logisticsTrackInfoVOMap.containsKey(key)) {
                continue;
            }
            LogisticsTrackInfoVO logisticsTrackInfoVO = mapToLogisticsTrackInfoVO(orderTrackingEntity, lang);
            if (ObjectUtils.isNotEmpty(logisticsTrackInfoVO)) {
                logisticsTrackInfoVOMap.put(key, logisticsTrackInfoVO);
            }
        }
        return logisticsTrackInfoVOMap.values().stream().toList();
    }

    /**
     * Maps an OrderTrackingEntity to a LogisticsTrackInfoVO, including setting the status and localized status description.
     *
     * @param orderTrackingEntity the entity containing tracking data
     * @param lang                the language code for localization
     * @return the mapped LogisticsTrackInfoVO with status and description set
     */
    private LogisticsTrackInfoVO mapToLogisticsTrackInfoVO(OrderTrackingEntity orderTrackingEntity, String lang) {
        LogisticsTrackInfoVO logisticsTrackInfoVO = new LogisticsTrackInfoVO();
        logisticsTrackInfoVO.setContext(orderTrackingEntity.getContent());
        logisticsTrackInfoVO.setTime(orderTrackingEntity.getTime());
        String orderTrackingOrderStatus = orderTrackingEntity.getOrderStatus();
        if (StringUtils.isBlank(orderTrackingOrderStatus)) {
            log.error("Order status is not set for order tracking entity with id: {}", orderTrackingEntity.getId());
            return logisticsTrackInfoVO;
        }
        logisticsTrackInfoVO.setStatus(orderTrackingOrderStatus);
        if (orderTrackingEntity.getSource().equals(Constants.VIPO_SOURCE)) {
            setLocalizedStatusDescription(logisticsTrackInfoVO, orderTrackingOrderStatus, lang);
        } else {
            LogisticsTrackingVTP logisticsTrackingVTP = Constants.SOURCE_ORDER_STATUS_TRACKING_GROUP.get(String.format("%s_%s", orderTrackingEntity.getSource(), orderTrackingEntity.getStatusOfPartner()));
            if (ObjectUtils.isNotEmpty(logisticsTrackingVTP) && ObjectUtils.isNotEmpty(logisticsTrackingVTP.getNameGroup())) {
                logisticsTrackInfoVO.setStatusDesc(logisticsTrackingVTP.getNameGroup());
            }
            if (ObjectUtils.isNotEmpty(logisticsTrackingVTP) && ObjectUtils.isNotEmpty(logisticsTrackingVTP.getContext())) {
                logisticsTrackInfoVO.setContext(logisticsTrackingVTP.getContext());
            }
        }
        return logisticsTrackInfoVO;
    }

    /**
     * Sets the localized status description in the LogisticsTrackInfoVO based on the order status code.
     *
     * @param logisticsTrackInfoVO the VO object to update
     * @param orderStatusCode      the order status code for which to fetch the description
     * @param lang                 the language code for localization
     */
    private void setLocalizedStatusDescription(LogisticsTrackInfoVO logisticsTrackInfoVO, String orderStatusCode, String lang) {
        // Find the root order status based on the order status code
        OrderStatusDTO rootOrderStatus = buyerOrderStatusService.findRootOrderStatusByOrderStatusCode(orderStatusCode);
        if (ObjectUtils.isEmpty(rootOrderStatus)) {
            return;
        }

        // Retrieve the localized status description using the language map
        String orderStatusLang = Constants.APPLICATION_LANG_TO_ORDER_STATUS_LANG_MAP.get(lang);
        if (StringUtils.isNotBlank(orderStatusLang)) {
            OrderStatusLanguageDTO rootOrderStatusLanguage = rootOrderStatus.getLanguageDTOMap().get(orderStatusLang);
            if (ObjectUtils.isNotEmpty(rootOrderStatusLanguage) && StringUtils.isNotBlank(rootOrderStatusLanguage.getName())) {
                switch (orderStatusCode) {
                    case Constants.ORDER_CREATED_STATUS ->
                            logisticsTrackInfoVO.setStatusDesc(Constants.ORDER_CREATED_STATUS_TITLE);
                    case Constants.PLACING_LAZBAO_ORDER_STATUS ->
                            logisticsTrackInfoVO.setStatusDesc(Constants.PLACING_LAZBAO_ORDER_STATUS_TITLE);
                    case Constants.START_LAZBAO_ORDER_STATUS ->
                            logisticsTrackInfoVO.setStatusDesc(Constants.START_ORDER_STATUS_TALK_TITLE);
                    default -> logisticsTrackInfoVO.setStatusDesc(rootOrderStatusLanguage.getName());
                }
            }
        }

        // Use the default status name if no localized description is available
        if (StringUtils.isBlank(logisticsTrackInfoVO.getStatusDesc())) {
            switch (orderStatusCode) {
                case Constants.ORDER_CREATED_STATUS ->
                        logisticsTrackInfoVO.setStatusDesc(Constants.ORDER_CREATED_STATUS_TITLE);
                case Constants.PLACING_LAZBAO_ORDER_STATUS ->
                        logisticsTrackInfoVO.setStatusDesc(Constants.PLACING_LAZBAO_ORDER_STATUS_TITLE);
                case Constants.START_LAZBAO_ORDER_STATUS ->
                        logisticsTrackInfoVO.setStatusDesc(Constants.START_ORDER_STATUS_TALK_TITLE);
                default -> logisticsTrackInfoVO.setStatusDesc(rootOrderStatus.getName());
            }
        }
    }

}
