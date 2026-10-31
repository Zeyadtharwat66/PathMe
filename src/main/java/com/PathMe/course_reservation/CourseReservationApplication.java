package com.PathMe.course_reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class CourseReservationApplication {

	public static void main(String[] args) {
		SpringApplication.run(CourseReservationApplication.class, args);
	}

}
