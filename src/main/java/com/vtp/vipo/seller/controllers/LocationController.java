package com.vtp.vipo.seller.controllers;

import com.vtp.vipo.seller.common.BaseController;
import com.vtp.vipo.seller.services.LocationService;
import com.vtp.vipo.seller.services.impl.CountryServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/location")
public class LocationController extends BaseController<LocationService> {

    private final CountryServiceImpl countryService;

    public LocationController(CountryServiceImpl countryService) {
        super();
        this.countryService = countryService;
    }

    @GetMapping("/province")
    public ResponseEntity<?> province() {
        return toResult(service.getAllProvince());
    }

    @GetMapping("/district")
    public ResponseEntity<?> district(@Valid @RequestParam(name = "provinceCode") Long provinceId) {
        return toResult(service.getAllDisByProvinceCode(provinceId));
    }

    @GetMapping("/ward")
    public ResponseEntity<?> ward(@Valid @RequestParam(name = "disCode") Long disCode) {
        return toResult(service.getAllWardByDisCode(disCode));
    }

    @GetMapping("/country")
    public ResponseEntity<?> country(){
//        return toResult(service.getAllCountry());
        /* VIPO-3903: Upload E-Contract: Optimize Country Querying */
        return toResult(countryService.getAllCountryResponse());
    }
}
