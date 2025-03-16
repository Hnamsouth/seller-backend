package com.vtp.vipo.seller.services.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.vtp.vipo.seller.common.BaseService;
import com.vtp.vipo.seller.common.constants.AuthConstant;
import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.dao.entity.*;
import com.vtp.vipo.seller.common.dao.entity.enums.merchant.ContractStatus;
import com.vtp.vipo.seller.common.dao.entity.enums.merchant.MerchantContractType;
import com.vtp.vipo.seller.common.dto.request.merchant.MerchantRequestV2;
import com.vtp.vipo.seller.common.dto.response.merchant.MerchantLogAttributeChangeInfo;
import com.vtp.vipo.seller.common.dao.repository.*;
import com.vtp.vipo.seller.common.dao.entity.enums.MerchantLogActionEnum;
import com.vtp.vipo.seller.common.dto.request.MerchantRequest;
import com.vtp.vipo.seller.common.dto.request.auth.ChangePasswordRequest;
import com.vtp.vipo.seller.common.dto.request.auth.LoginRequest;
import com.vtp.vipo.seller.common.dto.request.auth.RegisterRequestType2;
import com.vtp.vipo.seller.common.dto.request.auth.TokenRefreshRequest;
import com.vtp.vipo.seller.common.dto.response.AuthResponse;
import com.vtp.vipo.seller.common.dto.response.MerchantResponse;
import com.vtp.vipo.seller.common.dto.response.cbb.ComboboxRes;
import com.vtp.vipo.seller.common.dto.response.merchant.*;
import com.vtp.vipo.seller.common.enumseller.MerchantBusinessType;
import com.vtp.vipo.seller.common.enumseller.MerchantInactiveStatus;
import com.vtp.vipo.seller.common.enumseller.MerchantStatusEnum;
import com.vtp.vipo.seller.common.exception.*;
import com.vtp.vipo.seller.common.mapper.SellerMapper;
import com.vtp.vipo.seller.common.utils.DataUtils;
import com.vtp.vipo.seller.common.utils.DateUtils;
import com.vtp.vipo.seller.common.utils.JsonMapperUtils;
import com.vtp.vipo.seller.config.security.VipoUserDetails;
import com.vtp.vipo.seller.config.security.jwt.JwtTokenService;
import com.vtp.vipo.seller.services.CountryService;
import com.vtp.vipo.seller.services.MerchantService;

import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;

