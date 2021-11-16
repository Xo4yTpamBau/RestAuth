package com.sprect.Jwt;

import com.sprect.service.jwt.JwtService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static com.sprect.utils.DefaultString.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@Sql(value = {"/create-user-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/create-user-after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@TestPropertySource("/application-test.properties")
public class JwtTest {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    void expiredToken() {
        Map<String, Object> tokens = jwtService.createTokens("test", List.of("access", "refresh"));

        Thread.sleep(5000); //For the access token to expire

        this.mockMvc.perform(get("/user/get")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer_" + tokens.get("accessToken")))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(ACCESS_EXPIRED)));
    }

    @Test
    @SneakyThrows
    void userBlocked() {
        Map<String, Object> tokens = jwtService.createTokens("test3", List.of("access", "refresh"));

        this.mockMvc.perform(get("/user/get")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer_" + tokens.get("accessToken")))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 403, USER_BLOCKED))));
    }

    @Test
    @SneakyThrows
    void notValidTokenGetUser() {
        Map<String, Object> tokens = jwtService.createTokens("test", List.of("access", "refresh"));

        Thread.sleep(5000); //For the access token to expire

        this.mockMvc.perform(get("/user/get")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer_" + "header.body.JWS"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(ACCESS_INVALID)));
    }

    @Test
    @SneakyThrows
    void getNewTokens() {
        Map<String, Object> tokens = jwtService.createTokens("test", List.of("access", "refresh"));
        String format = String.format("{\n" +
                "    \"accessToken\" : \"%s\",\n" +
                "    \"refreshToken\" : \"%s\"\n" +
                "}", tokens.get("accessToken").toString(), tokens.get("refreshToken").toString());

        Thread.sleep(5000); //For the access token to expire

        this.mockMvc.perform(post("/auth/updateTokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(format))
                .andDo(print())
                .andExpect(content().string(containsString("accessToken")))
                .andExpect(content().string(containsString("refreshToken")));
    }

    @Test
    @SneakyThrows
    void failedUpdateNewTokensAccessHasNotExpired() {
        Map<String, Object> tokens = jwtService.createTokens("test", List.of("access", "refresh"));
        String format = String.format("{\n" +
                "    \"accessToken\" : \"%s\",\n" +
                "    \"refreshToken\" : \"%s\"\n" +
                "}", tokens.get("accessToken").toString(), tokens.get("refreshToken").toString());

        this.mockMvc.perform(post("/auth/updateTokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(format))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 401, ACCESS_NOT_EXPIRED))));
    }

    @Test
    @SneakyThrows
    void failedUpdateNewTokensRefreshExpired() {
        Map<String, Object> tokens = jwtService.createTokens("test", List.of("access", "refresh"));
        String format = String.format("{\n" +
                "    \"accessToken\" : \"%s\",\n" +
                "    \"refreshToken\" : \"%s\"\n" +
                "}", tokens.get("accessToken").toString(), tokens.get("refreshToken").toString());

        Thread.sleep(10000); //For the refresh token to expire

        this.mockMvc.perform(post("/auth/updateTokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(format))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 401, REFRESH_EXPIRED))));
    }

    @Test
    @SneakyThrows
    void failedUpdateNewTokensAccessNotValid() {
        Map<String, Object> tokens = jwtService.createTokens("test", List.of("access", "refresh"));
        String format = String.format("{\n" +
                "    \"accessToken\" : \"3213\",\n" +
                "    \"refreshToken\" : \"%s\"\n" +
                "}", tokens.get("refreshToken").toString());

        this.mockMvc.perform(post("/auth/updateTokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(format))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 401, ACCESS_INVALID))));
    }

    @Test
    @SneakyThrows
    void failedUpdateNewTokensRefreshNotValid() {
        Map<String, Object> tokens = jwtService.createTokens("test", List.of("access", "refresh"));
        String format = String.format("{\n" +
                "    \"accessToken\" : \"%s\",\n" +
                "    \"refreshToken\" : \"1234\"\n" +
                "}", tokens.get("accessToken").toString());

        Thread.sleep(5000); //For the access token to expire

        this.mockMvc.perform(post("/auth/updateTokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(format))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 401, REFRESH_INVALID))));
    }
}
