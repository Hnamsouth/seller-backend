package com.vtp.vipo.seller.services.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.vtp.vipo.seller.common.BaseService;
import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.dao.entity.*;
import com.vtp.vipo.seller.common.dao.entity.enums.BuyerOrderStatus;
import com.vtp.vipo.seller.common.dao.repository.*;
import com.vtp.vipo.seller.common.dto.CustomMultipartFile;
import com.vtp.vipo.seller.common.dto.request.product.*;
import com.vtp.vipo.seller.common.dto.request.product.approve.ApproveProductRequest;
import com.vtp.vipo.seller.common.dto.request.product.search.ProductSearchReq;
import com.vtp.vipo.seller.common.dto.request.product.update.*;
import com.vtp.vipo.seller.common.dto.response.ProductCertificateResponse;
import com.vtp.vipo.seller.common.dto.response.base.PagingRs;
import com.vtp.vipo.seller.common.dto.response.product.approve.ApproveProductResponse;
import com.vtp.vipo.seller.common.dto.response.product.create.ImportByFileResultRes;
import com.vtp.vipo.seller.common.dto.response.product.create.ProductCreateByFileExcel;
import com.vtp.vipo.seller.common.dto.response.product.create.ProductSkuCreateByFileExcel;
import com.vtp.vipo.seller.common.dto.response.product.detail.ManageProductCodeDetailResponse;
import com.vtp.vipo.seller.common.dto.response.product.detail.ProductAttributesDetailResponse;
import com.vtp.vipo.seller.common.dto.response.product.detail.ProductDetailResponse;
import com.vtp.vipo.seller.common.dto.response.product.detail.SellerClassifyInfo;
import com.vtp.vipo.seller.common.dto.response.product.search.ProductSearchRes;
import com.vtp.vipo.seller.common.enumseller.CertificateStatus;
import com.vtp.vipo.seller.common.enumseller.ProductPriceTypeEnum;
import com.vtp.vipo.seller.common.enumseller.ProductReasonType;
import com.vtp.vipo.seller.common.enumseller.ProductStatus;
import com.vtp.vipo.seller.common.exception.*;
import com.vtp.vipo.seller.common.mapper.ProductMapper;
import com.vtp.vipo.seller.common.mapper.ProductCertificateMapper;
import com.vtp.vipo.seller.common.utils.*;
import com.vtp.vipo.seller.config.security.VipoUserDetails;
import com.vtp.vipo.seller.services.AmazonS3Service;
import com.vtp.vipo.seller.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.vtp.vipo.seller.common.dto.request.product.SellingProductInfo.*;

@Validated
@Service("productService")
@RequiredArgsConstructor
public class ProductServiceImpl extends BaseService<ProductEntity, Long, ProductRepository> implements ProductService {

    private final CategoryRepository categoryRepository;
    private final ProductTemporaryRepository productTemporaryRepository;
    private final SellerAttributeRepository sellerAttributeRepository;
    private final SellerClassifyRepository sellerClassifyRepository;
    private final ProductSellerSkuRepository productSellerSkuRepository;
    private final SellerAttributeTemporaryRepository sellerAttributeTemporaryRepository;
    private final SellerClassifyTemporaryRepository sellerClassifyTemporaryRepository;
    private final ProductSellerSkuTemporaryRepository productSellerSkuTemporaryRepository;
    private final OrderPackageRepository orderPackageRepository;
    private final AmazonS3Service amazonS3Service;
    private final SellerOpenRepository sellerOpenRepository;
    private final VipoLanguageUtils vipoLanguageUtils;
    private final MerchantRepository merchantRepository;
    private final ProductCertificateEntityRepository productCertificateEntityRepository;
    private final ProductCertificateMapper productCertificateMapper;

    private final ProductMapper productMapper;
    private final PackageProductRepository packageProductRepository;
    private final MerchantNewRepository merchantNewRepository;


    @Value("${template.product.create.vi}")
    private String createTemplatePathVi;
    @Value("${template.product.create.en}")
    private String createTemplatePathEn;

    private final List<ProductStatus> statusDisplayMainProduct = List.of(ProductStatus.PENDING, ProductStatus.REJECT, ProductStatus.SELLING, ProductStatus.STOPPED, ProductStatus.PAUSED, ProductStatus.CANCELED, ProductStatus.ADJUST_REJECT);

    @Override
    public PagingRs search(ProductSearchReq request, Pageable pageable) {
        PagingRs page = new PagingRs();
        checkMerchant();
        if (!validateDates(request.getFromDate(), request.getToDate())) {
            throw new VipoBusinessException(ErrorCodeResponse.INVALID_FROM_TO_DATE);
        }
        VipoUserDetails user = getCurrentUser();
        Long toDateToMilli = DateUtils.convertMilTimeToSecond(request.getToDate().plus(1, ChronoUnit.DAYS).toEpochMilli());
        Page<ProductSearchRes> productsResponses = repo.searchProducts(request.getKey(),
                request.getCategoryId(),
                request.getStatus(),
                DateUtils.convertMilTimeToSecond(request.getFromDate().toEpochMilli()),
                toDateToMilli,
                user.getId(),
                getLocale(),
                pageable);
        List<ProductSearchRes> products = productsResponses.getContent();
        int total = productsResponses.getTotalPages();
        page.setCurrentPage(productsResponses.getNumber() + 1);
        page.setTotalCount(total);
        page.setData(products);
        return page;
    }

    @Override
    public ProductDetailResponse getDetail(Long id) {
        String currentLanguage = vipoLanguageUtils.getCurrentLanguage();
        ProductEntity product = findOne(id);
//        /**
//         * Trạng thái lấy chi tiết sp từ bảng chính
//         * Chờ duyệt tạo, Từ chối duyệt tạo , Đang bán, Đã khoá, Tạm dừng bán, Ngừng bán, Đã huỷ
//         * */
//        if (!statusDisplayMainProduct.contains(product.getStatus()) ) {
//            var res = getDetailTemporary(id);
//            if(!ObjectUtils.isEmpty(res)){
//                return res;
//            }
//        }

        ProductDetailResponse result = new ProductDetailResponse();
        result.setId(id);
        result.setStatus(product.getStatus().getValue());
        List<String> productMedia = new ArrayList<>();
        if (StringUtils.isNotBlank(product.getImages())) {
            productMedia
                    = Arrays.stream(product.getImages().split(","))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(product.getTrailerVideo())) {
            productMedia.addAll(
                    Arrays.stream(product.getTrailerVideo().split(","))
                            .filter(StringUtils::isNotBlank)
                            .toList()
            );
        }
        result.setBaseProductInfo(BaseProductInfo.builder()
                .categoryId(product.getCategoryId())
                .displayName(product.getName())
                .fullName(product.getOriginalProductName())
                .productDescription(product.getDescription())
                .productThumbnail(Arrays.stream(product.getImage().split(",")).toList())
                .productMedia(productMedia)
                .productCodeCustomer(product.getProductCodeCustomer())
                .build());
        result.setSellingProductInfo(builder()
                .minPurchaseQuantity(product.getMinOrderQuantity())
                .productPrice(product.getPrice())
                .priceType(product.getProductPriceType())
                .platformDiscountRate(product.getPlatformDiscountRate())
                .minPurchaseType(product.getQuoteType())
                .build());
        List<ProductSpecInfo> productSpecInfo;
        productSpecInfo = JsonMapperUtils.convertJsonToObject(product.getProductSpecInfo(),
                new TypeReference<>() {
                });
        if (DataUtils.isNullOrEmpty(productSpecInfo)) productSpecInfo = new ArrayList<>();
        result.setProductSpecInfo(new HashSet<>(productSpecInfo));
        List<StepPriceInfo> stepPriceInfos = new ArrayList<>();
        if (!DataUtils.isNullOrEmpty(product.getPriceRanges())) {
            stepPriceInfos = JsonMapperUtils.convertJsonToObject(product.getPriceRanges(),
                    new TypeReference<>() {
                    });
            if (ObjectUtils.isNotEmpty(stepPriceInfos))
                result.setStepPriceInfo(new HashSet<>(stepPriceInfos));
        }

//        List<SellerAttributeEntity> sellerAttributeEntities = sellerAttributeRepository.findAllByProductId(id, currentLanguage);
        List<SellerAttributeEntity> sellerAttributeEntities = sellerAttributeRepository.findAllByProductIdAndDeletedFalse(id);
//        List<SellerClassifyEntity> sellerClassifyEntities = sellerClassifyRepository.findAllByProductId(id, currentLanguage);
        List<SellerClassifyEntity> sellerClassifyEntities = sellerClassifyRepository.findAllByProductIdAndDeletedFalse(id);
        Map<Long, List<SellerClassifyEntity>> sellerClassifyMap = sellerClassifyEntities.stream()
                .collect(Collectors.groupingBy(SellerClassifyEntity::getSellerAttributeId));
        List<ProductAttributesDetailResponse> productAttributesDetailResponses = new ArrayList<>();
        for (SellerAttributeEntity i : sellerAttributeEntities) {
            LinkedHashMap<String, String> nameAndImage = new LinkedHashMap<>();
            ProductAttributesDetailResponse attributesDetailResponse = new ProductAttributesDetailResponse();
            attributesDetailResponse.setId(i.getId());
            attributesDetailResponse.setAttributeName(i.getAttributeName());
            attributesDetailResponse.setStt(i.getAttributeOrder());
            List<SellerClassifyEntity> classifyEntityList = sellerClassifyMap.get(i.getId());

            String sellerName = null;

            /* VIPO-3903: Upload E-Contract: replace merchant by merchant_new */
//            Optional<MerchantEntity> merchant = merchantRepository.findById(product.getMerchantId());
//            if (merchant.isEmpty()) {
//                throw new VipoNotFoundException();
//            }
            Optional<MerchantNewEntity> merchant = merchantNewRepository.findById(product.getMerchantId());
            if (merchant.isEmpty()) {
                throw new VipoNotFoundException();
            }

            classifyEntityList.forEach(j -> {
                if (ObjectUtils.isEmpty(j.getSellerName())) {
                    j.setSellerName(merchant.get().getName());
                }
                nameAndImage.put(j.getSellerName(), j.getSellerImage());
            });
            attributesDetailResponse.setNameAndImage(nameAndImage);

            /* Phase 5.5: Product Approval Fix */
            attributesDetailResponse.setSellerClassifyInfos(
                    classifyEntityList.stream()
                            .map(productMapper::toSellerClassifyInfo)
                            .sorted(
                                    Comparator.comparing(
                                            SellerClassifyInfo::getOrderClassify,
                                            Comparator.nullsFirst(Integer::compareTo)
                                    )
                            )
                            .collect(Collectors.toList())
            );

            productAttributesDetailResponses.add(attributesDetailResponse);
        }
        productAttributesDetailResponses.sort(Comparator.comparingInt(ProductAttributesDetailResponse::getStt));
        result.setProductAttributesInfo(new HashSet<>(productAttributesDetailResponses));
        List<ProductSellerSkuEntity> productSellerSkuEntities
                = productSellerSkuRepository.findAllByProductIdAndDeletedFalse(id);
        Map<Long, SellerClassifyEntity> sellerClassifyEntityMap = sellerClassifyEntities.stream()
                .collect(Collectors.toMap(SellerClassifyEntity::getId, Function.identity()));
        List<ManageProductCodeDetailResponse> manageProductCodeDetailResponses = new ArrayList<>();
        productSellerSkuEntities.forEach(i -> {
            ManageProductCodeDetailResponse manageProductCodeDetailResponse = new ManageProductCodeDetailResponse();
            List<Long> classifyIds = Arrays.stream(i.getSellerClassifyId().split(","))
                    .map(DataUtils::safeToLong).toList();

            /* Phase 5.5: Product Approval Fix */
            manageProductCodeDetailResponse.setSellerClassifyIds(classifyIds);

            ArrayList<String> attributes = new ArrayList<>();
            classifyIds.forEach(t -> attributes.add(sellerClassifyEntityMap.get(t).getSellerName()));
            manageProductCodeDetailResponse.setAttribute(attributes);
            manageProductCodeDetailResponse.setId(i.getId());
            manageProductCodeDetailResponse.setProductImage(i.getProductImage());
            manageProductCodeDetailResponse.setUnitPrice(i.getUnitPrice());
            manageProductCodeDetailResponse.setStock(i.getStock());
            manageProductCodeDetailResponse.setMinPurchase(i.getMinPurchase());
            manageProductCodeDetailResponse.setWeight(i.getWeight());
            manageProductCodeDetailResponse.setLength(DataUtils.safeToDouble(i.getLength()));
            manageProductCodeDetailResponse.setWidth(DataUtils.safeToDouble(i.getWidth()));
            manageProductCodeDetailResponse.setHeight(DataUtils.safeToDouble(i.getHeight()));
            manageProductCodeDetailResponse.setShippingFee(i.getShippingFee());
            manageProductCodeDetailResponse.setActiveStatus(i.getActiveStatus());
            manageProductCodeDetailResponses.add(manageProductCodeDetailResponse);
        });
        result.setManageProductCodeInfo(new HashSet<>(manageProductCodeDetailResponses));

        // Get certificates
        List<ProductCertificateEntity> productCertificateEntities = productCertificateEntityRepository.findCertificatesByProduct(id);
        List<ProductCertificateResponse> certificatesInfo = productCertificateMapper.toResponseList(productCertificateEntities);
        result.setCertificatesInfo(new HashSet<>(certificatesInfo));

        /* Phase 5.5: Product Approval Fix */
        /* Create a Map that link seller classify id to the list of sku ids */
        Map<Long, List<Long>> sellerClassifyIdToSkuIds = new HashMap<>();
        for (ManageProductCodeDetailResponse sku : result.getManageProductCodeInfo()) {
            List<Long> classifyIds = sku.getSellerClassifyIds();
            if (ObjectUtils.isEmpty(classifyIds))
                continue;
            classifyIds = classifyIds.stream().filter(ObjectUtils::isNotEmpty).toList();
            if (ObjectUtils.isEmpty(classifyIds))
                continue;
            for (Long classifyId : classifyIds) {
                sellerClassifyIdToSkuIds
                        .computeIfAbsent(classifyId, k -> new ArrayList<>())
                        .add(sku.getId());
            }
        }
        for (ProductAttributesDetailResponse attribute: result.getProductAttributesInfo()) {
            for (SellerClassifyInfo classifyResponse: attribute.getSellerClassifyInfos()) {
                List<Long> skuIds = sellerClassifyIdToSkuIds.get(classifyResponse.getId());
                if (ObjectUtils.isNotEmpty(skuIds))
                    classifyResponse.setSkuIds(skuIds);
            }
        }

        /* Phase 5.5: Product Approval Fix: block to create new skus when modifying the product
        *
        * 1. find all skuId that appears in the package product that is in the order process (the order package is not
        * cancelled by the user, the order that has been delivered to the customer.
        * 2. if there is any skus, block to remove or add new attributes. Moreover, block to modify the name of the
        * attributes. Provide messages for fe when blocking user.
        * 3. determine all seller_classify that contain the sku above, block to delete them and block to modify the name
        * of classify
        *  */
        List<Long> inOrderProcessSkuIds = packageProductRepository.findAllSkuIdsInOrderPackageByProductId(
                id, BuyerOrderStatus.ALLOW_TO_MODIFY_PRODUCT_BUYER_ORDER_STATUSES
        );
        if (ObjectUtils.isEmpty(inOrderProcessSkuIds))
            return result;

        /* block to remove or add new attributes */
        result.setAllowToAddOrRemoveAttribute(false);
        result.setBlockAddingOrRemovingAttributeMsg(Constants.NOT_ALLOW_TO_ADD_OR_REMOVE_ATTRIBUTES_MESSAGE);
        /* block to rename attribute */
        result.getProductAttributesInfo().forEach(
                attribute -> {
                    attribute.setAllowToRenameAttribute(false);
                    attribute.setBlockRenamingAttributeMsg(Constants.NOT_ALLOW_TO_RENAME_ATTRIBUTES_MESSAGE);
                    attribute.setAllowToRemoveAttribute(false);
                    attribute.setBlockRemovingAttributeMsg(Constants.NOT_ALLOW_TO_REMOVE_ATTRIBUTES_MESSAGE);
                }
        );
        /* block to rename classify if it has a sku that is contained in an order in the process */
        result.getProductAttributesInfo().stream()
                .flatMap(attribute -> attribute.getSellerClassifyInfos().stream())
                .forEach(
                        classify -> {
                            classify.getSkuIds().stream().filter(inOrderProcessSkuIds::contains).findAny().ifPresent(
                                    skuId ->{
                                        /* block to rename or remove the classify if it has a sku that is contained in
                                        an order in the process */
                                        classify.setAllowToRenameClassify(false);
                                        classify.setBlockRenamingClassifyMsg(Constants.NOT_ALLOW_TO_RENAME_CLASSIFY_MESSAGE);
                                        classify.setAllowToRemoveClassify(false);
                                        classify.setBlockRemovingClassify(Constants.NOT_ALLOW_TO_REMOVE_CLASSIFY_MESSAGE);
                                        classify.setAllowToChangeClassifyImg(false);
                                        classify.setBlockChangingClassifyImgMsg(Constants.NOT_ALLOW_TO_CHANGE_CLASSIFY_IMAGE);
                                    }
                            );
                        }
                );

        return result;
    }

    @Override
    public ProductDetailResponse getDetailTemporary(Long id) {
        ProductEntity product = findOne(id);
        ProductTemporaryEntity productTemporary = productTemporaryRepository.findByProductId(id).orElse(null);
        if(ObjectUtils.isEmpty(productTemporary)) return null;
        ProductDetailResponse result = new ProductDetailResponse();
        result.setId(id);
        result.setStatus(product.getStatus().getValue());
        List<String> productThumbnail = new ArrayList<>();
        productThumbnail.add(productTemporary.getImage());
        if (!DataUtils.isNullOrEmpty(productTemporary.getImages())) {
            productThumbnail.addAll(Arrays.stream(productTemporary.getImages().split(",")).toList());
        }
        result.setBaseProductInfo(BaseProductInfo.builder()
                .categoryId(productTemporary.getCategoryId())
                .displayName(productTemporary.getName())
                .fullName(productTemporary.getOriginalProductName())
                .productDescription(productTemporary.getDescription())
                .productMedia(Arrays.stream(productTemporary.getTrailerVideo().split(",")).collect(Collectors.toList()))
                .productThumbnail(productThumbnail)
                .productCodeCustomer(productTemporary.getProductCodeCustomer())
                .build());
        result.setSellingProductInfo(builder()
                .minPurchaseQuantity(productTemporary.getMinOrderQuantity())
                .productPrice(productTemporary.getPrice())
                .priceType(productTemporary.getProductPriceType())
                .platformDiscountRate(productTemporary.getPlatformDiscountRate())
                .minPurchaseType(productTemporary.getQuoteType())
                .build());
        List<ProductSpecInfo> productSpecInfo;
        productSpecInfo = JsonMapperUtils.convertJsonToObject(productTemporary.getProductSpecInfo(),
                new TypeReference<>() {
                });
        if (DataUtils.isNullOrEmpty(productSpecInfo)) productSpecInfo = new ArrayList<>();
        result.setProductSpecInfo(new HashSet<>(productSpecInfo));
        List<StepPriceInfo> stepPriceInfos;
        stepPriceInfos = JsonMapperUtils.convertJsonToObject(productTemporary.getPriceRanges(),
                new TypeReference<>() {
                });
        if (DataUtils.isNullOrEmpty(stepPriceInfos)) stepPriceInfos = new ArrayList<>();
        result.setStepPriceInfo(new HashSet<>(stepPriceInfos));
        List<SellerAttributeTemporaryEntity> sellerAttributeEntities
                = sellerAttributeTemporaryRepository.findAllByProductTemporaryId(productTemporary.getId());
        List<SellerClassifyTemporaryEntity> sellerClassifyEntities
                = sellerClassifyTemporaryRepository.findAllByProductTemporaryId(productTemporary.getId());
        Map<Long, List<SellerClassifyTemporaryEntity>> sellerClassifyMap = sellerClassifyEntities.stream()
                .collect(Collectors.groupingBy(SellerClassifyTemporaryEntity::getSellerAttributeTemporaryId));
        List<ProductAttributesDetailResponse> productAttributesDetailResponses = new ArrayList<>();
        for (SellerAttributeTemporaryEntity i : sellerAttributeEntities) {
            LinkedHashMap<String, String> nameAndImage = new LinkedHashMap<>();
            ProductAttributesDetailResponse attributesDetailResponse = new ProductAttributesDetailResponse();
            attributesDetailResponse.setId(i.getId());
            attributesDetailResponse.setAttributeName(i.getAttributeName());
            attributesDetailResponse.setStt(i.getAttributeOrder());
            List<SellerClassifyTemporaryEntity> classifyEntityList = sellerClassifyMap.get(i.getId());
            classifyEntityList.forEach(j -> nameAndImage.put(j.getSellerName(), j.getSellerImage()));
            attributesDetailResponse.setNameAndImage(nameAndImage);
            productAttributesDetailResponses.add(attributesDetailResponse);
        }
        result.setProductAttributesInfo(new HashSet<>(productAttributesDetailResponses));
        List<ProductSellerSkuTemporaryEntity> productSellerSkuEntities = productSellerSkuTemporaryRepository
                .findAllByProductTemporaryId(productTemporary.getId());
        Map<Long, SellerClassifyTemporaryEntity> sellerClassifyEntityMap = sellerClassifyEntities.stream()
                .collect(Collectors.toMap(SellerClassifyTemporaryEntity::getId, Function.identity()));
        List<ManageProductCodeDetailResponse> manageProductCodeDetailResponses = new ArrayList<>();
        productSellerSkuEntities.forEach(i -> {
            ManageProductCodeDetailResponse manageProductCodeDetailResponse = new ManageProductCodeDetailResponse();
            List<Long> classifyIds = Arrays.stream(i.getSellerClassifyTemporaryId().split(","))
                    .map(DataUtils::safeToLong).collect(Collectors.toList());
            ArrayList<String> attributes = new ArrayList<>();
            classifyIds.forEach(t -> attributes.add(sellerClassifyEntityMap.get(t).getSellerName()));
            manageProductCodeDetailResponse.setAttribute(attributes);
            manageProductCodeDetailResponse.setId(i.getId());
            manageProductCodeDetailResponse.setProductImage(i.getProductImage());
            manageProductCodeDetailResponse.setUnitPrice(i.getUnitPrice());
            manageProductCodeDetailResponse.setStock(i.getStock());
            manageProductCodeDetailResponse.setMinPurchase(i.getMinPurchase());
            manageProductCodeDetailResponse.setWeight(i.getWeight());
            manageProductCodeDetailResponse.setLength(DataUtils.safeToDouble(i.getLength()));
            manageProductCodeDetailResponse.setWidth(DataUtils.safeToDouble(i.getWidth()));
            manageProductCodeDetailResponse.setHeight(DataUtils.safeToDouble(i.getHeight()));
            manageProductCodeDetailResponse.setShippingFee(i.getShippingFee());
            manageProductCodeDetailResponse.setActiveStatus(i.getActiveStatus());
            manageProductCodeDetailResponses.add(manageProductCodeDetailResponse);
        });
        result.setManageProductCodeInfo(new HashSet<>(manageProductCodeDetailResponses));
        return result;
    }

    @Override
    public String deleteProduct(PauseStopSellingProductReq request) {
        ProductEntity product = findOne(request.getId());
        if (product.getStatus().equals(ProductStatus.LOCKED)) {
            throw new VipoBusinessException(ErrorCodeResponse.INVALID_PRODUCT_LOCKED);
        }
        List<ProductReasonType> reasonTypeList = List.of(
                ProductReasonType.WRONG_SPEC_INFO,
                ProductReasonType.DUPLICATE_PRODUCT,
                ProductReasonType.BUSINESS_DISCONTINUED,
                ProductReasonType.MARKET_INCOMPATIBILITY,
                ProductReasonType.BUSINESS_STRATEGY_CHANGE,
                ProductReasonType.MANUFACTURER_REQUEST,
                ProductReasonType.OTHER_DELETE);
        checkValidType(reasonTypeList, ProductReasonType.fromValue(request.getReasonType()));
        if (orderPackageRepository.existsByProductIdAndPaymentTimeNotNull(request.getId())) {
            ProductReasonType productReasonType = ProductReasonType.fromValue(request.getReasonType());
            String reason = null;
            product.setReasonType(productReasonType);
            if (productReasonType.equals(ProductReasonType.OTHER_DELETE)) {
                reason = request.getReason();
            }
            product.setReason(reason);
            product.setStatus(ProductStatus.ADJUST_PENDING);
        } else {
            product.setActivated(0);
            product.setIsDeleted(1);
            product.setStatus(ProductStatus.STOPPED);
        }
        repo.save(product);
        return BaseExceptionConstant.SUCCESS_DESCRIPTION;
    }

    @Override
    public String requestReview(Long id) {
        ProductEntity product = findOne(id);
        List<ProductStatus> lstStatusAvailable = List.of(ProductStatus.REJECT,
                ProductStatus.ADJUST_REJECT);
        checkValidStatus(lstStatusAvailable, product.getStatus());
        if (product.getStatus().equals(ProductStatus.REJECT)) {
            product.setStatus(ProductStatus.PENDING);
        }
        if (product.getStatus().equals(ProductStatus.ADJUST_REJECT)) {
            product.setStatus(ProductStatus.ADJUST_PENDING);
        }
        repo.save(product);
        return BaseExceptionConstant.SUCCESS_DESCRIPTION;
    }

