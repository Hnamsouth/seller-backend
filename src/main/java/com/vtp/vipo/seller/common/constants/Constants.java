package com.vtp.vipo.seller.common.constants;


import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus;
import com.vtp.vipo.seller.common.dto.response.order.LogisticsTrackingVTP;
import com.vtp.vipo.seller.common.enumseller.OrderFilterTab;

import com.vtp.vipo.seller.common.utils.PagingUtils;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {

    private Constants() {
        throw new IllegalStateException(UTITLITY_CLASS_ERROR);
    }

    public static final String UTITLITY_CLASS_ERROR = "Utility class!!!";

    public static final String BEARER_TOKEN_TYPE = "Bearer";

    public final static String LOGGED_USER = "loggedUser";

    public static final String HEADER = "Authorization";

    public static final String HEADER2 = "x-access-token";

    /* Http constants */
    public static final String HTTP_RESPONSE_HEADER_CONTENT_TYPE = "application/json;charset=UTF-8";

    public static final String HTTP_RESPONSE_HEADER_CONTENT_TYPE_EXCEL = "application/vnd.ms-excel";

    public static final String MESSAGE = "message";

    public static final String UNKNOWN_ERROR_VALIDATING_JWT_TOKEN = "Error on validate token with exception {}";

    /* i18n */
    public static final String FAILED_TO_FIND_IN_MESSAGE_SOURCE = "Failed to find the key {} in the message source";

    public static final String CURRENCY = "VND";

    public static final String LANGUAGE = "vi";
    public static final String LANGUAGE_VI = "vi";
    public static final String LANGUAGE_EN = "en";

    public static final String CHINESE_LANGUAGE = "zh";
    public static final List<String> CANCEL_NOTE_REQUIRED_CANCEL_STATUSES = List.of("092", "094", "210");

    public static final List<String> ALLOW_CANCEL_ORDER_STATUS = List.of("010", "011", "012", "014", "015", "016", "020", "021");

    public static final List<String> PAYMENT_STATUS_SUCCESSFULL = List.of("501");

    public static final List<String> PAYMENT_STATUS_UNSUCCESSFULL = List.of("091", "092", "093", "094", "201", "505", "515", "502", "504", "516", "517", "600", "601");

    public static final LocalDateTime START_DATE_FOR_DATE_FETCHING = LocalDateTime.of(2024, 6, 1, 0, 0);
    public static final String PREFIX_PROD_CODE = "PROD";
    public static final String PREFIX_ATTRIBUTE_CODE = "A";
    public static final Map<String, String> LIST_KEYWORD_SEARCH = Map.of(
            "ORDER_CODE", "Mã đơn hàng",
            "BUYER_NAME", "Tên người mua",
            "PRODUCT_NAME", "Tên sản phẩm",
            "TRACKING_NUMBER", "Mã vận đơn",
            "BUYER_PHONE_NUMBER", "Số điện thoại người mua"
    );

    public static final List<String> VALID_IMAGE_TYPE = List.of("JPG", "PNG", "JPEG");
    public static final List<String> VALID_VIDEO_TYPE = List.of("MP4");
    public static final int MAX_DAYS_DIFF = 90;
    public static final String XLS = "xls";
    public static final int LIMIT_RECORD = 100;
    public static final int LIMIT_RECORD_SKU = 2700;
    public static final int TEMPLATE_ROW = 37;
    public static final int TEMPLATE_ROW_SKU = 11;

    public static final String PARAM_LOCALE = "{###locale###}";
    public static final String PRODUCT_EXAMPLE_PATH_TEMPLATE = "template/productManagement/create/" + PARAM_LOCALE
            + "/create_template.xlsx";

    // paging constants
    public static final int DEFAULT_PAGE_NUM = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int PAGE_SIZE_LIMIT = 200;
    public static final Sort DEFAULT_SORT_FOR_WAREHOUSE_ADDRESS_LIST
            = PagingUtils.createSortFromString("isDefault,desc,updatedAt,desc");

    //warehouse address
    public static final String WAREHOUSE_ADDRESS_NOT_EXIST = "Địa chỉ không còn tồn tại";
    public static final int VIETNAM_COUNTRY_ID = 236;

    public static final String ORDER_PACKAGE_NOT_FOUND = "Không tìm thấy đơn hàng";

    public static final String NOT_ELIGIBLE_FOR_APPROVAL_WARNING
            = "Chỉ cho phép duyệt đơn hàng ở trạng thái đơn \"Chờ nhà bán xác nhận\"";

    public static final String NO_PACKAGE_PRODUCTS_IN_THE_ORDER_PACKAGE_WARNING
            = "Không tìm thấy sản phẩm tồn tại trên đơn này!";

    public static final String SOME_CHANGES_WERE_MADE_WHEN_EXECUTING
            = "Thông tin của đơn có sự thay đổi. Vui lòng thử lại!";

    public static final String VTP_CARRIER_NAME = "Viettel Post";

    public static final String DEFAULT_SHIPMENT_METHOD = "Lấy hàng tại kho";

    public static final String DEFAULT_COUNTRY_NAME = "Việt Nam";

    public static final String DEFAULT_PAYMENT_METHOD = "Thanh toán khi nhận hàng";

    public static final Map<OrderFilterTab, SellerOrderStatus> FILTER_TAB_TO_SELLER_STATUS
            = Map.of(
            OrderFilterTab.WAITING_PAYMENT, SellerOrderStatus.PARENT_WAITING_PAYMENT,
            OrderFilterTab.WAITING_APPROVAL, SellerOrderStatus.PARENT_WAITING_APPROVAL,
            OrderFilterTab.WAITING_SHIPMENT, SellerOrderStatus.PARENT_WAITING_SHIPMENT,
            OrderFilterTab.IN_TRANSIT, SellerOrderStatus.PARENT_IN_TRANSIT,
            OrderFilterTab.DELIVERED, SellerOrderStatus.PARENT_DELIVERED,
            OrderFilterTab.RETURN_REFUND_CANCEL, SellerOrderStatus.PARENT_CANCELLED
    );

    public static final double PERCENTAGE_PREPAYMENT = 0.7;
    public static final int MAX_LENGTH_OF_PRODUCT_NAME = 150;
    public static final int MAX_LENGTH_OF_PRODUCT_DESCRIPTION = 150;

    public static final String HISTORY_ADJUSTMENT_NOT_FOUND = "Không tìm thấy lịch sử điều chỉnh giá";
    public static final String HISTORY_ADJUSTMENT_NOT_MATCH_ORDER = "Lịch sử điều chỉnh giá không phù hợp với đơn hàng";
    public static final String ORDER_NOT_IN_STATUS_WAITING_FOR_PAYMENT = "Chỉ cho phép điều chỉnh giá ở trạng thái đơn \"Chờ thanh toán\"";
    public static final String ADJUSTED_AMOUNT_GREATER_THAN_TOTAL_PRICE = "Số tiền điều chỉnh phải nhỏ hơn hoặc bằng tổng tiền hàng";
    public static final String SKU_ID_NOT_FOUND_IN_ORDER_PACKAGE = "SKU {skuId} không tìm thấy trong đơn hàng";
    public static final String NO_SKU_FOUND = "Không có SKU trong đơn hàng này";
    public static final String ADJUSTED_AMOUNT_GREATER_THAN_SKU_PRICE = "Số tiền điều chỉnh phải nhỏ hơn giá SKU";
    public static final String ORDER_NOT_IN_STATUS_WAITING_FOR_ORDER_PREPARATION = "Chỉ cho phép chuẩn bị hàng ở trạng thái đơn \"Chờ chuẩn bị đơn hàng\"";
    public static final String MESSAGE_CREATE_ORDER_SUCCESS = "Đơn chuẩn bị hàng thành công";
    public static final String MESSAGE_CREATE_ORDER_PENDING = "Đơn chờ cập nhật mã vận chuyển từ đối tác";
    public static final String MESSAGE_CREATE_ORDER_FAIL = "Không thể kết nối với máy chủ. Vui lòng thử lại sau";
    public static final String ORDER_NOT_IN_STATUS_ORDER_SHIPMENT_CONNECTION_SUCCESS = "Chỉ in phiếu giao với những đơn có trạng thái đã chuẩn bị hàng và sinh mã vận đơn";
    public static final String INVALID_PAPER_SIZE_OR_NUMBER_OF_COPIES = "Kích thước in hoặc số liên không hợp lệ";
    public static final String CARRIER_NOT_FOUND = "Không tìm thấy đối tác vận chuyển";
    public static final String WAREHOUSE_NOT_FOUND = "Không tìm thấy kho hàng";
    public static final String WARD_NOT_FOUND = "Không tìm thấy phường/xã";
    public static final String DISTRICT_NOT_FOUND = "Không tìm thấy quận/huyện";
    public static final String PRODUCT_NOT_FOUND_IN_ORDER_PACKAGE = "Không tìm thấy sản phẩm trong đơn hàng";
    public static final String SKU_NOT_FOUND_IN_PRODUCT = "Không tìm thấy SKU trong sản phẩm";
    public static final String ORDER_SHIPMENT_NOT_FOUND = "Không tìm thấy vận đơn";

    public static final String MESSAGE_LOG_CONNECT_ORDER_SHIPMENT_SUCCESS = "Tạo vận đơn VTPOST thành công";
    public static final String MESSAGE_LOG_CONNECT_ORDER_SHIPMENT_FAIL = "Tạo vận đơn VTPOST thất bại";

    public static final String ORDER_IDS_NOT_EMPTY = "Danh sách đơn hàng không được để trống";
    public static final String CARRIER_CODE_NOT_EMPTY = "Mã đơn vị vận chuyển không được để trống";
    public static final String WAREHOUSE_ADDRESS_ID_NOT_EMPTY = "Địa chỉ kho không được để trống";
    public static final String WAREHOUSE_ADDRESS_ID_INVALID = "Địa chỉ kho không hợp lệ";
    public static final String PRINT_ORDER_MIN_MAX = "Vui lòng in tối thiểu 1 đơn hàng và tối đa 100 đơn hàng";
    public static final String PRINT_STATUS_NOT_EMPTY = "Xác nhận trạng thái in không được để trống";
    public static final String PRINT_PAPER_SIZE_NOT_EMPTY = "Kích thước in không được để trống";
    public static final String PRINT_PAPER_SIZE_INVALID = "Kích thước in không hợp lệ";
    public static final String PRINT_NUMBER_OF_COPIES_NOT_EMPTY = "Số liên cần in không được để trống";
    public static final String PRINT_NUMBER_OF_COPIES_INVALID = "Số liên cần in không hợp lệ";
    public static final String SORT_TYPE_NOT_EMPTY = "Hình thức sắp xếp không được để trống";
    public static final String SORT_TYPE_INVALID = "Hình thức sắp xếp không hợp lệ";

    public static final String SERVICE_VIPO = "VVPO";
    public static final String PRODUCT_TYPE_VIPO = "HH";
    public static final List<String> EXTRA_SERVICE_VIPO = List.of("GDK");
    public static final String PREFIX_ORDER_CODE_VIPO = "VPO";

    public static final String SOURCE_VIPO = "VIPO";

    public static final String ORDER_NOT_FOUND = "Không tìm thấy đơn hàng";
    public static final String ORDER_NOT_IN_STATUS_WAITING_FOR_SELLER_CONFIRMATION
            = "Đơn hàng ở trạng thái đã chuẩn bị hàng sẽ được phép hủy";
    public static final String STATUS_ORDER_CHANGED = "Đơn hàng đã chuyển trạng thái";
    public static final String ORDER_NOT_ALLOWED_TO_CANCEL
            = "Đơn hàng phải ở trạng thái đang chuẩn bị hàng hoặc đã chuẩn bị hàng để có thể hủy";

    public static final String SELLER_APPROVE_ORDER_PACKAGE = "Nhà bán đã xác nhận đơn hàng";

    public static final String VIPO_SOURCE = "VIPO";

    public static final int DYNAMIC_START_FEE_TAG = 12;

    public static final String PAGE_DEFAULT = "1";
    public static final String PAGE_SIZE_DEFAULT = "10";
    public static final int PRODUCT_NOT_DELETED = 0;
    public static final int PRODUCT_DELETED = 1;
    public static final double MAX_UPLOAD_CERTIFICATE_SIZE_MB = 5.0;
    public static final int MAX_UPLOAD_CERTIFICATE_COUNT = 10;
    public static final int MAX_DISPLAY_CERTIFICATE_COUNT = 5;

    public static final String PRODUCT_NOT_FOUND = "Không tìm thấy sản phẩm";
    public static final String CERTIFICATE_NOT_FOUND = "Không tìm thấy chứng chỉ sản phẩm";
    public static final String NO_FILE_UPLOADED = "Không có file được upload";
    public static final String FILE_NOT_EXIST = "File không tồn tại";
    public static final String FILE_INVALID_EXTENSION = "Sai định dạng";
    public static final String FILE_EXCEED_SIZE = "Quá dung lượng";
    public static final String UNKNOWN_FILE = "unknown-file";
    public static final String MAX_CERTIFICATE_PER_PRODUCT_EXCEEDED = "Vượt quá số lượng chứng chỉ cho sản phẩm";
    public static final String MAX_DISPLAY_CERTIFICATE_PER_PRODUCT_EXCEEDED = "Vượt quá số lượng chứng chỉ hiển thị cho sản phẩm";

    public static final String VIETNAMESE_LANGUAGE = "vi";
    public static final String ENGLISH_LANGUAGE = "en";
    public static final String KHMER_LANGUAGE = "km";
    public static final String KOREA_LANGUAGE = "ko";
    public static final String LANGUAGE_DEFAULT = "vi";

    //order status language
    public static final String ORDER_STATUS_VIETNAMESE_LANGUAGE = "vi";
    public static final String ORDER_STATUS_ENGLISH_LANGUAGE = "en";
    public static final String ORDER_STATUS_CHINESE_LANGUAGE = "zh";
    public static final String ORDER_STATUS_KHMER_LANGUAGE = "km";
    public static final String ORDER_STATUS_KOREA_LANGUAGE= "ko";
    //map between spring boot application language and language in the table "order_status_language"
    public static final Map<String, String> APPLICATION_LANG_TO_ORDER_STATUS_LANG_MAP
            = Map.of(
            VIETNAMESE_LANGUAGE, ORDER_STATUS_VIETNAMESE_LANGUAGE,
            ENGLISH_LANGUAGE, ORDER_STATUS_ENGLISH_LANGUAGE,
            CHINESE_LANGUAGE, ORDER_STATUS_CHINESE_LANGUAGE,
            KHMER_LANGUAGE, ORDER_STATUS_KHMER_LANGUAGE,
            KOREA_LANGUAGE, ORDER_STATUS_KOREA_LANGUAGE
    );

    /* order statuses */
    public static final String ORDER_CREATED_STATUS = "011";
    public static final String ORDER_CREATED_STATUS_TITLE = "Đặt hàng thành công";
    public static final String ORDER_CREATED_STATUS_DESCRIPTION = "Đơn hàng đã được đặt";
    public static final String PLACING_LAZBAO_ORDER_STATUS = "015";
    public static final String PLACING_LAZBAO_ORDER_STATUS_TITLE = "Thanh toán thành công";
    public static final String PLACING_LAZBAO_ORDER_STATUS_DESCRIPTION = "Đơn hàng đang được gửi tới nhà bán";
    public static final String START_LAZBAO_ORDER_STATUS = "020";
    public static final String START_ORDER_STATUS_TALK = "030";
    public static final String START_ORDER_STATUS_TALK_TITLE = "Đang chuẩn bị hàng";
    public static final String START_ORDER_STATUS_DESCRIPTION = "Nhà bán %s đang chuẩn bị hàng";

    public static final String IS_RREPARING_ORDER_STATUS = "030";
    public static final String CONFLICT_LAZBAO_ORDER_STATUS = "025";

    public static final Map<String, LogisticsTrackingVTP> SOURCE_ORDER_STATUS_TRACKING_GROUP = new HashMap<>();

    static {
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_110", new LogisticsTrackingVTP(110L,"[Quốc tế] Đơn hàng đang được xử lý ở Nước ngoài","Nhà bán đang chuẩn bị hàng"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_120", new LogisticsTrackingVTP(120L,"[Quốc tế] Đơn hàng đang được xử lý ở Nước ngoài","Đơn vị vận chuyển quốc tế đã nhận hàng"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_130", new LogisticsTrackingVTP(130L,"[Quốc tế] Đơn hàng đang được xử lý ở Nước ngoài","Đơn vị vận chuyển quốc tế đang vận chuyển tới kho Trung Quốc"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_140", new LogisticsTrackingVTP(140L,"[Quốc tế] Đơn hàng đang được xử lý ở Nước ngoài","Đang giao hàng nội địa Trung Quốc"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_150", new LogisticsTrackingVTP(150L,"[Quốc tế] Đơn hàng xảy ra vấn đề xử lý ở Nước ngoài","Đơn hàng xảy ra vấn đề trong nước"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_160", new LogisticsTrackingVTP(160L,"[Quốc tế] Đơn hàng xảy ra vấn đề xử lý ở Nước ngoài","Đơn hàng được hoàn lại"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_170", new LogisticsTrackingVTP(170L,"[Quốc tế] Đơn hàng xảy ra vấn đề xử lý ở Nước ngoài","Hoàn hàng thành công"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_180", new LogisticsTrackingVTP(180L,"[Quốc tế] Đơn hàng xảy ra vấn đề xử lý ở Nước ngoài","Đơn hàng bị từ chối nhận"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_200", new LogisticsTrackingVTP(200L,"[Quốc tế] Đơn hàng đang được xử lý ở Nước ngoài","Kho Trung Quốc đã nhận hàng từ nhà bán"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_210", new LogisticsTrackingVTP(210L,"[Quốc tế] Đơn hàng đang được xử lý ở Nước ngoài","Kho Trung Quốc đang xử lý hàng"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_220", new LogisticsTrackingVTP(220L,"[Quốc tế] Đơn hàng đang được xử lý ở Nước ngoài","Kho Trung Quốc hoàn tất cân hàng"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_250", new LogisticsTrackingVTP(250L,"[Quốc tế] Đơn hàng đang được xử lý ở Nước ngoài","Kho Trung Quốc hoàn tất đóng gói"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_300", new LogisticsTrackingVTP(300L,"[Quốc tế] Đơn hàng đang được xử lý ở Nước ngoài","Kho Trung Quốc niêm phong hàng hóa"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_330", new LogisticsTrackingVTP(330L,"[Quốc tế] Đơn hàng đang được vận chuyển đến Việt Nam","Bắt đầu thủ tục xuất khẩu Trung Quốc"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_350", new LogisticsTrackingVTP(350L,"[Quốc tế] Đơn hàng đang được vận chuyển đến Việt Nam","Đơn hàng xuất khẩu thành công"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_400", new LogisticsTrackingVTP(400L,"[Quốc tế] Đơn hàng đang được vận chuyển đến Việt Nam","Đơn hàng đang trên tuyến vận chuyển đến cửa khẩu"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_450", new LogisticsTrackingVTP(450L,"[Quốc tế] Đơn hàng đang được vận chuyển đến Việt Nam","Đơn hàng đã đến cửa khẩu Việt Nam"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_480", new LogisticsTrackingVTP(480L,"[Quốc tế] Đơn hàng đang được vận chuyển đến Việt Nam","Bắt đầu thủ tục nhập khẩu"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_490", new LogisticsTrackingVTP(490L,"[Quốc tế] Đơn hàng đang được vận chuyển đến Việt Nam","Đơn hàng nhập khẩu thành công"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_500", new LogisticsTrackingVTP(500L,"[Quốc tế] Đơn hàng đang được vận chuyển đến Việt Nam","Đơn hàng đang chờ vận chuyển tới kho Vipo"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_520", new LogisticsTrackingVTP(520L,"[Quốc tế] Đơn hàng đang được vận chuyển đến Việt Nam","Đơn hàng đang chờ vận chuyển tới kho Vipo"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_530", new LogisticsTrackingVTP(530L,"[Quốc tế] Đơn hàng đang được vận chuyển đến Việt Nam","Đơn hàng bắt đầu vận chuyển trạm trung chuyển tại Việt Nam"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_550", new LogisticsTrackingVTP(550L,"[Quốc tế] Đơn hàng đang được vận chuyển đến Việt Nam","Đơn hàng đang vận chuyển tới trạm trung chuyển tại Việt Nam"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_600", new LogisticsTrackingVTP(600L,"[Quốc tế] Đơn hàng đang được vận chuyển đến Việt Nam","Đã đến trạm trung chuyển tại Việt Nam"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_750", new LogisticsTrackingVTP(750L,"[Quốc tế] Đơn hàng đang được vận chuyển đến Việt Nam","Đơn hàng đang vận chuyển tới kho Vipo"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_760", new LogisticsTrackingVTP(760L,"[Quốc tế] Đơn hàng đang được vận chuyển đến Việt Nam","Đơn hàng đang chờ vận chuyển tới kho Vipo"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("LAZBAO_800", new LogisticsTrackingVTP(800L,"[Quốc tế] Đơn hàng đang được vận chuyển đến Việt Nam","Kho Vipo đang nhận hàng"));

        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_103", new LogisticsTrackingVTP(103L,"Đơn hàng đang giao","Đơn hàng giao cho bưu cục nhận hàng"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_107", new LogisticsTrackingVTP(107L,"Đơn hàng đang giao","Đơn hàng xảy ra vấn đề xử lý trong nước"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_200", new LogisticsTrackingVTP(200L,"Đơn hàng đang giao","Đơn hàng đã bàn giao cho đơn vị vận chuyển trong nước"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_201", new LogisticsTrackingVTP(201L,"Đơn hàng đang giao","Đơn hàng xảy ra vấn đề xử lý trong nước"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_202", new LogisticsTrackingVTP(202L,"Đơn hàng đang giao","Đơn hàng đang vận chuyển trong nước"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_300", new LogisticsTrackingVTP(300L,"Đơn hàng đang giao","Đơn hàng đang vận chuyển trong nước"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_320", new LogisticsTrackingVTP(320L,"Đơn hàng đang giao","Đơn hàng đang vận chuyển trong nước"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_410", new LogisticsTrackingVTP(410L,"Đơn hàng đang giao","Đơn hàng đang vận chuyển trong nước"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_400", new LogisticsTrackingVTP(400L,"Đơn hàng đang giao","Đơn hàng đang vận chuyển trong nước"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_500", new LogisticsTrackingVTP(500L,"Đơn hàng đang giao","Đơn hàng sắp được giao"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_501", new LogisticsTrackingVTP(501L,"Đơn hàng giao thành công","Đơn hàng giao thành công"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_503", new LogisticsTrackingVTP(503L,"Đơn hàng đang giao","Đơn hàng xảy ra vấn đề xử lý trong nước"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_505", new LogisticsTrackingVTP(505L,"Đơn hàng hoàn","Đơn hàng hoàn"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_506", new LogisticsTrackingVTP(506L,"Đơn hàng đang giao","Đơn hàng đang vận chuyển trong nước"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_507", new LogisticsTrackingVTP(507L,"Đơn hàng đang giao","Đơn hàng đang vận chuyển trong nước"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_508", new LogisticsTrackingVTP(508L,"Đơn hàng đang giao","Đơn hàng đang vận chuyển trong nước"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_509", new LogisticsTrackingVTP(509L,"Đơn hàng đang giao","Đơn hàng đang vận chuyển trong nước"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_515", new LogisticsTrackingVTP(515L,"Đơn hàng hoàn","Đơn hàng hoàn"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_516", new LogisticsTrackingVTP(516L,"Đơn hàng đang giao","Đơn hàng xảy ra vấn đề xử lý trong nước"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_517", new LogisticsTrackingVTP(517L,"Đơn hàng đang giao","Đơn hàng xảy ra vấn đề xử lý trong nước"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_502", new LogisticsTrackingVTP(502L,"Đơn hàng hoàn","Đơn hàng hoàn"));
        SOURCE_ORDER_STATUS_TRACKING_GROUP.put("VIETTELPOST_504", new LogisticsTrackingVTP(504L,"Đơn hàng hoàn","Đơn hàng hoàn"));
    }

    /* phase 5.5: Product Approval Fix */
    public static final String NOT_ALLOW_TO_ADD_OR_REMOVE_ATTRIBUTES_MESSAGE
            = "Không cho phép xóa hoặc thêm thuộc tính mới do sản phẩm đã phát sinh đơn";

    public static final String NOT_ALLOW_TO_RENAME_ATTRIBUTES_MESSAGE
            = "Không cho phép đổi tên thuộc tính do sản phẩm đã phát sinh đơn";

    public static final String NOT_ALLOW_TO_REMOVE_ATTRIBUTES_MESSAGE
            = "Không cho phép xóa thuộc tính do sản phẩm đã phát sinh đơn";

    public static final String NOT_ALLOW_TO_RENAME_CLASSIFY_MESSAGE
            = "Không cho phép đổi tên giá trị thuộc tính này do thuộc tính đã phát sinh đơn";

    public static final String NOT_ALLOW_TO_REMOVE_CLASSIFY_MESSAGE
            = "Không cho phép xóa giá trị thuộc tính này do thuộc tính đã phát sinh đơn";

    public static final String NOT_ALLOW_TO_REMOVE_SKU_MESSAGE
            = "Không cho phép xóa sku do thuộc tính đã phát sinh đơn";

    public static final String NOT_ALLOW_TO_CHANGE_CLASSIFY_IMAGE
            = "Không cho phép thay đổi ảnh của giá trị thuộc tính đã phát sinh đơn";

    public static final String NOT_ALLOW_TO_CHANGE_SKU_IMAGE
            = "Không cho phép thay đổi ảnh của sku đã phát sinh đơn";

    public static final int IS_NOT_DELETED = 0;

    //todo: remove this flag later
    public static final ThreadLocal<Boolean> isNewProductApproval = new ThreadLocal<>();


    public static final List<SellerOrderStatus> NOT_PAID_SELLER_ORDER_STATUSES
            = List.of(SellerOrderStatus.WAITING_FOR_PAYMENT, SellerOrderStatus.ORDER_PRICE_ADJUSTED);

    public static final List<SellerOrderStatus> SUCCESS_TO_CONNECT_DELIVERY_SELLER_ORDER_STATUSES
            = List.of(
            SellerOrderStatus.ORDER_SHIPMENT_CONNECTION_SUCCESS,
            SellerOrderStatus.ORDER_DELIVERED_TO_SHIPPING,
            SellerOrderStatus.ORDER_IN_TRANSIT,
            SellerOrderStatus.ORDER_COMPLETED
    );

    public static final List<SellerOrderStatus> SUCCESS_TO_PREPARE_ORDER_PACKAGE
            = List.of(
            SellerOrderStatus.ORDER_PREPARED,
            SellerOrderStatus.ORDER_SHIPMENT_CONNECTION_SUCCESS,
            SellerOrderStatus.ORDER_DELIVERED_TO_SHIPPING,
            SellerOrderStatus.ORDER_IN_TRANSIT,
            SellerOrderStatus.ORDER_COMPLETED
    );

    public static final String NO_PRICE_ADJUSTED_ORDER_PACKAGE_TYPE = "chưa điều chỉnh giá";

    public static final String PRICE_ADJUSTED_ORDER_PACKAGE_TYPE = "điều chỉnh giá";

    public static final String NO_CUSTOMER_DATA_SHOWN = "";

    public static final String TEMPORARY_NO_DATA_PLACEHOLDER = "";

    public static final String REVENUE_REPORT_EXCEED_ORDER_PACKAGE_LIMIT
            = "Số lượng bản ghi vượt quá số lượng cho phép (1.000.000). Vui lòng chia nhỏ để tiếp tục xuất file";
}
