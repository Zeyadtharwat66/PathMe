package com.PathMe.course_reservation;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MockMvc;

import com.PathMe.course_reservation.service.CoursesService;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:course-reservation-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.jpa.hibernate.ddl-auto=none"
})
@AutoConfigureMockMvc
class CourseReservationApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CoursesService coursesService;

	@Test
	void contextLoads() {
	}

	@Test
	void publicCoursesEndpointRespondsSuccessfully() throws Exception {
		when(coursesService.getAllCourses(1, 6, "en")).thenReturn(Page.empty());

		mockMvc.perform(get("/courses")
				.param("page", "1")
				.param("size", "6"))
				.andExpect(status().isOk());
		verify(coursesService).getAllCourses(1, 6, "en");
	}

	@Test
	void publicCoursesEndpointPassesRequestedLanguage() throws Exception {
		when(coursesService.getAllCourses(1, 6, "ar")).thenReturn(Page.empty());

		mockMvc.perform(get("/courses")
				.header("language", "ar"))
				.andExpect(status().isOk());

		verify(coursesService).getAllCourses(1, 6, "ar");
	}
}