    @Override
    public String cancelApprovalRequest(Long id) {
        ProductEntity product = findOne(id);
        List<ProductStatus> lstStatusAvailable = List.of(ProductStatus.REJECT,
                ProductStatus.ADJUST_REJECT,
                ProductStatus.PENDING,
                ProductStatus.ADJUST_PENDING);
        checkValidStatus(lstStatusAvailable, product.getStatus());
        if (product.getStatus().equals(ProductStatus.REJECT)
                || product.getStatus().equals(ProductStatus.PENDING)) {
            product.setStatus(ProductStatus.NEW);
        }
        if (product.getStatus().equals(ProductStatus.ADJUST_REJECT)
                || product.getStatus().equals(ProductStatus.ADJUST_PENDING)) {
            product.setStatus(ProductStatus.SELLING);
        }
        repo.save(product);
        return BaseExceptionConstant.SUCCESS_DESCRIPTION;
    }

    @Override
    public String cancelDraft(Long id) {
        ProductEntity product = findOne(id);
        List<ProductStatus> lstStatusAvailable = List.of(ProductStatus.NEW,
                ProductStatus.EDITING);
        checkValidStatus(lstStatusAvailable, product.getStatus());
        if (product.getStatus().equals(ProductStatus.NEW)) {
            product.setStatus(ProductStatus.CANCELED);
        }
        if (product.getStatus().equals(ProductStatus.EDITING)) {
            product.setStatus(ProductStatus.SELLING);
        }
        repo.save(product);
        return BaseExceptionConstant.SUCCESS_DESCRIPTION;
    }

    /**
     * Sản phẩm đang ở trạng thái "đang bán" và "đã duyệt kiểm tạo", ấn "sửa thông tin cơ bản" sẽ gọi Hàm này
     *
     * This method is called when a product in the "selling" and "approved" status
     * needs to update its basic information. It updates information like category,
     * name, image, and description. If not in draft mode, it changes the product's
     * status to "adjust pending".
     *
     * @param request The request object containing the updated base information for the product.
     * @param isDraft A boolean indicating if the product is in draft status or not.
     * @return A success description message indicating the result of the operation.
     */
    @Override
    public String updateBaseInfo(UpdateBaseInfoProductReq request, boolean isDraft) {

        // Find the existing product entity by ID from the database.
        ProductEntity product = findOne(request.getId());

        // Validate that the category ID exists in the system.
        findCategory(Long.valueOf(request.getCategoryId()));

        // Create a list of statuses that are valid for updating base information (approved and selling).
        List<ProductStatus> lstStatusAvailable = List.of(ProductStatus.APPROVED, ProductStatus.SELLING);

        // Check if the current status of the product is valid for updating.
        checkValidStatus(lstStatusAvailable, product.getStatus());

        // Attempt to find an existing temporary product entity; if none exists, create a new one.
//        ProductTemporaryEntity productTemporary =
//                productTemporaryRepository.findByProductId(request.getId())
//                        .orElseGet(() -> productMapper.toProductTemporaryEntity(product));
        ProductTemporaryEntity productTemporary =
                productTemporaryRepository.findByProductId(request.getId())
                        .orElse(null);
        if (ObjectUtils.isEmpty(productTemporary))
            productTemporary = productMapper.toProductTemporaryEntity(product);
        else
            productMapper.toProductTemporaryEntity(product, productTemporary);

        // Set the updated product information in the temporary entity.
        productTemporary.setCategoryId(request.getCategoryId());
        productTemporary.setName(request.getDisplayName());
        productTemporary.setOriginalProductName(request.getFullName());
//        productTemporary.setImage(request.getProductThumbnail().get(0)); // Set the first thumbnail image as the main image.
//        productTemporary.setImages(String.join(",", request.getProductThumbnail())); // Concatenate all thumbnails into a comma-separated string.
        //            productTemporaryEntity.setImage(baseProductInfo.getProductThumbnail().get(0));
        productTemporary.setImage(request.getProductThumbnail().stream().distinct().collect(Collectors.joining(",")));
        if (ObjectUtils.isNotEmpty(request.getProductMedia())) {
            Set<String> listImage = new HashSet<>();
            Set<String> listVideo = new HashSet<>();
            for (String link : request.getProductMedia()) {
                if (link.toLowerCase().endsWith(".mp4")) {
                    listVideo.add(link);
                } else {
                    listImage.add(link);
                }
            }
            productTemporary.setImages(String.join(",", listImage));
            productTemporary.setTrailerVideo(String.join(",", listVideo));
        }

        productTemporary.setDescription(request.getProductDescription()); // Set the product description.
        productTemporary.setProductId(request.getId()); // Link the temporary entity to the original product by its ID.

        productTemporary.setProductPriceType(product.getProductPriceType());
        // Save the updated temporary product information in the repository.
        productTemporaryRepository.save(productTemporary);

        // If the product is not in draft mode, update its status to "adjust pending".
        if (!isDraft) {
            product.setStatus(ProductStatus.ADJUST_PENDING);
            repo.save(product); // Save the product entity with the updated status.
        }

        // Return a success message indicating the operation was successful.

        /*Product certificate*/
        saveCertificates(request.getCertificatesInfo(), request.getId(), product, productTemporary);

        // Return a success message indicating the operation was successful.
        return BaseExceptionConstant.SUCCESS_DESCRIPTION;
    }

    @Override
    public String updateSellingInfo(UpdateSellingInfoProductReq request, boolean isDraft) {
        String currentLanguage = vipoLanguageUtils.getCurrentLanguage();
        ProductEntity product = findOne(request.getId());
        List<ProductStatus> lstStatusAvailable = List.of(ProductStatus.APPROVED,
                ProductStatus.SELLING);
        checkValidStatus(lstStatusAvailable, product.getStatus());
        ProductCreateUpdateRequest productCreateUpdateRequest = new ProductCreateUpdateRequest();
        productCreateUpdateRequest.setId(request.getId());

        List<String> productThumbnail = new ArrayList<>();
        productThumbnail.add(product.getImage());
        if (!DataUtils.isNullOrEmpty(product.getImages())) {
            productThumbnail.addAll(Arrays.stream(product.getImages().split(",")).toList());
        }
        productCreateUpdateRequest.setBaseProductInfo(BaseProductInfo.builder()
                .categoryId(product.getCategoryId())
                .displayName(product.getName())
                .fullName(product.getOriginalProductName())
                .productDescription(product.getDescription())
                .productMedia(DataUtils.isNullOrEmpty(product.getTrailerVideo()) ? null
                        : Arrays.stream(product.getTrailerVideo().split(",")).collect(Collectors.toList()))
                .productThumbnail(productThumbnail)
                .build());
        productCreateUpdateRequest.setSellingProductInfo(builder()
                .minPurchaseQuantity(request.getMinPurchaseQuantity())
                .productPrice(request.getProductPrice())
                .priceType(request.getPriceType())
                .platformDiscountRate(request.getPlatformDiscountRate())
                .minPurchaseType(request.getMinPurchaseType())
                .build());
        List<ProductSpecInfo> productSpecInfo;
        productSpecInfo = JsonMapperUtils.convertJsonToObject(product.getProductSpecInfo(),
                new TypeReference<>() {
                });
        if (DataUtils.isNullOrEmpty(productSpecInfo)) productSpecInfo = new ArrayList<>();
        productCreateUpdateRequest.setProductSpecInfo(new HashSet<>(productSpecInfo));
        ProductStatus productStatus;
        if (!isDraft) {
            productStatus = ProductStatus.ADJUST_PENDING;
        } else {
            productStatus = product.getStatus();
        }
        List<SellerAttributeEntity> attributeEntities = sellerAttributeRepository.findAllByProductId(product.getId(), currentLanguage);
        List<SellerClassifyEntity> classifyEntities = sellerClassifyRepository.findAllByProductId(product.getId(), currentLanguage);
        Map<Long, LinkedHashMap<String, String>> linkedHashMapMap = mapSellerClassifyEntities(classifyEntities);
        Set<ProductAttributesInfo> productAttributesInfos = new HashSet<>();
        attributeEntities.forEach(i -> {
            productAttributesInfos.add(ProductAttributesInfo.builder()
                    .stt(i.getAttributeOrder())
                    .id(i.getId())
                    .attributeName(i.getAttributeName())
                    .nameAndImage(linkedHashMapMap.get(i.getId()))
                    .build());
        });
        productCreateUpdateRequest.setProductAttributesInfo(productAttributesInfos);
        List<ProductSellerSkuEntity> productSellerSkuEntities = productSellerSkuRepository.findAllByProductIdAndDeletedFalse(product.getId());
        productCreateUpdateRequest.setManageProductCodeInfo(
                new HashSet<>(mapToManageProductCodeInfoList(productSellerSkuEntities, classifyEntities)));
        return createOrUpdateProduct(productCreateUpdateRequest, product, productStatus, isDraft, true);
    }

    /**
     * This method is called when a product in the "selling" and "approved" status
     * needs to update its specifications ("sửa thông số").
     * It updates the product's specification
     * information. If not in draft mode, it changes the product's status to "adjust pending".
     *
     * @param request The request object containing the updated specification information for the product.
     * @param isDraft A boolean indicating if the product is in draft status or not.
     * @return A success description message indicating the result of the operation.
     */
    @Override
    public String updateSpecInfo(UpdateSpecInfoProductReq request, boolean isDraft) {

        // Find the existing product entity by ID from the database.
        ProductEntity product = findOne(request.getId());

        // Create a list of statuses that are valid for updating specification information (approved and selling).
        List<ProductStatus> lstStatusAvailable = List.of(ProductStatus.APPROVED, ProductStatus.SELLING);

        // Check if the current status of the product is valid for updating.
        checkValidStatus(lstStatusAvailable, product.getStatus());

        // Attempt to find an existing temporary product entity by product ID.
        // If none exists, create a new one.
//        ProductTemporaryEntity productTemporary =
//                productTemporaryRepository.findByProductId(request.getId())
//                        .orElseGet(() -> productMapper.toProductTemporaryEntity(product));

        ProductTemporaryEntity productTemporary =
                productTemporaryRepository.findByProductId(request.getId())
                        .orElse(null);
        if (ObjectUtils.isEmpty(productTemporary))
            productTemporary = productMapper.toProductTemporaryEntity(product);
        else
            productMapper.toProductTemporaryEntity(product, productTemporary);

        // Set the product ID, category ID, and specification info in the temporary entity.
        productTemporary.setProductId(product.getId());
        productTemporary.setCategoryId(product.getCategoryId());

        // Convert the product specification information into a JSON string and set it in the temporary entity.
        productTemporary.setProductSpecInfo(JsonMapperUtils.writeValueAsString(request.getProductSpecInfo()));

        // Set the original product name in the temporary entity.
        productTemporary.setOriginalProductName(product.getOriginalProductName());

        // Save the updated temporary entity in the database.
        ProductTemporaryEntity savedProductTemporary = productTemporaryRepository.save(productTemporary);

        // If the product is not in draft mode, update its status to "adjust pending".
        if (!isDraft) {
            product.setStatus(ProductStatus.ADJUST_PENDING);
            repo.save(product);
        }
        /*Product Certificate*/
        updateCertificates(request.getId(), savedProductTemporary.getId());

        // Return a success description message.
        return BaseExceptionConstant.SUCCESS_DESCRIPTION;
    }


    /**
     * isDraft = true means that "Sửa thông tin phân loại" for "sản phẩm nháp"
     *
     * Hàm đang hơi lằng nhàng nên chưa tác động tạo docs và comments.
     * tuy nhiên hiện tại đã kiểm tra và đang tác động chỉ lên bảng nháp chứ k phải bảng chính
     */
    @Override
    @Transactional
    public String updateAttribute(UpdateAttributeProductReq request, boolean isDraft) {
        ProductEntity product
                = repo.findByIdAndMerchantIdAndIsDeleted(
                        request.getId(), getCurrentUser().getId(), Constants.IS_NOT_DELETED
        );

        if (ObjectUtils.isEmpty(product))
            throw new VipoNotFoundException("Không tìm thấy sản phẩm!");

        /* phase 5.5: product approval fix: validate the request */
        if (Boolean.TRUE.equals(Constants.isNewProductApproval.get()))
            validateTheUpdateAttributeRequestInCaseSkuIncludedInAnOrder(
                    product, request.getManageProductCodeInfo(), request.getProductAttributesInfo()
            );

        List<ProductStatus> lstStatusAvailable = List.of(ProductStatus.APPROVED,
                ProductStatus.SELLING);
        checkValidStatus(lstStatusAvailable, product.getStatus());
        ProductStatus productStatus;
        if (!isDraft) {
            productStatus = ProductStatus.ADJUST_PENDING;
        } else {
            productStatus = product.getStatus();
        }
        ProductCreateUpdateRequest productCreateUpdateRequest = new ProductCreateUpdateRequest();
        productCreateUpdateRequest.setId(request.getId());
        productCreateUpdateRequest.setManageProductCodeInfo(request.getManageProductCodeInfo());
        productCreateUpdateRequest.setStepPriceInfo(request.getStepPriceInfo());
        productCreateUpdateRequest.setProductAttributesInfo(request.getProductAttributesInfo());
        List<String> productThumbnail = new ArrayList<>();
        productThumbnail.add(product.getImage());
        if (!DataUtils.isNullOrEmpty(product.getImages())) {
            productThumbnail.addAll(Arrays.stream(product.getImages().split(",")).toList());
        }
        productCreateUpdateRequest.setBaseProductInfo(BaseProductInfo.builder()
                .categoryId(product.getCategoryId())
                .displayName(product.getName())
                .fullName(product.getOriginalProductName())
                .productDescription(product.getDescription())
                .productMedia(DataUtils.isNullOrEmpty(product.getTrailerVideo()) ? null
                        : Arrays.stream(product.getTrailerVideo().split(",")).collect(Collectors.toList()))
                .productThumbnail(productThumbnail)
                .build());
        productCreateUpdateRequest.setSellingProductInfo(builder()
                .minPurchaseQuantity(product.getMinOrderQuantity())
                .productPrice(product.getPrice())
                .priceType(product.getProductPriceType())
                .platformDiscountRate(product.getPlatformDiscountRate())
                .minPurchaseType(product.getQuoteType())
                .build());
        List<ProductSpecInfo> productSpecInfo;
        productSpecInfo = JsonMapperUtils.convertJsonToObject(product.getProductSpecInfo(),
                new TypeReference<>() {
                });
        if (DataUtils.isNullOrEmpty(productSpecInfo)) productSpecInfo = new ArrayList<>();
        productCreateUpdateRequest.setProductSpecInfo(new HashSet<>(productSpecInfo));

        /*Product Certificate*/
        productCreateUpdateRequest.setCertificatesInfo(getCertificates(request.getId()));
        return createOrUpdateProduct(productCreateUpdateRequest, product, productStatus, isDraft, true);
    }

    /**
     * Validate the update attribute request for case that there is an order_package contains the sku
     */
    private void validateTheUpdateAttributeRequestInCaseSkuIncludedInAnOrder(
            ProductEntity product,
            Set<ManageProductCodeInfo> manageProductCodeInfo,
            Set<ProductAttributesInfo> productAttributesInfo

    ) {
        Long productId = product.getId();

        /* find all sku ids that contains in the order package */
        List<Long> inOrderProcessSkuIds = packageProductRepository.findAllSkuIdsInOrderPackageByProductId(
                productId, BuyerOrderStatus.ALLOW_TO_MODIFY_PRODUCT_BUYER_ORDER_STATUSES
        );

        if (ObjectUtils.isEmpty(inOrderProcessSkuIds))  //if there is no sku contains in the order package, no need to validate
            return;

        /* get all attribute, classify and sku  */
        List<SellerAttributeEntity> sellerAttributeEntities
                = sellerAttributeRepository.findAllByProductIdAndDeletedFalse(productId);
        List<SellerClassifyEntity> sellerClassifyEntities
                = sellerClassifyRepository.findAllByProductIdAndDeletedFalse(productId);
        List<ProductSellerSkuEntity> productSellerSkuEntities
                = productSellerSkuRepository.findAllByProductIdAndDeletedFalse(productId);
        /* check if all the provided attribute id, classify id, and sku id in the request are stored in the database */
        List<Long> sellerAttributeIds = sellerAttributeEntities.stream().map(SellerAttributeEntity::getId).toList();
        if (
                productAttributesInfo.stream()
                        .map(ProductAttributesInfo::getId)
                        .filter(ObjectUtils::isNotEmpty)
                        .anyMatch(requestAttributeId -> !sellerAttributeIds.contains(requestAttributeId))
        )
            throw new VipoInvalidDataRequestException("id của thuộc tính không hợp lệ");
        List<Long> sellerClassifyIds = sellerClassifyEntities.stream().map(SellerClassifyEntity::getId).toList();
        if (
                productAttributesInfo.stream()
                        .flatMap(attributeRequest -> attributeRequest.getSellerClassifyInfos().stream())
                        .map(SellerClassifyInfo::getId)
                        .filter(ObjectUtils::isNotEmpty)
                        .anyMatch(requestAttributeId -> !sellerClassifyIds.contains(requestAttributeId))
        )
            throw new VipoInvalidDataRequestException("id của giá trị thuộc tính không hợp lệ");
        List<Long> skuIds = productSellerSkuEntities.stream().map(ProductSellerSkuEntity::getId).toList();
        if (
                manageProductCodeInfo.stream()
                        .map(ManageProductCodeInfo::getId)
                        .filter(ObjectUtils::isNotEmpty)
                        .anyMatch(requestAttributeId -> !skuIds.contains(requestAttributeId))
        )
            throw new VipoInvalidDataRequestException("id của giá trị thuộc tính không hợp lệ");

        /* find all in-order sku */
        List<ProductSellerSkuEntity> inOrderProductSellerSkuEntities
                = productSellerSkuEntities.stream().filter(sku -> inOrderProcessSkuIds.contains(sku.getId())).toList();
        //check if all in-order sku ids are included in the request
       List<Long> inRequestSkuIds
               = manageProductCodeInfo.stream()
               .map(ManageProductCodeInfo::getId)
               .filter(ObjectUtils::isNotEmpty)
               .toList();
       if(
               inOrderProcessSkuIds.stream().anyMatch(inOrderSkuId -> !inRequestSkuIds.contains(inOrderSkuId))
       )
           throw new VipoInvalidDataRequestException(Constants.NOT_ALLOW_TO_REMOVE_SKU_MESSAGE);

        /* find all classify that has in-order sku */
        Set<Long> inOrderClassifyIds
                = inOrderProductSellerSkuEntities.stream()
                .flatMap(sku -> NumUtils.convertStringToLongList(sku.getSellerClassifyId()).stream())
                .collect(Collectors.toSet());

        /* validate the attribute and classify */
        /* check if the attribute is all included */
        List<SellerClassifyEntity> inOrderClassifyEntity
                = sellerClassifyEntities.stream()
                .filter(classifyEntity -> inOrderClassifyIds.contains(classifyEntity.getId()))
                .toList();
        Set<Long> inOrderAttributeId
                = inOrderClassifyEntity.stream().map(SellerClassifyEntity::getSellerAttributeId).collect(Collectors.toSet());
        Set<Long> inRequestAttributeIds
                = productAttributesInfo.stream()
                .map(ProductAttributesInfo::getId)
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toSet());
        if (
                !inOrderAttributeId.equals(inRequestAttributeIds)
        )
            throw new VipoInvalidDataRequestException(Constants.NOT_ALLOW_TO_ADD_OR_REMOVE_ATTRIBUTES_MESSAGE);

        // check if the classify ids in the request contains all in-order classify Ids
        List<SellerClassifyInfo> updateClassifyRequest
                = productAttributesInfo.stream().flatMap(attribute -> attribute.getSellerClassifyInfos().stream()).toList();
        List<Long> requestedSellerClassifyIds
                = updateClassifyRequest.stream().map(SellerClassifyInfo::getId).filter(ObjectUtils::isNotEmpty).toList();
        if (
                inOrderClassifyIds.stream()
                        .anyMatch(inOrderClassifyId -> !requestedSellerClassifyIds.contains(inOrderClassifyId))
        )
            throw new VipoInvalidDataRequestException(Constants.NOT_ALLOW_TO_REMOVE_CLASSIFY_MESSAGE);


        /* for new classify and sku created, make sure they have temp code, so we can connect them later */
        List<SellerClassifyInfo> newClassifies
                = updateClassifyRequest.stream()
                .filter(sellerClassifyInfo -> ObjectUtils.isEmpty(sellerClassifyInfo.getId()))
                .toList();
        if (
                newClassifies.stream().anyMatch(
                                sellerClassifyInfo -> ObjectUtils.isEmpty(sellerClassifyInfo.getTempId())
                        )
        )
            throw new VipoInvalidDataRequestException("Với những giá trị thuộc tính thêm mới, hãy cung cấp tempCode để có thể reference tới");
        List<ManageProductCodeInfo> newSkus
                = manageProductCodeInfo.stream().filter(requestSku -> ObjectUtils.isEmpty(requestSku.getId())).toList();
        if (
                newSkus.stream()
                        .anyMatch(
                                requestSku
                                        -> ObjectUtils.isEmpty(requestSku.getTempId())
                                        && ObjectUtils.isEmpty(requestSku.getSellerClassifyTempIds())
                        )
        )
            throw new VipoInvalidDataRequestException(
                    "Với những sku thêm mới, hãy cung cấp tempCode của những giá trị thuộc tính mới để có thể reference tới"
            );
        //check if the ref in new skus are appeared in the new classify
        List<String> classifyTempIds
                = newClassifies.stream()
                .map(SellerClassifyInfo::getTempId)
                .filter(StringUtils::isNotBlank)
                .toList();
        if (
                newSkus.stream()
                        .flatMap(newSku -> newSku.getSellerClassifyTempIds().stream())
                        .anyMatch(classifyTempId -> !classifyTempIds.contains(classifyTempId))
        )
            throw new VipoInvalidDataRequestException("Các sku không chứa ref hợp lệ tới những giá trị thuộc tính mới");

        List<String> newSkuTempCodes
                = newSkus.stream()
                .map(ManageProductCodeInfo::getTempId).filter(StringUtils::isNotBlank).toList();

        if (
                newClassifies.stream().flatMap(newClassify -> newClassify.getSkuTempIds().stream())
                        .anyMatch(newSkuTempCodes::contains)
        )
            throw new VipoInvalidDataRequestException("Các giá trị thuộc tính không chứa ref hợp lệ với các sku mới");

        /* block renaming attribute and classify name and image */
        Map<Long, SellerAttributeEntity> idToSellerAttributeEntity
                = sellerAttributeEntities.stream().collect(Collectors.toMap(
                SellerAttributeEntity::getId,
                Function.identity(),
                (existing, replacement) -> existing
        ));
        productAttributesInfo.stream()
                .filter(sellerAttributesInfo -> ObjectUtils.isNotEmpty(sellerAttributesInfo.getId()))
                .forEach(
                sellerAttributesInfo -> {
                    SellerAttributeEntity sellerAttributeEntity = idToSellerAttributeEntity.get(sellerAttributesInfo.getId());
                    if (!sellerAttributesInfo.getAttributeName().equals(sellerAttributeEntity.getAttributeName()))
                        throw new VipoInvalidDataRequestException(Constants.NOT_ALLOW_TO_RENAME_ATTRIBUTES_MESSAGE);
                }
        );

        Map<Long, SellerClassifyEntity> idToClassifyEntity
                = sellerClassifyEntities.stream().collect(Collectors.toMap(
                SellerClassifyEntity::getId,
                Function.identity(),
                (existing, replacement) -> existing
        ));

