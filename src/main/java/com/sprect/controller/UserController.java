package com.sprect.controller;

import com.sprect.model.entity.User;
import com.sprect.model.response.ResponseError;
import com.sprect.model.response.ResponseSignIn;
import com.sprect.service.file.FileService;
import com.sprect.service.jwt.JwtService;
import com.sprect.service.user.UserService;
import com.sprect.utils.Validator;
import com.sprect.utils.DefaultString;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "User", description = "API for user management")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;
    private final Validator validator;
    private final FileService fileService;

    public UserController(UserService userService,
                          JwtService jwtService,
                          Validator validator,
                          FileService fileService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.validator = validator;
        this.fileService = fileService;
    }

    @Operation(summary = "Getting User Information", tags = {"User"})
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The user was found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "No such user was found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseError.class)))
    })
    @GetMapping("/get")
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String token) {
        String username = jwtService.getClaims(token.substring(7)).getBody().getSubject();

        User user = userService.findUserByUEP(username);

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);

        if (user.isAvatar()) {
            user.setUrlAvatar(fileService.getUrlForDownloadAvatar(user.getIdUser().toString()));
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Password reset via email", tags = {"User"})
    @io.swagger.v3.oas.annotations.parameters.RequestBody
            (content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = DefaultString.EXAMPLE_RESET_PASSWORD_THROUGH_EMAIL)))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The password has been successfully changed"),
            @ApiResponse(
                    responseCode = "400",
                    description = DefaultString.FAILED_VALIDATE_PASSWORD,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseError.class)))
    })
    @PostMapping("/resetPasswordThroughEmail")
    public ResponseEntity<?> resetPasswordThroughEmail(@RequestHeader("Authorization") String token,
                                                       @RequestBody Map<String, String> body) {
        validator.regExpPassword(body.get("password"));
        String username = jwtService.getClaims(token.substring(7)).getBody().getSubject();

        userService.resetPassword(username, body.get("password"));
        jwtService.addBlackList(token.substring(7));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Password change via profile", tags = {"User"})
    @io.swagger.v3.oas.annotations.parameters.RequestBody
            (content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = DefaultString.EXAMPLE_RESET_PASSWORD)))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The password has been successfully changed"),
            @ApiResponse(
                    responseCode = "400",
                    description = DefaultString.FAILED_VALIDATE_PASSWORD + " or " + DefaultString.WRONG_OLD_PASSWORD,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseError.class)))
    })
    @PostMapping("/edit/password")
    public ResponseEntity<?> resetPassword(@RequestHeader("Authorization") String token,
                                           @RequestBody Map<String, String> body) {
        validator.regExpPassword(body.get("newPassword"));

        String username = jwtService.getClaims(token.substring(7)).getBody().getSubject();

        validator.checkOldPassword(username, body.get("oldPassword"));

        userService.resetPassword(username, body.get("newPassword"));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Changing a user's name through a profile", tags = {"User"})
    @io.swagger.v3.oas.annotations.parameters.RequestBody
            (content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = DefaultString.EXAMPLE_USERNAME)))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The password has been successfully changed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseSignIn.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = DefaultString.FAILED_VALIDATE_USERNAME + " or " + DefaultString.USERNAME_IS_BUSY,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseError.class)))
    })
    @PostMapping("/edit/username")
    public ResponseEntity<?> editUsername(@RequestHeader("Authorization") String token,
                                          @RequestBody Map<String, String> body) {
        validator.regExpUsername(body.get("newUsername"));

        String oldUsername = jwtService.getClaims(token.substring(7)).getBody().getSubject();

        User newUser = userService.editUsername(oldUsername, body.get("newUsername"));
        Map<String, Object> response = jwtService.createTokens(newUser.getUsername(), List.of("access", "refresh"));

        jwtService.addBlackList(token.substring(7));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Changing the profile description", tags = {"User"})
    @io.swagger.v3.oas.annotations.parameters.RequestBody
            (content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = DefaultString.EXAMPLE_PROFILE_DESCRIPTION)))
    @ApiResponse(
            responseCode = "200",
            description = "The profile description has been successfully changed")
    @PostMapping("/edit/profileDescription")
    public ResponseEntity<?> editProfileDescription(@RequestHeader("Authorization") String token,
                                                    @RequestBody Map<String, String> body) {
        String username = jwtService.getClaims(token.substring(7)).getBody().getSubject();
        User newUser = userService.editProfileDescription(username, body.get("newProfileDescription"));
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @Operation(summary = "Deleting a user", tags = {"user"})
    @ApiResponse(
            responseCode = "200",
            description = "The user was successfully deleted")
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String token) {
        Object id = jwtService.getClaims(token.substring(7)).getBody().get("id");
        userService.delete(id.toString());
        jwtService.addBlackList(token.substring(7));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "logout", tags = {"User"})
    @ApiResponse(
            responseCode = "200",
            description = "the logout was made successfully")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String accessToken) {
        jwtService.addBlackList(accessToken.substring(7));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
