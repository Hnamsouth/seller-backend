package com.vtp.vipo.seller.common;

import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.dao.entity.MerchantEntity;
import com.vtp.vipo.seller.common.dao.repository.MerchantRepository;
import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.common.exception.VipoUnAuthorizationException;
import com.vtp.vipo.seller.common.utils.DataUtils;
import com.vtp.vipo.seller.config.security.VipoUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
/**
 * Author : Le Quang Dat </br>
 * Email: quangdat0993@gmail.com</br>
 * Jan 20, 2024
 */
public abstract class BaseService<T, ID extends Serializable, R extends JpaRepository<T, ID>> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected R repo;

    public T get(ID id) {
        Optional<T> t =  repo.findById(id);
        if (t.isPresent()) {
            return t.get();
        }
        return null;
    }

    public T save(T t) {
        logger.info("\n\n ----> save record: {} \n\n", t.toString());
        t = repo.save(t);
        if (!ObjectUtils.isEmpty(t)) {
            return t;
        }
        return null;
    }

    public T delete(ID id) {
        Optional<T> t = repo.findById(id);

        if (t.isPresent()) {
            repo.deleteById(id);
            return t.get();
        }
        return null;
    }

    public List<T> findAll(List<ID> ids) {
        return repo.findAllById(ids);
    }

    public VipoUserDetails getCurrentUser() {
        VipoUserDetails info = null;
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                info = (VipoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        if(!ObjectUtils.isEmpty(info) && !ObjectUtils.isEmpty(info.getId())){
//            if (info == null || ObjectUtils.isEmpty(info.getPhone())){
//                throw new VipoUnAuthorizationException();
//            }
            return info;
        }
        else {
            throw new VipoUnAuthorizationException();
        }
    }

    public String getTokenFromRequest(HttpServletRequest request){
        String token = request.getHeader(Constants.HEADER);
        if (ObjectUtils.isEmpty(token)) {
            token = request.getHeader(Constants.HEADER2);
        }
        return token;
    }
    @Autowired
    private Environment environment;
    protected boolean bypassLogic;
    @Autowired
    private MerchantRepository merchantRepository;
    @PostConstruct
    public void init() {
        // Kiểm tra nếu profile hiện tại là 'local' thì đặt cờ bypassLogic thành true
        bypassLogic = Arrays.asList(environment.getActiveProfiles()).contains("local");
    }
    public String getLocale(){
        return LocaleContextHolder.getLocale().getLanguage();
    }
    protected HttpHeaders createDownloadHeaders(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        String base64Filename = Base64.getEncoder().encodeToString(fileName.getBytes(StandardCharsets.UTF_8));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + base64Filename);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE + ";charset=UTF-8");
        return headers;
    }
    public void checkMerchant() {
        VipoUserDetails user = getCurrentUser();
        MerchantEntity merchantEntity = merchantRepository.findByContactPhoneAndInactiveAndStatus(user.getId(),
                0, List.of(1,3));
        if (DataUtils.isNullOrEmpty(merchantEntity)) {
            throw new VipoBusinessException(ErrorCodeResponse.INVALID_AUTHEN);
        }
    }
}
