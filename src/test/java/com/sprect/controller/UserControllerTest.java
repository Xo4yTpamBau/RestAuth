package com.sprect.controller;

import com.sprect.repository.nosql.BlackListRepositories;
import com.sprect.service.jwt.JwtService;
import com.sprect.service.user.UserService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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
@Sql(value = {"/create-user-before.sql",}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/create-user-after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@TestPropertySource("/application-test.properties")
class UserControllerTest {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private BlackListRepositories blackListRepositories;

    @Test
    @SneakyThrows
    void getUserNotAvatar() {
        Map<String, Object> tokens = jwtService.createTokens("test", List.of("access", "refresh"));

        this.mockMvc.perform(get("/user/get")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer_" + tokens.get("accessToken")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(String.format(
                        USER, "1", "test", "test@mail.com", "375251234567", "null", "null"))));
    }

    @Test
    @SneakyThrows
    void getUser() {
        Map<String, Object> tokens = jwtService.createTokens("test4", List.of("access", "refresh"));

        this.mockMvc.perform(get("/user/get")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer_" + tokens.get("accessToken")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(String.format(
                        USER, "4", "test4", "test4@mail.com", "375441234567", "null", ""))));
    }


    @Test
    @SneakyThrows
    void resetPasswordThroughEmail() {
        Map<String, Object> tokens = jwtService.createTokens("test", List.of("resetPassword"));

        this.mockMvc.perform(post("/user/resetPasswordThroughEmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer_" + tokens.get("resetPasswordToken"))
                        .content("{\"password\":\"newPass1234\"}"))
                .andExpect(status().isOk());


        Assertions.assertTrue(passwordEncoder.matches("newPass1234",
                userService.findUserByUEP("test").getPassword()));
    }

    @Test
    @SneakyThrows
    void failedResetPasswordThroughEmail() {
        Map<String, Object> tokens = jwtService.createTokens("test", List.of("resetPassword"));

        this.mockMvc.perform(post("/user/resetPasswordThroughEmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer_" + tokens.get("resetPasswordToken"))
                        .content("{\"password\":\"new\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 400, FAILED_VALIDATE_PASSWORD))));

        Assertions.assertTrue(passwordEncoder.matches(
                "1234Test",
                userService.findUserByUEP("test").getPassword()));
    }

    @Test
    @SneakyThrows
    void successEditPassword() {
        Map<String, Object> tokens = jwtService.createTokens("test", List.of("access", "refresh"));
        this.mockMvc.perform(post("/user/edit/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer_" + tokens.get("accessToken"))
                        .content("{" +
                                "\"oldPassword\":\"1234Test\"," +
                                "\"newPassword\":\"newPass1234\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isOk());

        Assertions.assertTrue(passwordEncoder.matches("newPass1234",
                userService.findUserByUEP("test").getPassword()));
    }

    @Test
    @SneakyThrows
    void failedEditPasswordWrongOldPassword() {
        Map<String, Object> tokens = jwtService.createTokens("test", List.of("access", "refresh"));

        this.mockMvc.perform(post("/user/edit/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer_" + tokens.get("accessToken"))
                        .content("{" +
                                "\"oldPassword\":\"1234Tes\"," +
                                "\"newPassword\":\"newPass1234\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 400, WRONG_OLD_PASSWORD))));

        Assertions.assertTrue(passwordEncoder.matches(
                "1234Test",
                userService.findUserByUEP("test").getPassword()));
    }

    @Test
    @SneakyThrows
    void failedEditPasswordRegExp() {
        Map<String, Object> tokens = jwtService.createTokens("test", List.of("access", "refresh"));

        this.mockMvc.perform(post("/user/edit/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer_" + tokens.get("accessToken"))
                        .content("{" +
                                "\"oldPassword\":\"1234Tes\"," +
                                "\"newPassword\":\"\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 400, FAILED_VALIDATE_PASSWORD))));

        Assertions.assertTrue(passwordEncoder.matches(
                "1234Test",
                userService.findUserByUEP("test").getPassword()));
    }

    @Test
    @SneakyThrows
    void successEditUsername() {
        Map<String, Object> tokens = jwtService.createTokens("test", List.of("access", "refresh"));

        this.mockMvc.perform(post("/user/edit/username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer_" + tokens.get("accessToken"))
                        .content("{\"newUsername\":\"testtest\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("accessToken")))
                .andExpect(content().string(containsString("refreshToken")))
                .andExpect(content().string(containsString(String.format(
                        USER, "1", "testtest", "test@mail.com", "375251234567", "null", "null"))));
    }

    @Test
    @SneakyThrows
    void failedEditUsername() {
        Map<String, Object> tokens = jwtService.createTokens("test", List.of("access", "refresh"));

        this.mockMvc.perform(post("/user/edit/username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer_" + tokens.get("accessToken"))
                        .content("{\"newUsername\":\"te\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 400, FAILED_VALIDATE_USERNAME))));
    }

    @Test
    @SneakyThrows
    void successEditProfileDescription() {
        Map<String, Object> tokens = jwtService.createTokens("test", List.of("access", "refresh"));

        this.mockMvc.perform(post("/user/edit/profileDescription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer_" + tokens.get("accessToken"))
                        .content("{\"newProfileDescription\":\"newDescription\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"profileDescription\":\"newDescription\"")));
    }

    @Test
    @SneakyThrows
    void delete() {
        Map<String, Object> tokens = jwtService.createTokens("test", List.of("access", "refresh"));

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer_" + tokens.get("accessToken")))
                .andDo(print())
                .andExpect(status().isOk());

        UsernameNotFoundException exception;

        exception = Assertions.assertThrows(
                UsernameNotFoundException.class,
                () -> userService.findUserByUEP("test"));
        Assertions.assertEquals(USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    @SneakyThrows
    void logout() {
        Map<String, Object> tokens = jwtService.createTokens("test", List.of("access", "refresh"));

        this.mockMvc.perform(post("/user/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer_" + tokens.get("accessToken")))
                .andDo(print())
                .andExpect(status().isOk());

        Assertions.assertTrue(blackListRepositories.existsById(tokens.get("accessToken").toString()));
    }

    @Test
    @SneakyThrows
    void checkLogout() {
        Map<String, Object> tokens = jwtService.createTokens("test", List.of("access", "refresh"));

        this.mockMvc.perform(get("/user/get")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer_" + tokens.get("accessToken")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(String.format(
                        USER, "1", "test", "test@mail.com", "375251234567", "null", "null"))));

        this.mockMvc.perform(post("/user/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer_" + tokens.get("accessToken")))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/user/get")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer_" + tokens.get("accessToken")))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 401, ACCESS_INVALID))));
    }
}