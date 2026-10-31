package com.PathMe.course_reservation.annotations.validator;

import com.PathMe.course_reservation.annotations.AgeRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class AgeRangeValidator implements ConstraintValidator<AgeRange, Integer> {
    private int min;
    private int max;

    @Override
    public void initialize(AgeRange constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }
    @Override
    public boolean isValid(Integer age, ConstraintValidatorContext context) {
        if (age == null) return false;
        return age >= min && age <= max;
    }
}
