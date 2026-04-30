package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void contextLoads() {
	}

	@Test
	void jwtLoginAndProtectedEndpointFlowWorks() throws Exception {
		String username = "jwt_test_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		String registerBody = """
				{"username":"%s","password":"123456"}
				""".formatted(username);

		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(registerBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200));

		MvcResult loginResult = mockMvc.perform(post("/api/users/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(registerBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andReturn();

		String loginJson = loginResult.getResponse().getContentAsString();
		JsonNode loginNode = objectMapper.readTree(loginJson);
		String jwt = loginNode.get("data").asText();
		assertNotNull(jwt);
		assertFalse(jwt.isBlank());
		assertFalse(jwt.startsWith("Bearer "));

		mockMvc.perform(get("/api/users/1"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value(401));

		mockMvc.perform(get("/api/users/1")
						.header("Authorization", "Bearer " + jwt))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.data").value("User lookup succeeded for id=1"));
	}

}
