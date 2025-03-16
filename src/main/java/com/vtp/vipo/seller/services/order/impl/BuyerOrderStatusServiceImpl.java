package com.vtp.vipo.seller.services.order.impl;

import com.vtp.vipo.seller.common.dao.entity.OrderPackageEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.BuyerOrderStatus;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderPackageEvent;
import com.vtp.vipo.seller.common.dao.entity.projection.OrderStatusProjection;
import com.vtp.vipo.seller.common.dao.repository.OrderStatusRepository;
import com.vtp.vipo.seller.common.dto.response.order.OrderStatusDTO;
import com.vtp.vipo.seller.common.dto.response.order.OrderStatusLanguageDTO;
import com.vtp.vipo.seller.common.exception.VipoFailedToExecuteException;
import com.vtp.vipo.seller.services.order.BuyerOrderStatusService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Service;
import com.vtp.vipo.seller.common.constants.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link BuyerOrderStatusService} that handles updating the buyer order status
 * based on incoming events using a state machine.
 */
@RequiredArgsConstructor
@Service
public class BuyerOrderStatusServiceImpl implements BuyerOrderStatusService {

    /**
     * Factory to create instances of state machines for buyer order statuses.
     */
    private final StateMachineFactory<BuyerOrderStatus, SellerOrderPackageEvent> buyerOrderStatusStateMachineFactory;

    /**
     * Persister to save and restore the state of the state machine associated with an order package.
     */
    private final StateMachinePersister<BuyerOrderStatus, SellerOrderPackageEvent, OrderPackageEntity> buyerOrderStatusPersister;

    private final ApplicationContext applicationContext;
    private final OrderStatusRepository orderStatusRepository;

    private BuyerOrderStatusServiceImpl getProxy() {
        return applicationContext.getBean(BuyerOrderStatusServiceImpl.class);
    }

    /**
     * Updates the buyer order status of the given {@link OrderPackageEntity} based on the provided event.
     * This method uses a state machine to handle the state transition and persists the updated state.
     *
     * @param orderPackageEntity the order package entity whose status is to be updated
     * @param event              the event triggering the status update
     * @throws VipoFailedToExecuteException if there is an error restoring or persisting the state machine,
     *                                      or if the event is not accepted by the state machine
     */
    @Override
    public void updateBuyerOrderStatusByEvent(OrderPackageEntity orderPackageEntity, SellerOrderPackageEvent event) {
        // Obtain a state machine instance uniquely identified by the order's ID
        StateMachine<BuyerOrderStatus, SellerOrderPackageEvent> stateMachine =
                buyerOrderStatusStateMachineFactory.getStateMachine(String.valueOf(orderPackageEntity.getId()));

        try {
            // Restore the state machine's state from the order entity
            buyerOrderStatusPersister.restore(stateMachine, orderPackageEntity);
        } catch (Exception ex) {
            // Wrap and rethrow any exceptions during state restoration
            throw new VipoFailedToExecuteException("Failed to restore state machine: " + ex.getMessage());
        }

        // Start the state machine to begin processing events
        stateMachine.start();

        // Create a message with the event and set the header
        Message<SellerOrderPackageEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader("EVENT_KEY", event)
                .build();

        // Send the event to the state machine and check if it was accepted
        boolean accepted = stateMachine.sendEvent(message);

        if (!accepted) {
            // If the event was not accepted, throw an exception
            throw new VipoFailedToExecuteException("Event not accepted: " + event);
        }

        try {
            // Persist the updated state of the state machine back to the order entity
            buyerOrderStatusPersister.persist(stateMachine, orderPackageEntity);
        } catch (Exception ex) {
            // Wrap and rethrow any exceptions during state persistence
            throw new VipoFailedToExecuteException("Failed to persist state machine: " + ex.getMessage());
        }
    }

    /**
     * Finds the root order status entity based on a given order status code.
     * If the provided status code has a corresponding root code, retrieves the entity using the root code.
     *
     * @param status the status code of the order
     * @return the root OrderStatusEntity, or null if no root exists or the status is invalid
     */
    @Override
    public OrderStatusDTO findRootOrderStatusByOrderStatusCode(String status) {
        // Find the order status entity based on the provided status code
        OrderStatusDTO orderStatusEntity = getOrderStatusDTOByCode(status);

        // Return null if the entity is not found or if the root code is missing
        if (ObjectUtils.isEmpty(orderStatusEntity) || StringUtils.isBlank(orderStatusEntity.getRootCode())) {
            return null;
        }

        // Find and return the root order status entity using the root code
        return getOrderStatusDTOByCode(orderStatusEntity.getRootCode());
    }

