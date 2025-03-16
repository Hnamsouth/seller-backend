package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.WithdrawalRequestEntity;
import com.vtp.vipo.seller.common.dao.entity.dto.PrepaymentTransactionData;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawRequestStatusEnum;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawalRequestType;
import com.vtp.vipo.seller.common.dao.entity.projection.WithdrawalRequestProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequestEntity, Long> {

    @Query(value =
            """
                select
                    wr.id as id,
                    wr.createdAt as date,
                    wr.type as withdrawalRequestType,
                    wr.status as withdrawalRequestStatus,
                    wr.totalAmount as amount,
                    wr.cancelReason as reason,
                    mpc.bankCode as bankCode,
                    mpc.accountNumber as accountNumber,
                    mpc.accountOwner as accountName,
                    mpc.branch as bankBranch,
                    min(cast(wri.reCreated as integer) ) as reCreated
                from
                     WithdrawalRequestEntity wr
                     join WithdrawalRequestItemEntity wri on wri.withdrawalRequestId = wr.id
                     join MerchantEntity m on wr.merchantId = m.id
                     join MerchantPaymentCardEntity mpc on mpc.merchantId = m.id and mpc.isDefault = 1
                where
                    wr.deleted = false and
                    (:requestType is null or wr.type = :requestType)
                and (:status is null or wr.status in (:status))
                and (:amountFrom is null or wr.totalAmount >= :amountFrom)
                and (:amountTo is null or wr.totalAmount <= :amountTo)
                and (:startDate is null or wr.createdAt >= :startDate)
                and (:endDate is null or wr.createdAt <= :endDate)
                and (:merchantId is null or wr.merchantId = :merchantId)
                group by wr.id
                order by wr.id desc
                    """)
    Page<WithdrawalRequestProjection> findAllByFilterRequest(
            WithdrawalRequestType requestType,
            List<WithdrawRequestStatusEnum> status,
            BigDecimal amountFrom,
            BigDecimal amountTo,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long merchantId,
            Pageable pageable
    );

    @Query(value =
            """
                select
                    wr.id as id,
                    wr.createdAt as date,
                    wr.type as withdrawalRequestType,
                    wr.status as withdrawalRequestStatus,
                    wr.totalAmount as amount,
                    wr.cancelReason as reason,
                    mpc.bankCode as bankCode,
                    mpc.accountNumber as accountNumber,
                    mpc.accountOwner as accountName,
                    mpc.branch as bankBranch
                from
                     WithdrawalRequestEntity wr
                     join MerchantEntity m on wr.merchantId = m.id
                     join MerchantPaymentCardEntity mpc on mpc.merchantId = m.id and mpc.isDefault = 1
                where
                    wr.deleted = false and
                    (:requestType is null or wr.type = :requestType)
                and (:status is null or wr.status in (:status))
                and (:amountFrom is null or wr.totalAmount >= :amountFrom)
                and (:amountTo is null or wr.totalAmount <= :amountTo)
                and (:startDate is null or wr.createdAt >= :startDate)
                and (:endDate is null or wr.createdAt <= :endDate)
                and (:merchantId is null or wr.merchantId = :merchantId)
                order by wr.id desc
                    """)
    List<WithdrawalRequestProjection> getAllWithdrawalRequestsByFilter(
            WithdrawalRequestType requestType,
            List<WithdrawRequestStatusEnum> status,
            BigDecimal amountFrom,
            BigDecimal amountTo,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long merchantId
    );

    @Query(value = """
       select
                    wr.id as id,
                    wr.createdAt as date,
                    wr.type as withdrawalRequestType,
                    wr.status as withdrawalRequestStatus,
                    wr.totalAmount as amount,
                    wr.cancelReason as reason,
                    wr.tax as tax,
                    wr.taxValue as taxValue,
                    wr.totalAmount as totalWithdrawal,
                    mpc.bankCode as bankCode,
                    mpc.accountNumber as accountNumber,
                    mpc.accountOwner as accountName,
                    mpc.branch as bankBranch,
                    min(cast(wri.reCreated as integer) ) as reCreated
                from
                     WithdrawalRequestEntity wr
                     join WithdrawalRequestItemEntity wri on wri.withdrawalRequestId = wr.id
                     join MerchantEntity m on wr.merchantId = m.id
                     join MerchantPaymentCardEntity mpc on mpc.merchantId = m.id and mpc.isDefault = 1
                where
                    wr.deleted = false and wr.id = :withdrawalRequestId
                    and (:merchantId is null or wr.merchantId = :merchantId)
                    group by wr.id
    """)
    Optional<WithdrawalRequestProjection> findByIdAndMerchantId(Long withdrawalRequestId, Long merchantId);


    @Query(value = """
        select count(wr.merchantId) from withdrawal_request wr
                 where wr.status not in ('REJECTED', 'CANCELED')
                   and wr.merchantId = :merchantId
                   and wr.createdAt >= :fromDate
                   and wr.createdAt <= :toDate
        group by wr.merchantId
        """, nativeQuery = true)
    Integer getRemainingWithdrawalsByMerchantId(
            Long merchantId,
            Long fromDate,
            Long toDate
    );

    @Query(value = """
        select count(wr.merchantId) < wc.maxWithdrawalAttemptInAMonth from withdrawal_request wr
            join merchant m on wr.merchantId = m.id
            join withdrawal_config wc on wc.merchantGroupId = m.merchantGroupId
        where wr.status not in ('REJECTED', 'CANCELED')
          and wr.merchantId = :merchantId
          and wr.createdAt >= :fromDate
          and wr.createdAt <= :toDate
        group by wr.merchantId
        """, nativeQuery = true)
    Boolean checkWithdrawTimes(
            Long merchantId,
            Long fromDate,
            Long toDate
    );

    Optional<WithdrawalRequestEntity> findByIdAndMerchantIdAndDeletedFalse(Long id, Long merchantId);

    @Query(value = "select wr.* from withdrawal_request wr where wr.id = :id", nativeQuery = true)
    Optional<WithdrawalRequestEntity> getRequestById(String id);


    @Query(value = """
        select
             op.price - coalesce(sum(pfd.feeValue), 0) - coalesce(sum(pp.sellerPlatformDiscountAmount), 0) as estimatedProfit
        from order_package op
             join (select sum(ppd.sellerPlatformDiscountAmount) as sellerPlatformDiscountAmount, ppd.packageId from package_product ppd group by ppd.packageId) pp on pp.packageId = op.id
             join merchant m on op.merchantId = m.id
             left join withdrawal_config wc on wc.merchantGroupId = m.merchantGroupId
             left join platform_fee_detail pfd on pfd.packageId = op.id
        where op.merchantId = :merchantId
          and op.orderStatus = '501'
          and op.id not in (select distinct sub_wri.packageId
                          from withdrawal_request_item sub_wri
                                   left join withdrawal_request sub_wr on sub_wri.withdrawalRequestId = sub_wr.id
                          where sub_wr.merchantId = :merchantId
                            and sub_wr.status = 'SUCCESS')
          and from_unixtime((op.deliverySuccessTime + wc.withdrawAfterSecond)) <= current_timestamp
        group by op.id order by op.id
        """, nativeQuery = true)
    List<BigDecimal> getBalancePendingByMerchantId(Long merchantId);

    @Query(value = """
        select
            sum(wr.totalAmount) as totalWithdrawal
        from withdrawal_request wr
        where wr.merchantId = :merchantId
            and wr.status = 'SUCCESS'
            and (:fromDate is null or wr.createdAt >= :fromDate)
            and (:toDate is null or wr.createdAt <= :toDate)
        group by wr.merchantId
        """, nativeQuery = true)
    Optional<BigDecimal> getTotalWithdrawalByMerchantId(Long merchantId, Long fromDate, Long toDate);

    @Query(value = """
        select
            mpc.bankCode as bankCode,
            mpc.accountNumber as accountNumber,
            mpc.accountOwner as accountName,
            mpc.branch as bankBranch
        from MerchantPaymentCardEntity mpc
            where mpc.isDefault = 1
            and mpc.merchantId = :merchantId
        """)
    Optional<WithdrawalRequestProjection> getBankAccountInfoByMerchantId(Long merchantId);

    @Query(value = """
            select
            coalesce(op.price, 0) - coalesce(sum(pfd.feeValue), 0) - coalesce(sum(pp.sellerPlatformDiscountAmount), 0) as estimatedProfit
        from order_package op
            join (select sum(ppd.sellerPlatformDiscountAmount) as sellerPlatformDiscountAmount, ppd.packageId from package_product ppd group by ppd.packageId) pp on pp.packageId = op.id
            join merchant m on op.merchantId = m.id
            left join withdrawal_config wc on wc.merchantGroupId = m.merchantGroupId
            left join platform_fee_detail pfd on pfd.packageId = op.id
        where op.merchantId = :merchantId
            and op.orderStatus = '501'
            and from_unixtime((op.deliverySuccessTime + wc.withdrawAfterSecond)) <= current_timestamp
        group by op.id order by op.id
        """,
            nativeQuery = true)
    List<BigDecimal> getTotalRevenueByMerchantId(Long merchantId);

    @Query(value = """
            select
            coalesce(op.price, 0) - coalesce(sum(pfd.feeValue), 0) - coalesce(sum(pp.sellerPlatformDiscountAmount), 0) as estimatedProfit
        from order_package op
            join (select sum(ppd.sellerPlatformDiscountAmount) as sellerPlatformDiscountAmount, ppd.packageId from package_product ppd group by ppd.packageId) pp on pp.packageId = op.id
            join merchant m on op.merchantId = m.id
            left join withdrawal_config wc on wc.merchantGroupId = m.merchantGroupId
            left join platform_fee_detail pfd on pfd.packageId = op.id
        where op.merchantId = :merchantId
            and op.orderStatus = '501'
            and op.id not in (select distinct sub_wri.packageId
                  from withdrawal_request_item sub_wri
                           left join withdrawal_request sub_wr on sub_wri.withdrawalRequestId = sub_wr.id
                  where sub_wr.merchantId = :merchantId
                    and sub_wr.status = 'SUCCESS')
            and from_unixtime((op.deliverySuccessTime + wc.withdrawAfterSecond)) <= current_timestamp
        group by op.id order by op.id
        """,
            nativeQuery = true)
    List<BigDecimal> getAvailableBalanceByMerchantId(Long merchantId);


    @Query(value = """
        select
            new com.vtp.vipo.seller.common.dao.entity.dto.PrepaymentTransactionData(op.id, t.vtTransactionId, t.createTime)
        from TransactionEntity t
            join OrderPackageEntity op on op.order.id = t.orderId
        where t.status = 1
            and op.id in (:packageIds)
        group by t.orderId
        """)
    Collection<PrepaymentTransactionData> getTransactionCodeByOrderPackageIdIn(Collection<Long> packageIds);


    @Query(value = """
            SELECT sum(totalAmount)
            FROM withdrawal_request wr
            WHERE wr.merchantId = :merchantId
              AND wr.status = 'SUCCESS'
              AND wr.withdrawSuccessTime >= :startEpochSecond
              AND wr.withdrawSuccessTime <= :endEpochSecond
            """, nativeQuery = true)
    BigDecimal sumTotalTransferedPayment(long merchantId, long startEpochSecond, long endEpochSecond);
}