@Slf4j
@Service("merchantService")
@RequiredArgsConstructor
public class MerchantServiceImpl extends BaseService<MerchantEntity, Long, MerchantRepository> implements MerchantService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenService jwtTokenService;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CountryService countryService;

    @Value("${security.vipo.secret-key.secret-key-jwt}")
    private String jwtSecret;

    @Value("${security.vipo.exp.jwt-refresh}")
    private Long refreshTokenDurationMs;

    @Autowired
    private MerchantPaymentCardRepository merchantPaymentCardRepository;

    @Autowired
    private SellerOpenRepository sellerOpenRepository;

    @Autowired
    private MerchantGroupRepository merchantGroupRepository;

    private final MerchantLogRepository merchantLogRepository;

    private final MerchantContractFileRepository merchantContractFileRepository;

    private final MerchantRepository merchantRepository;

    private final MerchantNewRepository merchantNewRepository;

    private final SellerMapper sellerMapper;

    @Override
    @Transactional
    public String register(RegisterRequestType2 request) {
        MerchantEntity merchant = repo.findByContactPhone(request.getPhone());

        if (!ObjectUtils.isEmpty(merchant)) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, AuthConstant.PHONE_EXIST);
        }
        if (!DataUtils.isNullOrEmpty(request.getCountryId())) {
            countryRepository.findById(request.getCountryId()).orElseThrow(
                    () -> new VipoBusinessException(ErrorCodeResponse.COMMON_NOT_FOUND_ID,
                            request.getCountryId().toString())
            );
        }

        merchant = mapRegisterRequestToMerchantEntity(request);
        merchant.setContactPhone(request.getPhone());
        merchant.setContactEmail(request.getEmail());
        merchant.setPassword(encoder.encode(jwtSecret + request.getPassword()));
        merchant.setInactive(0);
        merchant.setStatus(0);
        //note: set default merchant group . need to update later
        List<MerchantGroupEntity> merchantGroupEntities = merchantGroupRepository.findAll();
        if(!CollectionUtils.isEmpty(merchantGroupEntities)){
            merchant.setMerchantGroupId(merchantGroupEntities.get(0).getId());
        }

        repo.save(merchant);
        sellerOpenRepository.save(SellerOpenEntity.builder()
                .platformType(null)
                .merchantId(merchant.getId())
                .sellerOpenId(genSellerOpenId())
                .build());
        return BaseExceptionConstant.SUCCESS_DESCRIPTION;
    }

    private String genSellerOpenId() {
        return String.format("vipo-seller-%s", UUID.randomUUID().toString());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication auth = authentication(request.getPhone(), request.getPassword());
        SecurityContextHolder.getContext().setAuthentication(auth);

        VipoUserDetails user = (VipoUserDetails) auth.getPrincipal();
        String jwt = jwtTokenService.generateTokenFromUserInfo(user);
        String refreshToken = generateRefreshToken(user.getId(), jwt);
        return new AuthResponse(jwt, refreshToken, user.getId());
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        MerchantEntity merchant = repo.findByRefreshToken(requestRefreshToken);
        if (ObjectUtils.isEmpty(merchant)) {
            throw new VipoBusinessException(BaseExceptionConstant.MISSING_REFRESH_TOKEN_DESCRIPTION);
        }

        if (merchant.getExpiredAt().compareTo(Instant.now().getEpochSecond()) < 0) {
            throw new VipoTokenExpiredException(BaseExceptionConstant.EXPIRED_REFRESH_TOKEN);
        }

        String sellerOpenId = sellerOpenRepository.getSellerOpenIdByMerchantId(merchant.getId());

        String jwt = jwtTokenService.generateTokenFromUserInfo(
                new VipoUserDetails(
                        merchant.getId(), merchant.getContactPhone(), merchant.getPassword(), merchant.getRefreshToken(),
                        merchant.getCountryId(), sellerOpenId, merchant.getContactEmail(), merchant.getName()
                )
        );

//        return new AuthResponse(jwtTokenService.generateTokenFromUserInfo(String.valueOf(merchant.getId())), merchant.getRefreshToken(), merchant.getId());
        return new AuthResponse(jwt, merchant.getRefreshToken(), merchant.getId());
    }


    private String generateRefreshToken(Long id, String jwt) {
        MerchantEntity merchant = repo.findById(id).orElseThrow(
                () ->  new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST_DESCRIPTION)
        );
        String refreshToken = UUID.randomUUID().toString();
        merchant.setRefreshToken(refreshToken);
        merchant.setExpiredAt(DateUtils.convertMilTimeToSecond(System.currentTimeMillis() + refreshTokenDurationMs));
