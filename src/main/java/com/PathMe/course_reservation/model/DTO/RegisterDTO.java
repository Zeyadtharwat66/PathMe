package com.PathMe.course_reservation.model.DTO;
import com.PathMe.course_reservation.annotations.AgeRange;
import com.PathMe.course_reservation.annotations.UniqueEmail;
import com.PathMe.course_reservation.annotations.UniqueUsername;
import com.PathMe.course_reservation.model.enums.Gender;
import com.PathMe.course_reservation.model.enums.Role;
import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.LocalDate;

public record RegisterDTO(@NotBlank(message = "username is required")
                          @UniqueUsername
                          @Size(min=3,max = 20,message = "username must be min 3 and max 20")
                          @Pattern(
                                  regexp = "^[A-Za-z][A-Za-z0-9]*$",
                                  message = "Username must start with a letter and contain only letters and numbers"
                          )
                          String username,
                          @NotBlank(message = "password is required")
                          @Pattern(
                                  regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                                  message = "Not valid password"
                          )
                          String password,
                          @NotNull(message = "role is required")
                          Role role,
                          @AgeRange(min = 8, max =80, message = "Enter your real Age")
                          Integer age,
                          @NotNull(message = "gender is required")
                          Gender gender,
                          @NotBlank(message = "email is required")
                          @Email(message = "Invalid email format")
                          @UniqueEmail
                          String email,
                          @NotBlank(message = "phoneNumber is required")
                          String phoneNumber,
                          String countryIso2) {}
