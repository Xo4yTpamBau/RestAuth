package com.sprect.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseError {

    @Schema(description = "Time of error origin",
            example = "Wed Oct 06 15:29:35 MSK 2021")
    private String timestamp;

    @Schema(description = "HTTP Status",
            example = "500")
    private int status;

    @Schema(description = "Brief description of the error",
            example = "errorMessage")
    private String error;
}
