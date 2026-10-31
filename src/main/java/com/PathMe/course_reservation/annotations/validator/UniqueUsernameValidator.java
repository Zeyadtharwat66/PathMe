package com.PathMe.course_reservation.annotations.validator;


import com.PathMe.course_reservation.annotations.UniqueUsername;
import com.PathMe.course_reservation.repository.UserRepo;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String>{

        @Autowired
        private UserRepo userRepository;

        @Override
        public boolean isValid(String username, ConstraintValidatorContext context) {
        return username == null || !userRepository.existsByUsername(username);
    }
}
