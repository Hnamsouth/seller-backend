package com.vtp.vipo.seller.services.evtp;

import com.vtp.vipo.seller.common.dto.request.CreateFullOrderEvtpRequest;
import com.vtp.vipo.seller.common.dto.request.CreateSimplifiedOrderEvtpRequest;
import com.vtp.vipo.seller.common.dto.request.PrintLabelOrderRequest;
import com.vtp.vipo.seller.common.dto.request.ServiceInfoEvtpRequest;
import com.vtp.vipo.seller.common.dto.response.CreateOrderEvtpResponse;
import com.vtp.vipo.seller.common.dto.response.PrintLabelOrderResponse;
import com.vtp.vipo.seller.common.dto.response.ServiceInfoEvtpResponse;

import java.util.List;

public interface EvtpOrderService {
    CreateOrderEvtpResponse createFullOrder(CreateFullOrderEvtpRequest request);

    CreateOrderEvtpResponse createSimplifiedOrder(CreateSimplifiedOrderEvtpRequest request);

    PrintLabelOrderResponse printLabelOrder(PrintLabelOrderRequest request);

    List<ServiceInfoEvtpResponse> getServiceInfo(ServiceInfoEvtpRequest request);
}