//        merchant.setAccessToken(jwt);
        repo.save(merchant);
        return refreshToken;
    }

    @Override
    @Transactional
    public String logout() {
        if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser")) {
            VipoUserDetails userDetails = getCurrentUser();
            if (!ObjectUtils.isEmpty(userDetails)) {
                MerchantEntity merchant = repo.findById(userDetails.getId()).get();
                merchant.setRefreshToken("");
                merchant.setExpiredAt(0L);
                repo.save(merchant);
            }
        }
        return BaseExceptionConstant.SUCCESS_DESCRIPTION;
    }

    @Override
    public Boolean changePassword(ChangePasswordRequest request) {
        VipoUserDetails user = getCurrentUser();
        MerchantEntity merchant = repo.findById(user.getId()).get();
        if (ObjectUtils.isEmpty(merchant)) {
            throw new VipoBusinessException(BaseExceptionConstant.NOT_FOUND_ENTITY_DESCRIPTION);
        }
        if (!encoder.matches(jwtSecret + request.getCurrentPassword(), user.getPassword())) {
            throw new VipoBusinessException(AuthConstant.INCORRECT_PASSWORD);
        }
        merchant.setPassword(encoder.encode(jwtSecret + request.getNewPassword()));
        repo.save(merchant);
        return true;
    }

    public Authentication authentication(String phone, String password) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(phone, jwtSecret + password));
    }

    @Override
    public MerchantResponse getInfo() {
        VipoUserDetails userDetails = getCurrentUser();
        MerchantEntity merchant = repo.findById(userDetails.getId()).get();

        /* get country name */
        CountryEntity countryEntity = Optional.ofNullable(merchant.getCountryId())
                .flatMap(countryId -> countryRepository.findById(Long.valueOf(countryId)))
                .orElse(null);

        merchant.setIsPolicyTerms(true);
        mapper.addConverter(new Converter<MerchantBusinessType, Integer>() {
            public Integer convert(MappingContext<MerchantBusinessType, Integer> context) {
                return context.getSource() != null ? context.getSource().getValue() : null;
            }
        });
        MerchantResponse response = mapper.map(merchant, MerchantResponse.class);
        response.setCountryName(ObjectUtils.isNotEmpty(countryEntity) ? countryEntity.getName() : null);
        response.setLicensePicture(merchant.getBusinessLicenseImages());
        response.setType(DataUtils.isNullOrEmpty(merchant.getBusinessType()) ? MerchantBusinessType.HOUSEHOLD_BUSINESS.getValue()
                : merchant.getBusinessType().getValue());
        response.setDescriptionShop(merchant.getDescription());
//        merchantPaymentCardRepository.findByMerchantId(response.getId())
//                .ifPresent(card -> {
//                    response.setPaymentBank(card.getBankCode());
//                    response.setPaymentBranch(card.getBranch());
//                    response.setPaymentCard(card.getAccountNumber());
//                    response.setPaymentName(card.getAccountOwner());
//                });

        merchantPaymentCardRepository.findFirstByMerchantIdOrderByIsDefaultDescUpdateTimeDesc(response.getId())
                .ifPresent(card -> {
                    response.setPaymentBank(card.getBankCode());
                    response.setPaymentBranch(card.getBranch());
                    response.setPaymentCard(card.getAccountNumber());
                    response.setPaymentName(card.getAccountOwner());
                });
        return response;
    }

    @Override
    public MerchantResponseV2 getInfoV2() {
        VipoUserDetails userDetails = getCurrentUser();
        MerchantNewEntity merchant
                = merchantNewRepository.findById(userDetails.getId()).orElseThrow(
                        () -> new VipoNotFoundException("Not found merchant!")
        );

        /* get country name */
        CountryEntity countryEntity = countryService.findById(merchant.getCountryId());

        MerchantResponseV2 response = sellerMapper.toMerchantResponseV2(merchant);

        response.setCountryName(ObjectUtils.isNotEmpty(countryEntity) ? countryEntity.getName() : null);
        response.setCountryId(ObjectUtils.isNotEmpty(countryEntity) ? countryEntity.getId() : null);
        response.setLicensePicture(merchant.getBusinessLicenseImages());
        response.setDescriptionShop(merchant.getDescription());

        /* VIPO-3903: Upload e-contract */
        response.setIsCountryEditable(Boolean.TRUE.equals(merchant.getEnabledEditCountry()));
        response.setContractStatus(merchant.getContractStatus());
        /* contract */
        MerchantContractType merchantContractType = merchant.getType();
        Optional<MerchantContractFileEntity> merchantContractFileEntityOpt
                = merchantContractFileRepository.findFirstByMerchantIdAndDeletedFalseOrderByCreateTimeDesc(merchant.getId());
        String merchantContractUrl
                = merchantContractFileEntityOpt.map(MerchantContractFileEntity::getFileLink).orElse(null);
        if (merchant.getContractStatus() != ContractStatus.NO_CONTRACT) {
            response.setContract(
                    MerchantContractResponse.builder()
                            .contractType(merchantContractType)
                            .contractUrl(merchantContractUrl)
                            .partyB(
                                    MerchantContractType.BUSINESS.equals(merchantContractType)?
                                            merchant.getBusinessName() : merchant.getContactName()
                            )
                            .idInfo(
                                    MerchantIdInfo.builder()
                                            .idValue(merchant.getIdCard())
                                            .issuedDate(DateUtils.toDateString(merchant.getIdCardIssueDate(), DateUtils.ddMMyyyy))
                                            .placeOfIssue(merchant.getIdCardPlaceOfIssue())
                                            .build()
                            )
                            .bankAccount(
                                    MerchantBankAccountResponse.builder()
                                            .bank(merchant.getBankCode())
                                            .branch(merchant.getBankBranch())
                                            .accountNumber(merchant.getBankNumber())
                                            .accountHolder(merchant.getBankOwner())
                                            .build()
                            )
                            .businessNumber(merchant.getBusinessNumber())
                            .businessRepresent(merchant.getBusinessRepresent())
                            .businessPosition(merchant.getBusinessPosition())
                            .businessContactPhone(merchant.getContactPhone())
                            .businessContactEmail(merchant.getContactEmail())
                            .build()
            );
        }

        return response;
    }


    @Override
    @Transactional
    public Boolean changeInfo(MerchantRequest dto) {
        VipoUserDetails userDetails = getCurrentUser();
        MerchantEntity merchant = repo.findById(userDetails.getId()).get();
        if (merchant.getContactPhone() != null && !merchant.getContactPhone().equalsIgnoreCase(dto.getPhone()) && repo.existsByContactPhone(dto.getPhone())) {
            throw new VipoBusinessException(AuthConstant.PHONE_EXIST);
        }
        if (merchant.getStatus().equals(1) && merchant.getName() != null) {
            throw new VipoBusinessException(BaseExceptionConstant.REFUSE_UPDATE_MERCHANT, BaseExceptionConstant.REFUSE_UPDATE_MERCHANT_DESCRIPTION);
        }
        if (merchant.getStatus().equals(2)) {
            merchant.setStatus(0);
        }

        /* Business rule: only allow the user to change their info one time only */
        // old logic
//        if(!Objects.equals(merchant.getCreateTime(), merchant.getUpdateTime())){
//            throw new VipoBusinessException(BaseExceptionConstant.REFUSE_UPDATE_MERCHANT, BaseExceptionConstant.REFUSE_UPDATE_MERCHANT_DESCRIPTION);
//        }
        //check if there is any merchant_log that has the info change request from user
        if (
                merchantLogRepository.existsByMerchantIdAndAction(
                        userDetails.getId(), MerchantLogActionEnum.MERCHANT_REQUEST_TO_CHANGE_INFO.name()
                )
        )
            throw new VipoBusinessException(
                    BaseExceptionConstant.REFUSE_UPDATE_MERCHANT,
                    BaseExceptionConstant.REFUSE_UPDATE_MERCHANT_DESCRIPTION
            );

        mapDtoToEntity(merchant, dto);
        merchant = repo.save(merchant);

        /* create merchant_log for this action */
        merchantLogRepository.save(
                MerchantLogEntity.builder()
                        .merchantId(userDetails.getId())
                        .action(MerchantLogActionEnum.MERCHANT_REQUEST_TO_CHANGE_INFO.name())
                        .note(MerchantLogActionEnum.MERCHANT_REQUEST_TO_CHANGE_INFO.getMsg())
                        .data(JsonMapperUtils.writeValueAsString(merchant))
                        .build()
        );

        if (dto.getPaymentBank() != null && dto.getPaymentCard() != null && dto.getPaymentName() != null && dto.getPaymentBranch() != null) {
            merchantPaymentCardRepository.deleteByMerchantId(merchant.getId());
            merchantPaymentCardRepository.save(MerchantPaymentCardEntity.builder()
                    .merchantId(merchant.getId())
                    .bankCode(dto.getPaymentBank())
                    .accountNumber(dto.getPaymentCard())
                    .accountOwner(dto.getPaymentName())
                    .branch(dto.getPaymentBranch())
                    .isDefault(0)
                    .build());
        }
        return !ObjectUtils.isEmpty(merchant.getId());
    }

    @Override
    @Transactional
    public Boolean changeInfoV2(MerchantRequestV2 merchantRequest) {
        VipoUserDetails userDetails = getCurrentUser();
        MerchantNewEntity merchant
                = merchantNewRepository.findById(userDetails.getId())
                .orElseThrow(() -> new VipoNotFoundException("Not found merchant!"));

        /* VIPO-3903: Upload E-Contract: check if the merchant is allowed to change the country */
        if (
                Boolean.FALSE.equals(merchant.getEnabledEditCountry())
                && ObjectUtils.compare(merchant.getCountryId(), merchantRequest.getCountryId()) != 0
        ) {
            throw new VipoInvalidDataRequestException("Merchant is not allowed to change the country");
        }

        sellerMapper.updateMerchantNew(merchantRequest, merchant);

        merchant = merchantNewRepository.save(merchant);

        /* create merchant_log for this action */
        merchantLogRepository.save(
                MerchantLogEntity.builder()
                        .merchantId(userDetails.getId())
                        .action(MerchantLogActionEnum.MERCHANT_CHANGE_INFO.name())
                        .note(MerchantLogActionEnum.MERCHANT_CHANGE_INFO.getMsg())
                        .data(JsonMapperUtils.writeValueAsString(merchant))
                        .build()
        );

        return true;
    }

    private void mapDtoToEntity(MerchantEntity merchant, MerchantRequest request) {
        merchant.setContactPhone(request.getPhone());
        merchant.setContactEmail(request.getEmail());
        merchant.setProvinceId(DataUtils.safeToInt(request.getProvinceId()));
        merchant.setDistrictId(DataUtils.safeToInt(request.getDistrictId()));
        merchant.setWardId(DataUtils.safeToInt(request.getWardId()));
        merchant.setName(request.getName());
        merchant.setAddress(request.getAddress());
        merchant.setTaxCode(request.getTaxCode());
        merchant.setAvatar(request.getAvatar());
        merchant.setStartTimeTaxcode(request.getStartTimeTaxcode());
        merchant.setAddressTaxcode(request.getAddressTaxcode());
        merchant.setIsPolicyTerms(request.getIsPolicyTerms());
        merchant.setBusinessType(MerchantBusinessType.fromValue(request.getType()));
        merchant.setDescription(request.getDescriptionShop());
        merchant.setBusinessLicenseImages(request.getLicensePicture());

    }

    @Override
    public List<ComboboxRes> getCbb() {
        return countryService.getCbb();
    }

    public MerchantEntity mapRegisterRequestToMerchantEntity(RegisterRequestType2 request) {
        MerchantEntity merchant = new MerchantEntity();

        merchant.setContactPhone(request.getPhone());
        merchant.setPassword(request.getPassword());
        merchant.setContactEmail(request.getEmail());
        merchant.setContactName(request.getContactName());
        merchant.setProvinceId(request.getProvinceId());
        merchant.setDistrictId(request.getDistrictId());
        merchant.setWardId(request.getWardId());
        merchant.setStatus(request.getStatus());
        merchant.setInactive(request.getIsReject());
        merchant.setCountryId(DataUtils.safeToInt(request.getCountryId()));
        merchant.setIsPolicyTerms(request.getIsPolicyTerms());
        // Các giá trị mặc định
        merchant.setAllowMultipleStores(0);  // Default value
        return merchant;
    }

    @Transactional
    @Override
    public MerchantContractHistoryResponse getMerchantContractHistory(int pageNum, int pageSize) {

        Pageable pageRequest = PageRequest.of(pageNum, pageSize);

        Page<MerchantLogEntity> merchantLogEntitiesPage
                = merchantLogRepository.findByMerchantIdAndActionInOrderByCreateTimeDesc(
                        getCurrentUser().getId(), MerchantLogActionEnum.getMerchantContractActions(), pageRequest
        );

        if (
                ObjectUtils.isEmpty(merchantLogEntitiesPage)
                || merchantLogEntitiesPage.getTotalElements() == 0
        )
            return MerchantContractHistoryResponse.builder().build();

        List<MerchantLogEntity> merchantLogEntities = merchantLogEntitiesPage.getContent();
        List<MerchantLogEntity> updatedMerchantLogEntities = new ArrayList<>();

        List<MerchantContractHistoryInfo> histories = new ArrayList<>();

        long merchantLogEntitiesLength = merchantLogEntities.toArray().length;

        for (int i = 0; i < merchantLogEntitiesLength; i++) {
            MerchantLogEntity merchantLogEntity = merchantLogEntities.get(i);

            MerchantLogActionEnum merchantLogActionEnum = MerchantLogActionEnum.of(merchantLogEntity.getAction());
            if (ObjectUtils.isEmpty(merchantLogActionEnum))
                continue;

            /* VIPO-3903: Upload e-contract: in case the action is CONTRACT_ATTRIBUTE_CHANGED, we need to specify which
            * attribute is changed and how it changed */
            List<MerchantLogAttributeChangeInfo> attributeChangeInfo = new ArrayList<>();
            if (merchantLogActionEnum == MerchantLogActionEnum.CONTRACT_ATTRIBUTE_CHANGED) {
                if (StringUtils.isNotBlank(merchantLogEntity.getData())) {
                        if (StringUtils.isNotBlank(merchantLogEntity.getDataChange())) {
                            List<MerchantLogAttributeChangeInfo> savedAttributeChangeList
                                    = JsonMapperUtils.convertJsonToObject(
                                            merchantLogEntity.getDataChange(),new TypeReference<>() {}
                            );
                            if (ObjectUtils.isNotEmpty(savedAttributeChangeList))
                                attributeChangeInfo = savedAttributeChangeList;
                        } else {
                            //find the previous merchant log
                            if (i < merchantLogEntitiesLength - 1) {
                                MerchantLogEntity previousMerchantLogEntity = merchantLogEntities.get(i+1);
                                if (StringUtils.isNotBlank(previousMerchantLogEntity.getData())) {
                                    try {
                                        attributeChangeInfo
                                                = JsonMapperUtils.mapJsonDiffToAttributeChangeInfo(
                                                        merchantLogEntity.getData(), previousMerchantLogEntity.getData()
                                        );
                                        merchantLogEntity.setDataChange(
                                                JsonMapperUtils.writeValueAsString(attributeChangeInfo)
                                        );
                                        updatedMerchantLogEntities.add(merchantLogEntity);
                                    } catch (JsonProcessingException exception) {
                                        log.warn(
                                                "Error when compare two jsons of merchant log: {}",
                                                exception.getLocalizedMessage()
                                        );
                                    }
                                }
                            } else { //case when the last element, we need to query one more row
                                Page<MerchantLogEntity> merchantLogEntitiesPrevious
                                        = merchantLogRepository.findByMerchantIdAndActionInOrderByCreateTimeDesc(
                                                getCurrentUser().getId(),
                                        MerchantLogActionEnum.getMerchantContractActions(),
                                        PageRequest.of(pageNum +1, pageSize)
                                );
                                if (
                                        ObjectUtils.isNotEmpty(merchantLogEntitiesPrevious)
                                                && !merchantLogEntitiesPrevious.isEmpty()
                                ) {
                                    MerchantLogEntity previousMerchantLogEntity = merchantLogEntitiesPrevious.iterator().next();
                                    if (StringUtils.isNotBlank(previousMerchantLogEntity.getData())) {
                                        try {
                                        attributeChangeInfo
                                                = JsonMapperUtils.mapJsonDiffToAttributeChangeInfo(
                                                merchantLogEntity.getData(), previousMerchantLogEntity.getData()
                                        );
                                        merchantLogEntity.setDataChange(
                                                JsonMapperUtils.writeValueAsString(attributeChangeInfo)
                                        );
                                        updatedMerchantLogEntities.add(merchantLogEntity);
                                        } catch (JsonProcessingException exception) {
                                            log.warn(
                                                    "Error when compare two jsons of merchant log: {}",
                                                    exception.getLocalizedMessage()
                                            );
                                        }
                                    }
                                }
                            }
                        }
                }
            }

            histories.add(
                    MerchantContractHistoryInfo.builder()
                            .time(DateUtils.convertToEpochSeconds(merchantLogEntity.getCreateTime()))
                            .updateBy(String.valueOf(merchantLogEntity.getStaffId()))
                            .updateActionType(merchantLogActionEnum)
                            .updateActionMessage(merchantLogActionEnum.getMsg())
                            .changes(attributeChangeInfo)
                            .build()
            );
        }

        //update all dataChange
        if (ObjectUtils.isNotEmpty(updatedMerchantLogEntities))
            merchantLogRepository.saveAll(updatedMerchantLogEntities);

        return MerchantContractHistoryResponse.builder()
                .histories(histories)
                .totalNum(merchantLogEntitiesPage.getTotalElements())
                .build();
    }

    @Transactional
    @Override
    public Boolean acceptMerchantTerms() {
        MerchantNewEntity merchant
                = merchantNewRepository.findById(getCurrentUser().getId())
                .orElseThrow(() -> new VipoNotFoundException("Not found merchant!"));

        if (
                ObjectUtils.isEmpty(merchant.getContractStatus())
                        || !merchant.getContractStatus().equals(ContractStatus.CONTRACT_PENDING_ACCEPTANCE)
        )
            throw new VipoInvalidDataRequestException("Merchant contract is not valid");

        merchant.setContractStatus(ContractStatus.CONTRACT_ACCEPTED);
        merchant.setStatus(MerchantStatusEnum.APPROVED.getValue());
        merchant.setInactive(MerchantInactiveStatus.ACTIVE.getValue());

        merchantNewRepository.save(merchant);

        return true;
    }

}
