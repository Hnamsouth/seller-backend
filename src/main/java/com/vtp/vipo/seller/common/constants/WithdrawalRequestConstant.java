package com.vtp.vipo.seller.common.constants;

import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawRequestStatusEnum;

import java.util.Collection;
import java.util.List;

public class WithdrawalRequestConstant {

    public static final Collection<WithdrawRequestStatusEnum> FAIL_STATUS = List.of(
            WithdrawRequestStatusEnum.CANCELED,
            WithdrawRequestStatusEnum.REJECTED
    );

    public static final String BANK_ACCOUNT_INFO = "%s (%s) - Chi nhánh: %s - %s";

    public static final String NOT_FOUND = "Không tìm thấy Yêu cầu";

    public static final String CREATE_REQUEST_FAIL = "Tạo yêu cầu thất bại";

    public static final String ORDER_PACKAGE_INVALID = "Đơn hàng không đủ điều kiện rút tiền";

    public static final String RE_CREATE_FAIL = "Các đơn hàng trong yêu cầu này đã được tạo yêu cầu. Vui lòng chọn yêu cầu khác";

    public static final String RE_CREATE_STATUS_INVALID = "Chỉ được tạo lại các yêu cầu ở trạng thái hủy hoặc từ chối";

    public static final String BANK_ACCOUNT_INFO_NOT_FOUND = "Không tìm thấy thông tin tài khoản của nhà bán";

    public static final String ORDER_HAVE_REQUEST = "Đơn %s đã được tạo request";

    public static final String ORDER_HAVE_REQUESTED = "Tạo yêu cầu thất bại: các đơn đã chọn đang trong quá trình rút tiền";

    public static final String RECREATE_ORDER_HAVE_REQUESTED = "Không thể tạo lại yêu cầu này do toàn bộ đơn trong yêu cầu này đều đang được xử lý chi tiền";

    public static final String REQUEST_LIMITED = "Qúy khách đã hết lượt rút tiền trong tháng này. Lượt rút tiền còn lại của quý khách là 0/%s lượt";

    public static final String NO_AVAILABLE_BALANCE = "Tạo yêu cầu thất bại: số dư không đủ để thực hiện yêu cầu";

    public static final String INVALID_AVAILABLE_BALANCE = "Tạo yêu cầu thất bại: yêu cầu chọn đơn hàng có lợi nhuận > 0";

}
