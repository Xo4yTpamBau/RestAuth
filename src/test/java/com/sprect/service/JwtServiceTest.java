package com.sprect.service;

import com.sprect.model.entity.Role;
import com.sprect.model.entity.User;
import com.sprect.service.jwt.JwtService;
import com.sprect.service.user.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
class JwtServiceTest {
    @Autowired
    private JwtService jwtService;
    @MockBean
    private UserService userService;

    @Test
    void createTwoTokens() {
        User user = new User();
        user.setUsername("user");
        user.setIdUser(1L);
        user.setRole(Collections.singletonList(new Role(1, "USER")));

        Mockito.doReturn(user)
                .when(userService)
                .findUserByUEP("user");

        Map<String, Object> tokens = jwtService.createTokens("user", List.of("access", "refresh"));

        Assertions.assertEquals(3, tokens.size());
    }
}