        productAttributesInfo.stream()
                .flatMap(attributeInfo -> attributeInfo.getSellerClassifyInfos().stream())
                .filter(classifyInfo
                        -> ObjectUtils.isNotEmpty(classifyInfo.getId())
                        && inOrderClassifyIds.contains(classifyInfo.getId())
                ).forEach(
                        sellerClassifyInfo -> {
                            SellerClassifyEntity sellerClassifyEntity = idToClassifyEntity.get(sellerClassifyInfo.getId());
                            if (!sellerClassifyInfo.getName().equals(sellerClassifyEntity.getSellerName()))
                                throw new VipoInvalidDataRequestException(Constants.NOT_ALLOW_TO_RENAME_CLASSIFY_MESSAGE);
                            if (
                                    StringUtils.isNotBlank(sellerClassifyEntity.getSellerImage())
                                    && StringUtils.isNotBlank(sellerClassifyInfo.getImage())
                                    && !sellerClassifyInfo.getImage().equals(sellerClassifyEntity.getSellerImage())
                            )
                                throw new VipoInvalidDataRequestException(Constants.NOT_ALLOW_TO_CHANGE_CLASSIFY_IMAGE);
                        }
                );
        /* in order sku is not allowed to change image  */
        Map<Long, ProductSellerSkuEntity> idToInOrderProductSellerSku
                = inOrderProductSellerSkuEntities.stream()
                .collect(Collectors.toMap(
                        ProductSellerSkuEntity::getId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        manageProductCodeInfo.stream().filter(
                skuInfo -> ObjectUtils.isNotEmpty(skuInfo.getId()) && inOrderProcessSkuIds.contains(skuInfo.getId())
        ).forEach(
                skuInfo -> {
                    ProductSellerSkuEntity productSellerSkuEntity = idToInOrderProductSellerSku.get(skuInfo.getId());
                    if (!skuInfo.getProductImage().equals(productSellerSkuEntity.getProductImage()))
                        throw new VipoInvalidDataRequestException(Constants.NOT_ALLOW_TO_CHANGE_SKU_IMAGE);
                }
        );

    }

    @Override
    @Transactional
    public String continueSelling(ContinueSellingProductReq request) {
        request.validate();
        ProductEntity product = findOne(request.getId());
        List<ProductStatus> lstValidStatus = List.of(ProductStatus.PAUSED, ProductStatus.STOPPED);
        checkValidStatus(lstValidStatus, product.getStatus());
        LinkedHashMap<Long, Long> mapIdStock = request.getMapStock();
        List<ProductSellerSkuEntity> lstEntities
                = productSellerSkuRepository.findAllByProductIdAndDeletedFalse(request.getId());
        for (ProductSellerSkuEntity i : lstEntities) {
            if (!DataUtils.isNullOrEmpty(mapIdStock.get(i.getId())))
                i.setStock(mapIdStock.get(i.getId()));
        }
//        if (product.getMinOrderQuantity() > lstEntities.stream()
//                .mapToLong(ProductSellerSkuEntity::getStock)
//                .sum()) {
//            throw new VipoBusinessException(ErrorCodeResponse.INVALID_STOCK_MIN_QUANTITY);
//        }
        productSellerSkuRepository.saveAll(lstEntities);
        product.setStatus(ProductStatus.SELLING);
        product.setReasonType(null);
        product.setReason(null);
        repo.save(product);
        return BaseExceptionConstant.SUCCESS_DESCRIPTION;
    }

    @Override
    @Transactional
    public String updateStock(ContinueSellingProductReq request) {
        request.validate();
        ProductEntity product = findOne(request.getId());
        List<ProductStatus> lstValidStatus = List.of(ProductStatus.SELLING, ProductStatus.APPROVED);
        checkValidStatus(lstValidStatus, product.getStatus());
        LinkedHashMap<Long, Long> mapIdStock = request.getMapStock();
        List<ProductSellerSkuEntity> lstEntities
                = productSellerSkuRepository.findAllByProductIdAndDeletedFalse(request.getId());
        for (ProductSellerSkuEntity i : lstEntities) {
            if (!DataUtils.isNullOrEmpty(mapIdStock.get(i.getId())))
                i.setStock(mapIdStock.get(i.getId()));
        }
//        if (product.getMinOrderQuantity() > lstEntities.stream()
//                .mapToLong(ProductSellerSkuEntity::getStock)
//                .sum()) {
//            throw new VipoBusinessException(ErrorCodeResponse.INVALID_STOCK_MIN_QUANTITY);
//        }
        productSellerSkuRepository.saveAll(lstEntities);
        product.setUpdateTime(DateUtils.convertMilTimeToSecond(Instant.now().toEpochMilli()));
        repo.save(product);
        return BaseExceptionConstant.SUCCESS_DESCRIPTION;
    }

    @Override
    @Transactional
    public String pauseSelling(PauseStopSellingProductReq request) {
        request.validate();
        List<ProductReasonType> lstValidStatus = List.of(
                ProductReasonType.TEMP_OUT_OF_STOCK,
                ProductReasonType.MAINTENANCE,
                ProductReasonType.PRICE_ADJUSTMENT,
                ProductReasonType.SEASONAL_SUSPENSION,
                ProductReasonType.SUPPLIER_QUALITY_ISSUE_PAUSE,
                ProductReasonType.OTHER_PAUSE
        );
        checkValidType(lstValidStatus, ProductReasonType.fromValue(request.getReasonType()));
        ProductReasonType productReasonType = ProductReasonType.fromValue(request.getReasonType());
        ProductEntity product = findOne(request.getId());
        List<ProductStatus> lstStatusValid = List.of(
                ProductStatus.SELLING
        );
        checkValidStatus(lstStatusValid, product.getStatus());
        String reason = null;
        product.setReasonType(productReasonType);
        if (productReasonType.equals(ProductReasonType.OTHER_PAUSE)) {
            reason = request.getReason();
        }
        product.setReason(reason);
        product.setStatus(ProductStatus.ADJUST_PENDING);
        repo.save(product);
        return BaseExceptionConstant.SUCCESS_DESCRIPTION;
    }

    @Override
    @Transactional
    public String stopSelling(PauseStopSellingProductReq request) {
        request.validate();
        List<ProductReasonType> lstValidStatus = List.of(
                ProductReasonType.BUSINESS_CLOSED,
                ProductReasonType.PRODUCTION_STOPPED,
                ProductReasonType.PRODUCT_DEFECT,
                ProductReasonType.SUPPLIER_QUALITY_ISSUE_STOP,
                ProductReasonType.OTHER_STOP
        );
        checkValidType(lstValidStatus, ProductReasonType.fromValue(request.getReasonType()));
        ProductReasonType productReasonType = ProductReasonType.fromValue(request.getReasonType());
        ProductEntity product = findOne(request.getId());
        List<ProductStatus> lstStatusValid = List.of(
                ProductStatus.SELLING
        );
        checkValidStatus(lstStatusValid, product.getStatus());
        String reason = null;
        product.setReasonType(productReasonType);
        if (productReasonType.equals(ProductReasonType.OTHER_STOP)) {
            reason = request.getReason();
        }
        product.setReason(reason);
        product.setStatus(ProductStatus.ADJUST_PENDING);
        repo.save(product);
        return BaseExceptionConstant.SUCCESS_DESCRIPTION;
    }

    @Override
    @Transactional
    public String saveDraft(ProductCreateUpdateRequest request) {
        request.setId(null);
        return createOrUpdateProduct(request, null, ProductStatus.NEW, false, false);
    }

    /**
     * Khi tạo sản phẩm mới sẽ gọi hàm này. Sẽ tạo một bản ghi trên product chứ không tạo bản ghi trên bảng tạm
     */
    @Override
    @Transactional
    public String saveSendApprove(ProductCreateUpdateRequest request) {
        request.setId(null);
        return createOrUpdateProduct(request, null, ProductStatus.PENDING, false, false);
    }

    /**
     *
     * Sản phẩm đang ở trạng thái 7: "Đang sửa sản phẩm", ấn "Cập nhật", r sẽ gọi API này.
     *
     * This method is called when a product, currently in the "under editing" (status 7),
     * needs to be updated based on a draft flag. It handles the logic for either
     * transitioning the product status to "pending" or "adjust pending" and updates the product.
     *
     * @param request The request object containing the product update data.
     * @return A success description message indicating the result of the operation.
     */
    @Override
    @Transactional
    public String updateDraft(ProductCreateUpdateRequest request) {
        if (DataUtils.isNullOrEmpty(request.getId())) {
            throw new VipoBusinessException(ErrorCodeResponse.REQUIRED_FIELD, "id");
        }
        if (DataUtils.isNullOrEmpty(request.getIsDraft())) {
            throw new VipoBusinessException(ErrorCodeResponse.REQUIRED_FIELD, "isDraft");
        }
        ProductEntity product = findOne(request.getId());
        List<ProductStatus> lstStatusValid = List.of(
                ProductStatus.NEW,
                ProductStatus.EDITING
        );
        checkValidStatus(lstStatusValid, product.getStatus());
        ProductStatus status;
        if (product.getStatus().equals(ProductStatus.NEW)) {
            status = ProductStatus.PENDING;
            if (request.getIsDraft()) {
                status = ProductStatus.NEW;
            }
            return createOrUpdateProduct(request, product, status, false, false);
        } else {
            status = ProductStatus.ADJUST_PENDING;
            if (request.getIsDraft()) {
                status = ProductStatus.EDITING;
            }
            return createOrUpdateProduct(request, product, status, true, true);
        }
    }

    /**
     * HA note:
     *
     * Sản phẩm đang ở trạng thái đang bán, ấn "sửa toàn bộ thông tin", sẽ gọi method này này
     *
     * isDraft: thể hiện sản phẩm đang ở trạng thái nháp hay không?
     *
     * sau đó các thông tin được sửa vào bảng tạm, chứ không sửa vào bảng chính
     */
    @Override
    @Transactional
    public String updateTotal(ProductCreateUpdateRequest request, boolean isDraft) {
        if (DataUtils.isNullOrEmpty(request.getId())) {
            throw new VipoBusinessException(ErrorCodeResponse.REQUIRED_FIELD, "id");
        }
        ProductEntity product = findOne(request.getId());
        List<ProductStatus> lstStatusValid = List.of(
                ProductStatus.APPROVED,
                ProductStatus.SELLING
        );
        checkValidStatus(lstStatusValid, product.getStatus());
        ProductStatus status = ProductStatus.ADJUST_PENDING;
        if (isDraft) {
            status = ProductStatus.EDITING;
        }

        /* phase 5.5: product approval fix: validate the request */
        if (Boolean.TRUE.equals(Constants.isNewProductApproval.get()))
            validateTheUpdateAttributeRequestInCaseSkuIncludedInAnOrder(
                    product, request.getManageProductCodeInfo(), request.getProductAttributesInfo()
            );

        return createOrUpdateProduct(request, product, status, isDraft, true);
    }

    @Override
    public String getCreateTemplate() {
        checkMerchant();
        if (Constants.LANGUAGE_VI.equals(getLocale())) {
            return createTemplatePathVi;
        } else {
            return createTemplatePathEn;
        }
    }

    @Override
    public ResponseEntity<InputStreamResource> getTemplate() {
        checkMerchant();
        try {
            return getCreateTemplateSub(getLocale());
        } catch (Exception e) {
            return getCreateTemplateSub(Constants.LANGUAGE_EN);
        }
    }

    private ResponseEntity<InputStreamResource> getCreateTemplateSub(String locale) {
        try {
            String pathTemplate = Constants.PRODUCT_EXAMPLE_PATH_TEMPLATE.replace(Constants.PARAM_LOCALE, locale);
            ClassPathResource classPathResource = new ClassPathResource(pathTemplate);
            InputStreamResource resource = new InputStreamResource(classPathResource.getInputStream());
            String fileName = Translator.toLocale("product.management.create.file.template") + "_" + locale + ".xlsx";
            return ResponseEntity.ok()
                    .headers(this.createDownloadHeaders(fileName))
                    .body(resource);
        } catch (FileNotFoundException e) {
            throw new VipoBusinessException(ErrorCodeResponse.ERROR_FILE_NOT_FOUND);
        } catch (IOException e) {
            throw new VipoBusinessException(ErrorCodeResponse.IO_EXCEPTION);
        }
    }


    private String createOrUpdateProduct(ProductCreateUpdateRequest request,
                                         ProductEntity product,
                                         ProductStatus status,
                                         boolean isDraft,
                                         boolean isUpdate) {
        validateBeforeCheckDb(request, false);
        validateCheckImageAndVideo(request);
        Long merchantId = validateCheckDb(request);
        String sellerOpenId = sellerOpenRepository.getSellerOpenIdByMerchantId(merchantId);
        Pair<ProductEntity, ProductTemporaryEntity> entityPair = saveForCreateOrUpdate(request, merchantId, sellerOpenId, status,
                product, isDraft, isUpdate);
        ProductEntity productEntity = entityPair.getLeft();
        ProductTemporaryEntity productTemporary = entityPair.getRight();
        if (Constants.isNewProductApproval.get())
            saveAttributesClassifyAndSkuV2(request, productEntity, productTemporary);
        else
            saveAttributesClassifyAndSku(request, productEntity, productTemporary);
        saveCertificates(request.getCertificatesInfo(), request.getId(), productEntity, productTemporary);
        return DataUtils.safeToString(productEntity.getId());
    }

    public Set<CertificateRequest> getCertificates(Long requestProductId) {
        // 1. Kiểm tra dữ liệu đầu vào
        if (ObjectUtils.isEmpty(requestProductId)) {
            logger.info("[getCertificates] No certificates found.");
            return new HashSet<>();
        }

        // 2. Tìm kiếm chứng chỉ liên kết với productId
        List<ProductCertificateEntity> existingCertificates = productCertificateEntityRepository.findByProductIdAndDeletedFalse(requestProductId);
        logger.info("[getCertificates] Number of existing certificates: {}", existingCertificates.size());

        // 3. Chuyển đổi sang dạng Set<CertificateRequest>
        return existingCertificates.stream()
                .map(cert -> CertificateRequest.builder()
                        .id(String.valueOf(cert.getId()))
                        .status(cert.getStatus())
                        .build())
                .collect(Collectors.toSet());
    }

    public void updateCertificates(Long productId, Long productTempId) {
        logger.info("[updateCertificates] Updating certificates for productId={}, productTempId={}", productId, productTempId);
        // 1. Kiểm tra dữ liệu đầu vào
        if (ObjectUtils.isEmpty(productId)) {
            logger.info("[updateCertificates] No certificates found. Skipping update.");
            return;
        }

        // 2. Tìm kiếm chứng chỉ liên kết với productId
        List<ProductCertificateEntity> existingCertificates = productCertificateEntityRepository.findByProductIdAndDeletedFalse(productId);
        logger.info("[updateCertificates] Number of existing certificates: {}", existingCertificates.size());

        // 3. Update tất cả các chứng chỉ hiện tại
        for (ProductCertificateEntity cert : existingCertificates) {
            cert.setProductTempId(productTempId);
            cert.setTempStatus(cert.getStatus());
        }
        productCertificateEntityRepository.saveAll(existingCertificates);
        logger.info("[updateCertificates] Updated {} existing certificates for productId={}", existingCertificates.size(), productId);
    }

    public void saveCertificates(Set<CertificateRequest> certificateRequests, Long requestProductId,
                                  ProductEntity productEntity, ProductTemporaryEntity productTemporary) {
        logger.info("[saveCertificates] Saving certificates for product with productId={}, productTempId={}, request: {}",
                productEntity.getId(), productTemporary == null ? null : productTemporary.getId(), certificateRequests);

        // 1. Kiểm tra dữ liệu đầu vào
        if (ObjectUtils.isEmpty(certificateRequests)) {
            logger.info("[saveCertificates] No certificates found. Skipping update.");
            return;
        }

        // 2. Kiểm tra số lượng chứng của sản phẩm
        logger.info("[saveCertificates] Number of certificates: {}", certificateRequests.size());
        if (certificateRequests.size() > Constants.MAX_UPLOAD_CERTIFICATE_COUNT) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    Constants.MAX_CERTIFICATE_PER_PRODUCT_EXCEEDED);
        }

        // 3. Kiểm tra số lượng chứng chỉ ACTIVE
        List<CertificateRequest> activeCertificates = certificateRequests.stream()
                .filter(c -> CertificateStatus.ACTIVE.equals(c.getStatus()))
                .toList();

