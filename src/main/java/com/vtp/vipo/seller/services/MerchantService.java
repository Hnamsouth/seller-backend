package com.vtp.vipo.seller.services;

import com.vtp.vipo.seller.common.dto.request.MerchantRequest;
import com.vtp.vipo.seller.common.dto.request.auth.*;
import com.vtp.vipo.seller.common.dto.request.merchant.MerchantRequestV2;
import com.vtp.vipo.seller.common.dto.response.AuthResponse;
import com.vtp.vipo.seller.common.dto.response.MerchantResponse;
import com.vtp.vipo.seller.common.dto.response.cbb.ComboboxRes;
import com.vtp.vipo.seller.common.dto.response.merchant.MerchantContractHistoryResponse;
import com.vtp.vipo.seller.common.dto.response.merchant.MerchantResponseV2;

import java.util.List;

public interface MerchantService {

    String register(RegisterRequestType2 request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(TokenRefreshRequest request);

    String logout();

    Boolean changePassword(ChangePasswordRequest request);

    MerchantResponse getInfo();

    MerchantResponseV2 getInfoV2();

    Boolean changeInfo(MerchantRequest dto);

    Boolean changeInfoV2(MerchantRequestV2 dto);

    List<ComboboxRes> getCbb();

    MerchantContractHistoryResponse getMerchantContractHistory(int pageNum, int pageSize);

    Boolean acceptMerchantTerms();
}
