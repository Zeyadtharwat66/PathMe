package com.PathMe.course_reservation.service.imp;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.PathMe.course_reservation.annotations.validator.PhoneNumberValidator;
import com.PathMe.course_reservation.service.EnrollmentService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.PathMe.course_reservation.model.DTO.ChangePasswordRequest;
import com.PathMe.course_reservation.model.DTO.LoginDTO;
import com.PathMe.course_reservation.model.DTO.MyEnrollmentResponse;
import com.PathMe.course_reservation.model.DTO.RegisterDTO;
import com.PathMe.course_reservation.model.DTO.UpdateProfileRequest;
import com.PathMe.course_reservation.model.DTO.UserProfileResponse;
import com.PathMe.course_reservation.model.entity.CourseEntity;
import com.PathMe.course_reservation.model.entity.EnrollmentEntity;
import com.PathMe.course_reservation.model.entity.UserEntity;
import com.PathMe.course_reservation.model.enums.EnrollmentStatus;
import com.PathMe.course_reservation.repository.CourseRepository;
import com.PathMe.course_reservation.repository.EnrollmentRepository;
import com.PathMe.course_reservation.repository.UserRepo;
import com.PathMe.course_reservation.service.JWTTokenService;
import com.PathMe.course_reservation.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
@Service
@AllArgsConstructor
public class UserServiceImp implements UserService {
        private final AuthenticationManager authenticationManager;
        private final UserRepo userRepo;
        private final JWTTokenService jwtTokenService;
        private final PasswordEncoder passwordEncoder;
        private final EnrollmentRepository enrollmentRepo;
        private final CourseRepository courseRepo;
        private final PhoneNumberValidator phoneNumberValidator;

