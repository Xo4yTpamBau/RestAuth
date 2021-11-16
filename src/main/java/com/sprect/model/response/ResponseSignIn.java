package com.sprect.model.response;


import com.sprect.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseSignIn {

    @Schema(description = "access token living for 10 minutes, needed for access",
            example = "header.body.JWS")
    private String accessToken;

    @Schema(description = "refresh token that lives for 1 day, is used to update access token",
            example = "header.body.JWS")
    private String refreshToken;

    @Schema(description = "Basic User Information")
    private User user;
}

