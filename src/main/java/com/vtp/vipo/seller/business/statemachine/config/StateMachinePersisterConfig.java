package com.vtp.vipo.seller.business.statemachine.config;

import com.vtp.vipo.seller.business.statemachine.config.orderstatus.buyer.BuyerOrderStatusOrderPackageStateMachinePersist;
import com.vtp.vipo.seller.business.statemachine.config.orderstatus.seller.SellerOrderStatusOrderPackageStateMachinePersist;
import com.vtp.vipo.seller.common.dao.entity.OrderPackageEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.BuyerOrderStatus;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderPackageEvent;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

/**
 * Configuration class for setting up the {@link StateMachinePersister} bean.
 * <p>
 * This class defines a Spring bean responsible for persisting the state of the state machine
 * associated with {@link OrderPackageEntity}. The persister uses {@link SellerOrderStatusOrderPackageStateMachinePersist}
 * to handle the persistence logic, ensuring that the state machine's current state is accurately
 * saved and restored from the {@code sellerOrderStatus} column in the {@code order_package} table.
 * </p>
 *
 * <p>
 * By configuring the {@link StateMachinePersister}, the application ensures that state transitions
 * are consistently maintained across different layers and transactions, facilitating reliable
 * state management for seller orders.
 * </p>
 *
 * @see StateMachinePersister
 * @see DefaultStateMachinePersister
 * @see SellerOrderStatusOrderPackageStateMachinePersist
 * @see OrderPackageEntity
 */
@RequiredArgsConstructor
@Configuration
public class StateMachinePersisterConfig {

    /**
     * Instance of {@link SellerOrderStatusOrderPackageStateMachinePersist} responsible for handling the actual
     * persistence logic of the state machine's state.
     * <p>
     * This component interacts with the {@link OrderPackageEntity} to update and retrieve
     * the {@code sellerOrderStatus}, ensuring that the state machine's current state is
     * accurately reflected in the database.
     * </p>
     */
    private final SellerOrderStatusOrderPackageStateMachinePersist sellerOrderStatusOrderPackageStateMachinePersist;

    private final BuyerOrderStatusOrderPackageStateMachinePersist buyerOrderStatusOrderPackageStateMachinePersist;

    /**
     * Defines the {@link StateMachinePersister} bean for persisting and restoring the state
     * of the state machine associated with {@link OrderPackageEntity}.
     * <p>
     * The persister is configured using {@link DefaultStateMachinePersister} and is supplied
     * with an instance of {@link SellerOrderStatusOrderPackageStateMachinePersist} to handle the underlying persistence
     * operations.
     * </p>
     *
     * <p>
     * This bean can be injected wherever state machine persistence is required, such as in
     * services or repositories that manage seller orders.
     * </p>
     *
     * @return a configured {@link StateMachinePersister} for {@link SellerOrderStatus} and {@link SellerOrderPackageEvent}
     * associated with {@link OrderPackageEntity}
     */
    @Bean
    public StateMachinePersister<SellerOrderStatus, SellerOrderPackageEvent, OrderPackageEntity> sellerOrderStatusPersister() {
        return new DefaultStateMachinePersister<>(sellerOrderStatusOrderPackageStateMachinePersist);
    }

    /**
     * Defines the {@link StateMachinePersister} bean for persisting and restoring the state
     * of the state machine associated with {@link OrderPackageEntity} for buyer order statuses.
     *
     * <p>The persister is configured using {@link DefaultStateMachinePersister} and is supplied
     * with an instance of {@link BuyerOrderStatusOrderPackageStateMachinePersist} to handle the underlying persistence
     * operations.</p>
     *
     * <p>This bean can be injected wherever state machine persistence is required for buyer orders,
     * such as in services or repositories that manage buyer order state transitions.</p>
     *
     * @return a configured {@link StateMachinePersister} for {@link BuyerOrderStatus} and {@link SellerOrderPackageEvent}
     * associated with {@link OrderPackageEntity}
     */
    @Bean
    public StateMachinePersister<BuyerOrderStatus, SellerOrderPackageEvent, OrderPackageEntity> buyerOrderStatusPersister() {
        return new DefaultStateMachinePersister<>(buyerOrderStatusOrderPackageStateMachinePersist);
    }
}