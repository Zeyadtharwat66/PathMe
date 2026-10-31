package com.PathMe.course_reservation.annotations.validator;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Component
public class PhoneNumberValidator {

    private final PhoneNumberUtil phoneUtil =
            PhoneNumberUtil.getInstance();

    public String validateAndFormat(
            String phoneNumber,
            String countryIso2
    ) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Phone number is required"
            );
        }

        if (countryIso2 == null || countryIso2.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Country code is required"
            );
        }

        try {
            String region = countryIso2.trim()
                    .toUpperCase(Locale.ROOT);

            Phonenumber.PhoneNumber parsedNumber =
                    phoneUtil.parse(phoneNumber.trim(), region);

            if (!phoneUtil.isValidNumberForRegion(
                    parsedNumber, region)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid phone number for selected country"
                );
            }

            return phoneUtil.format(
                    parsedNumber,
                    PhoneNumberUtil.PhoneNumberFormat.E164
            );

        } catch (NumberParseException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid phone number format"
            );
        }
    }
}