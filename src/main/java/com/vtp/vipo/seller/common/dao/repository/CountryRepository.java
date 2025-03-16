package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.CountryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<CountryEntity, Long> {
}
