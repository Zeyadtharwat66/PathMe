package com.PathMe.course_reservation.config;

import com.PathMe.course_reservation.model.entity.UserEntity;
import com.PathMe.course_reservation.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomStudentDetailsService implements UserDetailsService  {
    private final UserRepo userRepo;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user= userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not correct"));
        return new CustomStudentDetails(user);
    }

}