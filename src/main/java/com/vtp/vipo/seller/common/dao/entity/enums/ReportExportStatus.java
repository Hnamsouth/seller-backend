package com.vtp.vipo.seller.common.dao.entity.enums;

    import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

    import java.util.List;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ReportExportStatus {
    //'pending', 'completed', 'failed

    //todo: uppercase later
    pending("Đang xử lý"),

    completed("Đã hoàn thành"),

    failed("Thất bại");

    String name;

    public static final List<ReportExportStatus> NOT_SHOW_IN_HISTORY_STATUS = List.of(failed);

}