    /**
     * Retrieves an {@link OrderStatusDTO} based on the given status code.
     * This method fetches a pre-cached map of order status codes to their respective DTOs
     * and returns the DTO that matches the provided status code.
     *
     * @param statusCode the code representing the specific order status to be retrieved.
     *                   Must not be null or empty.
     * @return an {@link OrderStatusDTO} that corresponds to the provided status code, or null if not found.
     * @throws IllegalArgumentException if the statusCode is blank.
     */
    @Override
    public OrderStatusDTO getOrderStatusDTOByCode(String statusCode) {
        // Retrieve the map of status codes to OrderStatusDTO using a cached method.
        Map<String, OrderStatusDTO> statusDTOMap = getProxy().getStatusCodeToOrderStatusDTOMap();
        // Return the OrderStatusDTO associated with the provided status code.
        return statusDTOMap.get(statusCode);
    }

    /**
     * Retrieves a map of status codes to {@link OrderStatusDTO} instances.
     * This method is cached to reduce redundant database queries, improving performance.
     * It converts {@link OrderStatusProjection} entities into {@link OrderStatusDTO} objects.
     *
     * <p>
     * If a language-specific translation does not exist for Vietnamese (vi), the default
     * order status values are used for the Vietnamese language.
     * </p>
     *
     * @return a map of status codes to {@link OrderStatusDTO} instances.
     */
    @Cacheable(cacheManager = "memoryCacheManager", cacheNames = "orderStatusCache")
    public Map<String, OrderStatusDTO> getStatusCodeToOrderStatusDTOMap() {
        // Retrieve all order status projections from the repository.
        List<OrderStatusProjection> projections = orderStatusRepository.getAllOrderStatus();

        // If there are no projections, return an empty map.
        if (ObjectUtils.isEmpty(projections)) {
            return new HashMap<>();
        }

        // Convert the list of projections to a map with statusCode as the key and OrderStatusDTO as the value.
        Map<String, OrderStatusDTO> statusDTOMap = projections.stream()
                .collect(
                        Collectors.toMap(
                                // Use the statusCode as the key for the map.
                                OrderStatusProjection::getStatusCode,
                                // Map each projection to an OrderStatusDTO.
                                orderStatusProjection -> OrderStatusDTO.builder()
                                        .id(orderStatusProjection.getId())
                                        .name(orderStatusProjection.getName())
                                        .description(orderStatusProjection.getDescription())
                                        .rootCode(orderStatusProjection.getRootCode())
                                        .statusCode(orderStatusProjection.getStatusCode())
                                        // Build the language-specific DTO map for translations.
                                        .languageDTOMap(
                                                Map.of(
                                                        orderStatusProjection.getLanguage(),
                                                        OrderStatusLanguageDTO.builder()
                                                                .orderStatusId(orderStatusProjection.getId())
                                                                .language(orderStatusProjection.getLanguage())
                                                                .description(orderStatusProjection.getDescriptionInLanguage())
                                                                .name(orderStatusProjection.getNameInLanguage())
                                                                .build()
                                                )
                                        )
                                        .build(),
                                // Handle cases where multiple entries with the same statusCode exist.
                                (existing, replacement) -> {
                                    // Merge languageDTOMap of the existing and replacement DTOs.
                                    Map<String, OrderStatusLanguageDTO> languageMap = new HashMap<>();
                                    languageMap.putAll(existing.getLanguageDTOMap());
                                    languageMap.putAll(replacement.getLanguageDTOMap());
                                    existing.setLanguageDTOMap(languageMap);
                                    return existing;
                                }
                        )
                );

        // Ensure that each OrderStatusDTO has a Vietnamese translation because of order_status table is using Vietnamese,
        //  the order_status_language may not have any records for language = 'vi"
        statusDTOMap.values().stream()
                .filter(orderStatusDTO ->
                        // Check if there is no "vi" translation for the order status.
                        ObjectUtils.isEmpty(
                                orderStatusDTO.getLanguageDTOMap().get(Constants.ORDER_STATUS_VIETNAMESE_LANGUAGE)
                        )
                ).forEach(orderStatusDTO ->
                        // Add the default name and description to the Vietnamese translation if missing.
                        orderStatusDTO.getLanguageDTOMap().put(
                                Constants.ORDER_STATUS_VIETNAMESE_LANGUAGE,
                                OrderStatusLanguageDTO.builder()
                                        .orderStatusId(orderStatusDTO.getId())
                                        .language(Constants.ORDER_STATUS_VIETNAMESE_LANGUAGE)
                                        .description(orderStatusDTO.getDescription())
                                        .name(orderStatusDTO.getName())
                                        .build()
                        )
                );

        // Return the fully populated map of status codes to OrderStatusDTOs.
        return statusDTOMap;
    }
}