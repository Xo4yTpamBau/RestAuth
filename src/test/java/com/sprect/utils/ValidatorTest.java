package com.sprect.utils;

import lombok.SneakyThrows;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.oxm.ValidationFailureException;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
class ValidatorTest {
    private final String FAILED_VALIDATE_PHONE = "The phone is specified incorrectly";
    private final String FAILED_VALIDATE_EMAIL = "The email address is specified incorrectly";
    private final String FAILED_VALIDATE_USERNAME = "Username must be between 3 and 18 characters long";
    private final String FAILED_VALIDATE_PASSWORD = "The password must consist of 8 characters and contain at least one digit, one uppercase and one lowercase";

    @Autowired
    private Validator validator;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    @SneakyThrows
    void validationEmail() {
        validator.regExpEmail("user@mail.ru");
        validator.regExpEmail("user@mail.com");
        validator.regExpEmail("user@outlook.com");
        validator.regExpEmail("adafsd-2fsa-3214-fsa@mail.ru");

        ValidationFailureException exception;

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpEmail("&$#*@$(*@mail.ru"));
        Assertions.assertEquals(FAILED_VALIDATE_EMAIL, exception.getMessage());

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpEmail("123"));
        Assertions.assertEquals(FAILED_VALIDATE_EMAIL, exception.getMessage());

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpEmail("user@"));
        Assertions.assertEquals(FAILED_VALIDATE_EMAIL, exception.getMessage());

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpEmail("user@mail"));
        Assertions.assertEquals(FAILED_VALIDATE_EMAIL, exception.getMessage());

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpEmail(""));
        Assertions.assertEquals(FAILED_VALIDATE_EMAIL, exception.getMessage());

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpEmail("     "));
        Assertions.assertEquals(FAILED_VALIDATE_EMAIL, exception.getMessage());

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpEmail("    @mail.ru"));
        Assertions.assertEquals(FAILED_VALIDATE_EMAIL, exception.getMessage());

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpEmail(" fasd   @mail.ru"));
        Assertions.assertEquals(FAILED_VALIDATE_EMAIL, exception.getMessage());
    }

    @Test
    @SneakyThrows
    void validationUsername() {
        validator.regExpUsername("fjklsdjfklas");
        validator.regExpUsername("321412312");
        validator.regExpUsername("d4h454jk34h");
        validator.regExpUsername("*$$(@$*(");

        ValidationFailureException exception;

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpUsername(""));
        Assertions.assertEquals(FAILED_VALIDATE_USERNAME, exception.getMessage());

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpUsername("      "));
        Assertions.assertEquals(FAILED_VALIDATE_USERNAME, exception.getMessage());

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpUsername("12"));
        Assertions.assertEquals(FAILED_VALIDATE_USERNAME, exception.getMessage());

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpUsername("qwertyuiop[]asdfghj"));
        Assertions.assertEquals(FAILED_VALIDATE_USERNAME, exception.getMessage());
    }

    @Test
    @SneakyThrows
    void validationPhone() {
        validator.regExpPhone("375251234567");
        validator.regExpPhone("375291234567");
        validator.regExpPhone("375331234567");
        validator.regExpPhone("375441234567");

        ValidationFailureException exception;

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpPhone("37544      "));
        Assertions.assertEquals(FAILED_VALIDATE_PHONE, exception.getMessage());

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpPhone("           "));
        Assertions.assertEquals(FAILED_VALIDATE_PHONE, exception.getMessage());

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpPhone(""));
        Assertions.assertEquals(FAILED_VALIDATE_PHONE, exception.getMessage());

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpPhone("37544fghfgds"));
        Assertions.assertEquals(FAILED_VALIDATE_PHONE, exception.getMessage());

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpPhone("37517fghfgds"));
        Assertions.assertEquals(FAILED_VALIDATE_PHONE, exception.getMessage());
    }

    @Test
    @SneakyThrows
    void validationPassword() {
        validator.regExpPassword("1234Qwer");
        validator.regExpPassword("1234QWeDsdasaVA123441#$@#$");
        validator.regExpPassword("ASJKHfj3124*%&#$");

        ValidationFailureException exception;

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpPassword("3124561353"));
        Assertions.assertEquals(FAILED_VALIDATE_PASSWORD, exception.getMessage());

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpPassword("3124hsjhf$#@"));
        Assertions.assertEquals(FAILED_VALIDATE_PASSWORD, exception.getMessage());

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpPassword("31245AJHFJSAD421312"));
        Assertions.assertEquals(FAILED_VALIDATE_PASSWORD, exception.getMessage());

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpPassword("312456$&#*$"));
        Assertions.assertEquals(FAILED_VALIDATE_PASSWORD, exception.getMessage());

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpPassword("fsadAJFHAJ$@#@"));
        Assertions.assertEquals(FAILED_VALIDATE_PASSWORD, exception.getMessage());

        exception = Assertions.assertThrows(
                ValidationFailureException.class,
                () -> validator.regExpPassword("$#&$&^#&"));
        Assertions.assertEquals(FAILED_VALIDATE_PASSWORD, exception.getMessage());

    }
}