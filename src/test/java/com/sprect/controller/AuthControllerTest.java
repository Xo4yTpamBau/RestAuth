package com.sprect.controller;

import com.sprect.model.StatusUser;
import com.sprect.model.redis.TryAuth;
import com.sprect.repository.nosql.TryAuthRepository;
import com.sprect.service.jwt.JwtService;
import com.sprect.service.user.UserService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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

import java.time.LocalDate;
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
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserService userService;
    @Autowired
    private TryAuthRepository tryAuthRepository;

    @AfterEach
    public void clearRedis() {
        tryAuthRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void successLoginForUsername() {
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"test4\"," +
                                "\"password\":\"1234Test\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("accessToken")))
                .andExpect(content().string(containsString("refreshToken")))
                .andExpect(content().string(containsString(String.format(
                        USER, "4", "test4", "test4@mail.com", "375441234567", "null", ""))));
    }

    @Test
    @SneakyThrows
    void successLoginForPhone() {
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"375441234567\"," +
                                "\"password\":\"1234Test\"" +
                                "}"))
                .andDo(print())
                .andExpect(content().string(containsString("accessToken")))
                .andExpect(content().string(containsString("refreshToken")))
                .andExpect(content().string(containsString(String.format(
                        USER, "4", "test4", "test4@mail.com", "375441234567", "null", ""))));
    }

    @Test
    @SneakyThrows
    void successLoginForEmail() {
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"test4@mail.com\"," +
                                "\"password\":\"1234Test\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("accessToken")))
                .andExpect(content().string(containsString("refreshToken")))
                .andExpect(content().string(containsString(String.format(
                        USER, "4", "test4", "test4@mail.com", "375441234567", "null", ""))));
    }

    @Test
    @SneakyThrows
    void successLoginForUsernameNotAvatar() {
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"test\"," +
                                "\"password\":\"1234Test\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("accessToken")))
                .andExpect(content().string(containsString("refreshToken")))
                .andExpect(content().string(containsString(String.format(
                        USER, "1", "test", "test@mail.com", "375251234567", "null", "null"))));
    }

    @Test
    @SneakyThrows
    void successLoginForPhoneNotAvatar() {
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"375251234567\"," +
                                "\"password\":\"1234Test\"" +
                                "}"))
                .andDo(print())
                .andExpect(content().string(containsString("accessToken")))
                .andExpect(content().string(containsString("refreshToken")))
                .andExpect(content().string(containsString(String.format(
                        USER, "1", "test", "test@mail.com", "375251234567", "null", "null"))));
    }

    @Test
    @SneakyThrows
    void successLoginForEmailNotAvatar() {
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"test@mail.com\"," +
                                "\"password\":\"1234Test\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("accessToken")))
                .andExpect(content().string(containsString("refreshToken")))
                .andExpect(content().string(containsString(String.format(
                        USER, "1", "test", "test@mail.com", "375251234567", "null", "null"))));
    }


    @Test
    @SneakyThrows
    void badCredentialsWrongPasswordLoginForUsername() {
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"test\"," +
                                "\"password\":\"1234Tes\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 401, BAD_CREDENTIALS))));
    }

    @Test
    @SneakyThrows
    void badCredentialsWrongPasswordLoginForPhone() {
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"375251234567\"," +
                                "\"password\":\"1234Tes\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 401, BAD_CREDENTIALS))));
    }

    @Test
    @SneakyThrows
    void badCredentialsWrongPasswordLoginForEmail() {
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"test@mail.com\"," +
                                "\"password\":\"1234Tes\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 401, BAD_CREDENTIALS))));
    }

    @Test
    @SneakyThrows
    void BadCredentialsUserNotFoundLoginForUsername() {
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"tes\"," +
                                "\"password\":\"1234Test\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 401, BAD_CREDENTIALS))));
    }

    @Test
    @SneakyThrows
    void BadCredentialsUserNotFoundLoginForPhone() {
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"375251111111\"," +
                                "\"password\":\"1234Test\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 401, BAD_CREDENTIALS))));
    }

    @Test
    @SneakyThrows
    void BadCredentialsUserNotFoundLoginForEmail() {
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"test@mail.co\"," +
                                "\"password\":\"1234Test\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 401, BAD_CREDENTIALS))));
    }

    @Test
    @SneakyThrows
    void notConfirmEmailLoginForUsername() {
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"test2\"," +
                                "\"password\":\"1234Tes\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 401, NOT_CONFIRM_EMAIL))));
    }

    @Test
    @SneakyThrows
    void notConfirmEmailLoginForPhone() {
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"375331234567\"," +
                                "\"password\":\"1234Tes\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 401, NOT_CONFIRM_EMAIL))));
    }

    @Test
    @SneakyThrows
    void notConfirmEmailLoginForEmail() {
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"test2@mail.com\"," +
                                "\"password\":\"1234Tes\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 401, NOT_CONFIRM_EMAIL))));
    }

    @Test
    @SneakyThrows
    void blockedLoginForUsername() {
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"test3\"," +
                                "\"password\":\"1234Tes\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 401, USER_BLOCKED))));
    }

    @Test
    @SneakyThrows
    void blockedLoginForPhone() {
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"375291234567\"," +
                                "\"password\":\"1234Tes\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 401, USER_BLOCKED))));
    }

    @Test
    @SneakyThrows
    void blockedLoginForEmail() {
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"test3@mail.com\"," +
                                "\"password\":\"1234Tes\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 401, USER_BLOCKED))));
    }

    @Test
    @SneakyThrows
    void blockTryAuthLoginForUsername() {
        tryAuthRepository.save(new TryAuth(1L, 10L));
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"test\"," +
                                "\"password\":\"1234Test\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 403, BLOCKER_USER_TRY_AUTH))));
    }

    @Test
    @SneakyThrows
    void blockTryAuthLoginForPhone() {
        tryAuthRepository.save(new TryAuth(1L, 10L));
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"375251234567\"," +
                                "\"password\":\"1234Test\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 403, BLOCKER_USER_TRY_AUTH))));
    }

    @Test
    @SneakyThrows
    void blockTryAuthLoginForEmail() {
        tryAuthRepository.save(new TryAuth(1L, 10L));
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"test@mail.com\"," +
                                "\"password\":\"1234Test\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 403, BLOCKER_USER_TRY_AUTH))));
    }

    @Test
    @SneakyThrows
    void resetTryAuthSuccessLoginForUsername() {
        tryAuthRepository.save(new TryAuth(1L, 9L));
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"test\"," +
                                "\"password\":\"1234Test\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("accessToken")))
                .andExpect(content().string(containsString("refreshToken")))
                .andExpect(content().string(containsString(String.format(
                        USER, "1", "test", "test@mail.com", "375251234567", "null", "null"))));

        Assertions.assertFalse(tryAuthRepository.existsById(1L));
    }

    @Test
    @SneakyThrows
    void resetTryAuthSuccessLoginForEmail() {
        tryAuthRepository.save(new TryAuth(1L, 9L));
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"test@mail.com\"," +
                                "\"password\":\"1234Test\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("accessToken")))
                .andExpect(content().string(containsString("refreshToken")))
                .andExpect(content().string(containsString(String.format(
                        USER, "1", "test", "test@mail.com", "375251234567", "null", "null"))));

        Assertions.assertFalse(tryAuthRepository.existsById(1L));
    }

    @Test
    @SneakyThrows
    void resetTryAuthSuccessLoginPhone() {
        tryAuthRepository.save(new TryAuth(1L, 9L));
        this.mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"375251234567\"," +
                                "\"password\":\"1234Test\"" +
                                "}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("accessToken")))
                .andExpect(content().string(containsString("refreshToken")))
                .andExpect(content().string(containsString(String.format(
                        USER, "1", "test", "test@mail.com", "375251234567", "null", "null"))));

        Assertions.assertFalse(tryAuthRepository.existsById(1L));
    }


    @Test
    @SneakyThrows
    void successRegistration() {
        this.mockMvc.perform(post("/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"username\" : \"test123\",\n" +
                                "    \"firstName\" : \"test\",\n" +
                                "    \"lastName\" : \"test\",\n" +
                                "    \"password\" : \"1234Test\",\n" +
                                "    \"email\" : \"test123@mail.com\",\n" +
                                "    \"phone\" : \"375441111111\"\n" +
                                "}"))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString(String.format("" +
                        "\"username\":\"test123\"," +
                        "\"email\":\"test123@mail.com\"," +
                        "\"phone\":\"375441111111\"," +
                        "\"firstName\":\"test\"," +
                        "\"lastName\":\"test\"," +
                        "\"registrationDate\":\"%s\"," +
                        "\"profileDescription\":null," +
                        "\"urlAvatar\":null" +
                        "}", LocalDate.now()))));
    }

    @Test
    @SneakyThrows
    void badRequestRegistration() {
        this.mockMvc.perform(post("/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"username\" : \"\",\n" +
                                "    \"firstName\" : \"\",\n" +
                                "    \"lastName\" : \"\",\n" +
                                "    \"password\" : \"\",\n" +
                                "    \"email\" : \"\",\n" +
                                "    \"phone\" : \"\"\n" +
                                "}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(FAILED_VALIDATE_USERNAME)))
                .andExpect(content().string(containsString(FAILED_VALIDATE_FIRST_NAME)))
                .andExpect(content().string(containsString(FAILED_VALIDATE_LAST_NAME)))
                .andExpect(content().string(containsString(FAILED_VALIDATE_PASSWORD)))
                .andExpect(content().string(containsString(FAILED_VALIDATE_EMAIL)))
                .andExpect(content().string(containsString(FAILED_VALIDATE_PHONE)));
    }

    @Test
    @SneakyThrows
    void userAlreadyRegistration() {
        this.mockMvc.perform(post("/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"username\" : \"test\",\n" +
                                "    \"firstName\":\"test\"," +
                                "    \"lastName\":\"test\"," +
                                "    \"password\" : \"1234Test\",\n" +
                                "    \"email\" : \"test@mail.com\",\n" +
                                "    \"phone\" : \"375251234567\"\n" +
                                "}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 400, USER_ALREADY_REGISTRATION))));
    }

    @Test
    @SneakyThrows
    void confirmationEmail() {
        Map<String, Object> tokens = jwtService.createTokens("test2@mail.com", List.of("confirmationEmail"));

        this.mockMvc.perform(get("/auth/confirmationEmail/" + tokens.get("confirmationEmailToken"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is3xxRedirection());

        Assertions.assertEquals(StatusUser.ACTIVE,
                userService.findUserByUEP("test2@mail.com").getStatus());
    }

    @Test
    @SneakyThrows
    void expiredConfirmationEmail() {
        Map<String, Object> tokens = jwtService.createTokens("test2@mail.com", List.of("confirmationEmail"));

        Thread.sleep(10000); //For the access token to expire

        this.mockMvc.perform(get("/auth/confirmationEmail/" + tokens.get("confirmationEmailToken"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(CONFIRM_EXPIRED)));

        Assertions.assertEquals(StatusUser.NOT_ACTIVE,
                userService.findUserByUEP("test2@mail.com").getStatus());
    }

    @Test
    @SneakyThrows
    void failedConfirmationEmail() {
        this.mockMvc.perform(get("/auth/confirmationEmail/" + "header.body.JWS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString(CONFIRM_INVALID)));


        Assertions.assertEquals(StatusUser.NOT_ACTIVE,
                userService.findUserByUEP("test2").getStatus());
    }

    @Test
    @SneakyThrows
    void successSendEmailForResetPassword() {
        this.mockMvc.perform(post("/auth/sendEmailForResetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@mail.com\"}"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void failedSendEmailForChangePassword() {
        this.mockMvc.perform(post("/auth/sendEmailForResetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\"}"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 404, USER_NOT_FOUND))));
    }

    @Test
    @SneakyThrows
    void successResendEmailForConfirm() {
        this.mockMvc.perform(post("/auth/resendEmailForConfirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@mail.com\"}"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void failedResendEmailForConfirm() {
        this.mockMvc.perform(post("/auth/resendEmailForConfirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\"}"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 404, USER_NOT_FOUND))));
    }

    @Test
    @SneakyThrows
    void isExistEmail() {
        this.mockMvc.perform(post("/auth/isExistEmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test789487@mail.com\"}"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void badRequestIsExistEmail() {
        this.mockMvc.perform(post("/auth/isExistEmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 400, FAILED_VALIDATE_EMAIL))));
    }

    @Test
    @SneakyThrows
    void failedIsExistEmail() {
        this.mockMvc.perform(post("/auth/isExistEmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@mail.com\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 400, EMAIL_BUSY))));
    }

    @Test
    @SneakyThrows
    void isExistUsername() {
        this.mockMvc.perform(post("/auth/isExistUsername")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testexist\"}"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void badRequestIsExistUsername() {
        this.mockMvc.perform(post("/auth/isExistUsername")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 400, FAILED_VALIDATE_USERNAME))));
    }

    @Test
    @SneakyThrows
    void failedIsExistUsername() {
        this.mockMvc.perform(post("/auth/isExistUsername")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 400, USERNAME_BUSY))));
    }

    @Test
    @SneakyThrows
    void isExistPhone() {
        this.mockMvc.perform(post("/auth/isExistPhone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"375440000000\"}"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void badRequestIsExistPhone() {
        this.mockMvc.perform(post("/auth/isExistPhone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 400, FAILED_VALIDATE_PHONE))));
    }

    @Test
    @SneakyThrows
    void failedIsExistPhone() {
        this.mockMvc.perform(post("/auth/isExistPhone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"375251234567\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(String.format(
                        RESPONSE_ERROR, 400, PHONE_BUSY))));
    }
}