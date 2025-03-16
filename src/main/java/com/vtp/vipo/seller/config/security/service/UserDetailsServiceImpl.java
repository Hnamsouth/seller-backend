package com.vtp.vipo.seller.config.security.service;

import com.vtp.vipo.seller.common.dao.repository.MerchantRepository;
import com.vtp.vipo.seller.common.dto.UserDTO;
import com.vtp.vipo.seller.config.security.VipoUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private MerchantRepository merchantRepository;

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        List<UserDTO> users = merchantRepository.getUserInfo(Long.parseLong(phone));
        if (users == null || users.isEmpty()) {
            throw new UsernameNotFoundException("User Not Found with phone: " + phone);
        }
        // Assuming that there should be only one result, get the first one
        UserDTO user = users.get(0);
        return VipoUserDetails.build(user);
    }
}
