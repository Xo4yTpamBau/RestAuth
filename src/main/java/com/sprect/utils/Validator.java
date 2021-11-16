package com.sprect.utils;

import com.sprect.model.entity.User;
import com.sprect.repository.sql.UserRepository;
import org.springframework.oxm.ValidationFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Locale;

import static com.sprect.utils.DefaultString.*;
import static org.apache.http.entity.ContentType.*;

@Component
public class Validator {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Validator(UserRepository userRepository,
                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void regExpEmail(String email) {
        if (!email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
                "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"))
            throw new ValidationFailureException(FAILED_VALIDATE_EMAIL);
    }

    public void regExpUsername(String username) {
        String newUsername = username.trim().toLowerCase(Locale.ROOT);
        if (newUsername.length() < 3 || newUsername.length() > 18)
            throw new ValidationFailureException(FAILED_VALIDATE_USERNAME);
    }

    public void regExpPhone(String phone) {
        if (!phone.matches("^375(25|29|33|44)[0-9]{7}$"))
            throw new ValidationFailureException(FAILED_VALIDATE_PHONE);
    }

    public void regExpPassword(String password) {
        if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$"))
            throw new ValidationFailureException(FAILED_VALIDATE_PASSWORD);
    }


    public void existEmail(String email) {
        if (userRepository.existsByEmail(email))
            throw new ValidationFailureException(EMAIL_BUSY);

    }

    public void existPhone(String phone) {
        if (userRepository.existsByPhone(phone))
            throw new ValidationFailureException(PHONE_BUSY);
    }

    public void existUsername(String username) {
        if (userRepository.existsByUsername(username))
            throw new ValidationFailureException(USERNAME_BUSY);
    }

    public void checkOldPassword(String username, String oldPassword) {
        User user = userRepository.findUserByUsername(username);
        if (!passwordEncoder.matches(oldPassword, user.getPassword()))
            throw new ValidationFailureException(WRONG_OLD_PASSWORD);
    }

    public void typeFileAvatar(MultipartFile file) {
        if (!Arrays.asList(IMAGE_PNG.getMimeType(),
                IMAGE_BMP.getMimeType(),
                IMAGE_GIF.getMimeType(),
                IMAGE_JPEG.getMimeType()).contains(file.getContentType())) {
            throw new IllegalStateException("FIle uploaded is not an image");
        }
    }
}
