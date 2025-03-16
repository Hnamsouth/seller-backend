package com.vtp.vipo.seller.services.impl;


import com.vtp.vipo.seller.common.BaseService;
import com.vtp.vipo.seller.common.dao.entity.CategoryEntity;
import com.vtp.vipo.seller.common.dao.entity.CountryEntity;
import com.vtp.vipo.seller.common.dao.repository.CategoryRepository;
import com.vtp.vipo.seller.common.dao.repository.CountryRepository;
import com.vtp.vipo.seller.common.dto.response.CountryResponse;
import com.vtp.vipo.seller.common.dto.response.cbb.ComboboxRes;
import com.vtp.vipo.seller.services.CategoryService;
import com.vtp.vipo.seller.services.CountryService;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service("countryService")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CountryServiceImpl extends BaseService<CountryEntity, Long, CountryRepository> implements CountryService {

    ApplicationContext applicationContext;

    private CountryServiceImpl getProxy() {
        return applicationContext.getBean(CountryServiceImpl.class);
    }

    @Override
    public List<ComboboxRes> getCbb() {
        List<CountryEntity> categories = repo.findAll();
        return categories.stream()
                .map(i -> ComboboxRes.builder()
                        .key(i.getId())
                        .value(i.getName())
                        .build())
                .sorted(Comparator.comparing(c -> (String) c.getValue()))  // Sắp xếp theo 'value' theo thứ tự a -> z
                .collect(Collectors.toList());
    }

    /**
     * Finds a country entity by its ID.
     * Uses a cached map of country IDs to country entities to avoid database lookups.
     *
     * @param countryId the ID of the country to find, cannot be null
     * @return the corresponding CountryEntity, or null if not found
     */
    @Override
    public CountryEntity findById(@NotNull Long countryId) {
        // Retrieve country entity by its ID using a cached map
        return getProxy().getCountryIdToCountryMap().get(countryId);
    }

    @Override
    public List<CountryResponse> getAllCountryResponse() {
        return getProxy().getAllCountryEntities().stream()
                .map(i -> CountryResponse.builder()
                        .id(i.getId())
                        .name(i.getName())
                        .code(i.getCode())
                        .build())
                .toList();
    }

    /**
     * Builds a map of country IDs to their corresponding CountryEntity.
     * The map is cached to avoid frequent database queries.
     *
     * @return a map where the keys are country IDs and the values are CountryEntity objects
     */
    @Cacheable(cacheManager = "memoryCacheManager", cacheNames = "countryIdToCountryCache")
    public Map<Long, CountryEntity> getCountryIdToCountryMap() {
        log.info("refresh getCountryIdToCountryMap()");
        // Get all country entities from cache or database
        List<CountryEntity> countryEntities = getProxy().getAllCountryEntities();

        // If no countries are found, return an empty map
        if (ObjectUtils.isEmpty(countryEntities)) {
            return new HashMap<>();
        }

        // Stream the list of countries and collect them into a map using IDs as keys
        return countryEntities.stream().collect(Collectors.toMap(
                CountryEntity::getId,
                Function.identity(),
                (existing, replacement) -> existing // In case of duplicate keys, keep the existing entry
        ));
    }

    /**
     * Retrieves all country entities from the repository.
     * This method is cached to avoid repeated database queries.
     * From this cache, we can create other cache for find by code, find by name, etc.
     *
     * @return a list of all CountryEntity objects
     */
    @Cacheable(cacheManager = "memoryCacheManager", cacheNames = "countriesCache")
    public List<CountryEntity> getAllCountryEntities() {
        // Retrieve all country entities from the database
        return repo.findAll();
    }

}
