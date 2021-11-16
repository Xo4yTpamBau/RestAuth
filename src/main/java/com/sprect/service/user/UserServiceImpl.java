package com.sprect.service.user;

import com.sprect.exception.RegistrationException;
import com.sprect.exception.StatusException;
import com.sprect.model.StatusUser;
import com.sprect.model.entity.Role;
import com.sprect.model.entity.User;
import com.sprect.repository.sql.RoleRepository;
import com.sprect.repository.sql.UserRepository;
import com.sprect.service.file.FileService;
import com.sprect.service.tryAuth.TryAuthService;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.oxm.ValidationFailureException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.sprect.utils.DefaultString.*;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private final TryAuthService tryAuthService;


    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           @Lazy PasswordEncoder passwordEncoder,
                           @Lazy FileService fileService,
                           @Lazy TryAuthService tryAuthService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
        this.tryAuthService = tryAuthService;
    }

    @Override
    public User saveUser(User user) {
        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setUsername(user.getUsername().toLowerCase(Locale.ROOT).trim());
        user.setPassword(encodePassword);
        setDefaultRoleUser(user);
        user.setStatus(StatusUser.NOT_ACTIVE);

        User save;
        try {
            save = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new RegistrationException(REGISTRATION_EXCEPTION);
        }
        return save;

    }

    private void setDefaultRoleUser(User user) {
        Role role = roleRepository.findRoleByNameRole(DEFAULT_USER_ROLE);
        List<Role> roles = List.of(role);
        user.setRole(roles);
    }

    @Override
    public void checkBlockedUser(StatusUser status) {
        if (status.equals(StatusUser.BLOCKED)) {
            throw new StatusException(USER_BLOCKED);
        }
    }

    @Override
    public User findUserByUEP(String username) {
        User user = username.matches(PATTERN_EMAIL) ?
                userRepository.findUserByEmail(username) : username.matches(PATTERN_PHONE) ?
                userRepository.findUserByPhone(username) : userRepository.findUserByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException(USER_NOT_FOUND);
        }

        return user;
    }

    @Override
    public void confirmationEmail(String username) {
        User user = findUserByUEP(username);
        user.setStatus(StatusUser.ACTIVE);
        userRepository.save(user);
    }

    @Override
    public void resetPassword(String username, String password) {
        String encodePassword = passwordEncoder.encode(password);
        User user = userRepository.findUserByUsername(username);
        user.setPassword(encodePassword);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(String id) {
        fileService.deleteAvatar(id);
        userRepository.deleteById(Long.parseLong(id));
    }

    @Override
    public void saveAvatar(String id) {
        Optional<User> byId = userRepository.findById(Long.parseLong(id));
        if (byId.isPresent()) {
            User user = byId.get();
            user.setAvatar(true);
            userRepository.save(user);
        }
    }

    @Override
    public User editUsername(String oldUsername, String newUsername) {
        User user = userRepository.findUserByUsername(oldUsername);
        user.setUsername(newUsername);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ValidationFailureException(USERNAME_BUSY);
        }
        return user;
    }

    @Override
    public User editProfileDescription(String username, String newProfileDescription) {
        User user = userRepository.findUserByUsername(username);
        user.setProfileDescription(newProfileDescription);
        userRepository.save(user);
        return user;
    }

    @Override
    public boolean isEmailExist(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void deleteAvatar(String id) {
        Optional<User> byId = userRepository.findById(Long.parseLong(id));
        if (byId.isPresent()) {
            User user = byId.get();
            user.setAvatar(false);
            userRepository.save(user);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findUserByUEP(username);
        tryAuthService.checkTryAuth(user.getIdUser());
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username, String JWT) throws UsernameNotFoundException {
        User user = findUserByUEP(username);
        checkBlockedUser(user.getStatus());
        return user;
    }
}