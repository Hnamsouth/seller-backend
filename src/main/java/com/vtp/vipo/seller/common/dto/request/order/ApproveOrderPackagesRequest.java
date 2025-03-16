package com.vtp.vipo.seller.common.dto.request.order;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * DTO for approving order packages.
 * This request contains a list of order package IDs that need approval.
 */
@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApproveOrderPackagesRequest {

    //todo: limit the number of order package
    /**
     * List of order package IDs to be approved.
     * Must not be empty.
     */
    @NotEmpty
    List<Long> orderPackageIds;

}
