package com.vtp.vipo.seller.common.dto.response.withdrawalrequest;

import com.vtp.vipo.seller.common.utils.DataUtils;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeeColumn {

    String name;

    String code;

    public FeeColumn(FeeMap data){
        if(DataUtils.isNullOrEmpty(data))
            return;
        this.name = data.getFeeName();
        this.code =  data.getColumnCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeeColumn feeColumn = (FeeColumn) o;
        return Objects.equals(name, feeColumn.name) &&
                Objects.equals(code, feeColumn.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, code);
    }

}
