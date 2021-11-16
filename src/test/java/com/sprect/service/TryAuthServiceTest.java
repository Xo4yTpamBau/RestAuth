package com.sprect.service;

import com.sprect.exception.TryAuthException;
import com.sprect.model.entity.User;
import com.sprect.model.redis.TryAuth;
import com.sprect.repository.nosql.TryAuthRepository;
import com.sprect.service.tryAuth.TryAuthService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static com.sprect.utils.DefaultString.BLOCKED_USER_TRY_AUTH;

@SpringBootTest
@RunWith(SpringRunner.class)
class TryAuthServiceTest {
    @Autowired
    private TryAuthService tryAuthService;
    @MockBean
    private TryAuthRepository tryAuthRepository;

    @Test
    void checkTryAuth() {
        User user = new User();
        user.setIdUser(1L);
        TryAuth tryAuth = new TryAuth(1L, 1L);
        Mockito.doReturn(Optional.of(tryAuth)).when(tryAuthRepository).findById(1L);

        tryAuthService.checkTryAuth(1L);
        Mockito.verify(tryAuthRepository, Mockito.times(1)).save(new TryAuth(1L, 2L));
    }

    @Test
    void failedCheckTryAuth() {
        User user = new User();
        user.setIdUser(1L);
        TryAuth tryAuth = new TryAuth(1L, 10L);
        Mockito.doReturn(Optional.of(tryAuth)).when(tryAuthRepository).findById(1L);

        TryAuthException exception;

        exception = Assertions.assertThrows(
                TryAuthException.class,
                () -> tryAuthService.checkTryAuth(1L));
        Assertions.assertEquals(BLOCKED_USER_TRY_AUTH, exception.getMessage());

        Mockito.verify(tryAuthRepository, Mockito.times(1)).save(new TryAuth(1L, 11L));
    }
}