        logger.info("[saveCertificates] Number of active certificates: {}", activeCertificates.size());
        if (activeCertificates.size() > Constants.MAX_DISPLAY_CERTIFICATE_COUNT) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    Constants.MAX_DISPLAY_CERTIFICATE_PER_PRODUCT_EXCEEDED);
        }

        Long productId = productEntity.getId();
        Long productTemporaryId = null;

        logger.info("[saveCertificates] Processing for product status {}", productEntity.getStatus());
        if (productEntity.getStatus().equals(ProductStatus.NEW)
                || productEntity.getStatus().equals(ProductStatus.PENDING)
                || productEntity.getStatus().equals(ProductStatus.REJECT)) {
            logger.info("[saveCertificates] Processing for productId={}", productId);
            // Xử lý cho dữ liệu chính thức
            if (!DataUtils.isNullOrEmpty(requestProductId)) {
                // Lấy danh sách các chứng chỉ hiện tại liên kết với productId
                List<ProductCertificateEntity> existingCertificates = productCertificateEntityRepository.findByProductId(productId);

                // Đánh dấu tất cả các chứng chỉ hiện tại là đã bị xóa mềm
                for (ProductCertificateEntity cert : existingCertificates) {
                    cert.setProductId(null);
                    cert.setStatus(null);
                    cert.setDeleted(true);
                }
                productCertificateEntityRepository.saveAll(existingCertificates);
                logger.info("[saveCertificates MAIN] Soft deleted {} existing certificates for productId={}", existingCertificates.size(), productId);
            }

            // Cập nhật các chứng chỉ từ request
            for (CertificateRequest certificateRequest : certificateRequests) {
                if (!ObjectUtils.isEmpty(certificateRequest.getId())) {
                    Optional<ProductCertificateEntity> optionalCert = productCertificateEntityRepository.findById(Long.valueOf(certificateRequest.getId()));
                    if (optionalCert.isPresent()) {
                        ProductCertificateEntity certEntity = optionalCert.get();
                        certEntity.setProductId(productId);
                        certEntity.setProductTempId(null);
                        certEntity.setStatus(certificateRequest.getStatus());
                        certEntity.setTempStatus(null);
                        certEntity.setDeleted(false);
                        productCertificateEntityRepository.save(certEntity);
                        logger.info("[saveCertificates MAIN] Updated certificateId={} for productId={}", certEntity.getId(), productId);
                    } else {
                        logger.warn("[saveCertificates MAIN] Certificate with id={} not found. Skipping update.", certificateRequest.getId());
                    }
                } else {
                    logger.warn("[saveCertificates MAIN] CertificateRequest without id found. Skipping update.");
                }
            }
        } else {
            logger.info("[saveCertificates] Processing for productTemporary");
            // Xử lý cho dữ liệu nháp
            if (!ObjectUtils.isEmpty(productTemporary)) {
                productTemporaryId = productTemporary.getId();
                logger.info("[saveCertificates] Processing for productTemporaryId={}", productTemporaryId);

                // Lấy danh sách các chứng chỉ hiện tại liên kết với productTempId
                List<ProductCertificateEntity> existingCertificates = productCertificateEntityRepository.findByProductTempId(productTemporaryId);

                // Đánh dấu tất cả các chứng chỉ hiện tại là đã bị xóa mềm
                for (ProductCertificateEntity cert : existingCertificates) {
                    cert.setProductTempId(null);
                    cert.setTempStatus(null);
                    cert.setDeleted(true);
                }
                productCertificateEntityRepository.saveAll(existingCertificates);
                logger.info("[saveCertificates DRAFT] Soft deleted {} existing certificates for productTemporaryId={}", existingCertificates.size(), productTemporaryId);

                // Cập nhật các chứng chỉ từ request
                for (CertificateRequest certificateRequest : certificateRequests) {
                    if (!ObjectUtils.isEmpty(certificateRequest.getId())) {
                        Optional<ProductCertificateEntity> optionalCert = productCertificateEntityRepository.findById(Long.valueOf(certificateRequest.getId()));
                        if (optionalCert.isPresent()) {
                            ProductCertificateEntity certEntity = optionalCert.get();
                            certEntity.setProductId(ObjectUtils.isNotEmpty(certEntity.getProductId()) ? certEntity.getProductId() : null);
                            certEntity.setProductTempId(productTemporaryId);
                            certEntity.setStatus(ObjectUtils.isNotEmpty(certEntity.getStatus()) ? certEntity.getStatus() : null);
                            certEntity.setTempStatus(certificateRequest.getStatus());
                            certEntity.setDeleted(false);
                            productCertificateEntityRepository.save(certEntity);
                            logger.info("[saveCertificates DRAFT] Updated certificateId={} for productTemporaryId={}", certEntity.getId(), productTemporaryId);
                        } else {
                            logger.warn("[saveCertificates DRAFT] Certificate with id={} not found. Skipping update.", certificateRequest.getId());
                        }
                    } else {
                        logger.warn("[saveCertificates DRAFT] CertificateRequest without id found. Skipping update.");
                    }
                }
            }
        }
    }

    private void saveAttributesClassifyAndSku(ProductCreateUpdateRequest request,
                                              ProductEntity productEntity,
                                              ProductTemporaryEntity productTemporary) {
        Long productId = productEntity.getId();
        Long productTemporaryId;
        if (productEntity.getStatus().equals(ProductStatus.NEW)
                || productEntity.getStatus().equals(ProductStatus.PENDING)
                || productEntity.getStatus().equals(ProductStatus.REJECT)) {
            /*neu cap nhat khi chua duoc duyet thi xoa di cac bang lien quan product*/
            if (!DataUtils.isNullOrEmpty(request.getId())) {
                sellerAttributeRepository.deleteByProductId(productId);
                sellerClassifyRepository.deleteByProductId(productId);
                productSellerSkuRepository.deleteByProductId(productId);
            }
            /* Start save sellerAttribute */
            List<SellerAttributeEntity> sellerAttributeEntities = saveSellerAttribute(request, productId);
            /* End save sellerAttribute */
            /* Start save sellerClassify Phan loai */
            List<SellerClassifyEntity> sellerClassifyEntities = saveSellerClassify(request, sellerAttributeEntities,
                    productId);
            /* End save sellerClassify Phan loai */
            /* Start save productSellerSku Phan loai */
            productSellerSkuRepository.saveAll(getProductSellerSku(request,
                    sellerClassifyEntities,
                    sellerAttributeEntities,
                    productId));
            /* End save productSellerSku Phan loai */
        } else {
            if (!DataUtils.isNullOrEmpty(productTemporary)) {
                productTemporaryId = productTemporary.getId();
                sellerAttributeTemporaryRepository.deleteByProductTemporaryId(productTemporaryId);
                sellerClassifyTemporaryRepository.deleteByProductTemporaryId(productTemporaryId);
                productSellerSkuTemporaryRepository.deleteByProductTemporaryId(productTemporaryId);

                /* Start save sellerAttributeTemporary */
                List<SellerAttributeTemporaryEntity> sellerAttributeTemporaryEntities
                        = saveSellerAttributeTemporary(request, productTemporaryId);
                /* End save sellerAttributeTemporary */
                /* Start save sellerClassifyTemporary Phan loai */
                List<SellerClassifyTemporaryEntity> sellerClassifyTemporaryEntities
                        = saveSellerClassifyTemporary(request, sellerAttributeTemporaryEntities,
                        productTemporaryId);
                /* End save sellerClassifyTemporary Phan loai */
                /* Start save productSellerSkuTemporary Phan loai */
                saveProductSellerSkuTemporary(request, sellerClassifyTemporaryEntities,
                        sellerAttributeTemporaryEntities,
                        productTemporaryId, productId);
                /* End save productSellerSkuTemporary Phan loai */
            }
        }
    }

    /**
     * Phase 5.5: Product Approval Fix
     */
    private void saveAttributesClassifyAndSkuV2(ProductCreateUpdateRequest request,
                                              ProductEntity productEntity,
                                              ProductTemporaryEntity productTemporary) {
        Long productId = productEntity.getId();
        Long productTemporaryId;
        if (productEntity.getStatus().equals(ProductStatus.NEW)
                || productEntity.getStatus().equals(ProductStatus.PENDING)
                || productEntity.getStatus().equals(ProductStatus.REJECT)) {
            /*neu cap nhat khi chua duoc duyet thi xoa di cac bang lien quan product*/
            if (!DataUtils.isNullOrEmpty(request.getId())) {
                sellerAttributeRepository.deleteByProductId(productId);
                sellerClassifyRepository.deleteByProductId(productId);
                productSellerSkuRepository.deleteByProductId(productId);
            }
            /* Start save sellerAttribute */
            List<SellerAttributeEntity> sellerAttributeEntities = saveSellerAttribute(request, productId);
            /* End save sellerAttribute */
            /* Start save sellerClassify Phan loai */
            List<SellerClassifyEntity> sellerClassifyEntities = createNewSellerClassify(request, sellerAttributeEntities,
                    productId);
            /* End save sellerClassify Phan loai */
            /* Start save productSellerSku Phan loai */
            productSellerSkuRepository.saveAll(
                    getProductSellerSku(request, sellerClassifyEntities, sellerAttributeEntities, productId)
            );
            /* End save productSellerSku Phan loai */
        } else {
            if (!DataUtils.isNullOrEmpty(productTemporary)) {
                productTemporaryId = productTemporary.getId();
                sellerAttributeTemporaryRepository.deleteByProductTemporaryId(productTemporaryId);
                sellerClassifyTemporaryRepository.deleteByProductTemporaryId(productTemporaryId);
                productSellerSkuTemporaryRepository.deleteByProductTemporaryId(productTemporaryId);

                /* Start save sellerAttributeTemporary */
                List<SellerAttributeTemporaryEntity> sellerAttributeTemporaryEntities
                        = saveSellerAttributeTemporary(request, productTemporaryId);
                /* End save sellerAttributeTemporary */
                /* Start save sellerClassifyTemporary Phan loai */
                List<SellerClassifyTemporaryEntity> sellerClassifyTemporaryEntities
                        = createSellerClassifyTemporary(request, sellerAttributeTemporaryEntities, productTemporaryId);
                /* End save sellerClassifyTemporary Phan loai */
                /* Start save productSellerSkuTemporary Phan loai */
                createProductSellerSkuTemporary(request, sellerClassifyTemporaryEntities,
                        sellerAttributeTemporaryEntities,
                        productTemporaryId, productId);
                /* End save productSellerSkuTemporary Phan loai */
            }
        }
    }


    private Pair<ProductEntity, ProductTemporaryEntity> saveForCreateOrUpdate(ProductCreateUpdateRequest request,
                                                                              Long merchantId,
                                                                              String sellerOpenId,
                                                                              ProductStatus status,
                                                                              ProductEntity product,
                                                                              boolean isDraft,
                                                                              boolean isUpdate) {
        ProductTemporaryEntity productTemporaryEntity = null;
        ProductStatus beforeStatus = ProductStatus.UNKNOWN;
        if (DataUtils.isNullOrEmpty(product)) {
            product = new ProductEntity();
        } else {
            beforeStatus = product.getStatus();
        }
        BaseProductInfo baseProductInfo = request.getBaseProductInfo();
        SellingProductInfo sellingProductInfo = request.getSellingProductInfo();
        if ((beforeStatus.equals(ProductStatus.NEW)
                || beforeStatus.equals(ProductStatus.PENDING)
                || beforeStatus.equals(ProductStatus.REJECT)
                || beforeStatus.equals(ProductStatus.UNKNOWN)) && !isDraft) {
            //set gia tri request de luu lai

            product.setCategoryId(baseProductInfo.getCategoryId());
            product.setMerchantId(merchantId);
            product.setSellerOpenId(sellerOpenId);
            product.setName(baseProductInfo.getDisplayName());
            product.setOriginalProductName(baseProductInfo.getFullName());
            product.setImage(String.join(",",baseProductInfo.getProductThumbnail()));

            List<String> listProductMedia = new ArrayList<>(baseProductInfo.getProductMedia());
            Set<String> listImage = new HashSet<>();
            Set<String> listVideo = new HashSet<>();
            for (String link : listProductMedia) {
                if (link.toLowerCase().endsWith(".mp4")) {
                    listVideo.add(link);
                } else {
                    listImage.add(link);
                }
            }
            product.setImages(String.join(",", listImage));
            product.setTrailerVideo(String.join(",", listVideo));

            product.setDescription(baseProductInfo.getProductDescription());
            product.setQuoteType(sellingProductInfo.getMinPurchaseType());
            product.setPriceRanges(JsonMapperUtils.writeValueAsString(request.getStepPriceInfo()));
            if (!DataUtils.isNullOrEmpty(product.getPriceRanges()) &&
                    product.getPriceRanges().equals("null")) {
                product.setPriceRanges(null);
            }
            product.setProductPriceType(sellingProductInfo.getPriceType());
            product.setMinOrderQuantity(sellingProductInfo.getMinPurchaseQuantity());
            product.setProductSpecInfo(JsonMapperUtils.writeValueAsString(request.getProductSpecInfo()));
            product.setPlatformDiscountRate(sellingProductInfo.getPlatformDiscountRate());
            product.setPrice(sellingProductInfo.getProductPrice());
            product.setStatus(status);
            product.setProductCodeCustomer(baseProductInfo.getProductCodeCustomer());
            product.setCountryId(getCurrentUser().getCountryId());

            /* display price: giá hiển thị */
            BigDecimal displayPrice = null;
            // the comment belows is the legacy code
//            if (!DataUtils.isNullOrEmpty(product.getPrice())) {
//                displayPrice = product.getPrice();
//            } else {
//                for (ManageProductCodeInfo info : request.getManageProductCodeInfo()) {
//                    if (info.getUnitPrice() != null) {
//                        displayPrice = info.getUnitPrice();
//                        break; // Nếu chỉ cần lấy giá trị unitPrice đầu tiên không null, dừng vòng lặp.
//                    }
//                }
//            }
            // Check the product price type and determine displayPrice accordingly
            if (sellingProductInfo.getPriceType().equals(ProductPriceTypeEnum.NO_PRICE_RANGE.getValue())) {
                if (ObjectUtils.isEmpty(product.getPrice()))
                    throw new VipoInvalidDataRequestException("price is required when product_price_type = 0");
                displayPrice = product.getPrice();
            } else if (sellingProductInfo.getPriceType().equals(ProductPriceTypeEnum.PRODUCT_PRICE_RANGE.getValue())) {
                if (ObjectUtils.isEmpty(request.getStepPriceInfo()))
                    throw new VipoInvalidDataRequestException("Price ranges is required when product_price_type = 1");
                // Find the unit price with the smallest price step from the step price information
                displayPrice = request.getStepPriceInfo().stream()
                        .filter(
                                info -> ObjectUtils.isNotEmpty(info.getUnitPrice())
                                        && ObjectUtils.isNotEmpty(info.getPriceStep())
                        )
                        .max(Comparator.comparing(StepPriceInfo::getPriceStep))
                        .map(StepPriceInfo::getUnitPrice)
                        .orElse(null);
                if (ObjectUtils.isEmpty(displayPrice))
                    throw new VipoInvalidDataRequestException("No valid display price when product_price_type = 1");
            } else if (sellingProductInfo.getPriceType().equals(ProductPriceTypeEnum.SKU_PRICE.getValue())) {
                // If price type is SKU_PRICE_RANGE, find the nonnull smallest unit price from manage product code
                // information sorted by
                displayPrice = request.getManageProductCodeInfo().stream()
                        .map(ManageProductCodeInfo::getUnitPrice)
                        .filter(ObjectUtils::isNotEmpty)
                        .min(Comparator.naturalOrder())
                        .orElse(null);
                if (ObjectUtils.isEmpty(displayPrice))
                    throw new VipoInvalidDataRequestException("No valid display price when product_price_type = 2");
            }

            product.setDisplayPrice(displayPrice);
            product = repo.save(product);
        }
        product.setStatus(status);
        product.setProductCode(Constants.PREFIX_PROD_CODE + product.getId());
        repo.save(product);
        //Set bang productTemp neu bang ghi laf da duyet status = 3 gui duyet thay doi lai cho admin
        if (((!DataUtils.isNullOrEmpty(request.getId())
                && !(beforeStatus.equals(ProductStatus.NEW)
                || beforeStatus.equals(ProductStatus.PENDING)
                || beforeStatus.equals(ProductStatus.REJECT))) && isDraft) || isUpdate) {

            productTemporaryEntity = productTemporaryRepository.findByProductId(request.getId())
//                    .orElseGet(ProductTemporaryEntity::new);
                    .orElse(null);
            if (ObjectUtils.isEmpty(productTemporaryEntity))
                productTemporaryEntity = productMapper.toProductTemporaryEntity(product);
            else
                productMapper.toProductTemporaryEntity(product, productTemporaryEntity);

            productTemporaryEntity.setProductId(product.getId());
            productTemporaryEntity.setCategoryId(baseProductInfo.getCategoryId());
            productTemporaryEntity.setName(baseProductInfo.getDisplayName());
            productTemporaryEntity.setOriginalProductName(baseProductInfo.getFullName());
//            productTemporaryEntity.setImage(baseProductInfo.getProductThumbnail().get(0));
            productTemporaryEntity.setImage(baseProductInfo.getProductThumbnail().stream().distinct().collect(Collectors.joining(",")));
//            if (baseProductInfo.getProductThumbnail().size() > 1) {
//                List<String> lstImage = new ArrayList<>(baseProductInfo.getProductThumbnail());
//                lstImage.remove(baseProductInfo.getProductThumbnail().get(0));
//                productTemporaryEntity.setImages(
//                        String.join(",", lstImage));
//            }
            if (ObjectUtils.isNotEmpty(baseProductInfo.getProductMedia())) {
                Set<String> listImage = new HashSet<>();
                Set<String> listVideo = new HashSet<>();
                for (String link : baseProductInfo.getProductMedia()) {
                    if (link.toLowerCase().endsWith(".mp4")) {
                        listVideo.add(link);
                    } else {
                        listImage.add(link);
                    }
                }
                productTemporaryEntity.setImages(String.join(",", listImage));
                productTemporaryEntity.setTrailerVideo(String.join(",", listVideo));
            }
            productTemporaryEntity.setDescription(baseProductInfo.getProductDescription());
//            productTemporaryEntity.setTrailerVideo(String.join(",", request.getBaseProductInfo().getProductMedia()));
            productTemporaryEntity.setQuoteType(sellingProductInfo.getMinPurchaseType());
            productTemporaryEntity.setPriceRanges(JsonMapperUtils.writeValueAsString(request.getStepPriceInfo()));

            //Product Approve: no update on product.priceRanges
//            product.setPriceRanges(JsonMapperUtils.writeValueAsString(request.getStepPriceInfo()));

            if (!DataUtils.isNullOrEmpty(productTemporaryEntity.getPriceRanges()) &&
                    productTemporaryEntity.getPriceRanges().equals("null")) {
                productTemporaryEntity.setPriceRanges(null);
            }
            productTemporaryEntity.setProductPriceType(sellingProductInfo.getPriceType());
            productTemporaryEntity.setMinOrderQuantity(sellingProductInfo.getMinPurchaseQuantity());
            productTemporaryEntity.setProductSpecInfo(JsonMapperUtils.writeValueAsString(request.getProductSpecInfo()));
            productTemporaryEntity.setPlatformDiscountRate(sellingProductInfo.getPlatformDiscountRate());
            productTemporaryEntity.setPrice(sellingProductInfo.getProductPrice());
            BigDecimal displayPrice = null;
            if (!DataUtils.isNullOrEmpty(product.getPrice())) {
                displayPrice = product.getPrice();
            } else {
                for (ManageProductCodeInfo info : request.getManageProductCodeInfo()) {
                    if (info.getUnitPrice() != null) {
                        displayPrice = info.getUnitPrice();
                        break; // Nếu chỉ cần lấy giá trị unitPrice đầu tiên không null, dừng vòng lặp.
                    }
                }
            }
            productTemporaryEntity.setDisplayPrice(displayPrice);
            productTemporaryRepository.save(productTemporaryEntity);
        }
        return Pair.of(product, productTemporaryEntity);
    }

    private void validateCheckImageAndVideo(ProductCreateUpdateRequest request) {
        BaseProductInfo baseProductInfo = request.getBaseProductInfo();
        List<String> productThumbnail = baseProductInfo.getProductThumbnail();
        List<String> urls = new ArrayList<>(productThumbnail);
        List<String> productMedia = baseProductInfo.getProductMedia();
        if (ObjectUtils.isNotEmpty(productMedia))
            urls.addAll(productMedia);
        Set<ProductAttributesInfo> productAttributesInfos = request.getProductAttributesInfo();
        productAttributesInfos.forEach(i -> urls.addAll(i.getImage()));
        urls.addAll(request.getManageProductCodeInfo().stream()
                .map(ManageProductCodeInfo::getProductImage).collect(Collectors.toList()));
        //chung validate file nên y toan bo link di validate chung
        //Tiem an loi khi khong validate link anh duoc chi dung o validate http:// binh thuong
        validImageVideo(new ArrayList<>(new HashSet<>(urls)));
    }

    private Long validateCheckDb(ProductCreateUpdateRequest request) {
        VipoUserDetails user = getCurrentUser();
        checkMerchant();
        BaseProductInfo baseProductInfo = request.getBaseProductInfo();
        Integer categoryId = baseProductInfo.getCategoryId();
        findCategory(Long.valueOf(categoryId));
        if (!DataUtils.isNullOrEmpty(request.getBaseProductInfo().getProductCodeCustomer())) {
            List<String> productCodeCustomer = repo.findProductCustomerCodeByCodes(
                    Collections.singletonList(request.getBaseProductInfo().getProductCodeCustomer()),
                    getCurrentUser().getId(), request.getId()
            );
            if (!DataUtils.isNullOrEmpty(productCodeCustomer)) {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_EXIST_PRODUCT_CODE_CUS);
            }
        }
        return user.getId();
    }

    private String validateBeforeCheckDb(ProductCreateUpdateRequest request, boolean needString) {
        Set<ManageProductCodeInfo> manageProductCodeInfo = request.getManageProductCodeInfo();
        //validate so mua toi thieu > ton kho
        Long totalStock = manageProductCodeInfo.stream()
//                .filter(ManageProductCodeInfo::isActiveStatus)
                .map(i -> DataUtils.isNullOrEmpty(i.getStock()) ? 0 : i.getStock())
                .reduce(0L, Long::sum);
        SellingProductInfo sellingProductInfo = request.getSellingProductInfo();
        String error = sellingProductInfo.validate(totalStock, needString);
        if (needString && !DataUtils.isNullOrEmpty(error)) return error;
        if (FIXED_PRICE_SKU_NO_PRICE_STEP.equals(request.getSellingProductInfo().getPriceType())) {
            request.getManageProductCodeInfo().forEach(i -> i.setUnitPrice(request.getSellingProductInfo().getProductPrice()));
            request.setStepPriceInfo(null);
        }
        if (FIXED_PRICE_SKU_WITH_PRICE_STEP.equals(request.getSellingProductInfo().getPriceType())) {
            request.getSellingProductInfo().setProductPrice(null);
            if (DataUtils.isNullOrEmpty(request.getStepPriceInfo())) {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_REQUIRED_FIELD);
            }
        }
        if (AGGREGATE_ALL_PRODUCTS.equals(sellingProductInfo.getMinPurchaseType())) {
            request.getManageProductCodeInfo().forEach(i
                    -> i.setMinPurchase(request.getSellingProductInfo().getMinPurchaseQuantity()));
        }
        if (EACH_SKU_OF_PRODUCT.equals(sellingProductInfo.getMinPurchaseType())) {
            request.getSellingProductInfo().setMinPurchaseQuantity(null);
        }
        for (ManageProductCodeInfo info : request.getManageProductCodeInfo()) {
            error = info.validate(request.getSellingProductInfo().getPriceType(),
                    request.getSellingProductInfo().getMinPurchaseType(),
                    needString);
            if (needString && !DataUtils.isNullOrEmpty(error)) return error;
        }
        //validate các thuộc tinh
        Set<ProductAttributesInfo> productAttributesInfos = request.getProductAttributesInfo();
        if (!needString) {
            productAttributesInfos.forEach(ProductAttributesInfo::validate);
        }
        error = validateRequiredImage(productAttributesInfos, needString);
        if (needString && !DataUtils.isNullOrEmpty(error)) return error;
        error = validateSequentialStt(productAttributesInfos, needString);
        if (needString && !DataUtils.isNullOrEmpty(error)) return error;
        //validate thang gia
        error = validateAndSortStepPriceInfoSet(request.getStepPriceInfo(), sellingProductInfo.getMinPurchaseQuantity()
                , request.getSellingProductInfo().getPriceType(), needString);
        if (needString && !DataUtils.isNullOrEmpty(error)) return error;
        if (!request.getSellingProductInfo().getPriceType().equals(FIXED_PRICE_SKU_WITH_PRICE_STEP)) {
            request.setStepPriceInfo(null);
        }

        for (ProductAttributesInfo i : productAttributesInfos) {
            error = validateProductAttributesInfo(i.getImage(), needString);
            if (!DataUtils.isNullOrEmpty(error)) return error;
        }
        //Cho phép tải nhiều ảnh (tối đa 10 ảnh) hoặc video (tối đa 3 video)
        BaseProductInfo baseProductInfo = request.getBaseProductInfo();
        List<String> productMedia = baseProductInfo.getProductMedia();
        error = validateBaseProductInfo(productMedia, needString);
        if (!DataUtils.isNullOrEmpty(error)) return error;
        //validate các sku
        error = validateSku(request, needString);
        if (!DataUtils.isNullOrEmpty(error)) return error;
        return null;
    }

    private String validateSku(ProductCreateUpdateRequest request, boolean needString) {
        Set<ProductAttributesInfo> attributesInfoSet = request.getProductAttributesInfo();
        //gen các phân loại có the tao ra
        List<String> lstClassify = generateSortedClassify(attributesInfoSet);
        int sizeClassify = lstClassify.size();
        Set<ManageProductCodeInfo> manageProductCodeInfo = request.getManageProductCodeInfo();
        //Neu khong du cac phan tu thi bao loi
        if (sizeClassify != manageProductCodeInfo.size()) {
            if (needString) {
                return ErrorCodeResponse.INVALID_NUM_SKU.getMessageI18N();
            } else {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_NUM_SKU);
            }
        }
        //So sanh ma sku duoc gen va sku request truyen vao
        Map<String, Integer> skuCodeGen = createFrequencyMap(lstClassify);
        Map<String, Integer> skuCodeInput = createFrequencyMap(manageProductCodeInfo
                .stream().map(ManageProductCodeInfo::getStrAttribute).collect(Collectors.toList()));
        for (Map.Entry<String, Integer> entry : skuCodeGen.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            if (DataUtils.isNullOrEmpty(skuCodeInput.get(key))
                    || !skuCodeInput.get(key).equals(value)) {
                if (needString) {
                    return ErrorCodeResponse.INVALID_SKU.getMessageI18N();
                } else {
                    throw new VipoBusinessException(ErrorCodeResponse.INVALID_SKU);
                }
            }
        }
        return null;
    }

    private void validImageVideo(List<String> input) {
        if (DataUtils.isNullOrEmpty(input)) return;
        for (String i : input) {
            if (DataUtils.isNullOrEmpty(i)) continue;
            // Kiểm tra xem URL có kết thúc bằng một phần mở rộng hợp lệ
            String lowerCaseUrl = i.toLowerCase();
            // Chuyển toàn bộ URL về chữ thường để kiểm tra ignoreCase
            List<String> validType = new ArrayList<>(Constants.VALID_IMAGE_TYPE);
            validType.addAll(Constants.VALID_VIDEO_TYPE);
            boolean isFalse = validType.stream().noneMatch(type -> lowerCaseUrl.endsWith("." + type.toLowerCase()))
                    || !i.startsWith("http");
            if (isFalse) {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_REQUIRED_FIELD);
            }
        }
    }

    private String validateBaseProductInfo(List<String> files, boolean needString) {
        if (ObjectUtils.isNotEmpty(files)) {
            long mp4Count = files.stream()
                    .filter(file -> Constants.VALID_VIDEO_TYPE.stream()
                            .anyMatch(type -> file.toLowerCase().endsWith("." + type.toLowerCase())))
                    .count();

            long imageCount = files.stream()
                    .filter(file -> Constants.VALID_IMAGE_TYPE.stream()
                            .anyMatch(type -> file.toLowerCase().endsWith("." + type.toLowerCase())))
                    .count();
            if (mp4Count > 3 || imageCount > 10) {
                if (needString) {
                    return ErrorCodeResponse.INVALID_REQUEST_MAX_MEDIA.getMessageI18N();
                } else {
                    throw new VipoBusinessException(ErrorCodeResponse.INVALID_REQUEST_MAX_MEDIA);
                }
            }
        }
        return null;
    }

    private String validateProductAttributesInfo(List<String> files, boolean needString) {
        if (!DataUtils.isNullOrEmpty(files)) {
            for (String url : files) {
                if (DataUtils.isNullOrEmpty(url)) continue;
                boolean isValid = Constants.VALID_IMAGE_TYPE.stream()
                        .filter(i -> !DataUtils.isNullOrEmpty(i))
                        .anyMatch(type -> url.toUpperCase().endsWith("." + type));
                if (!isValid) {
                    if (needString) {
                        return ErrorCodeResponse.INVALID_LINK_IMAGE_VIDEO.getMessageI18N();
                    } else {
                        throw new VipoBusinessException(ErrorCodeResponse.INVALID_LINK_IMAGE_VIDEO);
                    }
                }
            }
        }
        return null;
    }


    public String validateSequentialStt(Set<ProductAttributesInfo> attributesInfoSet, boolean needString) {
        List<ProductAttributesInfo> sortedList = new ArrayList<>(attributesInfoSet);
        // Sort the list by `stt` in ascending order
        sortedList.sort(Comparator.comparingInt(ProductAttributesInfo::getStt));
        int expectedStt = 1; // Starting value
        for (ProductAttributesInfo info : sortedList) {
            if (info.getStt() != expectedStt) {
                if (needString) {
                    return ErrorCodeResponse.INVALID_STT_SEQUENCE.getMessageI18N();
                } else {
                    throw new VipoBusinessException(ErrorCodeResponse.INVALID_STT_SEQUENCE);
                }
            }
            expectedStt++;
        }
        return null;
    }

    public List<String> generateSortedClassify(Set<ProductAttributesInfo> attributesInfoSet) {
        // Tạo danh sách và sắp xếp theo stt
        List<ProductAttributesInfo> sortedList = new ArrayList<>(attributesInfoSet);
        sortedList.sort(Comparator.comparingInt(ProductAttributesInfo::getStt));
        // Danh sách để lưu các bộ phân loại (key sets từ từng ProductAttributesInfo)
        List<List<String>> attributeLists = new ArrayList<>();
        // Lặp qua từng ProductAttributesInfo và lưu key set vào attributeLists
        for (ProductAttributesInfo info : sortedList) {
            attributeLists.add(new ArrayList<>(info.getNameAndImage().keySet()));
        }
        // Phương thức để kết hợp các thuộc tính
        List<String> results = new ArrayList<>();
        combineAttributes(attributeLists, 0, "", results);
        return results;
    }

    // Hàm đệ quy để kết hợp các giá trị của các thuộc tính theo thứ tự
    private void combineAttributes(List<List<String>> attributeLists, int index, String current, List<String> results) {
        if (index == attributeLists.size()) {
            results.add(current);
            return;
        }

        for (String value : attributeLists.get(index)) {
            String newValue = current.isEmpty() ? value : current + "-" + value;
            combineAttributes(attributeLists, index + 1, newValue, results);
        }
    }

    private Map<String, Integer> createFrequencyMap(List<String> stringList) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        // Lặp qua danh sách chuỗi và cập nhật số lần xuất hiện trong map
        for (String str : stringList) {
            frequencyMap.put(str, frequencyMap.getOrDefault(str, 0) + 1);
        }
        return frequencyMap;
    }

    private List<SellerAttributeEntity> saveSellerAttribute(ProductCreateUpdateRequest request,
                                                            Long productId) {
        List<ProductAttributesInfo> productAttributesInfos = new ArrayList<>(request.getProductAttributesInfo());
        productAttributesInfos.sort(Comparator.comparingInt(ProductAttributesInfo::getStt));

        List<SellerAttributeEntity> sellerAttributeEntities = new ArrayList<>();
        for (ProductAttributesInfo i : productAttributesInfos) {
            sellerAttributeEntities.add(SellerAttributeEntity.builder()
                    .productId(productId)
                    .attributeName(i.getAttributeName())
                    .attributeOrder(i.getStt())
                    .build());
        }
        return sellerAttributeRepository.saveAll(sellerAttributeEntities);
    }

    private List<SellerClassifyEntity> saveSellerClassify(ProductCreateUpdateRequest request,
                                                          List<SellerAttributeEntity> sellerAttributeEntities,
                                                          Long productId) {
        Map<Integer, ProductAttributesInfo> productAttributesInfoMap =
                new ArrayList<>(request.getProductAttributesInfo()).stream().collect(Collectors.toMap(
                        ProductAttributesInfo::getStt, Function.identity()
                ));
        List<SellerClassifyEntity> sellerClassifyEntities = new ArrayList<>();
        for (SellerAttributeEntity i : sellerAttributeEntities) {
            ProductAttributesInfo productAttributesInfo = productAttributesInfoMap.get(i.getAttributeOrder());
            int orderClassify = 1;
            for (Map.Entry<String, String> entry : productAttributesInfo.getNameAndImage().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                sellerClassifyEntities.add(SellerClassifyEntity.builder()
                        .productId(productId)
                        .sellerAttributeId(i.getId())
                        .sellerName(key)
                        .sellerImage(value)
                        .orderClassify(orderClassify++)
                        .build());
            }
        }
        return sellerClassifyRepository.saveAll(sellerClassifyEntities);
    }

    private List<SellerClassifyEntity> createNewSellerClassify(
            ProductCreateUpdateRequest request, List<SellerAttributeEntity> sellerAttributeEntities, Long productId
    ) {
        Map<Integer, ProductAttributesInfo> productAttributesInfoMap =
                new ArrayList<>(request.getProductAttributesInfo()).stream().collect(Collectors.toMap(
                        ProductAttributesInfo::getStt, Function.identity()
                ));
        List<SellerClassifyEntity> sellerClassifyEntities = new ArrayList<>();
        for (SellerAttributeEntity i : sellerAttributeEntities) {
            ProductAttributesInfo productAttributesInfo = productAttributesInfoMap.get(i.getAttributeOrder());
            int orderClassify = 1;
            for (SellerClassifyInfo classifyInfo : productAttributesInfo.getSellerClassifyInfos()) {
                sellerClassifyEntities.add(
                        SellerClassifyEntity.builder()
                                .productId(productId)
                                .sellerAttributeId(i.getId())
                                .sellerName(classifyInfo.getName())
                                .sellerImage(classifyInfo.getImage())
                                .orderClassify(orderClassify++)
                                .build()
                );
            }
        }
        return sellerClassifyRepository.saveAll(sellerClassifyEntities);
    }

    private List<ProductSellerSkuEntity> getProductSellerSku(ProductCreateUpdateRequest request,
                                                             List<SellerClassifyEntity> sellerClassifyEntities,
                                                             List<SellerAttributeEntity> sellerAttributeEntities,
                                                             Long productId
    ) {
        Map<String, SellerClassifyEntity> mapSellerClassify = sellerClassifyEntities.stream().collect(Collectors.toMap(
                i -> (i.getSellerAttributeId().toString().concat("-").concat(i.getSellerName())), Function.identity()
        ));
        Map<String, SellerAttributeEntity> mapSellerAttribute = sellerAttributeEntities
                .stream().collect(Collectors.toMap(
                        i -> (i.getProductId().toString().concat("-")
                                .concat(DataUtils.safeToString(i.getAttributeOrder()))), Function.identity()
                ));
        Map<Integer, ProductAttributesInfo> productAttributesInfoMap = request.getProductAttributesInfo()
                .stream().collect(Collectors.toMap(ProductAttributesInfo::getStt, Function.identity()));
        Set<ManageProductCodeInfo> manageProductCodeInfo = request.getManageProductCodeInfo();
        List<ProductSellerSkuEntity> productSellerSkuEntities = new ArrayList<>();
        for (ManageProductCodeInfo i : manageProductCodeInfo) {
            ArrayList<String> attributes = i.getAttribute();
            List<String> sellerClassifyIds = new ArrayList<>();
            StringBuilder code = new StringBuilder(Constants.PREFIX_PROD_CODE);
            code.append(productId);
            for (int j = 0; j < attributes.size(); j++) {
                SellerAttributeEntity attributeEntity
                        = mapSellerAttribute.get(productId.toString().concat("-")
                        .concat(DataUtils.safeToString(j + 1)));
                SellerClassifyEntity sellerClassifyEntity =
                        mapSellerClassify.get(
                                attributeEntity.getId().toString().concat("-").concat(attributes.get(j)));
                sellerClassifyIds.add(String.valueOf(sellerClassifyEntity.getId()));
                ProductAttributesInfo productAttributesInfo = productAttributesInfoMap.get(j + 1);
                code.append(Constants.PREFIX_ATTRIBUTE_CODE).append(productAttributesInfo.getStt())
                        .append(sellerClassifyEntity.getOrderClassify());
            }
            productSellerSkuEntities.add(ProductSellerSkuEntity.builder()
                    .productId(productId)
                    .sellerClassifyId(String.join(",", sellerClassifyIds))
                    .unitPrice(i.getUnitPrice())
                    .stock(i.getStock())
                    .minPurchase(i.getMinPurchase())
                    .code(code.toString())
                    .productImage(i.getProductImage())
                    .activeStatus(i.isActiveStatus())
                    .height(!DataUtils.isNullOrEmpty(i.getHeight()) ? BigDecimal.valueOf(i.getHeight()) : null)
                    .width(!DataUtils.isNullOrEmpty(i.getWidth()) ? BigDecimal.valueOf(i.getWidth()) : null)
                    .length(!DataUtils.isNullOrEmpty(i.getLength()) ? BigDecimal.valueOf(i.getLength()) : null)
                    .weight(!DataUtils.isNullOrEmpty(i.getWeight()) ? i.getWeight() : null)
                    .shippingFee(!DataUtils.isNullOrEmpty(i.getShippingFee()) ? i.getShippingFee() : null)
                    .build());
        }
        return productSellerSkuEntities;
    }

    private List<SellerAttributeTemporaryEntity> saveSellerAttributeTemporary
            (ProductCreateUpdateRequest request,
             Long productTemporaryId) {
        List<ProductAttributesInfo> productAttributesInfos = new ArrayList<>(request.getProductAttributesInfo());
        productAttributesInfos.sort(Comparator.comparingInt(ProductAttributesInfo::getStt));
        List<SellerAttributeTemporaryEntity> sellerAttributeTemporaryEntities = new ArrayList<>();
        for (ProductAttributesInfo i : productAttributesInfos) {
            sellerAttributeTemporaryEntities.add(
                    SellerAttributeTemporaryEntity.builder()
                            .productTemporaryId(productTemporaryId)
                            .attributeName(i.getAttributeName())
                            .attributeOrder(i.getStt())
                            .sellerAttributeId(i.getId())
                            .build()
            );
        }
        return sellerAttributeTemporaryRepository.saveAll(sellerAttributeTemporaryEntities);
    }

    private List<SellerClassifyTemporaryEntity> saveSellerClassifyTemporary
            (ProductCreateUpdateRequest request,
             List<SellerAttributeTemporaryEntity> sellerAttributeTemporaryEntities,
             Long productTemporaryId) {
        Map<Integer, ProductAttributesInfo> productAttributesInfoMap =
                new ArrayList<>(request.getProductAttributesInfo()).stream().collect(Collectors.toMap(
                        ProductAttributesInfo::getStt, Function.identity()
                ));
        List<SellerClassifyTemporaryEntity> sellerClassifyTemporaryEntities = new ArrayList<>();
        for (SellerAttributeTemporaryEntity i : sellerAttributeTemporaryEntities) {
            ProductAttributesInfo productAttributesInfo = productAttributesInfoMap.get(i.getAttributeOrder());
            int orderClassify = 1;
            for (Map.Entry<String, String> entry : productAttributesInfo.getNameAndImage().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                sellerClassifyTemporaryEntities.add(SellerClassifyTemporaryEntity.builder()
                        .productTemporaryId(productTemporaryId)
                        .sellerAttributeTemporaryId(i.getId())
                        .sellerName(key)
                        .sellerImage(value)
                        .orderClassify(++orderClassify)
                        .build());
            }
        }
        return sellerClassifyTemporaryRepository.saveAll(sellerClassifyTemporaryEntities);
    }

    /**
     * Phase 5.5: Product Approval Fix:
     */
    private List<SellerClassifyTemporaryEntity> createSellerClassifyTemporary(
            ProductCreateUpdateRequest request,
            List<SellerAttributeTemporaryEntity> sellerAttributeTemporaryEntities,
            Long productTemporaryId
    ) {
        Map<Integer, ProductAttributesInfo> productAttributesInfoMap =
                new ArrayList<>(request.getProductAttributesInfo()).stream().collect(Collectors.toMap(
                        ProductAttributesInfo::getStt, Function.identity()
                ));
        List<SellerClassifyTemporaryEntity> sellerClassifyTemporaryEntities = new ArrayList<>();
        for (SellerAttributeTemporaryEntity i : sellerAttributeTemporaryEntities) {
            ProductAttributesInfo productAttributesInfo = productAttributesInfoMap.get(i.getAttributeOrder());
            int orderClassify = 1;
            productAttributesInfo.getSellerClassifyInfos().sort(
                    Comparator.comparing(
                            SellerClassifyInfo::getOrderClassify,
                            Comparator.nullsFirst(Integer::compareTo)
                    )
            );
            for (SellerClassifyInfo classifyInfo : productAttributesInfo.getSellerClassifyInfos()) {
                sellerClassifyTemporaryEntities.add(
                        SellerClassifyTemporaryEntity.builder()
                                .productTemporaryId(productTemporaryId)
                                .sellerAttributeTemporaryId(i.getId())
                                .sellerName(classifyInfo.getName())
                                .sellerImage(classifyInfo.getImage())
                                .orderClassify(orderClassify++)
                                .sellerClassifyId(classifyInfo.getId())
                                .tempId(classifyInfo.getTempId())
                                .build());
            }
        }
        return sellerClassifyTemporaryRepository.saveAll(sellerClassifyTemporaryEntities);
    }

    private void saveProductSellerSkuTemporary(ProductCreateUpdateRequest request,
                                               List<SellerClassifyTemporaryEntity> sellerClassifyTemporaryEntities,
                                               List<SellerAttributeTemporaryEntity> sellerAttributeTemporaryEntities,
                                               Long productTemporaryId,
                                               Long productId
    ) {
        Map<String, SellerClassifyTemporaryEntity> mapSellerClassify = sellerClassifyTemporaryEntities.stream().collect(Collectors.toMap(
                i -> (i.getSellerAttributeTemporaryId().toString().concat("-").concat(i.getSellerName())), Function.identity()
        ));
        Map<String, SellerAttributeTemporaryEntity> mapSellerAttribute = sellerAttributeTemporaryEntities
                .stream().collect(Collectors.toMap(
                        i -> (i.getProductTemporaryId().toString().concat("-")
                                .concat(DataUtils.safeToString(i.getAttributeOrder()))), Function.identity()
                ));
        Map<Integer, ProductAttributesInfo> productAttributesInfoMap = request.getProductAttributesInfo()
                .stream().collect(Collectors.toMap(ProductAttributesInfo::getStt, Function.identity()));
        Set<ManageProductCodeInfo> manageProductCodeInfo = request.getManageProductCodeInfo();
        List<ProductSellerSkuTemporaryEntity> productSellerSkuTemporaryEntities = new ArrayList<>();
        for (ManageProductCodeInfo i : manageProductCodeInfo) {
            ArrayList<String> attributes = i.getAttribute();
            List<String> sellerClassifyIds = new ArrayList<>();
            StringBuilder code = new StringBuilder(Constants.PREFIX_PROD_CODE);
            code.append(productId);
            for (int j = 0; j < attributes.size(); j++) {
                SellerAttributeTemporaryEntity attributeEntity
                        = mapSellerAttribute.get(productTemporaryId.toString().concat("-")
                        .concat(DataUtils.safeToString(j + 1)));
                SellerClassifyTemporaryEntity sellerClassifyEntity =
                        mapSellerClassify.get(
                                attributeEntity.getId().toString().concat("-").concat(attributes.get(j)));
                sellerClassifyIds.add(String.valueOf(sellerClassifyEntity.getId()));
                ProductAttributesInfo productAttributesInfo = productAttributesInfoMap.get(j + 1);
                code.append(Constants.PREFIX_ATTRIBUTE_CODE).append(productAttributesInfo.getStt())
                        .append(sellerClassifyEntity.getOrderClassify());
            }
            productSellerSkuTemporaryEntities.add(ProductSellerSkuTemporaryEntity.builder()
                    .productTemporaryId(productTemporaryId)
                    .sellerClassifyTemporaryId(String.join(",", sellerClassifyIds))
                    .unitPrice(i.getUnitPrice())
                    .stock(i.getStock())
                    .minPurchase(i.getMinPurchase())
                    .code(code.toString())
                    .productImage(i.getProductImage())
                    .activeStatus(i.isActiveStatus())
                    .height(!DataUtils.isNullOrEmpty(i.getHeight()) ? BigDecimal.valueOf(i.getHeight()) : null)
                    .width(!DataUtils.isNullOrEmpty(i.getWidth()) ? BigDecimal.valueOf(i.getWidth()) : null)
                    .length(!DataUtils.isNullOrEmpty(i.getLength()) ? BigDecimal.valueOf(i.getLength()) : null)
                    .weight(!DataUtils.isNullOrEmpty(i.getWeight()) ? i.getWeight() : null)
                    .shippingFee(!DataUtils.isNullOrEmpty(i.getShippingFee()) ? i.getShippingFee() : null)
                    .build());
        }
        productSellerSkuTemporaryRepository.saveAll(productSellerSkuTemporaryEntities);
    }

    private void createProductSellerSkuTemporary(ProductCreateUpdateRequest request,
                                               List<SellerClassifyTemporaryEntity> sellerClassifyTemporaryEntities,
                                               List<SellerAttributeTemporaryEntity> sellerAttributeTemporaryEntities,
                                               Long productTemporaryId,
                                               Long productId
    ) {

        /* Create a Map that map id of the classify to the entity */
        Map<Long, SellerClassifyTemporaryEntity> sellerClassifyIdToSellerClassifyTemporaryEntity
                = sellerClassifyTemporaryEntities.stream()
                .filter(
                        sellerClassifyTemporaryEntity
                                -> ObjectUtils.isNotEmpty(sellerClassifyTemporaryEntity.getSellerClassifyId())
                )
                .collect(
                        Collectors.toMap(
                                SellerClassifyTemporaryEntity::getSellerClassifyId,
                                Function.identity(),
                                (existing, replacement) -> existing
                        )
        );
        /* Create a Map that map tempId of the classify to the entity */
        Map<String, SellerClassifyTemporaryEntity> tempIdToSellerClassifyTemporaryEntity
                = sellerClassifyTemporaryEntities.stream()
                .filter(classifyEntity -> StringUtils.isNotBlank(classifyEntity.getTempId()))
                .collect(
                        Collectors.toMap(
                                SellerClassifyTemporaryEntity::getTempId,
                                Function.identity(),
                                (existing, replacement) -> existing
                        )
                );

        /* convert manageProductCodeInfo into ProductSellerSkuTemporaryEntity */
        List<ProductSellerSkuTemporaryEntity> productSellerSkuTemporaryEntities
                = request.getManageProductCodeInfo().stream().map(
                        manageProductCodeInfo -> {

                    /* Create a List of sellerClassifyTemporaryEntity.id that use for ProductSellerSkuTemporary.sellerClassifyTemporaryId */
                    Set<SellerClassifyTemporaryEntity> sellerClassifyTemporaries = new HashSet<>();
                    if (ObjectUtils.isNotEmpty(manageProductCodeInfo.getSellerClassifyIds())) {
                        sellerClassifyTemporaries.addAll(
                                manageProductCodeInfo.getSellerClassifyIds().stream()
                                .map(sellerClassifyIdToSellerClassifyTemporaryEntity::get)
                                .toList()
                        );
                    }
                    if (ObjectUtils.isNotEmpty(manageProductCodeInfo.getSellerClassifyTempIds())) {
                        sellerClassifyTemporaries.addAll(
                                manageProductCodeInfo.getSellerClassifyTempIds().stream()
                                        .map(tempIdToSellerClassifyTemporaryEntity::get)
                                        .toList()
                        );
                    }

                    /* calculate the code for the temporary sku */
                    StringBuilder code = new StringBuilder(Constants.PREFIX_PROD_CODE);
                    code.append(productId);
                    sellerAttributeTemporaryEntities.sort(Comparator.comparing(
                            SellerAttributeTemporaryEntity::getAttributeOrder,
                            Comparator.nullsFirst(Integer::compareTo)
                    ));
                    Map<Long, SellerClassifyTemporaryEntity> tempAttributeIdToTempClassify
                            = sellerClassifyTemporaries.stream().collect(Collectors.toMap(
                            SellerClassifyTemporaryEntity::getSellerAttributeTemporaryId,
                            Function.identity(),
                            (existing, replacement) -> existing
                    ));

                    for (SellerAttributeTemporaryEntity sellerAttributeTemporaryEntity: sellerAttributeTemporaryEntities) {
                        SellerClassifyTemporaryEntity skuTempClassify
                                = tempAttributeIdToTempClassify.get(sellerAttributeTemporaryEntity.getId());
                        if (ObjectUtils.isEmpty(skuTempClassify))
                            continue; //todo: think about throwing exception here later
                        code.append(Constants.PREFIX_ATTRIBUTE_CODE)
                                .append(sellerAttributeTemporaryEntity.getAttributeOrder())
                                .append(skuTempClassify.getOrderClassify());
                    }

                    return ProductSellerSkuTemporaryEntity.builder()
                            .productTemporaryId(productTemporaryId)
                            .sellerClassifyTemporaryId(
                                    sellerClassifyTemporaries.stream()
                                            .map(SellerClassifyTemporaryEntity::getId)
                                            .map(String::valueOf)
                                            .collect(Collectors.joining(","))
                            )
                            .unitPrice(manageProductCodeInfo.getUnitPrice())
                            .stock(manageProductCodeInfo.getStock())
                            .minPurchase(manageProductCodeInfo.getMinPurchase())
                            .code(code.toString())
                            .productImage(manageProductCodeInfo.getProductImage())
                            .activeStatus(manageProductCodeInfo.isActiveStatus())
                            .height(
                                    !DataUtils.isNullOrEmpty(manageProductCodeInfo.getHeight()) ?
                                            BigDecimal.valueOf(manageProductCodeInfo.getHeight()) : null
                            )
                            .width(
                                    !DataUtils.isNullOrEmpty(manageProductCodeInfo.getWidth()) ?
                                            BigDecimal.valueOf(manageProductCodeInfo.getWidth()) : null)
                            .length(
                                    !DataUtils.isNullOrEmpty(manageProductCodeInfo.getLength()) ?
                                            BigDecimal.valueOf(manageProductCodeInfo.getLength()) : null
                            )
                            .weight(
                                    !DataUtils.isNullOrEmpty(manageProductCodeInfo.getWeight()) ?
                                            manageProductCodeInfo.getWeight() : null
                            )
                            .shippingFee(
                                    !DataUtils.isNullOrEmpty(manageProductCodeInfo.getShippingFee()) ?
                                            manageProductCodeInfo.getShippingFee() : null
                            )
                            .build();
                }
        ).toList();
        productSellerSkuTemporaryRepository.saveAll(productSellerSkuTemporaryEntities);
    }

    private String validateAndSortStepPriceInfoSet(Set<StepPriceInfo> stepPriceInfoSet,
                                                   Integer minPurchaseQuantity,
                                                   Integer priceType,
                                                   boolean needString) {
        if (priceType.equals(FIXED_PRICE_SKU_WITH_PRICE_STEP) && DataUtils.isNullOrEmpty(stepPriceInfoSet)) {
            if (needString) {
                return ErrorCodeResponse.INVALID_REQUIRED_FIELD.getMessageI18N();
            } else {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_REQUIRED_FIELD);
            }
        }
        if (priceType.equals(FIXED_PRICE_SKU_WITH_PRICE_STEP)) {
            // Chuyển Set sang List và sắp xếp theo priceStep
            List<StepPriceInfo> sortedList = sortStepPriceInfoSet(stepPriceInfoSet);
            // Khởi tạo biến để theo dõi giá trị kiểm tra
            int expectedPriceStep = 1;
            Integer expectedFromQuantity = !DataUtils.isNullOrEmpty(minPurchaseQuantity) ? minPurchaseQuantity : 1;
            BigDecimal previousUnitPrice = null;
            Integer previousToQuantity = null;

            for (int i = 0; i < sortedList.size(); i++) {
                StepPriceInfo info = sortedList.get(i);

                // Validate priceStep phải tăng dần
                String error = validatePriceStep(info, expectedPriceStep, needString);
                if (!DataUtils.isNullOrEmpty(error)) return error;

                // Validate fromQuantity phải liên tục và bắt đầu từ số lượng mua tối thiểu của sản phẩm
                error = validateFromQuantity(info, expectedFromQuantity, needString);
                if (!DataUtils.isNullOrEmpty(error)) return error;

                // Validate toQuantity (nếu có) của phần tử trước đó phải bằng fromQuantity - 1 của phần tử hiện tại
                error = validatePreviousToQuantity(info, previousToQuantity, needString);
                if (!DataUtils.isNullOrEmpty(error)) return error;

                // Validate unitPrice phải nhỏ hơn phần tử trước
                error = validateUnitPrice(info, previousUnitPrice, needString);
                if (!DataUtils.isNullOrEmpty(error)) return error;
                if (info.getPriceStep() == 1) {
                    error = validateFromLessThanToForStepPrice1(info, needString);
                    if (!DataUtils.isNullOrEmpty(error)) return error;
                }
                // Validate fromQuantity phải nhỏ hơn toQuantity (trừ trường hợp toQuantity là null)
                error = validateFromLessThanTo(info, needString);
                if (!DataUtils.isNullOrEmpty(error)) return error;

                // Validate chỉ có StepPriceInfo cuối cùng mới được có toQuantity là null
                error = isLastStepWithNullToQuantity(info, i, sortedList.size(), needString);
                if (!DataUtils.isNullOrEmpty(error)) return error;
                // Cập nhật giá trị cho vòng lặp tiếp theo
                expectedPriceStep++;
                previousUnitPrice = info.getUnitPrice();
                previousToQuantity = info.getToQuantity();
                expectedFromQuantity = updateExpectedFromQuantity(info);
            }
        }
        return null;
    }

    private List<StepPriceInfo> sortStepPriceInfoSet(Set<StepPriceInfo> stepPriceInfoSet) {
        List<StepPriceInfo> sortedList = new ArrayList<>(stepPriceInfoSet);
        sortedList.sort(Comparator.comparing(StepPriceInfo::getPriceStep));
        return sortedList;
    }

    private String validatePriceStep(StepPriceInfo info, int expectedPriceStep, boolean needString) {
        if (info.getPriceStep() == null || info.getPriceStep() != expectedPriceStep) {
            if (needString) {
                return ErrorCodeResponse.INVALID_PRICESTEP_MUST_START_FROM_1_AND_INCREASE_SEQUENTIALLY.getMessageI18N();
            } else {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_PRICESTEP_MUST_START_FROM_1_AND_INCREASE_SEQUENTIALLY);
            }
        }
        return null;
    }

    private String validateFromQuantity(StepPriceInfo info, int expectedFromQuantity, boolean needString) {
        if (info.getFromQuantity() == null || info.getFromQuantity() != expectedFromQuantity) {
            if (needString) {
                return ErrorCodeResponse.INVALID_FROMQUANTITY_MUST_START_FROM_MIN_PURCHASE_AND_BE_SEQUENTIAL.getMessageI18N();
            } else {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_FROMQUANTITY_MUST_START_FROM_MIN_PURCHASE_AND_BE_SEQUENTIAL);
            }
        }
        return null; // Không có lỗi
    }


    private String validatePreviousToQuantity(StepPriceInfo info, Integer previousToQuantity, boolean needString) {
        if (previousToQuantity != null && info.getFromQuantity() != previousToQuantity + 1) {
            if (needString) {
                return ErrorCodeResponse.INVALID_FROMQUANTITY_MUST_EQUAL_TOQUANTITY_PLUS_1_OF_PREVIOUS_ELEMENT.getMessageI18N();
            } else {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_FROMQUANTITY_MUST_EQUAL_TOQUANTITY_PLUS_1_OF_PREVIOUS_ELEMENT);
            }
        }
        return null; // Không có lỗi
    }


    private String validateUnitPrice(StepPriceInfo info, BigDecimal previousUnitPrice, boolean needString) {
        if (previousUnitPrice != null && info.getUnitPrice().compareTo(previousUnitPrice) >= 0) {
            if (needString) {
                return ErrorCodeResponse.INVALID_UNITPRICE_MUST_BE_LESS_THAN_UNITPRICE_OF_PREVIOUS_ELEMENT.getMessageI18N();
            } else {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_UNITPRICE_MUST_BE_LESS_THAN_UNITPRICE_OF_PREVIOUS_ELEMENT);
            }
        }
        return null; // Không có lỗi
    }


    private String validateFromLessThanTo(StepPriceInfo info, boolean needString) {
        if (info.getToQuantity() != null && info.getFromQuantity() >= info.getToQuantity()) {
            if (needString) {
                return ErrorCodeResponse.INVALID_FROMQUANTITY_MUST_BE_LESS_THAN_TOQUANTITY_UNLESS_TOQUANTITY_IS_NULL.getMessageI18NParam(
                        info.getFromQuantity().toString(), DataUtils.safeToString(info.getToQuantity() + 1)
                );
            } else {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_FROMQUANTITY_MUST_BE_LESS_THAN_TOQUANTITY_UNLESS_TOQUANTITY_IS_NULL,
                        info.getFromQuantity().toString(), info.getToQuantity().toString());
            }
        }
        return null; // Không có lỗi
    }

    private String validateFromLessThanToForStepPrice1(StepPriceInfo info, boolean needString) {
        if (info.getToQuantity() != null && info.getFromQuantity() >= info.getToQuantity()) {
            if (needString) {
                return Translator.toLocale("invalid.step.price.1.must.greater.than.minquantity");
            } else {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_FROMQUANTITY_MUST_BE_LESS_THAN_TOQUANTITY_UNLESS_TOQUANTITY_IS_NULL,
                        info.getFromQuantity().toString(), info.getToQuantity().toString());
            }
        }
        return null; // Không có lỗi
    }

    private String isLastStepWithNullToQuantity(StepPriceInfo info, int currentIndex, int totalSize, boolean needString) {
        if (info.getToQuantity() == null && currentIndex != totalSize - 1) {
            if (needString) {
                return ErrorCodeResponse.INVALID_ONLY_LAST_STEPPRICEINFO_CAN_HAVE_TOQUANTITY_NULL.getMessageI18N();
            } else {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_ONLY_LAST_STEPPRICEINFO_CAN_HAVE_TOQUANTITY_NULL);
            }
        }
        return null; // Không có lỗi
    }


    private int updateExpectedFromQuantity(StepPriceInfo info) {
        return info.getToQuantity() != null ? info.getToQuantity() + 1 : 0;
    }

    private String validateLastToQuantityNull(boolean lastToQuantityNull, boolean needString) {
        if (!lastToQuantityNull) {
            if (needString) {
                return ErrorCodeResponse.INVALID_LAST_STEPPRICEINFO_MUST_HAVE_TOQUANTITY_NULL.getMessageI18N();
            } else {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_LAST_STEPPRICEINFO_MUST_HAVE_TOQUANTITY_NULL);
            }
        }
        return null; // Không có lỗi
    }


    private boolean validateDates(Instant fromDate, Instant toDate) {
        // Ngày hiện tại
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC).withHour(0).withMinute(0).withSecond(0).withNano(0);
        // Ngày từ hôm nay trừ 90 ngày
        LocalDateTime maxFromDate = now.minusDays(Constants.MAX_DAYS_DIFF);
        // Chuyển đổi Instant sang LocalDateTime với thời gian 00:00:00
        LocalDateTime fromDateTime = LocalDateTime.ofInstant(fromDate, ZoneOffset.UTC).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime toDateTime = LocalDateTime.ofInstant(toDate, ZoneOffset.UTC).withHour(0).withMinute(0).withSecond(0).withNano(0);
        if (fromDateTime.isAfter(now)) {
            return false; // Không cho phép chọn ngày "Từ ngày" trong tương lai
        }
        if (toDateTime.isAfter(now)) {
            return false; // Không cho phép chọn ngày "Đến ngày" trong tương lai
        }
        if (fromDateTime.isAfter(toDateTime)) {
            return false; // Không cho phép chọn "Từ ngày" lớn hơn "Đến ngày"
        }
        if (ChronoUnit.DAYS.between(fromDateTime, toDateTime) > Constants.MAX_DAYS_DIFF) {
            return false; // Đảm bảo khoảng cách không vượt quá 90 ngày
        }
        // Kiểm tra "Từ ngày" có nằm trong khoảng cho phép không
        return !fromDateTime.isBefore(maxFromDate);
    }


    private ProductEntity findOne(Long id) {
        VipoUserDetails user = getCurrentUser();
        checkMerchant();
        ProductEntity product = repo.findByIdAndMerchantIdAndIsDeleted(id, user.getId(), 0);
        if (DataUtils.isNullOrEmpty(product)) {
            throw new VipoBusinessException(ErrorCodeResponse.COMMON_NOT_FOUND_ID, id.toString());
        }
        return product;
    }

    private CategoryEntity findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new VipoBusinessException(ErrorCodeResponse.COMMON_NOT_FOUND_ID, id.toString()));
    }

    private void checkValidStatus(List<ProductStatus> validStatus, ProductStatus status) {
        if (!validStatus.contains(status)) {
            throw new VipoBusinessException(ErrorCodeResponse.INVALID_STATUS);
        }
    }

    private void checkValidType(List<ProductReasonType> validStatus, ProductReasonType status) {
        if (!validStatus.contains(status)) {
            throw new VipoBusinessException(ErrorCodeResponse.INVALID_TYPE);
        }
    }

    private String validateRequiredImage(Set<ProductAttributesInfo> productAttributesInfos, boolean needString) {
        int count = 0;
        for (ProductAttributesInfo i : productAttributesInfos) {
            if (i.isHaveImage()) {
                count++;
            }
        }
        if (!(count == 0 || count == productAttributesInfos.size())) {
            if (needString) {
                return ErrorCodeResponse.REQUIRED_FIELD.getMessageI18N();
            } else {
                throw new VipoBusinessException(ErrorCodeResponse.REQUIRED_FIELD, "nameAndImage");
            }
        }
        return null;
    }

    @Override
    @Transactional
    public ImportByFileResultRes createByFile(MultipartFile req) {
        if (!isValidExcelFile(req)) {
            throw new VipoBusinessException(ErrorCodeResponse.INVALID_REQUIRED_FIELD);
        }
        checkMerchant();
        Pair<List<ProductCreateByFileExcel>, Integer> resultInsert = processExcel(req);
        List<ProductCreateByFileExcel> resultExcel = resultInsert.getLeft();
        long totalCount = resultInsert.getRight();
        long successCount = resultExcel.stream().filter(ProductCreateByFileExcel::getIsValid).count();
        String linkResult = exportLinkResult(req, resultExcel);
        return ImportByFileResultRes.builder().fileName(req.getOriginalFilename())
                .totalCount(totalCount)
                .resultFileLink(linkResult)
                .failureCount(totalCount - successCount)
                .successCount(successCount)
                .build();
    }

    private String exportLinkResult(MultipartFile file, List<ProductCreateByFileExcel> resultExcel) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) { // Sử dụng ByteArrayOutputStream

            Sheet sheet = workbook.getSheetAt(1); // Lấy sheet đầu tiên

            // Duyệt qua từng hàng và dịch chuyển các cột sang phải
            for (Row row : sheet) {
                int lastColumnIndex = row.getLastCellNum(); // Lấy số cột cuối cùng

                // Duyệt qua từng ô từ phải qua trái để dịch chuyển cột
                for (int i = lastColumnIndex; i > 0; i--) {
                    Cell oldCell = row.getCell(i - 1); // Cột cũ
                    Cell newCell = row.createCell(i);  // Cột mới
                    if (oldCell != null) {
                        copyCellWithStyle(oldCell, newCell); // Sao chép dữ liệu và định dạng
                    }
                }

                // Thêm dữ liệu vào cột đầu tiên và sao chép định dạng từ cột gần đó (vd: cột thứ 2)
                Cell firstCell = row.createCell(0); // Tạo cột mới ở vị trí đầu tiên
                Cell cellToCopyStyle = row.getCell(1); // Cột từ đó sao chép định dạng

                if (cellToCopyStyle != null) {
                    firstCell.setCellStyle(cellToCopyStyle.getCellStyle()); // Sao chép định dạng từ ô gần đó
                }

                if (row.getRowNum() == 0) {
                    // Nếu là hàng tiêu đề, thêm tiêu đề cho cột mới
                    firstCell.setCellValue(Translator.toLocale("common.result"));
                } else if (row.getRowNum() >= 4) { // Bắt đầu từ dòng thứ 4 (row index 3)
                    int excelIndex = row.getRowNum() - 4; // Tính toán index trong danh sách `resultExcel`
                    if (excelIndex < resultExcel.size()) {
                        // Lấy giá trị `result` từ `ProductCreateByFileExcel` tại dòng tương ứng
                        ProductCreateByFileExcel productData = resultExcel.get(excelIndex);
                        firstCell.setCellValue(productData.getResult());
                    } else {
                        // Trường hợp không có dữ liệu tương ứng
                        firstCell.setCellValue("");
                    }
                }
            }
            // Ghi file Excel đã chỉnh sửa vào ByteArrayOutputStream
            workbook.write(outputStream);
            // Chuyển ByteArrayOutputStream thành InputStream
            InputStream newInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            String nameFile = "resultCreateProdByFile" + DateUtils.converTimeToString(new Date(), DateUtils.yyyyMMddHHmmssSSS)
                    + ".xlsx";
            if (!bypassLogic) {
                // Đẩy lên Amazon S3
                CustomMultipartFile mockMultipartFile = new CustomMultipartFile(nameFile, newInputStream);
                return amazonS3Service.uploadFile(mockMultipartFile, true);
            } else {
// Bước 4: Ghi file Excel đã chỉnh sửa vào thư mục "Download" của máy cục bộ
                String downloadDir = "D:/Users/vtp-os-75/Downloads/" + nameFile; // Đường dẫn đến thư mục Download

                try (FileOutputStream fileOut = new FileOutputStream(downloadDir)) {
                    workbook.write(fileOut); // Ghi dữ liệu vào file Excel trong thư mục Download
                }

                return "File đã được lưu thành công tại: " + downloadDir;
            }
        } catch (Exception e) {
            throw new VipoBusinessException(ErrorCodeResponse.IO_EXCEPTION);
        }
    }


    private void copyCellWithStyle(Cell oldCell, Cell newCell) {
        // Sao chép giá trị theo kiểu dữ liệu của ô
        switch (oldCell.getCellType()) {
            case STRING:
                newCell.setCellValue(oldCell.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(oldCell)) {
                    newCell.setCellValue(oldCell.getDateCellValue());
                } else {
                    newCell.setCellValue(oldCell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                newCell.setCellValue(oldCell.getBooleanCellValue());
                break;
            case FORMULA:
                newCell.setCellFormula(oldCell.getCellFormula());
                break;
            default:
                newCell.setCellValue(oldCell.getStringCellValue());
        }

        // Sao chép style của ô cũ sang ô mới
        newCell.setCellStyle(oldCell.getCellStyle());
    }

    private Pair<List<ProductCreateByFileExcel>, Integer> processExcel(MultipartFile file) {
        List<ProductCreateByFileExcel> result;
        List<ProductSkuCreateByFileExcel> resultSku;
        Integer totalRow;
        try (Workbook workbook = Constants.XLS.equalsIgnoreCase(file.getContentType())
                ? new HSSFWorkbook(file.getInputStream())
                : new XSSFWorkbook(file.getInputStream())) {
            int numberOfSheets = workbook.getNumberOfSheets();
            // Nếu số lượng sheet nhỏ hơn 3, ném ra ngoại lệ hoặc trả về lỗi
            if (numberOfSheets < 3) {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_STRUCTURE_EXCEL);
            }
            Sheet sheetSecond = workbook.getSheetAt(1);
            Sheet sheetThird = workbook.getSheetAt(2);
            Pair<List<ProductCreateByFileExcel>, Integer> pair = processProductInfo(sheetSecond, sheetSecond.getRow(0));
            result = pair.getLeft();
            totalRow = pair.getRight();
            resultSku = processSkuInfo(sheetThird, sheetThird.getRow(0), result);
            Map<String, List<ProductSkuCreateByFileExcel>> mapResultSku =
                    resultSku.stream()
                            .collect(Collectors.groupingBy(ProductSkuCreateByFileExcel::getProductCodeCustomer));
            Map<String, CategoryEntity> categoryCodesValid = categoryRepository.findAllByCode(
                    result.stream().filter(ProductCreateByFileExcel::getIsValid).map(i ->
                                    i.getBaseProductInfo()
                                            .getCategoryCode())
                            .collect(Collectors.toList())
            ).stream().collect(Collectors.toMap(CategoryEntity::getCode, Function.identity()));
            List<String> productCodeCustomerDb = repo.findProductCustomerCodeByCodes(
                    new ArrayList<>(
                            result.stream().filter(i ->
                                            i.getIsValid()
                                                    && !DataUtils.isNullOrEmpty(i.getProductCodeStr()))
                                    .map(ProductCreateByFileExcel::getProductCodeStr).collect(Collectors.toSet()))
                    , getCurrentUser().getId(), null);
            for (ProductCreateByFileExcel product : result) {
                if (Boolean.TRUE.equals(product.getIsValid())) {
                    List<ProductSkuCreateByFileExcel> lstSku = mapResultSku.get(product.getProductCodeStr());
                    if (DataUtils.isNullOrEmpty(lstSku)) {
                        product.setIsValid(false);
                        product.setResult(Translator.toLocale("invalid.empty.sku"));
                        continue;
                    }
                    if (lstSku.stream().allMatch(ProductSkuCreateByFileExcel::getIsValid)) {
                        product.setManageProductCodeInfo(new HashSet<>(lstSku));
                    } else {
                        product.setIsValid(false);
                        product.setResult(lstSku.stream().filter(i -> !i.getIsValid())
                                .findFirst().orElseThrow(() ->
                                        new VipoBusinessException(ErrorCodeResponse.INVALID_REQUIRED_FIELD)
                                ).getResult());
                        continue;
                    }
                    if (!categoryCodesValid.containsKey(product.getBaseProductInfo().getCategoryCode())) {
                        product.setIsValid(false);
                        product.setResult(Translator.toLocale("invalid.category"));
                        continue;
                    } else {
                        CategoryEntity category = categoryCodesValid.get(product.getBaseProductInfo().getCategoryCode());
                        product.getBaseProductInfo().setCategoryId(DataUtils.safeToInt(category.getId()));
                    }
                    if (productCodeCustomerDb.contains(product.getBaseProductInfo().getProductCodeCustomer())) {
                        product.setIsValid(false);
                        product.setResult(Translator.toLocale("invalid.exist.productCodeCustomer"));
                        continue;
                    }
                    String error = validateBeforeCheckDb(product, true);
                    if (!DataUtils.isNullOrEmpty(error)) {
                        product.setIsValid(false);
                        product.setResult(error);
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof VipoBusinessException) {
                VipoBusinessException vipoException = (VipoBusinessException) e;
                throw vipoException;
            }
            throw new VipoBusinessException(ErrorCodeResponse.IO_EXCEPTION);
        }
        List<ProductCreateByFileExcel> lstInsert = result.stream().filter(ProductCreateByFileExcel::getIsValid)
                .collect(Collectors.toList());
        if (!DataUtils.isNullOrEmpty(lstInsert)) {
            insertByFile(lstInsert);
        }
        return Pair.of(result, totalRow);
    }

    private Pair<List<ProductCreateByFileExcel>, Integer> processProductInfo(Sheet sheet, Row headerRow) {
        validateSheet(sheet, headerRow);
        AtomicReference<Integer> totalRow = new AtomicReference<>(0);
        List<ProductCreateByFileExcel> result = new ArrayList<>();
        Set<String> existingProductCodes = new HashSet<>();
        IntStream.range(4, sheet.getLastRowNum() + 1).forEach(index -> {
            Row row = sheet.getRow(index);
            if (DataUtils.isNullOrEmpty(row)) return;
            ProductCreateByFileExcel param = setParam(row);
            if (param.isAllFieldsNullOrEmpty()) {
                ProductCreateByFileExcel productCreateByFileExcel = new ProductCreateByFileExcel();
                productCreateByFileExcel.setIsValid(false);
                result.add(productCreateByFileExcel);
                return;
            }
            String error = getErrorStringParam(param, existingProductCodes);
            if (!DataUtils.isNullOrEmpty(error)) {
                param.setResult(error);
                param.setIsValid(false);
            } else {
                param.setResult(Translator.toLocale("common.success"));
                param.setIsValid(true);
                param.setValuesFromStrings();
            }
            totalRow.getAndSet(totalRow.get() + 1);
            result.add(param);
        });
        return Pair.of(result, totalRow.get());
    }


    private List<ProductSkuCreateByFileExcel> processSkuInfo(Sheet sheet, Row headerRow,
                                                             List<ProductCreateByFileExcel> input) {
        validateSheetSku(sheet, headerRow);
        List<ProductSkuCreateByFileExcel> sku = new ArrayList<>();
        Set<String> productCodeValid = input.stream().filter(ProductCreateByFileExcel::getIsValid)
                .map(ProductCreateByFileExcel::getProductCodeStr)
                .collect(Collectors.toSet());
        Map<String, ProductCreateByFileExcel> productValid = input.stream().filter(ProductCreateByFileExcel::getIsValid)
                .collect(Collectors.toMap(ProductCreateByFileExcel::getProductCodeStr, Function.identity()));
        IntStream.range(4, sheet.getLastRowNum() + 1).forEach(index -> {
            Row row = sheet.getRow(index);
            if (DataUtils.isNullOrEmpty(row)) return;
            ProductSkuCreateByFileExcel param = setParamSku(row);
            if (param.areAllFieldsNull()) return;
            if (!productCodeValid.contains(param.getProductCodeCustomer())) {
                return;
            }
            String error = getErrorStringParamSku(param, productValid);
            if (!DataUtils.isNullOrEmpty(error)) {
                param.setResult(error);
                param.setIsValid(Boolean.FALSE);
            } else {
                param.setValuesFromStrings();
                param.setIsValid(Boolean.TRUE);
            }
            sku.add(param);
        });
        return sku;
    }

    private void validateSheet(Sheet sheet, Row headerRow) {
        if (DataUtils.isNullOrEmpty(headerRow) || headerRow.getLastCellNum() != Constants.TEMPLATE_ROW)
            throw new VipoBusinessException(ErrorCodeResponse.INVALID_HEADER);
        int numberOfRowsWithData = 0;
        for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            if (row != null && row.getFirstCellNum() != -1) {
                numberOfRowsWithData++;
            }
        }
        if (numberOfRowsWithData > Constants.LIMIT_RECORD + 4)
            throw new VipoBusinessException(ErrorCodeResponse.INVALID_NUM_RECORD, String.valueOf(Constants.LIMIT_RECORD));
    }

    private void validateSheetSku(Sheet sheet, Row headerRow) {
        if (DataUtils.isNullOrEmpty(headerRow) || headerRow.getLastCellNum() != Constants.TEMPLATE_ROW_SKU)
            throw new VipoBusinessException(ErrorCodeResponse.INVALID_HEADER);
        int numberOfRowsWithData = 0;
        for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            if (row != null && row.getFirstCellNum() != -1) {
                numberOfRowsWithData++;
            }
        }
        if (numberOfRowsWithData > Constants.LIMIT_RECORD_SKU + 4)
            throw new VipoBusinessException(ErrorCodeResponse.INVALID_NUM_RECORD, String.valueOf(Constants.LIMIT_RECORD_SKU));
    }

    private boolean isValidExcelFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                contentType.equals("application/vnd.ms-excel");
    }

    private ProductCreateByFileExcel setParam(Row row) {
        ProductCreateByFileExcel param = new ProductCreateByFileExcel();

        // Mã sản phẩm
        param.setProductCodeStr(DataUtils.isNullOrEmpty(row.getCell(0))
                ? null : ExcelUtils.getCellValue(row.getCell(0)));
        // Tên hiển thị
        param.setDisplayNameStr(DataUtils.isNullOrEmpty(row.getCell(1))
                ? null : ExcelUtils.getCellValue(row.getCell(1)));
        // Tên đầy đủ
        param.setFullNameStr(DataUtils.isNullOrEmpty(row.getCell(2))
                ? null : ExcelUtils.getCellValue(row.getCell(2)));
        // Ngành hàng
        param.setCategoryIdStr(DataUtils.isNullOrEmpty(row.getCell(3))
                ? null : ExcelUtils.getCellValue(row.getCell(3)));
        // Ảnh đại diện sản phẩm
        param.setProductThumbnailStr(DataUtils.isNullOrEmpty(row.getCell(4))
                ? null : ExcelUtils.getCellValue(row.getCell(4)));
        // Ảnh/Video chi tiết 1-5
        param.setProductMedia1Str(DataUtils.isNullOrEmpty(row.getCell(5))
                ? null : ExcelUtils.getCellValue(row.getCell(5)));
        param.setProductMedia2Str(DataUtils.isNullOrEmpty(row.getCell(6))
                ? null : ExcelUtils.getCellValue(row.getCell(6)));
        param.setProductMedia3Str(DataUtils.isNullOrEmpty(row.getCell(7))
                ? null : ExcelUtils.getCellValue(row.getCell(7)));
        param.setProductMedia4Str(DataUtils.isNullOrEmpty(row.getCell(8))
                ? null : ExcelUtils.getCellValue(row.getCell(8)));
        param.setProductMedia5Str(DataUtils.isNullOrEmpty(row.getCell(9))
                ? null : ExcelUtils.getCellValue(row.getCell(9)));
        // Mô tả sản phẩm
        param.setProductDescriptionStr(DataUtils.isNullOrEmpty(row.getCell(10))
                ? null : ExcelUtils.getCellValue(row.getCell(10)));
        // Chiết khấu sàn
        param.setPlatformDiscountRateStr(DataUtils.isNullOrEmpty(row.getCell(11))
                ? null : ExcelUtils.getCellValue(row.getCell(11)));
        // Loại giá
        param.setPriceTypeStr(DataUtils.isNullOrEmpty(row.getCell(12))
                ? null : ExcelUtils.getCellValue(row.getCell(12)));
        // Giá áp dụng
        param.setAppliedPriceStr(DataUtils.isNullOrEmpty(row.getCell(13))
                ? null : ExcelUtils.getCellValue(row.getCell(13)));
        // Loại mua tối thiểu
        param.setMinPurchaseTypeStr(DataUtils.isNullOrEmpty(row.getCell(14))
                ? null : ExcelUtils.getCellValue(row.getCell(14)));
        // Tối thiểu cộng đồn sản phẩm
        param.setMinPurchaseQuantityStr(DataUtils.isNullOrEmpty(row.getCell(15))
                ? null : ExcelUtils.getCellValue(row.getCell(15)));
        // Tên thông số 1 và mô tả thông số 1
        param.setParameterName1(DataUtils.isNullOrEmpty(row.getCell(16))
                ? null : ExcelUtils.getCellValue(row.getCell(16)));
        param.setParameterDesc1(DataUtils.isNullOrEmpty(row.getCell(17))
                ? null : ExcelUtils.getCellValue(row.getCell(17)));
        // Tên thông số 2 và mô tả thông số 2
        param.setParameterName2(DataUtils.isNullOrEmpty(row.getCell(18))
                ? null : ExcelUtils.getCellValue(row.getCell(18)));
        param.setParameterDesc2(DataUtils.isNullOrEmpty(row.getCell(19))
                ? null : ExcelUtils.getCellValue(row.getCell(19)));
        // Tên thông số 3 và mô tả thông số 3
        param.setParameterName3(DataUtils.isNullOrEmpty(row.getCell(20))
                ? null : ExcelUtils.getCellValue(row.getCell(20)));
        param.setParameterDesc3(DataUtils.isNullOrEmpty(row.getCell(21))
                ? null : ExcelUtils.getCellValue(row.getCell(21)));
        // Tên thông số 4 và mô tả thông số 4
        param.setParameterName4(DataUtils.isNullOrEmpty(row.getCell(22))
                ? null : ExcelUtils.getCellValue(row.getCell(22)));
        param.setParameterDesc4(DataUtils.isNullOrEmpty(row.getCell(23))
                ? null : ExcelUtils.getCellValue(row.getCell(23)));
        // Tên thông số 5 và mô tả thông số 5
        param.setParameterName5(DataUtils.isNullOrEmpty(row.getCell(24))
                ? null : ExcelUtils.getCellValue(row.getCell(24)));
        param.setParameterDesc5(DataUtils.isNullOrEmpty(row.getCell(25))
                ? null : ExcelUtils.getCellValue(row.getCell(25)));
        // Thuộc tính 1 và phân loại cho thuộc tính 1
        param.setAttribute1(DataUtils.isNullOrEmpty(row.getCell(26))
                ? null : ExcelUtils.getCellValue(row.getCell(26)));
        param.setClassify1(DataUtils.isNullOrEmpty(row.getCell(27))
                ? null : ExcelUtils.getCellValue(row.getCell(27)));
        // Thuộc tính 2 và phân loại cho thuộc tính 2
        param.setAttribute2(DataUtils.isNullOrEmpty(row.getCell(28))
                ? null : ExcelUtils.getCellValue(row.getCell(28)));
        param.setClassify2(DataUtils.isNullOrEmpty(row.getCell(29))
                ? null : ExcelUtils.getCellValue(row.getCell(29)));
        // Thuộc tính 3 và phân loại cho thuộc tính 3
        param.setAttribute3(DataUtils.isNullOrEmpty(row.getCell(30))
                ? null : ExcelUtils.getCellValue(row.getCell(30)));
        param.setClassify3(DataUtils.isNullOrEmpty(row.getCell(31))
                ? null : ExcelUtils.getCellValue(row.getCell(31)));
        // Giá tiền thang giá 1
        param.setPriceStep1(DataUtils.isNullOrEmpty(row.getCell(32))
                ? null : ExcelUtils.getCellValue(row.getCell(32)));
        // Số lượng bắt đầu thang giá 2
        param.setQuantityStep2(DataUtils.isNullOrEmpty(row.getCell(33))
                ? null : ExcelUtils.getCellValue(row.getCell(33)));
        // Giá tiền thang giá 2
        param.setPriceStep2(DataUtils.isNullOrEmpty(row.getCell(34))
                ? null : ExcelUtils.getCellValue(row.getCell(34)));
        // Số lượng bắt đầu thang giá 3
        param.setQuantityStep3(DataUtils.isNullOrEmpty(row.getCell(35))
                ? null : ExcelUtils.getCellValue(row.getCell(35)));
        // Giá tiền thang giá 3
        param.setPriceStep3(DataUtils.isNullOrEmpty(row.getCell(36))
                ? null : ExcelUtils.getCellValue(row.getCell(36)));

        return param;
    }


    private ProductSkuCreateByFileExcel setParamSku(Row row) {
        ProductSkuCreateByFileExcel param = new ProductSkuCreateByFileExcel();

        param.setProductCodeCustomer(DataUtils.isNullOrEmpty(row.getCell(0))
                ? null : ExcelUtils.getCellValue(row.getCell(0)));

        param.setImageSku(DataUtils.isNullOrEmpty(row.getCell(1))
                ? null : ExcelUtils.getCellValue(row.getCell(1)));

        param.setClassifyInfo(DataUtils.isNullOrEmpty(row.getCell(2))
                ? null : ExcelUtils.getCellValue(row.getCell(2)));

        param.setUnitPriceStr(DataUtils.isNullOrEmpty(row.getCell(3))
                ? null : ExcelUtils.getCellValue(row.getCell(3)));

        param.setStockStr(DataUtils.isNullOrEmpty(row.getCell(4))
                ? null : ExcelUtils.getCellValue(row.getCell(4)));

        param.setMinPurchaseStr(DataUtils.isNullOrEmpty(row.getCell(5))
                ? null : ExcelUtils.getCellValue(row.getCell(5)));

        param.setWeightStr(DataUtils.isNullOrEmpty(row.getCell(6))
                ? null : ExcelUtils.getCellValue(row.getCell(6)));

        param.setLengthStr(DataUtils.isNullOrEmpty(row.getCell(7))
                ? null : ExcelUtils.getCellValue(row.getCell(7)));

        param.setWidthStr(DataUtils.isNullOrEmpty(row.getCell(8))
                ? null : ExcelUtils.getCellValue(row.getCell(8)));

        param.setHeightStr(DataUtils.isNullOrEmpty(row.getCell(9))
                ? null : ExcelUtils.getCellValue(row.getCell(9)));

        param.setShippingFeeStr(DataUtils.isNullOrEmpty(row.getCell(10))
                ? null : ExcelUtils.getCellValue(row.getCell(10)));

        return param;
    }


    private String getErrorStringParam(ProductCreateByFileExcel param, Set<String> productCode) {
        String fail;
        // Validate mã sản phẩm
        fail = validateProductCode(param.getProductCodeStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateProductCodeDuplicate(productCode, param.getProductCodeStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        // Validate tên hiển thị
        fail = validateDisplayName(param.getDisplayNameStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateFullName(param.getFullNameStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        // Validate ngành hàng
        fail = validateCategoryCode(param.getCategoryIdStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateProductThumbnail(param.getProductThumbnailStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateProductMedia(param.getProductMedia1Str(), false, "1");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateProductMedia(param.getProductMedia2Str(), false, "2");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateProductMedia(param.getProductMedia3Str(), true, "3");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateProductMedia(param.getProductMedia4Str(), true, "4");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateProductMedia(param.getProductMedia5Str(), true, "5");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateProductDescription(param.getProductDescriptionStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validatePlatformDiscountRate(param.getPlatformDiscountRateStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validatePriceType(param.getPriceTypeStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateAppliedPrice(param.getAppliedPriceStr(), param.getPriceTypeStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateMinPurchaseType(param.getMinPurchaseTypeStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateMinPurchaseQuantity(param.getMinPurchaseQuantityStr(), param.getPriceTypeStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateParameterName(param.getParameterName1(), false, "1");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateParameterName(param.getParameterName2(), false, "2");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateParameterName(param.getParameterName3(), false, "3");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateParameterName(param.getParameterName4(), true, "4");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateParameterName(param.getParameterName5(), true, "5");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateUniqueParameterNames(param.getParameterName1(),
                param.getParameterName2(),
                param.getParameterName3(),
                param.getParameterName4(),
                param.getParameterName5()
        );
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateParameterDesc(param.getParameterDesc1(), false, "1");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateParameterDesc(param.getParameterDesc2(), false, "2");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateParameterDesc(param.getParameterDesc3(), false, "3");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateParameterDesc(param.getParameterDesc4(), true, "4");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateParameterDesc(param.getParameterDesc5(), true, "5");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateParameterPairs(param.getParameterName4(), param.getParameterDesc4());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateParameterPairs(param.getParameterName5(), param.getParameterDesc5());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateAttribute(param.getAttribute1(), false, "1");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateAttribute(param.getAttribute2(), true, "2");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateAttribute(param.getAttribute3(), true, "3");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateUniqueAttributes(param.getAttribute1(), param.getAttribute2(), param.getAttribute3());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateClassify(param.getClassify1(), false, "1");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateClassify(param.getClassify2(), true, "2");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateClassify(param.getClassify3(), true, "3");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateClassifyUnique(param.getClassify1());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateClassifyUnique(param.getClassify2());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateClassifyUnique(param.getClassify3());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateAttributeClassify(param.getAttribute2(), param.getClassify2());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateAttributeClassify(param.getAttribute3(), param.getClassify3());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validatePriceStep(param.getPriceStep1(), param.getPriceTypeStr(), false, "1");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validatePriceStep(param.getPriceStep2(), param.getPriceTypeStr(), false, "2");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validatePriceStep(param.getPriceStep3(), param.getPriceTypeStr(), true, "3");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateQuantityStep(param.getQuantityStep2(), param.getPriceTypeStr(), param.getMinPurchaseQuantityStr(), false, "2");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateQuantityStep(param.getQuantityStep3(), param.getPriceTypeStr(), param.getMinPurchaseQuantityStr(), false, "3");
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateSupportType(param.getMinPurchaseTypeStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        return fail;
    }

    private String validateSupportType(String minPurchaseType) {
        if (!DataUtils.isNullOrEmpty(minPurchaseType) &&
                minPurchaseType.equals(EACH_SKU_OF_PRODUCT.toString())) {
            return Translator.toLocale("invalid.support.minPurchaseType.each.sku");
        }
        return null;
    }

    private String validateProductCode(String productCode) {
        // Kiểm tra mã sản phẩm có được nhập không
        if (productCode == null || productCode.isEmpty()) {
            return Translator.toLocale("invalid.productCode");
        }
        // Kiểm tra độ dài của mã sản phẩm
        if (productCode.length() < 3 || productCode.length() > 12) {
            return Translator.toLocale("invalid.productCode");
        }
        // Kiểm tra xem mã sản phẩm có chứa các ký tự Tiếng Việt
        if (!productCode.matches("^[a-zA-Z0-9\\p{Punct}]+$")) {
            return Translator.toLocale("invalid.productCode");
        }

        return null;
    }

    private String validateDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return Translator.toLocale("invalid.displayName");
        }
        if (displayName.length() < 3 || displayName.length() > 120) {
            return Translator.toLocale("invalid.displayName");
        }
        if (!displayName.matches("^(?=.*[\\p{L}0-9]).*$")) {
            return Translator.toLocale("invalid.displayName");
        }
        return null;
    }

    private String validateFullName(String fullName) {
        // Kiểm tra tên đầy đủ có được nhập không
        if (fullName == null || fullName.isEmpty()) {
            return Translator.toLocale("invalid.fullName");
        }
        // Kiểm tra độ dài của tên đầy đủ
        if (fullName.length() < 3 || fullName.length() > 120) {
            return Translator.toLocale("invalid.fullName");
        }
        // Kiểm tra xem tên đầy đủ có phải là tiếng Việt có dấu không
        if (!fullName.matches("^(?=.*[\\p{L}0-9]).*$")) {
            return Translator.toLocale("invalid.fullName");
        }
        return null;
    }

    private String validateCategoryCode(String categoryId) {
        // Kiểm tra ngành hàng có được nhập không
        if (categoryId == null || categoryId.isEmpty()) {
            return Translator.toLocale("invalid.categoryId");
        }
        return null;
    }

    private String validateProductThumbnail(String productThumbnail) {
        // Kiểm tra nếu thumbnail trống
        if (productThumbnail == null || productThumbnail.isEmpty()) {
            return Translator.toLocale("invalid.productThumbnail");
        }
        // Kiểm tra nếu thumbnail là URL hợp lệ (Bạn có thể mở rộng việc kiểm tra URL)
        if (!isURLImage(productThumbnail)) {
            return Translator.toLocale("invalid.productThumbnail");
        }
        return null;
    }

    private String validateProductMedia(String productMedia, boolean nullAble, String stt) {
        // Kiểm tra nếu productMedia1 trống
        if ((productMedia == null || productMedia.isEmpty()) && !nullAble) {
            return Translator.toLocale("invalid.productMedia" + stt);
        }
        if (!DataUtils.isNullOrEmpty(productMedia) && !isURLImageVid(productMedia)) {
            return Translator.toLocale("invalid.productMedia" + stt);
        }
        return null;
    }

    private String validateProductDescription(String productDescription) {
        // Kiểm tra nếu productDescription trống
        if (productDescription == null || productDescription.isEmpty()) {
            return Translator.toLocale("invalid.productDescription");
        }
        // Kiểm tra độ dài của productDescription
        int length = productDescription.length();
        if (length < 3 || length > 10000) {
            return Translator.toLocale("invalid.productDescription");
        }
        // Trả về null nếu không có lỗi
        return null;
    }

    private String validatePlatformDiscountRate(String platformDiscountRate) {
        // Kiểm tra nếu platformDiscountRate trống
        if (DataUtils.isNullOrEmpty(platformDiscountRate)) {
            return Translator.toLocale("invalid.platformDiscountRate");
        }
        try {
            // Chuyển đổi giá trị nhập vào thành số thực
            double discountRate = Double.parseDouble(platformDiscountRate);
            // Kiểm tra giá trị có nằm trong khoảng từ 0 đến 100 hay không
            if (discountRate < 0 || discountRate > 100) {
                return Translator.toLocale("invalid.platformDiscountRate");
            }
            // Kiểm tra nếu có quá 2 chữ số sau dấu thập phân
            if (platformDiscountRate.contains(".")) {
                String[] parts = platformDiscountRate.split("\\.");
                if (parts.length > 1 && parts[1].length() > 2) {
                    return Translator.toLocale("invalid.platformDiscountRate");
                }
            }
        } catch (NumberFormatException e) {
            // Nếu không thể chuyển đổi giá trị thành số thực, trả về lỗi định dạng
            return Translator.toLocale("invalid.platformDiscountRate");
        }
        // Trả về null nếu không có lỗi
        return null;
    }

    private String validatePriceType(String priceType) {
        // Kiểm tra nếu priceType trống
        if (priceType == null) {
            return Translator.toLocale("invalid.priceType");
        }
        // Kiểm tra nếu priceType chỉ có thể là 0, 1 hoặc 2
        if (!(priceType.equals("0") || priceType.equals("1")
                || priceType.equals("2"))) {
            return Translator.toLocale("invalid.priceType");
        }
        return null;
    }

    private String validateAppliedPrice(String appliedPrice, String priceType) {
        // Kiểm tra nếu loại giá là "Đồng giá SKU" (priceType = 0)
        if (priceType != null && priceType.equals("0")) {
            // Kiểm tra nếu appliedPrice trống
            if (appliedPrice == null || appliedPrice.isEmpty()) {
                return Translator.toLocale("invalid.appliedPrice");
            }
        }
        if (!DataUtils.isNullOrEmpty(appliedPrice)) {
            try {
                // Chuyển đổi appliedPrice thành BigDecimal
                BigDecimal price = new BigDecimal(appliedPrice);

                // Kiểm tra giá trị có nằm trong khoảng từ 100 đến 100,000,000 hay không
                BigDecimal minPrice = new BigDecimal("100");
                BigDecimal maxPrice = new BigDecimal("100000000");

                if (price.compareTo(minPrice) < 0 || price.compareTo(maxPrice) > 0) {
                    return Translator.toLocale("invalid.appliedPrice");
                }
            } catch (NumberFormatException | ArithmeticException e) {
                // Nếu không thể chuyển đổi giá trị thành BigDecimal, trả về lỗi định dạng
                return Translator.toLocale("invalid.appliedPrice");
            }
        }
        return null;
    }


    private String validateMinPurchaseType(String minPurchaseType) {
        // Kiểm tra nếu minPurchaseType trống
        if (minPurchaseType == null || minPurchaseType.isEmpty()) {
            return Translator.toLocale("invalid.minPurchaseType");
        }
        // Kiểm tra nếu minPurchaseType chỉ có thể là "0" hoặc "1"
        if (!minPurchaseType.equals("0") && !minPurchaseType.equals("1")) {
            return Translator.toLocale("invalid.minPurchaseType");
        }

        return null;
    }

    private String validateMinPurchaseQuantity(String minPurchaseQuantity, String priceType) {
        // Kiểm tra nếu Loại giá là 0 hoặc 1
        if (priceType != null && (priceType.equals(DataUtils.safeToString(FIXED_PRICE_SKU_WITH_PRICE_STEP))
                || priceType.equals(DataUtils.safeToString(FIXED_PRICE_SKU_NO_PRICE_STEP)))) {
            // Kiểm tra nếu minPurchaseQuantity trống
            if (minPurchaseQuantity == null || minPurchaseQuantity.isEmpty()) {
                return Translator.toLocale("invalid.minPurchaseQuantity");
            }
        }
        if (!DataUtils.isNullOrEmpty(minPurchaseQuantity)) {
            try {
                // Chuyển đổi minPurchaseQuantity thành số nguyên
                int quantity = Integer.parseInt(minPurchaseQuantity);

                // Kiểm tra giá trị có nằm trong khoảng từ 1 đến 10,000 hay không
                if (quantity < 1 || quantity > 10000) {
                    return Translator.toLocale("invalid.minPurchaseQuantity");
                }
            } catch (NumberFormatException e) {
                // Nếu không thể chuyển đổi giá trị thành số nguyên, trả về lỗi định dạng
                return Translator.toLocale("invalid.minPurchaseQuantity");
            }
        }
        // Trả về null nếu không có lỗi
        return null;
    }

    private String validateParameterName(String parameterName, boolean nullAble, String stt) {
        if ((parameterName == null || parameterName.isEmpty()) && !nullAble) {
            return Translator.toLocale("invalid.parameterName" + stt);
        }
        // Kiểm tra độ dài của parameterName
        if (!DataUtils.isNullOrEmpty(parameterName)
                && (parameterName.length() < 3 || parameterName.length() > 100)) {
            return Translator.toLocale("invalid.parameterName" + stt);
        }
        return null;
    }

    private String validateUniqueParameterNames(String parameterName1, String parameterName2,
                                                String parameterName3, String parameterName4,
                                                String parameterName5) {
        Set<String> parameterSet = new HashSet<>();
        if (!DataUtils.isNullOrEmpty(parameterName1)) {
            parameterSet.add(parameterName1);
        }
        if (!DataUtils.isNullOrEmpty(parameterName2)) {
            parameterSet.add(parameterName2);
        }
        if (!DataUtils.isNullOrEmpty(parameterName3)) {
            parameterSet.add(parameterName3);
        }
        if (!DataUtils.isNullOrEmpty(parameterName4)) {
            parameterSet.add(parameterName4);
        }
        if (!DataUtils.isNullOrEmpty(parameterName5)) {
            parameterSet.add(parameterName5);
        }
        // Kiểm tra nếu số lượng phần tử trong Set nhỏ hơn số lượng thông số không rỗng
        int nonEmptyParameterCount = 0;
        if (!DataUtils.isNullOrEmpty(parameterName1)) nonEmptyParameterCount++;
        if (!DataUtils.isNullOrEmpty(parameterName2)) nonEmptyParameterCount++;
        if (!DataUtils.isNullOrEmpty(parameterName3)) nonEmptyParameterCount++;
        if (!DataUtils.isNullOrEmpty(parameterName4)) nonEmptyParameterCount++;
        if (!DataUtils.isNullOrEmpty(parameterName5)) nonEmptyParameterCount++;
        if (parameterSet.size() < nonEmptyParameterCount) {
            return Translator.toLocale("spec.name.must.be.unique");
        }

        // Trả về null nếu không có lỗi
        return null;
    }


    private String validateParameterDesc(String parameterDesc, boolean nullAble, String stt) {
        if ((parameterDesc == null || parameterDesc.isEmpty()) && !nullAble) {
            return Translator.toLocale("invalid.parameterDesc" + stt);
        }
        // Kiểm tra độ dài của parameterName
        if (!DataUtils.isNullOrEmpty(parameterDesc)
                && (parameterDesc.length() < 3 || parameterDesc.length() > 255)) {
            return Translator.toLocale("invalid.parameterDesc" + stt);
        }
        return null;
    }

    private String validateParameterPairs(String parameterName, String parameterDesc) {
        // Kiểm tra nếu một trong hai trường tồn tại mà trường còn lại không tồn tại
        if ((parameterName != null && !parameterName.isEmpty() && (parameterDesc == null || parameterDesc.isEmpty())) ||
                (parameterDesc != null && !parameterDesc.isEmpty() && (parameterName == null || parameterName.isEmpty()))) {
            return Translator.toLocale("invalid.pair.parameter.name.desc");
        }

        // Trả về null nếu không có lỗi
        return null;
    }

    private String validateAttributeClassify(String attribute, String classify) {
        // Kiểm tra nếu một trong hai trường tồn tại mà trường còn lại không tồn tại
        if ((attribute != null && !attribute.isEmpty() && (classify == null || classify.isEmpty())) ||
                (classify != null && !classify.isEmpty() && (attribute == null || attribute.isEmpty()))) {
            return Translator.toLocale("invalid.pair.attribute.classify");
        }

        // Trả về null nếu không có lỗi
        return null;
    }

    private String validateAttribute(String attribute, boolean nullAble, String stt) {
        if ((attribute == null || attribute.isEmpty()) && !nullAble) {
            return Translator.toLocale("invalid.attribute" + stt);
        }
        // Kiểm tra độ dài của parameterName
        if (!DataUtils.isNullOrEmpty(attribute)
                && (attribute.length() < 3 || attribute.length() > 100)) {
            return Translator.toLocale("invalid.attribute" + stt);
        }
        return null;
    }

    private String validateUniqueAttributes(String attribute1, String attribute2, String attribute3) {
        Set<String> attributesSet = new HashSet<>();
        if (!DataUtils.isNullOrEmpty(attribute1)) {
            attributesSet.add(attribute1.trim());
        }
        if (!DataUtils.isNullOrEmpty(attribute2)) {
            attributesSet.add(attribute2.trim());
        }
        if (!DataUtils.isNullOrEmpty(attribute3)) {
            attributesSet.add(attribute3.trim());
        }
        // Nếu số lượng phần tử trong Set nhỏ hơn số thuộc tính không rỗng, thì có sự trùng lặp
        int nonEmptyAttributeCount = 0;
        if (attribute1 != null && !attribute1.trim().isEmpty()) nonEmptyAttributeCount++;
        if (attribute2 != null && !attribute2.trim().isEmpty()) nonEmptyAttributeCount++;
        if (attribute3 != null && !attribute3.trim().isEmpty()) nonEmptyAttributeCount++;

        if (attributesSet.size() < nonEmptyAttributeCount) {
            return Translator.toLocale("attribute.name.must.be.unique");
        }
        return null;
    }


    private String validateClassify(String classify, boolean nullAble, String stt) {
        // Kiểm tra nếu classify trống
        if ((classify == null || classify.isEmpty()) && !nullAble) {
            return Translator.toLocale("invalid.classify" + stt);
        }
        if (!DataUtils.isNullOrEmpty(classify)) {
            // Kiểm tra xem classify có chứa ít nhất một dấu ";" để đảm bảo là danh sách phân loại
            if (classify.contains(";")) {// Tách các phân loại dựa trên dấu ";"
                String[] classifications = classify.split(";");
                // Kiểm tra từng phân loại có rỗng không
                for (String item : classifications) {
                    if (item.trim().isEmpty()) {
                        return Translator.toLocale("invalid.classify" + stt);
                    }
                    item = item.trim();
                    // Kiểm tra độ dài của từng phân loại (từ 2 đến 150 ký tự)
                    if (item.length() < 2 || item.length() > 150) {
                        return Translator.toLocale("invalid.classify" + stt);
                    }
                }
            } else {
                if (classify.length() < 2 || classify.length() > 150) {
                    return Translator.toLocale("invalid.classify" + stt);
                }
            }
        }

        // Trả về null nếu không có lỗi
        return null;
    }

    private String validateClassifyUnique(String classify) {
        if (!DataUtils.isNullOrEmpty(classify)) {
            // Tách các phân loại bằng dấu ";"
            String[] classifications = classify.split(";");
            // Tạo một tập hợp (Set) để lưu trữ các giá trị duy nhất
            Set<String> classifySet = new HashSet<>();
            // Kiểm tra từng phân loại
            for (String item : classifications) {
                String trimmedItem = item.trim(); // Loại bỏ khoảng trắng ở đầu và cuối mỗi phân loại
                if (!trimmedItem.isEmpty()) {
                    if (!classifySet.add(trimmedItem)) {
                        return Translator.toLocale("classify.name.must.be.unique");
                    }
                }
            }
        }
        return null;
    }


    private String validatePriceStep(String priceStep, String priceType, boolean nullAble, String stt) {
        // Kiểm tra nếu Loại giá là "Đồng giá SKU theo thang" (priceType = 1)
        if (DataUtils.safeToString(FIXED_PRICE_SKU_WITH_PRICE_STEP).equals(priceType)) {
            // Kiểm tra nếu priceStep trống
            if (DataUtils.isNullOrEmpty(priceStep) && !nullAble) {
                return Translator.toLocale("invalid.priceStep" + stt);
            }
            if (!DataUtils.isNullOrEmpty(priceStep)) {
                try {
                    // Chuyển đổi priceStep thành số nguyên
                    long price = Long.parseLong(priceStep);

                    // Kiểm tra giá trị có nằm trong khoảng từ 100 đến 100,000,000 hay không
                    if (price < 100 || price > 100000000) {
                        return Translator.toLocale("invalid.priceStep" + stt);
                    }
                } catch (NumberFormatException e) {
                    // Nếu không thể chuyển đổi priceStep thành số, trả về lỗi định dạng
                    return Translator.toLocale("invalid.priceStep" + stt);
                }
            }
        }
        // Trả về null nếu không có lỗi
        return null;
    }

    private String validateQuantityStep(String quantityStep, String priceType, String minPurchaseQuantity,
                                        boolean nullAble, String stt) {
        // Kiểm tra nếu Loại giá là "Đồng giá SKU theo thang" (priceType = 1)
        if (DataUtils.safeToString(FIXED_PRICE_SKU_WITH_PRICE_STEP).equals(priceType)) {
            // Kiểm tra nếu quantityStep trống
            if (DataUtils.isNullOrEmpty(quantityStep) && !nullAble) {
                return Translator.toLocale("invalid.quantityStep" + stt);
            }
            if (DataUtils.isNullOrEmpty(quantityStep)) {
                try {
                    // Chuyển đổi quantityStep và minPurchaseQuantity thành số nguyên
                    long quantity = Long.parseLong(quantityStep);
                    long minPurchase = Long.parseLong(minPurchaseQuantity);

                    // Kiểm tra giá trị quantityStep có nằm trong khoảng từ 100 đến 100,000,000
                    if (quantity < 100 || quantity > 100000000) {
                        return Translator.toLocale("invalid.quantityStep" + stt);
                    }

                    // Kiểm tra quantityStep phải lớn hơn minPurchaseQuantity
                    if ("2".equals(stt) && quantity <= minPurchase) {
                        return Translator.toLocale("invalid.quantityStep" + stt);
                    }
                } catch (NumberFormatException e) {
                    // Nếu không thể chuyển đổi thành số, trả về lỗi định dạng
                    return Translator.toLocale("invalid.quantityStep" + stt);
                }
            }
        }
        return null;
    }

    private boolean isURLImage(String url) {
        String urlPattern = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
        String validExtensions = "(?i)(\\.jpg|\\.jpeg|\\.png|\\.webp)$";  // Sử dụng (?i) để bỏ qua phân biệt chữ hoa/thường
        return url.matches(urlPattern) && url.matches(".*" + validExtensions);
    }


    private boolean isURLImageVid(String url) {
        // Mẫu regex để kiểm tra URL cơ bản (HTTP, HTTPS, FTP)
        String urlPattern = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";

        // Các phần mở rộng tệp tin hình ảnh và video, không phân biệt hoa/thường
        String validExtensions = "(?i)(\\.jpg|\\.jpeg|\\.png|\\.mp4|\\.webp)$";

        // Mẫu regex cho URL YouTube
        String youtubePattern = "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.?be)/.+$";

        // Kiểm tra URL có đúng định dạng chung hay không, và cũng kiểm tra nếu là tệp hình ảnh/video hoặc link YouTube
        return url.matches(urlPattern) && (url.matches(".*" + validExtensions) || url.matches(youtubePattern));
    }


    public String validateProductCodeDuplicate(Set<String> existingProductCodes, String productCodeCustomerCode) {
        if (existingProductCodes.contains(productCodeCustomerCode)) {
            return Translator.toLocale("exist.product.name");
        } else {
            // Thêm mã sản phẩm vào Set nếu chưa trùng
            existingProductCodes.add(productCodeCustomerCode);
        }
        return null;
    }

    private String getErrorStringParamSku(ProductSkuCreateByFileExcel param,
                                          Map<String, ProductCreateByFileExcel> productValid) {
        ProductCreateByFileExcel productCreateByFileExcel = productValid.get(param.getProductCodeCustomer());
        String fail = validateProductCodeCustomer(param.getProductCodeCustomer());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateImageSku(param.getClassifyInfo());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateClassifyInfo(param.getClassifyInfo());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateUnitPrice(param.getUnitPriceStr(), productCreateByFileExcel.getSellingProductInfo().getPriceType());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateStock(param.getStockStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateMinPurchase(param.getMinPurchaseStr(),
                productCreateByFileExcel.getSellingProductInfo().getMinPurchaseType(),
                param.getStockStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateWeight(param.getWeightStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateLength(param.getLengthStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateWidth(param.getWidthStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateHeight(param.getHeightStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        fail = validateShippingFee(param.getShippingFeeStr());
        if (!DataUtils.isNullOrEmpty(fail)) return fail;
        return null;
    }

    private String validateProductCodeCustomer(String productCodeCustomer) {
        // Kiểm tra nếu mã sản phẩm bị bỏ trống hoặc null (bắt buộc)
        if (productCodeCustomer == null || productCodeCustomer.trim().isEmpty()) {
            return Translator.toLocale("invalid.productCodeCustomer");
        }
        // Kiểm tra nếu độ dài của mã sản phẩm nằm trong khoảng từ 3 đến 12 ký tự
        if (productCodeCustomer.length() < 3 || productCodeCustomer.length() > 12) {
            return Translator.toLocale("invalid.productCodeCustomer");
        }
        // Kiểm tra nếu mã sản phẩm chỉ chứa chữ và số, không bao gồm ký tự tiếng Việt
        if (!productCodeCustomer.matches("^[a-zA-Z0-9\\p{Punct}]+$")) {
            return Translator.toLocale("invalid.productCodeCustomer");
        }
        return null;
    }

    private String validateImageSku(String imageSku) {
        if (DataUtils.isNullOrEmpty(imageSku)) {
            return Translator.toLocale("invalid.image.sku");
        }
        if (!DataUtils.isNullOrEmpty(imageSku) && isURLImage(imageSku)) {
            return Translator.toLocale("invalid.image.sku");
        }
        return null;
    }

    private String validateClassifyInfo(String classifyInfo) {
        // Kiểm tra xem classifyInfo có bị bỏ trống hay không (bắt buộc)
        if (DataUtils.isNullOrEmpty(classifyInfo)) {
            return Translator.toLocale("invalid.classifyInfo");
        }
        return null;
    }

    private String validateUnitPrice(String unitPriceStr, Integer priceType) {
        // Kiểm tra nếu priceType là UNIT_PRICE_BY_SKU (2)
        if (priceType != null && priceType.equals(UNIT_PRICE_BY_SKU)) {
            // Kiểm tra nếu đơn giá bị bỏ trống (bắt buộc)
            if (DataUtils.isNullOrEmpty(unitPriceStr)) {
                return Translator.toLocale("invalid.unitPriceStr");
            }
        }
        if (!DataUtils.isNullOrEmpty(unitPriceStr)) {
            try {
                // Chuyển đổi đơn giá từ String sang số nguyên
                Long unitPrice = Long.parseLong(unitPriceStr.trim());

                // Kiểm tra nếu đơn giá nằm trong khoảng từ 100 đến 100000000
                if (unitPrice < 100 || unitPrice > 100000000) {
                    return Translator.toLocale("invalid.unitPriceStr");
                }
            } catch (NumberFormatException e) {
                // Trường hợp đơn giá không phải là số hợp lệ
                return Translator.toLocale("invalid.unitPriceStr");
            }
        }
        return null;
    }

    private String validateStock(String stockStr) {
        // Kiểm tra nếu tồn kho bị bỏ trống (bắt buộc)
        if (DataUtils.isNullOrEmpty(stockStr)) {
            return Translator.toLocale("invalid.stockStr");
        }
        try {
            // Chuyển đổi tồn kho từ String sang số nguyên
            long stock = Long.parseLong(stockStr.trim());

            // Kiểm tra nếu tồn kho nằm trong khoảng từ 1 đến 100000000
            if (stock < 1 || stock > 100000000) {
                return Translator.toLocale("invalid.stockStr");
            }
        } catch (NumberFormatException e) {
            // Trường hợp tồn kho không phải là số hợp lệ
            return Translator.toLocale("invalid.stockStr");
        }
        // Không có lỗi
        return null;
    }

    private String validateMinPurchase(String minPurchaseStr, Integer minPurchaseType, String stockStr) {
        // Kiểm tra nếu minPurchaseType là EACH_SKU_OF_PRODUCT (1)
        if (minPurchaseType.equals(EACH_SKU_OF_PRODUCT)) {
            // Kiểm tra nếu giá trị mua tối thiểu bị bỏ trống (bắt buộc)
            if (DataUtils.isNullOrEmpty(minPurchaseType)) {
                return Translator.toLocale("invalid.minPurchaseStr");
            }
            if (!DataUtils.isNullOrEmpty(minPurchaseStr)) {
                try {
                    // Chuyển đổi mua tối thiểu từ String sang số nguyên
                    Long minPurchase = Long.parseLong(minPurchaseStr.trim());
                    // Kiểm tra nếu giá trị mua tối thiểu nằm trong khoảng từ 1 đến 100000000
                    if (minPurchase < 1 || minPurchase > 100000000) {
                        return Translator.toLocale("invalid.minPurchaseStr");
                    }
                    // Kiểm tra tồn kho và đảm bảo tồn kho >= mua tối thiểu
                    if (!DataUtils.isNullOrEmpty(stockStr)) {
                        Long stock = Long.parseLong(stockStr.trim());
                        if (stock < minPurchase) {
                            return Translator.toLocale("invalid.minPurchaseStr");
                        }
                    }

                } catch (NumberFormatException e) {
                    // Trường hợp mua tối thiểu không phải là số hợp lệ
                    return Translator.toLocale("invalid.minPurchaseStr");
                }
            }
        }
        // Không có lỗi
        return null;
    }

    private String validateWeight(String weightStr) {
        // Kiểm tra nếu cân nặng bị bỏ trống (bắt buộc)
        if (DataUtils.isNullOrEmpty(weightStr)) {
            return Translator.toLocale("invalid.weightStr");
        }
        try {
            Long weight = Long.parseLong(weightStr.trim());
            if (weight < 1 || weight > 100000000) {
                return Translator.toLocale("invalid.weightStr");
            }
        } catch (NumberFormatException e) {
            return Translator.toLocale("invalid.weightStr");
        }
        return null;
    }

    private String validateLength(String lengthStr) {
        // Kiểm tra nếu trường chiều dài không bắt buộc nên có thể bỏ qua nếu trống
        if (!DataUtils.isNullOrEmpty(lengthStr)) {
            try {
                // Chuyển đổi chiều dài từ String sang BigDecimal (cho phép số thập phân)
                BigDecimal length = new BigDecimal(lengthStr.trim());
                // Kiểm tra nếu chiều dài nằm trong khoảng từ 1 đến 10000
                if (length.compareTo(BigDecimal.valueOf(1)) < 0 || length.compareTo(BigDecimal.valueOf(10000)) > 0) {
                    return Translator.toLocale("invalid.lengthStr");
                }
            } catch (NumberFormatException e) {
                // Trường hợp chiều dài không phải là số hợp lệ
                return Translator.toLocale("invalid.lengthStr");
            }
        }
        // Không có lỗi
        return null;
    }

    private String validateWidth(String widthStr) {
        if (!DataUtils.isNullOrEmpty(widthStr)) {
            try {
                BigDecimal width = new BigDecimal(widthStr.trim());
                // Kiểm tra nếu chiều rộng nằm trong khoảng từ 1 đến 10000
                if (width.compareTo(BigDecimal.valueOf(1)) < 0 || width.compareTo(BigDecimal.valueOf(10000)) > 0) {
                    return Translator.toLocale("invalid.widthStr");
                }
            } catch (NumberFormatException e) {
                // Trường hợp chiều rộng không phải là số hợp lệ
                return Translator.toLocale("invalid.widthStr");
            }
        }
        // Không có lỗi
        return null;
    }

    private String validateHeight(String heightStr) {
        // Kiểm tra nếu trường chiều cao không bắt buộc nên có thể bỏ qua nếu trống
        if (!DataUtils.isNullOrEmpty(heightStr)) {
            try {
                // Chuyển đổi chiều cao từ String sang BigDecimal (cho phép số thập phân)
                BigDecimal height = new BigDecimal(heightStr.trim());
                // Kiểm tra nếu chiều cao nằm trong khoảng từ 1 đến 10000
                if (height.compareTo(BigDecimal.valueOf(1)) < 0 || height.compareTo(BigDecimal.valueOf(10000)) > 0) {
                    return Translator.toLocale("invalid.heightStr");
                }
            } catch (NumberFormatException e) {
                // Trường hợp chiều cao không phải là số hợp lệ
                return Translator.toLocale("invalid.heightStr");
            }
        }
        // Không có lỗi
        return null;
    }

    private String validateShippingFee(String shippingFeeStr) {
        // Kiểm tra nếu phí vận chuyển bị bỏ trống (bắt buộc)
        if (DataUtils.isNullOrEmpty(shippingFeeStr)) {
            return Translator.toLocale("invalid.shippingFeeStr");
        }
        try {
            // Chuyển đổi phí vận chuyển từ String sang Long
            Long shippingFee = Long.parseLong(shippingFeeStr.trim());
            // Kiểm tra nếu phí vận chuyển nằm trong khoảng từ 100 đến 100000000
            if (shippingFee < 100 || shippingFee > 100000000) {
                return Translator.toLocale("invalid.shippingFeeStr");
            }
        } catch (NumberFormatException e) {
            // Trường hợp phí vận chuyển không phải là số hợp lệ
            return Translator.toLocale("invalid.shippingFeeStr");
        }
        return null;
    }

    private void insertByFile(List<ProductCreateByFileExcel> input) {
        List<ProductEntity> productEntities = new ArrayList<>();
        List<SellerAttributeEntity> sellerAttributeEntities = new ArrayList<>();
        List<SellerClassifyEntity> sellerClassifyEntities = new ArrayList<>();
        for (ProductCreateByFileExcel request : input) {
            ProductEntity product = new ProductEntity();
            BaseProductInfo baseProductInfo = request.getBaseProductInfo();
            SellingProductInfo sellingProductInfo = request.getSellingProductInfo();
            product.setCategoryId(baseProductInfo.getCategoryId());
            product.setMerchantId(getCurrentUser().getId());
            product.setName(baseProductInfo.getDisplayName());
            product.setOriginalProductName(baseProductInfo.getFullName());
            product.setImage(String.join(",", baseProductInfo.getProductThumbnail()));
            if (!DataUtils.isNullOrEmpty(request.getBaseProductInfo().getProductMedia())) {
                List<String> imagePaths = request.getBaseProductInfo().getProductMedia().stream()
                        .filter(path -> Constants.VALID_IMAGE_TYPE.stream()
                                .anyMatch(type -> path.toLowerCase().endsWith("." + type.toLowerCase())))
                        .toList();
                if (!DataUtils.isNullOrEmpty(imagePaths)) {
                    product.setImages(String.join(",", imagePaths));
                }
                String firstVideoPath = request.getBaseProductInfo().getProductMedia().stream()
                        .filter(path -> Constants.VALID_VIDEO_TYPE.stream()
                                .anyMatch(type -> path.toLowerCase().endsWith("." + type.toLowerCase())))
                        .findFirst().orElse(null);
                if (!DataUtils.isNullOrEmpty(firstVideoPath)) {
                    product.setTrailerVideo(firstVideoPath);
                }
            }
            product.setDescription(baseProductInfo.getProductDescription());

            product.setQuoteType(sellingProductInfo.getMinPurchaseType());
            product.setPriceRanges(JsonMapperUtils.writeValueAsString(request.getStepPriceInfo()));
            if (!DataUtils.isNullOrEmpty(product.getPriceRanges()) &&
                    product.getPriceRanges().equals("null")) {
                product.setPriceRanges(null);
            }
            product.setProductPriceType(sellingProductInfo.getPriceType());
            product.setMinOrderQuantity(sellingProductInfo.getMinPurchaseQuantity());
            product.setProductSpecInfo(JsonMapperUtils.writeValueAsString(request.getProductSpecInfo()));
            product.setPlatformDiscountRate(sellingProductInfo.getPlatformDiscountRate());
            if (!DataUtils.isNullOrEmpty(sellingProductInfo.getProductPrice())) {
                product.setPrice(sellingProductInfo.getProductPrice());
            }
            product.setPrice(sellingProductInfo.getProductPrice());
            product.setStatus(ProductStatus.PENDING);
            product.setProductCodeCustomer(request.getProductCodeStr());
            product.setCountryId(getCurrentUser().getCountryId());
            product.setSellerOpenId(getCurrentUser().getSellerOpenId());
            BigDecimal displayPrice = null;


//            if (!DataUtils.isNullOrEmpty(product.getPrice())) {
//                displayPrice = product.getPrice();
//            } else {
//                for (ManageProductCodeInfo info : request.getManageProductCodeInfo()) {
//                    if (info.getUnitPrice() != null) {
//                        displayPrice = info.getUnitPrice();
//                        break; // Nếu chỉ cần lấy giá trị unitPrice đầu tiên không null, dừng vòng lặp.
//                    }
//                }
//            }
            if (sellingProductInfo.getPriceType().equals(ProductPriceTypeEnum.NO_PRICE_RANGE.getValue())) {
                if (ObjectUtils.isEmpty(product.getPrice()))
                    throw new VipoInvalidDataRequestException("price is required when product_price_type = 0");
                displayPrice = product.getPrice();
            } else if (sellingProductInfo.getPriceType().equals(ProductPriceTypeEnum.PRODUCT_PRICE_RANGE.getValue())) {
                if (ObjectUtils.isEmpty(request.getStepPriceInfo()))
                    throw new VipoInvalidDataRequestException("Price ranges is required when product_price_type = 1");
                // Find the unit price with the smallest price step from the step price information
                displayPrice = request.getStepPriceInfo().stream()
                        .filter(
                                info -> ObjectUtils.isNotEmpty(info.getUnitPrice())
                                        && ObjectUtils.isNotEmpty(info.getPriceStep())
                        )
                        .max(Comparator.comparing(StepPriceInfo::getPriceStep))
                        .map(StepPriceInfo::getUnitPrice)
                        .orElse(null);
                if (ObjectUtils.isEmpty(displayPrice))
                    throw new VipoInvalidDataRequestException("No valid display price when product_price_type = 1");
            } else if (sellingProductInfo.getPriceType().equals(ProductPriceTypeEnum.SKU_PRICE.getValue())) {
                // If price type is SKU_PRICE_RANGE, find the nonnull smallest unit price from manage product code
                // information sorted by
                displayPrice = request.getManageProductCodeInfo().stream()
                        .map(ManageProductCodeInfo::getUnitPrice)
                        .filter(ObjectUtils::isNotEmpty)
                        .min(Comparator.naturalOrder())
                        .orElse(null);
                if (ObjectUtils.isEmpty(displayPrice))
                    throw new VipoInvalidDataRequestException("No valid display price when product_price_type = 2");
            }

            product.setDisplayPrice(displayPrice);
            productEntities.add(product);
        }
        repo.saveAll(productEntities);
        productEntities.forEach(i -> i.setProductCode(Constants.PREFIX_PROD_CODE.concat(String.valueOf(i.getId()))));
        repo.saveAll(productEntities);
        Map<String, ProductEntity> mapProductEntity = productEntities.stream().collect(Collectors.toMap(
                ProductEntity::getProductCodeCustomer, Function.identity()
        ));
        input.forEach(product -> {
            Long productId = mapProductEntity.get(product.getProductCodeStr()).getId();
            product.setId(productId);
            List<ProductAttributesInfo> productAttributesInfos = new ArrayList<>(product.getProductAttributesInfo());
            productAttributesInfos.sort(Comparator.comparingInt(ProductAttributesInfo::getStt));
            for (ProductAttributesInfo i : productAttributesInfos) {
                sellerAttributeEntities.add(SellerAttributeEntity.builder()
                        .productId(product.getId())
                        .attributeName(i.getAttributeName())
                        .attributeOrder(i.getStt())
                        .build());
            }
        });
        sellerAttributeRepository.saveAll(sellerAttributeEntities);
        Map<String, SellerAttributeEntity> mapProductAttributesInfo
                = sellerAttributeEntities.stream().collect(Collectors.toMap(
                i -> (i.getProductId().toString().concat("-").concat(i.getAttributeName())), Function.identity()
        ));
        input.forEach(product -> {
            List<ProductAttributesInfo> productAttributesInfos = new ArrayList<>(product.getProductAttributesInfo());
            productAttributesInfos.sort(Comparator.comparingInt(ProductAttributesInfo::getStt));
            for (ProductAttributesInfo attribute : productAttributesInfos) {
                LinkedHashMap<String, String> classifyMap = attribute.getNameAndImage();
                String name = attribute.getAttributeName();
                SellerAttributeEntity sellerAttributeEntity
                        = mapProductAttributesInfo.get(product.getId().toString().concat("-").concat(name));
                attribute.setId(sellerAttributeEntity.getId());
                int orderClassify = 1;
                for (Map.Entry<String, String> entry : classifyMap.entrySet()) {
                    sellerClassifyEntities.add(SellerClassifyEntity.builder()
                            .productId(product.getId())
                            .sellerAttributeId(sellerAttributeEntity.getId())
                            .sellerName(entry.getKey())
                            .sellerImage(entry.getValue())
                            .orderClassify(orderClassify++)
                            .build());
                }
            }
        });
        sellerClassifyRepository.saveAll(sellerClassifyEntities);
        Map<String, SellerClassifyEntity> mapSellerClassifyEntity
                = sellerClassifyEntities.stream().collect(Collectors.toMap(
                i -> (i.getProductId().toString().concat("-").concat(i.getSellerAttributeId().toString())
                        .concat("-")
                        .concat(i.getSellerName())), Function.identity()
        ));
        List<ProductSellerSkuEntity> productSellerSkuEntitiesLst = new ArrayList<>();
        input.forEach(product -> {
            List<ProductAttributesInfo> productAttributesInfos = new ArrayList<>(product.getProductAttributesInfo());
            productAttributesInfos.sort(Comparator.comparingInt(ProductAttributesInfo::getStt));
            List<SellerAttributeEntity> sellerAttributeEntitiesLst = new ArrayList<>();
            List<SellerClassifyEntity> classifyEntityList = new ArrayList<>();
            int attributeOrder = 1;
            for (ProductAttributesInfo attribute : productAttributesInfos) {
                sellerAttributeEntitiesLst.add(SellerAttributeEntity.builder()
                        .id(attribute.getId())
                        .productId(product.getId())
                        .attributeName(attribute.getAttributeName())
                        .attributeOrder(attributeOrder++)
                        .build());
                LinkedHashMap<String, String> classifyMap = attribute.getNameAndImage();
                for (Map.Entry<String, String> entry : classifyMap.entrySet()) {
                    SellerClassifyEntity sellerClassifyEntity =
                            mapSellerClassifyEntity.get(product.getId().toString()
                                    .concat("-").concat(attribute.getId().toString())
                                    .concat("-")
                                    .concat(entry.getKey()));
                    classifyEntityList.add(sellerClassifyEntity);
                }
            }
            productSellerSkuEntitiesLst.addAll(getProductSellerSku(
                    product, classifyEntityList, sellerAttributeEntitiesLst, product.getId()
            ));
        });
        productSellerSkuEntitiesLst.forEach(i -> i.setActiveStatus(true));
        productSellerSkuRepository.saveAll(productSellerSkuEntitiesLst);
    }

    @Transactional
    @Override
    public String updateRequest(ProductCreateUpdateRequest request) {
        if (DataUtils.isNullOrEmpty(request.getId())) {
            throw new VipoBusinessException(ErrorCodeResponse.REQUIRED_FIELD, "id");
        }
        ProductEntity product = findOne(request.getId());
        List<ProductStatus> lstStatusValid = List.of(
                ProductStatus.REJECT,
                ProductStatus.ADJUST_REJECT
        );
        checkValidStatus(lstStatusValid, product.getStatus());
        if (ProductStatus.REJECT.equals(product.getStatus())) {
//            return createOrUpdateProduct(request, product, product.getStatus(), false, false);
            return createOrUpdateProduct(request, product, ProductStatus.PENDING, false, false);
        } else {
//            return createOrUpdateProduct(request, product, product.getStatus(), true, true);
            return createOrUpdateProduct(request, product, ProductStatus.ADJUST_PENDING, true, true);
        }
    }

    public Map<Long, LinkedHashMap<String, String>> mapSellerClassifyEntities(List<SellerClassifyEntity> sellerClassifyEntities) {
        Map<Long, LinkedHashMap<String, String>> resultMap = new HashMap<>();
        sellerClassifyEntities.sort(Comparator.comparingInt(SellerClassifyEntity::getOrderClassify));
        for (SellerClassifyEntity entity : sellerClassifyEntities) {
            Long sellerAttributeId = entity.getSellerAttributeId();

            // Khởi tạo LinkedHashMap để giữ sellerName và sellerImage theo đúng thứ tự
            LinkedHashMap<String, String> nameAndImage = new LinkedHashMap<>();
            nameAndImage.put(entity.getSellerName(), entity.getSellerImage());

            // Kiểm tra nếu đã có sellerAttributeId trong resultMap, thì thêm vào danh sách LinkedHashMap
            resultMap.merge(sellerAttributeId, nameAndImage, (oldValue, newValue) -> {
                oldValue.putAll(newValue); // Thêm giá trị mới vào LinkedHashMap đã có
                return oldValue;
            });
        }

        return resultMap;
    }

    public List<ManageProductCodeInfo> mapToManageProductCodeInfoList(List<ProductSellerSkuEntity> skuEntities, List<SellerClassifyEntity> sellerClassifyEntities) {
        return skuEntities.stream()
                .map(skuEntity -> mapToManageProductCodeInfo(skuEntity, sellerClassifyEntities))
                .collect(Collectors.toList());
    }

    public ManageProductCodeInfo mapToManageProductCodeInfo(ProductSellerSkuEntity skuEntity, List<SellerClassifyEntity> sellerClassifyEntities) {
        ManageProductCodeInfo manageProductCodeInfo = new ManageProductCodeInfo();

        // Set các giá trị cơ bản
        manageProductCodeInfo.setProductImage(skuEntity.getProductImage());
        manageProductCodeInfo.setUnitPrice(skuEntity.getUnitPrice());
        manageProductCodeInfo.setStock(skuEntity.getStock());
        manageProductCodeInfo.setMinPurchase(skuEntity.getMinPurchase());
        manageProductCodeInfo.setWeight(skuEntity.getWeight());
        manageProductCodeInfo.setLength(skuEntity.getLength() != null ? skuEntity.getLength().doubleValue() : null);
        manageProductCodeInfo.setWidth(skuEntity.getWidth() != null ? skuEntity.getWidth().doubleValue() : null);
        manageProductCodeInfo.setHeight(skuEntity.getHeight() != null ? skuEntity.getHeight().doubleValue() : null);
        manageProductCodeInfo.setShippingFee(skuEntity.getShippingFee());
        manageProductCodeInfo.setProductSkuCodeCustomer(skuEntity.getCodeCustomer());

        // Map sellerClassifyId -> attribute (nối classifyName bằng dấu phẩy)
        List<String> attributes = Arrays.stream(skuEntity.getSellerClassifyId().split(","))
                .map(Long::parseLong) // Chuyển từng ID từ String sang Long
                .flatMap(classifyId -> sellerClassifyEntities.stream()
                        .filter(classify -> classify.getId().equals(classifyId)) // Lọc theo classifyId
                        .map(SellerClassifyEntity::getSellerName)) // Lấy tên thuộc tính
                .toList(); // Chuyển kết quả về List

        manageProductCodeInfo.setAttribute(new ArrayList<>(attributes));

        return manageProductCodeInfo;
    }

    @Transactional
    public ApproveProductResponse approveProduct(@Valid ApproveProductRequest approveProductRequest) {

        Long productId = approveProductRequest.getProductId();

        /* update product */
        ProductEntity product = repo.findById(productId).orElseThrow(VipoNotFoundException::new);
        ProductTemporaryEntity productTemporary
                = productTemporaryRepository.findByProductId(productId).orElseThrow(VipoNotFoundException::new);;
        productMapper.updateProductEntity(productTemporary, product);
        product.setStatus(ProductStatus.fromValue(approveProductRequest.getWantedStatus()));
        save(product);

        /* copy seller_attribute */
        List<SellerAttributeEntity> sellerAttributeEntities
                = sellerAttributeRepository.findAllByProductIdAndDeletedFalse(productId);
        List<SellerAttributeTemporaryEntity> sellerAttributeTemporaryEntityList
                = sellerAttributeTemporaryRepository.findAllByProductTemporaryId(productTemporary.getId());
        if (ObjectUtils.isEmpty(sellerAttributeEntities) || ObjectUtils.isEmpty(sellerAttributeTemporaryEntityList))
            throw new VipoFailedToExecuteException("Not found seller attribute");
        //delete all
        sellerAttributeRepository.deleteAll(sellerAttributeEntities);
        //insert new
        List<SellerAttributeEntity> newSellerAttributeEntities
                = sellerAttributeTemporaryEntityList.stream().map(productMapper::toSellerAttributeEntity).toList();
        newSellerAttributeEntities.forEach(newSellerAttributeEntity -> newSellerAttributeEntity.setProductId(productId));
        sellerAttributeRepository.saveAll(newSellerAttributeEntities);
        //create a map from temp to real
        Map<Long, Long> tempAttributeIdToReal
                = newSellerAttributeEntities.stream()
                .collect(
                        Collectors.toMap(
                                SellerAttributeEntity::getTempId,
                                SellerAttributeEntity::getId,
                                (replacement, existing) -> existing
                        )
                );

        /* copy seller_classify */
        List<SellerClassifyEntity> sellerClassifyEntities
                = sellerClassifyRepository.findAllByProductIdAndDeletedFalse(productId);
        List<SellerClassifyTemporaryEntity> sellerAttributeTemporaryEntities
                = sellerClassifyTemporaryRepository.findAllByProductTemporaryId(productTemporary.getId());
        if (ObjectUtils.isEmpty(sellerClassifyEntities) || ObjectUtils.isEmpty(sellerAttributeTemporaryEntities))
            throw new VipoFailedToExecuteException("Not found seller attribute");
        //delete all
        sellerClassifyRepository.deleteAll(sellerClassifyEntities);
        //insert new
        sellerAttributeTemporaryEntities
                .forEach(temp -> temp.setSellerAttributeTemporaryId(tempAttributeIdToReal.get(temp.getSellerAttributeTemporaryId())));
        List<SellerClassifyEntity> newSellerClassifyEntities
                = sellerAttributeTemporaryEntities.stream().map(productMapper::toSellerClassifyEntity).toList();
        newSellerClassifyEntities.forEach(newSellerAttributeEntity -> newSellerAttributeEntity.setProductId(productId));
        sellerClassifyRepository.saveAll(newSellerClassifyEntities);
        //create a map from temp to real
        Map<Long, Long> tempClassifyIdToReal
                = newSellerClassifyEntities.stream()
                .collect(
                        Collectors.toMap(
                                SellerClassifyEntity::getTempId,
                                SellerClassifyEntity::getId,
                                (replacement, existing) -> existing
                        )
                );

        /* copy product_seller_sku */
        List<ProductSellerSkuEntity> productSellerSkuEntities = productSellerSkuRepository.findAllByProductIdAndDeletedFalse(productId);
        List<ProductSellerSkuTemporaryEntity> sellerSkuTemporaryEntities
                = productSellerSkuTemporaryRepository.findAllByProductTemporaryId(productTemporary.getId());
        if (ObjectUtils.isEmpty(productSellerSkuEntities) || ObjectUtils.isEmpty(sellerSkuTemporaryEntities))
            throw new VipoFailedToExecuteException("Not found seller attribute");
        //delete all
        productSellerSkuRepository.deleteAll(productSellerSkuEntities);
        //insert new
        sellerSkuTemporaryEntities
                .forEach(temp ->{
                    String classifyTempIdsStr = temp.getSellerClassifyTemporaryId();
                    if (StringUtils.isBlank(classifyTempIdsStr)) return;
                    List<Long> skuSellerClassifyIds
                            = Stream.of(classifyTempIdsStr.split(","))
                            .map(Long::parseLong).toList();
                    if(ObjectUtils.isEmpty(skuSellerClassifyIds)) return;

                    List<Long> newSkuSellerClassifyIds
                            = skuSellerClassifyIds.stream().map(tempClassifyIdToReal::get).toList();

                    temp.setSellerClassifyTemporaryId(
                            newSkuSellerClassifyIds.stream().map(String::valueOf).collect(Collectors.joining(","))
                    );

                });
        List<ProductSellerSkuEntity> newProductSellerSkuEntities
                = sellerSkuTemporaryEntities.stream().map(productMapper::toProductSellerSkuEntity).toList();

        newProductSellerSkuEntities.forEach(newSellerAttributeEntity -> newSellerAttributeEntity.setProductId(productId));
        productSellerSkuRepository.saveAll(newProductSellerSkuEntities);

        return ApproveProductResponse.builder()
                .product(product)
                .sellerClassifyEntities(newSellerClassifyEntities)
                .sellerAttributeEntities(newSellerAttributeEntities)
                .productSellerSkuEntities(newProductSellerSkuEntities)
                .build();
    }

}
