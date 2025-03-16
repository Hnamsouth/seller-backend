package com.vtp.vipo.seller.common.dto.response.order;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Response DTO for approving order packages.
 * This contains two lists of order packages: one for successful actions and another for failed actions.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApproveOrderPackagesResponse {

    /**
     * List of order packages that were successfully approved by the seller.
     * Each item contains details about the order package and the seller's action.
     */
    List<OrderPackageResAfterSellerAction> successList;

    /**
     * List of order packages that failed during the approval process.
     * Each item contains details about the order package and the reason for failure.
     */
    List<OrderPackageResAfterSellerAction> failList;

}