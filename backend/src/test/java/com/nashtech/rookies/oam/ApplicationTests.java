package com.nashtech.rookies.oam;

import com.nashtech.rookies.oam.service.impl.AssignmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class ApplicationTests {
	@MockitoBean
	AssignmentServiceImpl assignmentService;
	@Test
	void contextLoads() {
	}

}