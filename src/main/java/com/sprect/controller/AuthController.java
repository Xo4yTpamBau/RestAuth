package com.sprect.controller;

import com.sprect.model.entity.User;
import com.sprect.model.response.ResponseError;
import com.sprect.model.response.ResponseSignIn;
import com.sprect.service.file.FileService;
import com.sprect.service.jwt.JwtService;
import com.sprect.service.mail.MailService;
import com.sprect.service.tryAuth.TryAuthService;
import com.sprect.service.user.UserService;
import com.sprect.utils.Validator;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.sprect.utils.DefaultString.*;

@Validated
@RestController
@RequestMapping("/auth")
@Slf4j
@Tag(name = "Auth", description = "API responsible for registration and authorization")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final MailService mailService;
    private final Validator validator;
    private final TryAuthService tryAuthService;
    private final FileService fileService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserService userService,
                          MailService mailService,
                          Validator validator,
                          TryAuthService tryAuthService,
                          FileService fileService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.mailService = mailService;
        this.validator = validator;
        this.tryAuthService = tryAuthService;
        this.fileService = fileService;
    }

    @Operation(summary = "registration", tags = {"Auth"})
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User was successfully registered",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "One of the fields failed validation or such a user is already registered",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseError.class)))
    })
    @PostMapping("/signUp")
    public ResponseEntity<?> registration(@Valid @RequestBody User user) {
        User saveUser = userService.saveUser(user);
        mailService.sendActivationCode(user.getEmail(), "Confirmation email");
        return new ResponseEntity<>(saveUser, HttpStatus.CREATED);
    }

    @Operation(summary = "Authorization and receipt of tokens", tags = {"Auth"})
    @io.swagger.v3.oas.annotations.parameters.RequestBody
            (content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = EXAMPLE_SIGNIN)))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User was successfully logged in",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseSignIn.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "The password is incorrect or " + USER_NOT_FOUND,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseError.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = NOT_CONFIRM_EMAIL + " or " + USER_BLOCKED,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseError.class))),
    })
    @PostMapping("/signIn")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        body.get("username"),
                        body.get("password")));

        Map<String, Object> response = jwtService.createTokens(body.get("username"), List.of("access", "refresh"));

        User user = (User) response.get("user");

        if (user.isAvatar()) {
            user.setUrlAvatar(fileService.getUrlForDownloadAvatar(user.getIdUser().toString()));
        }

        tryAuthService.deleteById(user.getIdUser());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Email confirmation after registration", tags = {"User"})
    @ApiResponse(
            responseCode = "301",
            description = "The mail was successfully confirmed"
    )
    @GetMapping("/confirmationEmail/{token}")
    public ResponseEntity<?> confirmationEmail(@PathVariable String token) {
        try {
            String username = jwtService.getClaims(token).getBody().getSubject();

            userService.confirmationEmail(username);

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create("https://hd.kinopoisk.ru/film/42d5ba8f195451fda78fe0ce899a964a?from_block=kp-button-online"));
            return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
        } catch (ExpiredJwtException e) {
            throw new JwtException(CONFIRM_EXPIRED);
        } catch (JwtException e) {
            throw new JwtException(CONFIRM_INVALID);
        }
    }

    @Operation(summary = "Request to send email for reset password", tags = {"Auth"})
    @io.swagger.v3.oas.annotations.parameters.RequestBody
            (content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = EXAMPLE_EMAIL)))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The password reset message was successfully send to the mail"),
            @ApiResponse(
                    responseCode = "404",
                    description = USER_NOT_FOUND,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseError.class)))
    })
    @PostMapping("/sendEmailForResetPassword")
    public ResponseEntity<?> sendEmailForResetPassword(@RequestBody Map<String, String> body) {
        if (!userService.isEmailExist(body.get("email"))) {
            throw new UsernameNotFoundException(USER_NOT_FOUND);
        }

        mailService.sendEmailForChangePassword(body.get("email"), "Reset password");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Request to resend the email to confirm the mail", tags = {"Auth"})
    @io.swagger.v3.oas.annotations.parameters.RequestBody
            (content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = EXAMPLE_EMAIL)))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The confirm message was successfully resend to the mail"),
            @ApiResponse(
                    responseCode = "404",
                    description = USER_NOT_FOUND,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseError.class)))
    })
    @PostMapping("/resendEmailForConfirm")
    public ResponseEntity<?> resendEmailForConfirm(@RequestBody Map<String, String> body) {
        if (!userService.isEmailExist(body.get("email"))) {
            throw new UsernameNotFoundException(USER_NOT_FOUND);
        }

        mailService.sendActivationCode(body.get("email"), "Confirmation email");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Getting a new pair of tokens when the access token expires", tags = {"Auth"})
    @io.swagger.v3.oas.annotations.parameters.RequestBody
            (content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = EXAMPLE_UPDATE_TOKENS)))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Getting a new pair of tokens",
                    content = @Content(
                            examples = @ExampleObject(value = EXAMPLE_UPDATE_TOKENS))),
            @ApiResponse(
                    responseCode = "400",
                    description = "The token is not valid or " + ACCESS_NOT_EXPIRED,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseError.class)))
    })
    @PostMapping("/updateTokens")
    public ResponseEntity<?> updateTokens(@RequestBody Map<String, String> body) {
        Map<String, Object> newTokens = jwtService.getNewTokens(body.get("accessToken"),
                body.get("refreshToken"));
        return new ResponseEntity<>(newTokens, HttpStatus.OK);
    }

    @Operation(summary = "Checking email for validity and uniqueness", tags = {"Auth"})
    @io.swagger.v3.oas.annotations.parameters.RequestBody
            (content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = EXAMPLE_EMAIL)))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Email is free and has passed validation"),
            @ApiResponse(
                    responseCode = "400",
                    description = FAILED_VALIDATE_EMAIL + " or " + EMAIL_BUSY,
                    content = @Content(
                            schema = @Schema(implementation = ResponseError.class)))
    })
    @PostMapping("/isExistEmail")
    public ResponseEntity<?> isExistEmail(@RequestBody Map<String, String> body) {
        validator.regExpEmail(body.get("email"));
        validator.existEmail(body.get("email"));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Checking username for validity and uniqueness", tags = {"Auth"})
    @io.swagger.v3.oas.annotations.parameters.RequestBody
            (content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = EXAMPLE_USERNAME)))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Username is free and has passed validation"),
            @ApiResponse(
                    responseCode = "400",
                    description = FAILED_VALIDATE_USERNAME + " or " + USERNAME_BUSY,
                    content = @Content(
                            schema = @Schema(implementation = ResponseError.class)))
    })
    @PostMapping("/isExistUsername")
    public ResponseEntity<?> isExistUsername(@RequestBody Map<String, String> body) {
        validator.regExpUsername(body.get("username"));
        validator.existUsername(body.get("username"));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Checking phone for validity and uniqueness", tags = {"Auth"})
    @io.swagger.v3.oas.annotations.parameters.RequestBody
            (content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = EXAMPLE_PHONE)))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Phone is free and has passed validation"),
            @ApiResponse(
                    responseCode = "400",
                    description = FAILED_VALIDATE_PHONE + " or " + PHONE_BUSY,
                    content = @Content(
                            schema = @Schema(implementation = ResponseError.class)))
    })
    @PostMapping("/isExistPhone")
    public ResponseEntity<?> isExistPhone(@RequestBody Map<String, String> body) {
        validator.regExpPhone(body.get("phone"));
        validator.existPhone(body.get("phone"));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
