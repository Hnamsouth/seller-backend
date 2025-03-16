package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.WarehouseAddressEntity;
import com.vtp.vipo.seller.common.dao.entity.projection.WarehouseAddressProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing and managing `WarehouseAddressEntity` data in the database.
 * <p>
 * This interface provides methods to interact with the `warehouse_addresses` table, such as retrieving
 * warehouse address information based on the merchant ID and whether it's marked as the default address.
 * </p>
 */
@Repository
public interface WarehouseAddressRepository extends JpaRepository<WarehouseAddressEntity, Long> {

    /**
     * Finds all warehouse addresses for a given merchant, filtered by whether the address is marked as default.
     *
     * @param merchantId The ID of the merchant whose addresses are being queried.
     * @param isDefault A boolean indicating whether the address is the default address.
     * @return A list of `WarehouseAddressEntity` objects that match the specified criteria.
     */
    List<WarehouseAddressEntity> findByMerchantIdAndIsDefaultAndDeletedFalse(long merchantId, boolean isDefault);

    Long countByMerchantIdAndDeletedFalse(Long merchantId);

    Optional<WarehouseAddressEntity> findByIdAndMerchantIdAndDeletedFalse(long id, long merchantId);

//    @Query(value = """
//                    select wa.*, w.name as wardName, d.id as districtId, d.name as districtName,
//                           p.id as provinceId, p.name as provinceName , p.code as provinceCode
//                    from warehouse_address wa
//                        left join evtp_ward w on wa.wardId = w.id
//                        left join evtp_district d on w.districtId = d.id
//                        left join evtp_province p on d.provinceId = p.id
//                    where
//                        wa.merchantId = :merchantId
//                        and (COALESCE(:wardId, null) IS NULL or wa.wardId = :wardId)
//                        and (COALESCE(:districtId, null) IS NULL or w.districtId = :districtId)
//                        and (COALESCE(:provinceId, null) IS NULL or d.provinceId = :provinceId)
//                        and (COALESCE(:phoneNumber, null) IS NULL or replace(wa.phoneNumber, '+84', '0') like concat('%', trim(replace(replace(:phoneNumber, '+84', '0'), ' 84', '0')) ,'%') )
//                        and (COALESCE(:name, null) IS NULL or lower(wa.name) like concat('%', lower(trim(:name)), '%') )
//            """,
//            countQuery = """
//                    select count(wa.id)
//                    from warehouse_address wa
//                        left join evtp_ward w on wa.wardId = w.id
//                        left join evtp_district d on w.districtId = d.id
//                        left join evtp_province p on d.provinceId = p.id
//                    where
//                        wa.merchantId = :merchantId
//                        and (COALESCE(:wardId, null) IS NULL or wa.wardId = :wardId)
//                        and (COALESCE(:districtId, null) IS NULL or w.districtId = :districtId)
//                        and (COALESCE(:provinceId, null) IS NULL or d.provinceId = :provinceId)
//                        and (COALESCE(:phoneNumber, null) IS NULL or replace(wa.phoneNumber, '+84', '0') like concat('%', trim(replace(replace(:phoneNumber, '+84', '0'), ' 84', '0')) ,'%'))
//                        and (COALESCE(:name, null) IS NULL or lower(wa.name) like concat('%', lower(trim(:name)), '%'))
//                        """,
//            nativeQuery = true)
//    Page<WarehouseAddressProjection> findByMerchantIdAndWardIdAndPhoneNumberAndName(
//            long merchantId, Long wardId, Long districtId, Long provinceId, String phoneNumber, String name, Pageable pageable
//    );

    @Query(value = """
                    select wa.*, w.name as wardName, d.id as districtId, d.name as districtName, 
                           p.id as provinceId, p.name as provinceName , p.code as provinceCode
                    from warehouse_address wa 
                        left join evtp_ward w on wa.wardId = w.id
                        left join evtp_district d on w.districtId = d.id
                        left join evtp_province p on d.provinceId = p.id
                    where 
                        wa.merchantId = :merchantId and wa.isDeleted = 0
                        and (COALESCE(:wardId, null) IS NULL or wa.wardId = :wardId)
                        and (COALESCE(:districtId, null) IS NULL or w.districtId = :districtId)
                        and (COALESCE(:provinceId, null) IS NULL or d.provinceId = :provinceId)
                        and (COALESCE(:phoneNumber, null) IS NULL or wa.phoneNumber like concat('%', :phoneNumber, '%') )
                        and (COALESCE(:name, null) IS NULL or lower(wa.name) like concat('%', lower(trim(:name)), '%') )
            """,
            countQuery = """
                    select count(wa.id)
                    from warehouse_address wa 
                        left join evtp_ward w on wa.wardId = w.id
                        left join evtp_district d on w.districtId = d.id
                        left join evtp_province p on d.provinceId = p.id
                    where 
                        wa.merchantId = :merchantId and wa.isDeleted = 0
                        and (COALESCE(:wardId, null) IS NULL or wa.wardId = :wardId)
                        and (COALESCE(:districtId, null) IS NULL or w.districtId = :districtId)
                        and (COALESCE(:provinceId, null) IS NULL or d.provinceId = :provinceId)
                        and (COALESCE(:phoneNumber, null) IS NULL or wa.phoneNumber like concat('%', :phoneNumber, '%') )
                        and (COALESCE(:name, null) IS NULL or lower(wa.name) like concat('%', lower(trim(:name)), '%'))
                        """,
            nativeQuery = true)
    Page<WarehouseAddressProjection> findByMerchantIdAndWardIdAndPhoneNumberAndName(
            long merchantId, Long wardId, Long districtId, Long provinceId, String phoneNumber, String name, Pageable pageable
    );

    @Query(value = """
                    select wa.*, w.name as wardName, d.id as districtId, d.name as districtName, 
                           p.id as provinceId, p.name as provinceName , p.code as provinceCode
                    from warehouse_address wa 
                        left join evtp_ward w on wa.wardId = w.id
                        left join evtp_district d on w.districtId = d.id
                        left join evtp_province p on d.provinceId = p.id
                    where 
                        wa.merchantId = :merchantId and wa.isDeleted = 0
            """, nativeQuery = true)
    Page<WarehouseAddressProjection> findAllByMerchantId(
            long merchantId, Pageable pageable
    );

}