        @Override
        public ResponseEntity<?> login(LoginDTO loginDTO) {
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                loginDTO.username(),
                                                loginDTO.password()));
                String token = jwtTokenService.generateJWTToken(authentication);
                UserEntity user = userRepo.findByUsername(loginDTO.username())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
                ResponseCookie jwtCookie = ResponseCookie.from("jwt", token)
                                .httpOnly(true)
                                .secure(false) // Development over HTTP only
                                .sameSite("Lax")
                                .path("/")
                                .maxAge(60 * 60) // 1 hour; match your JWT expiration
                                .build();
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("phone", user.getPhoneNumber());
                userData.put("email", user.getEmail());
                userData.put("birthdate", user.getAge());
                userData.put("gender", user.getGender());
                userData.put("role", user.getRole());
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                                .body(Map.of("user", userData));
        }

        @Override
        public ResponseEntity<?> getCurrentUser() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated() ||
                                authentication.getPrincipal().equals("anonymousUser")) {
                        return ResponseEntity.status(401).body("Not logged in");
                }
                // extract user details
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                UserEntity user = userRepo.findByUsername(authentication.getName())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
                HashMap<String, Object> data = new HashMap<>();
                data.put("id", user.getId());
                data.put("username", user.getUsername());
                data.put("phone", user.getPhoneNumber());
                data.put("email", user.getEmail());
                data.put("gender", user.getGender());
                return ResponseEntity.ok(data);
        }

        @Override
        public ResponseEntity<?> register(RegisterDTO registerDTO) {
                UserEntity user = UserEntity.builder()
                                .username(registerDTO.username())
                                .password(passwordEncoder.encode(registerDTO.password()))
                                .role(registerDTO.role())
                                .age(registerDTO.age())
                                .email(registerDTO.email())
                                .gender(registerDTO.gender())
                                .build();
                String normalizedPhone =
                        phoneNumberValidator.validateAndFormat(
                                registerDTO.phoneNumber(),
                                registerDTO.countryIso2()
                        );
                user.setPhoneNumber(normalizedPhone);
                userRepo.save(user);
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                registerDTO.username(),
                                                registerDTO.password()));
                String token = jwtTokenService.generateJWTToken(authentication);
                ResponseCookie jwtCookie = ResponseCookie.from("jwt", token)
                                .httpOnly(true)
                                .secure(false) // Development over HTTP only
                                .sameSite("Lax")
                                .path("/")
                                .maxAge(60 * 60) // Match your JWT expiration
                                .build();
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("phone", user.getPhoneNumber());
                userData.put("email", user.getEmail());
                userData.put("age", user.getAge());
                userData.put("gender", user.getGender());
                userData.put("role", user.getRole());
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                                .body(Map.of("user", userData));
        }

        @Override
        public ResponseEntity<?> logout(HttpServletResponse response) {
                ResponseCookie cookie = ResponseCookie.from("jwt", "")
                                .httpOnly(true)
                                .secure(false) // Development على localhost بـ HTTP فقط
                                .path("/")
                                .maxAge(0)
                                .sameSite("Lax")
                                .build();
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
                return ResponseEntity.ok(
                                Map.of("message", "Logged out successfully"));
        }

        public UserProfileResponse getProfile(String username) {
                UserEntity user = userRepo.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                return new UserProfileResponse(
                                user.getUsername(),
                                user.getRole(),
                                user.getEmail(),
                                user.getPhoneNumber(),
                                user.getGender(),
                                user.getAge(),
                                user.getCreatedAt());
        }

        private UserEntity currentUser(String principalName) {
                return userRepo.findByUsername(principalName)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }

        @Transactional
        public ResponseEntity<?> updateProfile(Authentication auth, UpdateProfileRequest req) {
                String principalName = auth.getName();
                UserEntity u = currentUser(principalName);

                if (req.username() != null) {
                        String username = req.username().trim();
                        if (!username.equals(u.getUsername())) {
                                if (userRepo.existsByUsernameIgnoreCaseAndIdNot(username, u.getId()))
                                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                                        "Username is already taken");
                                u.setUsername(username);
                        }
                }

                if (req.email() != null) {
                        String email = req.email().trim().toLowerCase();
                        if (!email.equalsIgnoreCase(u.getEmail())) {
                                if (userRepo.existsByEmailIgnoreCaseAndIdNot(email, u.getId()))
                                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                                        "Email is already registered");
                                u.setEmail(email);
                        }
                }
                ResponseEntity.BodyBuilder response = ResponseEntity.ok();

                // لو الـ username اتغير، اصدر JWT جديد
                if (!auth.getName().equals(u.getUsername())) {
                        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                                        u.getUsername(),
                                        null,
                                        auth.getAuthorities());
                        String newToken = jwtTokenService.generateJWTToken(newAuth);
                        ResponseCookie cookie = ResponseCookie.from("jwt", newToken)
                                        .httpOnly(true)
                                        .secure(false) // في Production مع HTTPS
                                        .sameSite("Lax")
                                        .path("/")
                                        .maxAge(Duration.ofHours(24))
                                        .build();
                        return ResponseEntity.ok()
                                        .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                        .body(Map.of("user", u));
                }
                if (req.phoneNumber() != null)
                        u.setPhoneNumber(req.phoneNumber());
                if (req.gender() != null)
                        u.setGender(req.gender());
                if (req.age() != null)
                        u.setAge(req.age());

                try {
                        return ResponseEntity.ok()
                                        .body(Map.of("user", u));
                } catch (DataIntegrityViolationException ex) {
                        // حماية من race condition: يوزرين بيحاولوا ياخدوا نفس الإيميل في نفس اللحظة
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Username or email is already in use");
                }
        }

        @Transactional
        public void changePassword(String principalName, ChangePasswordRequest req) {
                UserEntity u = currentUser(principalName);

                if (u.getPassword() == null)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "This account uses Google login and has no password");
                if (!passwordEncoder.matches(req.currentPassword(), u.getPassword()))
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
                if (passwordEncoder.matches(req.newPassword(), u.getPassword()))
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "New password must be different from the current one");

                u.setPassword(passwordEncoder.encode(req.newPassword()));
                userRepo.save(u);
        }

        @Transactional
        public void adminChangePassword(UUID userId, String newPassword) {
                UserEntity user = userRepo.findById(userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "User not found"));
                if (newPassword == null || newPassword.isBlank()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password is required");
                }
                if (newPassword.length() < 8 || newPassword.length() > 72) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Password must be between 8 and 72 characters");
                }
                if (user.getPassword() != null && passwordEncoder.matches(newPassword, user.getPassword())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "New password must be different from the current one");
                }
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepo.save(user);
        }

        @Transactional
        public void adminUpdatePhoneNumber(UUID userId, String phoneNumber) {
                UserEntity user = userRepo.findById(userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "User not found"));
                if (phoneNumber == null || phoneNumber.isBlank()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number is required");
                }
                if (!phoneNumber.matches("^\\+[1-9]\\d{1,14}$")) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Use international format, e.g. +201012345678");
                }
                user.setPhoneNumber(phoneNumber);
                userRepo.save(user);
        }

        public Page<MyEnrollmentResponse> myEnrollments(String principalName, Pageable pageable) {
                UserEntity u = currentUser(principalName);
                return enrollmentRepo.findByUser_Id(u.getId(), pageable).map(this::toMyEnrollment);
        }

        @Transactional
        public MyEnrollmentResponse enroll(String principalName, UUID courseId) {
                UserEntity u = currentUser(principalName);
                CourseEntity course = courseRepo.findById(courseId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Course not found"));

                if (enrollmentRepo.existsByUser_IdAndCourse_Id(u.getId(), courseId))
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                        "You are already enrolled in this course");

                EnrollmentEntity e = EnrollmentEntity.builder()
                                .user(u)
                                .course(course)
                                .status(EnrollmentStatus.NEW)
                                .build();
                return toMyEnrollment(enrollmentRepo.save(e));
        }

        @Transactional
        public void cancelEnrollment(String principalName, UUID enrollmentId) {
                UserEntity u = currentUser(principalName);
                EnrollmentEntity e = enrollmentRepo.findById(enrollmentId)
                                .filter(x -> x.getUser().getId().equals(u.getId())) // مش بتاعه = مش موجود
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Enrollment not found"));

                e.setDeletedAt(LocalDateTime.now());
                enrollmentRepo.save(e);
        }

        @Transactional
        public void deleteAccount(String principalName) {
                UserEntity u = currentUser(principalName);
                LocalDateTime now = LocalDateTime.now();
                enrollmentRepo.findAllByUser_Id(u.getId()).forEach(e -> {
                        e.setDeletedAt(now);
                        enrollmentRepo.save(e);
                });
                u.setDeletedAt(now);
                userRepo.save(u);
        }

        private UserProfileResponse toProfile(UserEntity user) {
                return new UserProfileResponse(user.getUsername(), user.getRole(),
                                user.getEmail(), user.getPhoneNumber(), user.getGender(),
                                user.getAge(), user.getCreatedAt());
        }

        private MyEnrollmentResponse toMyEnrollment(EnrollmentEntity e) {
                return new MyEnrollmentResponse(e.getId(), e.getCourse().getId(), e.getCourse().getName(),
                                e.getCourse().getCourseImg(), e.getStatus(), e.getCreatedAt());
        }

}
