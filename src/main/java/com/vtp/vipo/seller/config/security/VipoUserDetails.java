package com.vtp.vipo.seller.config.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vtp.vipo.seller.common.dto.UserDTO;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

@Getter
public class VipoUserDetails implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String phone;

    @JsonIgnore
    private String password;

    private String refreshToken;

    private Integer countryId;

    private String sellerOpenId;

    private String email;

    private String name;

    public VipoUserDetails() {
    }

    public VipoUserDetails(Long id, String phone, String password,
                           String refreshToken, Integer countryId,
                           String sellerOpenId, String email, String name) {
        this.id = id;
        this.phone = phone;
        this.password = password;
        this.refreshToken = refreshToken;
        this.countryId = countryId;
        this.sellerOpenId = sellerOpenId;
        this.email = email;
        this.name = name;
    }

    public static VipoUserDetails build(UserDTO user) {

        return new VipoUserDetails(
                user.getId(),
                user.getPhone(),
                user.getPassword(),
                user.getRefreshToken(),
                user.getCountryId(),
                user.getSellerOpenId(),
                user.getEmail(),
                user.getName()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return null;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VipoUserDetails user = (VipoUserDetails) o;
        return Objects.equals(phone, user.phone);
    }
}
