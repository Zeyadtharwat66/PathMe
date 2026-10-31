package com.PathMe.course_reservation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UtilsController {
    @GetMapping("/flags")
    public Map<String, Object> getCountryFlags() {

        List<Map<String, String>> countries = new ArrayList<>();

        countries.add(Map.of("name", "Algeria", "flag", "https://flagcdn.com/dz.svg", "iso2", "DZ", "iso3", "DZA", "code", "+213"));
        countries.add(Map.of("name", "Bahrain", "flag", "https://flagcdn.com/bh.svg", "iso2", "BH", "iso3", "BHR", "code", "+973"));
        countries.add(Map.of("name", "Comoros", "flag", "https://flagcdn.com/km.svg", "iso2", "KM", "iso3", "COM", "code", "+269"));
        countries.add(Map.of("name", "Djibouti", "flag", "https://flagcdn.com/dj.svg", "iso2", "DJ", "iso3", "DJI", "code", "+253"));
        countries.add(Map.of("name", "Egypt", "flag", "https://flagcdn.com/eg.svg", "iso2", "EG", "iso3", "EGY", "code", "+20"));
        countries.add(Map.of("name", "Iraq", "flag", "https://flagcdn.com/iq.svg", "iso2", "IQ", "iso3", "IRQ", "code", "+964"));
        countries.add(Map.of("name", "Jordan", "flag", "https://flagcdn.com/jo.svg", "iso2", "JO", "iso3", "JOR", "code", "+962"));
        countries.add(Map.of("name", "Kuwait", "flag", "https://flagcdn.com/kw.svg", "iso2", "KW", "iso3", "KWT", "code", "+965"));
        countries.add(Map.of("name", "Lebanon", "flag", "https://flagcdn.com/lb.svg", "iso2", "LB", "iso3", "LBN", "code", "+961"));
        countries.add(Map.of("name", "Libya", "flag", "https://flagcdn.com/ly.svg", "iso2", "LY", "iso3", "LBY", "code", "+218"));
        countries.add(Map.of("name", "Mauritania", "flag", "https://flagcdn.com/mr.svg", "iso2", "MR", "iso3", "MRT", "code", "+222"));
        countries.add(Map.of("name", "Morocco", "flag", "https://flagcdn.com/ma.svg", "iso2", "MA", "iso3", "MAR", "code", "+212"));
        countries.add(Map.of("name", "Oman", "flag", "https://flagcdn.com/om.svg", "iso2", "OM", "iso3", "OMN", "code", "+968"));
        countries.add(Map.of("name", "Palestine", "flag", "https://flagcdn.com/ps.svg", "iso2", "PS", "iso3", "PSE", "code", "+970"));
        countries.add(Map.of("name", "Qatar", "flag", "https://flagcdn.com/qa.svg", "iso2", "QA", "iso3", "QAT", "code", "+974"));
        countries.add(Map.of("name", "Saudi Arabia", "flag", "https://flagcdn.com/sa.svg", "iso2", "SA", "iso3", "SAU", "code", "+966"));
        countries.add(Map.of("name", "Somalia", "flag", "https://flagcdn.com/so.svg", "iso2", "SO", "iso3", "SOM", "code", "+252"));
        countries.add(Map.of("name", "Sudan", "flag", "https://flagcdn.com/sd.svg", "iso2", "SD", "iso3", "SDN", "code", "+249"));
        countries.add(Map.of("name", "Syria", "flag", "https://flagcdn.com/sy.svg", "iso2", "SY", "iso3", "SYR", "code", "+963"));
        countries.add(Map.of("name", "Tunisia", "flag", "https://flagcdn.com/tn.svg", "iso2", "TN", "iso3", "TUN", "code", "+216"));
        countries.add(Map.of("name", "United Arab Emirates", "flag", "https://flagcdn.com/ae.svg", "iso2", "AE", "iso3", "ARE", "code", "+971"));
        countries.add(Map.of("name", "Yemen", "flag", "https://flagcdn.com/ye.svg", "iso2", "YE", "iso3", "YEM", "code", "+967"));

        countries.add(Map.of("name", "United States", "flag", "https://flagcdn.com/us.svg", "iso2", "US", "iso3", "USA", "code", "+1"));
        countries.add(Map.of("name", "Canada", "flag", "https://flagcdn.com/ca.svg", "iso2", "CA", "iso3", "CAN", "code", "+1"));
        countries.add(Map.of("name", "United Kingdom", "flag", "https://flagcdn.com/gb.svg", "iso2", "GB", "iso3", "GBR", "code", "+44"));
        Map<String, Object> response = new HashMap<>();
        response.put("error", false);
        response.put("msg", "flags images retrieved");
        response.put("data", countries);

        return response;
    }
}
