package com.vtp.vipo.seller.common.dto.response.order;

import java.util.ArrayList;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.parameters.P;
import org.springframework.util.CollectionUtils;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
/**
 * Class representing the response to a request to refuse or cancel orders.
 * This class contains two lists: one for the orders that were successfully processed
 * and another for the orders that failed.
 */
public class OrderRefuseCancelResponse {

    /**
     * @return A list of orders that have been successfully refused or canceled
     */
    @Builder.Default
    List<OrderRefuseCancelInfo> completed = new ArrayList<>();

    /**
     * @return A list of orders that failed to be refused or canceled
     */
    @Builder.Default
    List<OrderRefuseCancelInfo> failed = new ArrayList<>();

    public void setCompleted(OrderRefuseCancelInfo completed) {
        if(CollectionUtils.isEmpty(this.completed)){
            this.completed = new ArrayList<>();
        }
        this.completed.add(completed);
    }

    public void setFailed(OrderRefuseCancelInfo failed) {
        if(CollectionUtils.isEmpty(this.failed)){
            this.failed = new ArrayList<>();
        }
        this.failed.add(failed);
    }

    public void setCompleted(List<OrderRefuseCancelInfo> completed) {
        this.completed = completed;
    }

    public void setFailed(List<OrderRefuseCancelInfo> failed) {
        this.failed = failed;
    }
}

